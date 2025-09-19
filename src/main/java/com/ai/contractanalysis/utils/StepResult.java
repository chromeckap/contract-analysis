package com.ai.contractanalysis.utils;

public record StepResult(
        String stepName,
        String input,
        String output,
        String message,
        boolean success
) {
}
