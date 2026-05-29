package mysimpleagent.llm.chatcompletions.payloads;

//TODO should I add tool calls here?
public record LLMChatCompletionMessage(String role, String content) {
}
