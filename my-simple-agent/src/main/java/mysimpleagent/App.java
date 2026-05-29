package mysimpleagent;

import mysimpleagent.llm.LLMService;
import mysimpleagent.llm.chatcompletions.payloads.LLMChatCompletionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class.getSimpleName());

    static void main() {
        logger.atInfo().log("inicializando...");

        var config = Config.loadFromEnv();
        var objectMapper = new ObjectMapper();

        HttpClient llmClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        var llmService = new LLMService(llmClient, objectMapper, config);
        List<LLMChatCompletionMessage> messages = llmService.newConversation();

        while (true) {
            var input = new Scanner(System.in);
            System.out.print(">>> ");

            // realmente preciso?
            System.out.flush();
            System.err.flush();

            final String prompt;
            try {
                prompt = input.nextLine().trim().toLowerCase();
            } catch (NoSuchElementException e) {
                // No lines == EOF
                break;
            }

            if (prompt.equals("/exit") || prompt.equals("/quit")) {
                break;
            }

            if (prompt.equals("/clear")) {
                messages = llmService.newConversation();
                continue;
            }

            final String answer;
            try {
                answer = llmService.chat(messages, prompt);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                continue;
            }

            System.out.println("=== Answer ===");
            System.out.println(answer);
        }
    }
}
