package mysimpleagent;

import mysimpleagent.llm.LLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Scanner;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class.getSimpleName());

    static void main() {
        logger.atInfo().log("inicializando...");

        var config = Config.loadFromEnv();
        var objectMapper = new ObjectMapper();

        HttpClient llmClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        var llmService = new LLMService(llmClient, objectMapper, config);

        while (true) {
            var input = new Scanner(System.in);
            System.out.print(">>> ");
            System.out.flush();
            System.err.flush();
            String prompt = input.nextLine();

            final String answer;
            try {
                answer = llmService.chat(prompt);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                continue;
            }

            System.out.println("=== Answer ===");
            System.out.println(answer);
        }
    }
}
