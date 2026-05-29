package mysimpleagent.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import mysimpleagent.Config;
import mysimpleagent.llm.chatcompletions.payloads.*;
import mysimpleagent.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class.getSimpleName());

    private final HttpClient llmClient;
    private final ObjectMapper objectMapper;
    private final Config config;
    private final String toolsString;
    private final List<Object> tools;

    public LLMService(HttpClient llmClient, ObjectMapper objectMapper, Config config) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.config = config;

        var toolsResourcePath = "/tools.json";
        this.toolsString = ResourceUtils.loadResourceAsString(toolsResourcePath);
        this.tools = this.objectMapper.readValue(toolsString, new TypeReference<>(){});
        logger.atInfo()
                .addKeyValue("toolsCount", this.tools.size())
                .addKeyValue("resourcePath", toolsResourcePath)
                .log("tools carregadas com sucesso");
    }

    public List<LLMChatCompletionMessage> newConversation() {
        var messages = new ArrayList<LLMChatCompletionMessage>();

        var systemMessage = new LLMChatCompletionMessage("system", getSystemPrompt());
        messages.add(systemMessage);

        return messages;
    }

    public String chat(List<LLMChatCompletionMessage> messages, String userPrompt) throws IOException, InterruptedException {
        logger.atInfo().log("preparando requisição...");

        messages.add(new LLMChatCompletionMessage("user", userPrompt));

        var stream = false;
        var payload = new LLMChatCompletionPayload(getModelName(), stream, messages, tools);

        String payloadStr = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        logger.atInfo()
                .addKeyValue("payload", payloadStr)
                .log("llm chat completions request");

        var uri = URI.create(this.config.getLlmBaseUrl() + "/chat/completions");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadStr))
                .timeout(Duration.ofMinutes(1))
                .build();

        logger.atInfo()
                .addKeyValue("method", request.method())
                .addKeyValue("url", uri)
                .log("enviando requisicão para o motor de inferência...");
        HttpResponse<InputStream> response = llmClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        logger.atInfo().log("resposta http obtida");

        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw new RuntimeException(String.format("HTTP Error: %d - %s", response.statusCode(), response.body()));
        }
        logger.atInfo().log("status code ok");

        LLMChatCompletionResponse respMsg = parseResponse(response);
        logger.atInfo()
                .addKeyValue("choices", respMsg.choices().size())
                .log("sucesso no parsing da resposta");

        if (respMsg.choices().isEmpty()) {
            logger.atInfo().log("fim da conversa.");
            return "";
        }

        //TODO melhorar mapeamento da tool call
        //        "tool_calls": [
        //          {
        //            "type": "function",
        //            "id": "209113905",
        //            "function": {
        //              "name": "write_file",
        //              "arguments": "{\"output_file_path\":\"/tmp/foo\",\"contents\":\"hello world\"}"
        //            }
        //          }
        //        ]
        LLMChoice choice = respMsg.choices().getFirst();
        LLMMessage responseMessage = choice.message();

        // FIXME create a polimorphic definition so you include the tool_calls property too
        messages.add(new LLMChatCompletionMessage(responseMessage.role(), responseMessage.content()));

        System.out.println("=== Reasoning ===");
        System.out.println(responseMessage.reasoningContent());

        if (responseMessage.toolCalls().isEmpty()) {
            return responseMessage.content();
        }
        logger.atInfo()
                .addKeyValue("toolCallCount", responseMessage.toolCalls().size())
                .log("existem chamadas a tools");

        for (LLMChatCompletionTool toolCall: responseMessage.toolCalls()) {
            System.out.printf("\uD83D\uDD28 [%s] %s: %s %s%n%n",
                    toolCall.id(),
                    toolCall.type(),
                    toolCall.function().name(),
                    toolCall.function().arguments()
            );
        }
        for (LLMChatCompletionTool toolCall: responseMessage.toolCalls()) {
            //TODO this could also be a map (toolname => tool function)
            switch (toolCall.function().name()) {
                case "write":
                    String toolResponse = callWriteTool(toolCall.function().arguments());

                    //TODO adicione polimorfismo e adicione mais campos
                    //   {
                    //    "role": "tool",
                    //    "tool_call_id": "call_abc123",
                    //    "name": "get_current_weather",
                    //    "content": "{\"temperature\": \"72°F\", \"condition\": \"Sunny\"}"
                    //  }
                    messages.add(new LLMChatCompletionMessage("tool", toolResponse));

                    //TODO send it back to LLM
                    break;
                default:
                    logger.atWarn()
                            .addKeyValue("toolName", toolCall.function().name())
                            .log("unknown tool call");
            }
        }
        return responseMessage.content();
    }

    private String callWriteTool(String arguments) {
        logger.atInfo()
                .addKeyValue("arguments", arguments)
                .log("calling write tool...");

        record Args(
                @JsonProperty("output_file_path") String outputFilePath,
                String contents
        ){}

        Args args = this.objectMapper.readValue(arguments, new TypeReference<>(){});

        try {
            Files.writeString(Path.of(args.outputFilePath), args.contents);
            return "arquivo escrito com sucesso";
        } catch (IOException e) {
            logger.atError().setCause(e).log("write falhou");
            return e.toString();
        }
    }

    private LLMChatCompletionResponse parseResponse(HttpResponse<InputStream> response) throws IOException {
        try (InputStream is = response.body()) {
            return this.objectMapper.readValue(is, new TypeReference<>() {});
        }
    }

    public String getModelName() {
        return this.config.getLlmModelName();
    }

    public String getSystemPrompt() {
        return this.config.getLlmSystemPrompt();
    }
}
