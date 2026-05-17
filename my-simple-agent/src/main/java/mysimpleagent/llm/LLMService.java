package mysimpleagent.llm;

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
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class.getSimpleName());

    private final HttpClient llmClient;
    private final ObjectMapper objectMapper;
    private final Config config;

    public LLMService(HttpClient llmClient, ObjectMapper objectMapper, Config config) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    public String chat(String userPrompt) throws IOException, InterruptedException {
        logger.atInfo().log("preparando requisição...");

        var stream = false;

        List<LLMChatCompletionMessage> messages = Arrays.asList(
                new LLMChatCompletionMessage("system", this.config.getLlmSystemPrompt()),
                new LLMChatCompletionMessage("user", userPrompt)
        );

        String toolsString = ResourceUtils.loadResourceAsString("tools.json");
        List<Object> tools = this.objectMapper.readValue(toolsString, new TypeReference<>(){});

        var payload = new LLMChatCompletionPayload(this.config.getLlmModelName(), stream, messages, tools);

        String payloadStr = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
//        System.err.println(payloadStr);

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

        LLMChatCompletionResponse respMsg = parseResponse(response);

        logger.atInfo()
                .addKeyValue("choices", respMsg.choices().size())
                .log("resposta obtida com sucesso.");

        return respMsg.choices().getFirst().message().content();
    }

    private LLMChatCompletionResponse parseResponse(HttpResponse<InputStream> response) throws IOException {
        try (InputStream is = response.body()) {
            return this.objectMapper.readValue(is, new TypeReference<>() {
            });
        }
    }
}
