package mysimpleagent.llm.chatcompletions.payloads;

public record LLMChatCompletionTool(
        String type,
        LLMChatCompletionToolFunction function
){}
