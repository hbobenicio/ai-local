package mysimpleagent.llm.chatcompletions.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LLMMessage(
        String role,
        String content,
        @JsonProperty("reasoning_content") String reasoningContent
) {
}
