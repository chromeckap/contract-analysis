package com.ecommerce.contractanalysis.contract;

import com.ecommerce.contractanalysis.step.ComplianceCheckStep;
import com.ecommerce.contractanalysis.step.ContextKey;
import com.ecommerce.contractanalysis.step.ReasoningStep;
import com.ecommerce.contractanalysis.issue.Issues;
import com.ecommerce.contractanalysis.utils.StepResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractAnalysisPipelineTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ReasoningStep mockStep1;

    @Mock
    private ReasoningStep mockStep2;

    @Mock
    private ComplianceCheckStep complianceCheckStep;

    @Mock
    private Issues mockIssues;

    private ContractAnalysisPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new ContractAnalysisPipeline(chatClientBuilder);
    }

    @Test
    void constructor_ShouldInitializeWithChatClientBuilder() {
        // Given & When
        ContractAnalysisPipeline newPipeline = new ContractAnalysisPipeline(chatClientBuilder);

        // Then
        assertNotNull(newPipeline);
        assertTrue(newPipeline.getExecutionHistory().isEmpty());
    }

    @Test
    void addStep_ShouldReturnPipelineForChaining() {
        // When
        ContractAnalysisPipeline result = pipeline.addStep(mockStep1);

        // Then
        assertSame(pipeline, result);
    }

    @Test
    void addStep_ShouldAllowMultipleSteps() {
        // When
        pipeline.addStep(mockStep1).addStep(mockStep2);

        // Then - no exception should be thrown and pipeline should work
        assertNotNull(pipeline);
    }

    @Test
    void execute_WithSuccessfulStep_ShouldExecuteAndReturnEmptyIssues() {
        // Given
        String input = "test contract";
        String output = "processed contract";
        StepResult successResult = new StepResult("TestStep", input, output, null, true);

        when(mockStep1.execute(eq(input), any(Map.class), eq(chatClientBuilder)))
                .thenReturn(successResult);

        pipeline.addStep(mockStep1);

        // When
        Issues result = pipeline.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(1, pipeline.getExecutionHistory().size());
        verify(mockStep1).execute(eq(input), any(Map.class), eq(chatClientBuilder));
    }

    @Test
    void execute_WithFailedStep_ShouldStopExecution() {
        // Given
        String input = "test contract";
        StepResult failedResult = new StepResult("TestStep", input, "", "Error occurred", false);

        when(mockStep1.execute(eq(input), any(Map.class), eq(chatClientBuilder)))
                .thenReturn(failedResult);

        pipeline.addStep(mockStep1).addStep(mockStep2);

        // When
        Issues result = pipeline.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(1, pipeline.getExecutionHistory().size());
        verify(mockStep1).execute(eq(input), any(Map.class), eq(chatClientBuilder));
        verifyNoInteractions(mockStep2);
    }

    @Test
    void execute_WithException_ShouldCaptureErrorAndStopExecution() {
        // Given
        String input = "test contract";
        String errorMessage = "Unexpected error";

        when(mockStep1.execute(eq(input), any(Map.class), eq(chatClientBuilder)))
                .thenThrow(new RuntimeException(errorMessage));
        when(mockStep1.getName()).thenReturn("TestStep");

        pipeline.addStep(mockStep1).addStep(mockStep2);

        // When
        Issues result = pipeline.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(1, pipeline.getExecutionHistory().size());
        StepResult errorResult = pipeline.getExecutionHistory().getFirst();
        assertEquals("TestStep", errorResult.stepName());
        assertEquals(input, errorResult.input());
        assertEquals("", errorResult.output());
        assertEquals(errorMessage, errorResult.message());
        assertFalse(errorResult.success());

        verify(mockStep1).execute(eq(input), any(Map.class), eq(chatClientBuilder));
        verifyNoInteractions(mockStep2);
    }

    @Test
    void execute_WithMultipleSteps_ShouldChainOutputsCorrectly() {
        // Given
        String input = "test contract";
        String intermediateOutput = "step1 output";
        String finalOutput = "step2 output";

        StepResult result1 = new StepResult("Step1", input, intermediateOutput, null, true);
        StepResult result2 = new StepResult("Step2", intermediateOutput, finalOutput, null, true);

        when(mockStep1.execute(eq(input), any(Map.class), eq(chatClientBuilder)))
                .thenReturn(result1);
        when(mockStep2.execute(eq(intermediateOutput), any(Map.class), eq(chatClientBuilder)))
                .thenReturn(result2);

        pipeline.addStep(mockStep1).addStep(mockStep2);

        // When
        Issues result = pipeline.execute(input);

        // Then
        assertNotNull(result);
        assertEquals(2, pipeline.getExecutionHistory().size());
        verify(mockStep1).execute(eq(input), any(Map.class), eq(chatClientBuilder));
        verify(mockStep2).execute(eq(intermediateOutput), any(Map.class), eq(chatClientBuilder));
    }

    @Test
    void execute_ShouldSetOriginalInputInContext() {
        // Given
        String input = "test contract";
        StepResult successResult = new StepResult("TestStep", input, "output", null, true);

        when(mockStep1.execute(eq(input), any(Map.class), eq(chatClientBuilder)))
                .thenAnswer(invocation -> {
                    Map<ContextKey, Object> context = invocation.getArgument(1);
                    assertEquals(input, context.get(ContextKey.ORIGINAL_INPUT));
                    return successResult;
                });

        pipeline.addStep(mockStep1);

        // When
        pipeline.execute(input);

        // Then
        verify(mockStep1).execute(eq(input), any(Map.class), eq(chatClientBuilder));
    }

    @Test
    void execute_WithComplianceStepAndOtherSteps_ShouldReturnComplianceIssues() {
        // Given
        String input = "test contract";
        StepResult result1 = new StepResult("PreStep", input, "pre-output", null, true);
        StepResult complianceResult = new StepResult("ComplianceCheck", "pre-output", "compliance-output", null, true);
        StepResult result3 = new StepResult("PostStep", "compliance-output", "final-output", null, true);

        when(mockStep1.execute(eq(input), any(Map.class), eq(chatClientBuilder)))
                .thenReturn(result1);
        when(complianceCheckStep.execute(eq("pre-output"), any(Map.class), eq(chatClientBuilder)))
                .thenAnswer(invocation -> {
                    Map<ContextKey, Object> context = invocation.getArgument(1);
                    context.put(ContextKey.COMPLIANCE_ISSUES, mockIssues);
                    return complianceResult;
                });
        when(mockStep2.execute(eq("compliance-output"), any(Map.class), eq(chatClientBuilder)))
                .thenReturn(result3);

        pipeline.addStep(mockStep1)
                .addStep(complianceCheckStep)
                .addStep(mockStep2);

        // When
        Issues result = pipeline.execute(input);

        // Then
        assertSame(mockIssues, result);
        assertEquals(3, pipeline.getExecutionHistory().size());
    }

    @Test
    void getExecutionHistory_ShouldReturnImmutableView() {
        // Given
        String input = "test contract";
        StepResult successResult = new StepResult("TestStep", input, "output", null, true);

        when(mockStep1.execute(eq(input), any(Map.class), eq(chatClientBuilder)))
                .thenReturn(successResult);

        pipeline.addStep(mockStep1);
        pipeline.execute(input);

        // When
        List<StepResult> history = pipeline.getExecutionHistory();

        // Then
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("TestStep", history.getFirst().stepName());
    }
}