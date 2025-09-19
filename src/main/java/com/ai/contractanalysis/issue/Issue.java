package com.ai.contractanalysis.issue;

public record Issue(
        String passage,
        String recommendation,
        Importance importance
) {
}
