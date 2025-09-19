package com.ecommerce.contractanalysis.step;

import com.ecommerce.contractanalysis.utils.StepResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LawRetrievalStepTest {

    private LawRetrievalStep step;
    private VectorStore vectorStore;
    private ChatClient.Builder chatClientBuilder;
    private ChatClient ragChatClient;
    private ChatClient.ChatClientRequestSpec promptSpec;
    private ChatClient.CallResponseSpec response;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        step = new LawRetrievalStep(vectorStore);

        chatClientBuilder = mock(ChatClient.Builder.class);
        ragChatClient = mock(ChatClient.class);
        promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        response = mock(ChatClient.CallResponseSpec.class);

        // Mock fluent API
        when(chatClientBuilder.defaultAdvisors(any(QuestionAnswerAdvisor.class))).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(ragChatClient);

        when(ragChatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.system(anyString())).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(response);
        when(response.content()).thenReturn("Mocked legal retrieval response");
    }

    @Test
    void testExecuteSuccess() {
        Map<ContextKey, Object> context = new HashMap<>();
        context.put(ContextKey.CONTRACT_ANALYSIS, "Sample contract analysis");

        StepResult result = step.execute("Sample input", context, chatClientBuilder);

        assertTrue(result.success());
        assertEquals("Mocked legal retrieval response", context.get(ContextKey.LEGAL_CONTEXT));
        assertEquals("Právní informace byly úspěšně získány", result.message());
    }

    @Test
    void testExecuteFailure() {
        when(promptSpec.call()).thenThrow(new RuntimeException("Chat service error"));

        Map<ContextKey, Object> context = new HashMap<>();
        context.put(ContextKey.CONTRACT_ANALYSIS, "Sample contract analysis");

        StepResult result = step.execute("Sample input", context, chatClientBuilder);

        assertFalse(result.success());
        assertEquals("", context.getOrDefault(ContextKey.LEGAL_CONTEXT, ""));
        assertTrue(result.message().contains("Chyba při získávání právních informací"));
    }
}
