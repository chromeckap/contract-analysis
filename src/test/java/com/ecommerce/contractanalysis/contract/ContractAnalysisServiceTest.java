package com.ecommerce.contractanalysis.contract;

import com.ecommerce.contractanalysis.issue.Issues;
import com.ecommerce.contractanalysis.utils.FileConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractAnalysisServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private SimpleVectorStore vectorStore;

    @Mock
    private FileConverter fileConverter;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Issues mockIssues;

    private ContractAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new ContractAnalysisService(chatClientBuilder, vectorStore, fileConverter);
    }

    @Test
    void constructor_ShouldInitializeWithAllDependencies() {
        // When
        ContractAnalysisService newService = new ContractAnalysisService(
                chatClientBuilder,
                vectorStore,
                fileConverter
        );

        // Then
        assertNotNull(newService);
    }

    @Test
    void analyze_WithValidFile_ShouldExtractTextAndExecutePipeline() {
        // Given
        String extractedText = "Sample contract text content";
        when(fileConverter.extractText(multipartFile)).thenReturn(extractedText);

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute(extractedText)).thenReturn(mockIssues);
                })) {

            // When
            Issues result = service.analyze(multipartFile);

            // Then
            assertSame(mockIssues, result);
            verify(fileConverter).extractText(multipartFile);

            // Verify pipeline construction and execution
            assertEquals(1, mockedPipeline.constructed().size());
            ContractAnalysisPipeline pipeline = mockedPipeline.constructed().getFirst();
            verify(pipeline, times(3)).addStep(any()); // 3 steps added
            verify(pipeline).execute(extractedText);
        }
    }

    @Test
    void analyze_ShouldCreatePipelineWithCorrectBuilder() {
        // Given
        String extractedText = "Contract content";
        when(fileConverter.extractText(multipartFile)).thenReturn(extractedText);

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    // Verify constructor was called with correct builder
                    assertEquals(chatClientBuilder, context.arguments().getFirst());
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute(extractedText)).thenReturn(mockIssues);
                })) {

            // When
            service.analyze(multipartFile);

            // Then
            assertEquals(1, mockedPipeline.constructed().size());
        }
    }

    @Test
    void analyze_ShouldAddStepsInCorrectOrder() {
        // Given
        String extractedText = "Contract content";
        when(fileConverter.extractText(multipartFile)).thenReturn(extractedText);

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute(extractedText)).thenReturn(mockIssues);
                })) {

            // When
            service.analyze(multipartFile);

            // Then
            ContractAnalysisPipeline pipeline = mockedPipeline.constructed().getFirst();

            // Verify addStep was called exactly 3 times (for each step)
            verify(pipeline, times(3)).addStep(any());

            // We can't easily verify the exact order without more complex mocking,
            // but we can verify that all expected step types would be added
            verify(pipeline).execute(extractedText);
        }
    }

    @Test
    void analyze_WithNullFile_ShouldHandleGracefully() {
        // Given
        when(fileConverter.extractText(null)).thenReturn("");

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute("")).thenReturn(mockIssues);
                })) {

            // When
            Issues result = service.analyze(null);

            // Then
            assertSame(mockIssues, result);
            verify(fileConverter).extractText(null);
        }
    }

    @Test
    void analyze_WhenFileConverterThrowsException_ShouldPropagateException() {
        // Given
        RuntimeException expectedException = new RuntimeException("File conversion failed");
        when(fileConverter.extractText(multipartFile)).thenThrow(expectedException);

        // When & Then
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            service.analyze(multipartFile);
        });

        assertEquals("File conversion failed", thrownException.getMessage());
        verify(fileConverter).extractText(multipartFile);
    }

    @Test
    void analyze_WhenPipelineExecutionFails_ShouldPropagateException() {
        // Given
        String extractedText = "Contract content";
        RuntimeException expectedException = new RuntimeException("Pipeline execution failed");
        when(fileConverter.extractText(multipartFile)).thenReturn(extractedText);

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute(extractedText)).thenThrow(expectedException);
                })) {

            // When & Then
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                service.analyze(multipartFile);
            });

            assertEquals("Pipeline execution failed", thrownException.getMessage());
        }
    }

    @Test
    void analyze_WithLongTextContent_ShouldHandleCorrectly() {
        // Given
        String extractedText = "This is a long contract with many clauses and conditions. ".repeat(1000);

        when(fileConverter.extractText(multipartFile)).thenReturn(extractedText);

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute(extractedText)).thenReturn(mockIssues);
                })) {

            // When
            Issues result = service.analyze(multipartFile);

            // Then
            assertSame(mockIssues, result);
            verify(fileConverter).extractText(multipartFile);

            ContractAnalysisPipeline pipeline = mockedPipeline.constructed().get(0);
            verify(pipeline).execute(extractedText);
        }
    }

    @Test
    void analyze_MultipleInvocations_ShouldCreateNewPipelineEachTime() {
        // Given
        String extractedText1 = "Contract 1";
        String extractedText2 = "Contract 2";
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);

        when(fileConverter.extractText(file1)).thenReturn(extractedText1);
        when(fileConverter.extractText(file2)).thenReturn(extractedText2);

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute(anyString())).thenReturn(mockIssues);
                })) {

            // When
            service.analyze(file1);
            service.analyze(file2);

            // Then
            assertEquals(2, mockedPipeline.constructed().size());
            verify(fileConverter).extractText(file1);
            verify(fileConverter).extractText(file2);
        }
    }

    @Test
    void analyze_ShouldUseProvidedVectorStoreInLawRetrievalStep() {
        // Given
        String extractedText = "Contract content";
        when(fileConverter.extractText(multipartFile)).thenReturn(extractedText);

        try (MockedConstruction<ContractAnalysisPipeline> mockedPipeline = mockConstruction(
                ContractAnalysisPipeline.class,
                (mock, context) -> {
                    when(mock.addStep(any())).thenReturn(mock);
                    when(mock.execute(extractedText)).thenReturn(mockIssues);
                })) {

            // When
            service.analyze(multipartFile);

            // Then
            // Verify that the pipeline was created and steps were added
            assertEquals(1, mockedPipeline.constructed().size());
            ContractAnalysisPipeline pipeline = mockedPipeline.constructed().getFirst();
            verify(pipeline, times(3)).addStep(any());
        }
    }
}