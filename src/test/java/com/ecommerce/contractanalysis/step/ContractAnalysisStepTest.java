package com.ecommerce.contractanalysis.step;

import com.ecommerce.contractanalysis.utils.StepResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContractAnalysisStepTest {

    private ContractAnalysisStep step;
    private ChatClient.Builder chatClientBuilder;
    private ChatClient.ChatClientRequestSpec promptSpec;
    private ChatClient chatClient;
    private ChatClient.CallResponseSpec response;

    @BeforeEach
    void setUp() {
        step = new ContractAnalysisStep();

        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        response = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);

        // Mock fluent API
        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(anyString())).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(response);
        when(response.content()).thenReturn("Mocked contract analysis response");
    }

    @Test
    void testExecuteSuccess() {
        Map<ContextKey, Object> context = new HashMap<>();
        String inputText = "Sample contract text";

        StepResult result = step.execute(inputText, context, chatClientBuilder);

        assertTrue(result.success());
        assertEquals("Mocked contract analysis response", context.get(ContextKey.CONTRACT_ANALYSIS));
        assertEquals(inputText, context.get(ContextKey.ORIGINAL_CONTRACT));
        assertEquals("Analýza smlouvy dokončena úspěšně", result.message());
    }

    @Test
    void testExecuteFailure() {
        when(promptSpec.call()).thenThrow(new RuntimeException("Chat service error"));

        Map<ContextKey, Object> context = new HashMap<>();
        String inputText = "Sample contract text";

        StepResult result = step.execute(inputText, context, chatClientBuilder);

        assertFalse(result.success());
        assertEquals("", context.getOrDefault(ContextKey.CONTRACT_ANALYSIS, ""));
        assertTrue(result.message().contains("Chyba při analýze smlouvy"));
    }
}
