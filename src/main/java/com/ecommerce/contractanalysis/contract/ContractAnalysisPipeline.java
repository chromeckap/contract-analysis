package com.ecommerce.contractanalysis.contract;

import com.ecommerce.contractanalysis.step.ReasoningStep;
import com.ecommerce.contractanalysis.issue.Issues;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractAnalysisPipeline {
    private final List<ReasoningStep> steps = new ArrayList<>();
    private final Map<String, Object> context = new HashMap<>();
    private final ChatClient.Builder chatClientBuilder;

    public ContractAnalysisPipeline(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public ContractAnalysisPipeline addStep(ReasoningStep step) {
        steps.add(step);
        return this;
    }

    public Issues execute(String input) {
        //todo add logic
        return null;
    }
}
