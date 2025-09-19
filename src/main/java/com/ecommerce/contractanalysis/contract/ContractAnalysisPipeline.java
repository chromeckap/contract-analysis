package com.ecommerce.contractanalysis.contract;

import com.ecommerce.contractanalysis.step.ComplianceCheckStep;
import com.ecommerce.contractanalysis.step.ContextKey;
import com.ecommerce.contractanalysis.step.ReasoningStep;
import com.ecommerce.contractanalysis.issue.Issues;
import com.ecommerce.contractanalysis.utils.StepResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractAnalysisPipeline {
    private final List<ReasoningStep> steps = new ArrayList<>();
    private final Map<ContextKey, Object> context = new HashMap<>();
    private final List<StepResult> executionHistory = new ArrayList<>();
    private final ChatClient.Builder chatClientBuilder;

    public ContractAnalysisPipeline(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public ContractAnalysisPipeline addStep(ReasoningStep step) {
        steps.add(step);
        return this;
    }

    public Issues execute(String input) {
        String currentInput = input;
        context.put(ContextKey.ORIGINAL_INPUT, input);

        Issues finalResult = null;

        for (ReasoningStep step : steps) {
            try {
                StepResult result = step.execute(currentInput, context, chatClientBuilder);
                executionHistory.add(result);

                if (!result.success()) {
                    break;
                }

                if (step instanceof ComplianceCheckStep) {
                    finalResult = (Issues) context.get(ContextKey.COMPLIANCE_ISSUES);
                }

                currentInput = result.output();

            } catch (Exception e) {
                executionHistory.add(new StepResult(
                        step.getName(), currentInput, "", e.getMessage(), false
                ));
                break;
            }
        }

        return finalResult != null ? finalResult : createEmptyIssues();
    }


    private Issues createEmptyIssues() {
        return new Issues(List.of());
    }

    public List<StepResult> getExecutionHistory() {
        return executionHistory;
    }
}
