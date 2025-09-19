package com.ai.contractanalysis.step;

import com.ai.contractanalysis.issue.Issues;
import com.ai.contractanalysis.utils.StepResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComplianceCheckStepTest {

    private ComplianceCheckStep step;
    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec promptSpec;
    private ChatClient.CallResponseSpec response;

    @BeforeEach
    void setUp() {
        step = new ComplianceCheckStep();

        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        response = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);

        // Mock fluent API
        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(anyString())).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);

        Issues mockedIssues = new Issues(List.of());
        when(promptSpec.call()).thenReturn(response);
        when(response.entity(Issues.class)).thenReturn(mockedIssues);
    }

    @Test
    void testExecuteSuccess() {
        Map<ContextKey, Object> context = new HashMap<>();
        context.put(ContextKey.CONTRACT_ANALYSIS, "Sample contract analysis");
        context.put(ContextKey.LEGAL_CONTEXT, "Sample legal context");
        context.put(ContextKey.ORIGINAL_CONTRACT, "Sample original contract");

        StepResult result = step.execute("Sample input", context, chatClientBuilder);

        assertTrue(result.success());
        assertNotNull(context.get(ContextKey.COMPLIANCE_ISSUES));
        assertEquals("Kontrola shody dokončena úspěšně", result.message());
    }

    @Test
    void testExecuteFailure() {
        when(promptSpec.call()).thenThrow(new RuntimeException("Chat service error"));

        Map<ContextKey, Object> context = new HashMap<>();
        context.put(ContextKey.CONTRACT_ANALYSIS, "Sample contract analysis");
        context.put(ContextKey.LEGAL_CONTEXT, "Sample legal context");
        context.put(ContextKey.ORIGINAL_CONTRACT, "Sample original contract");

        StepResult result = step.execute("Sample input", context, chatClientBuilder);

        assertFalse(result.success());
        assertEquals("", context.getOrDefault(ContextKey.COMPLIANCE_ISSUES, ""));
        assertTrue(result.message().contains("Chyba při kontrole shody"));
    }
}
