package com.ecommerce.contractanalysis.step;

import com.ecommerce.contractanalysis.utils.StepResult;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public abstract class ReasoningStep {
    protected final String name;
    protected final String description;

    protected ReasoningStep(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract StepResult execute(String input, Map<String, Object> context, ChatClient.Builder chatClient);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
