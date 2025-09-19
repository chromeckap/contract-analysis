package com.ai.contractanalysis.step;

import com.ai.contractanalysis.issue.Issues;

public enum ContextKey {
    ORIGINAL_INPUT(String.class),
    CONTRACT_ANALYSIS(String.class),
    LEGAL_CONTEXT(String.class),
    ORIGINAL_CONTRACT(String.class),
    COMPLIANCE_ISSUES(Issues.class);

    private final Class<?> type;

    ContextKey(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
