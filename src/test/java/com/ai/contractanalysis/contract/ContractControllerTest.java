package com.ai.contractanalysis.contract;

import com.ai.contractanalysis.issue.Issues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    @Mock
    private ContractAnalysisService contractAnalysisService;

    @Mock
    private Issues mockIssues;

    private ContractController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new ContractController(contractAnalysisService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void constructor_ShouldInitializeWithService() {
        // When
        ContractController newController = new ContractController(contractAnalysisService);

        // Then
        assertNotNull(newController);
    }

    @Test
    void analyzeContract_WithValidFile_ShouldReturnOkWithIssues() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test contract content".getBytes()
        );

        when(contractAnalysisService.analyze(any(MultipartFile.class))).thenReturn(mockIssues);

        // When
        ResponseEntity<Issues> response = controller.analyzeContract(mockFile);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(mockIssues, response.getBody());
        verify(contractAnalysisService).analyze(mockFile);
    }

    @Test
    void analyzeContract_WithEmptyFile_ShouldStillReturnOkWithIssues() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[0]
        );

        when(contractAnalysisService.analyze(any(MultipartFile.class))).thenReturn(mockIssues);

        // When
        ResponseEntity<Issues> response = controller.analyzeContract(emptyFile);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(mockIssues, response.getBody());
        verify(contractAnalysisService).analyze(emptyFile);
    }

    @Test
    void analyzeContract_WhenServiceThrowsException_ShouldPropagateException() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test content".getBytes()
        );

        RuntimeException expectedException = new RuntimeException("Analysis failed");
        when(contractAnalysisService.analyze(mockFile)).thenThrow(expectedException);

        // When & Then
        RuntimeException thrownException = assertThrows(RuntimeException.class,
                () -> controller.analyzeContract(mockFile));

        assertEquals("Analysis failed", thrownException.getMessage());
        verify(contractAnalysisService).analyze(mockFile);
    }

    @Test
    void analyzeContract_WithDifferentFileTypes_ShouldAcceptAll() {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf content".getBytes());
        MockMultipartFile docFile = new MockMultipartFile(
                "file", "contract.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx content".getBytes());
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "contract.txt", MediaType.TEXT_PLAIN_VALUE, "text content".getBytes());

        when(contractAnalysisService.analyze(any(MultipartFile.class))).thenReturn(mockIssues);

        // When
        ResponseEntity<Issues> pdfResponse = controller.analyzeContract(pdfFile);
        ResponseEntity<Issues> docResponse = controller.analyzeContract(docFile);
        ResponseEntity<Issues> txtResponse = controller.analyzeContract(txtFile);

        // Then
        assertEquals(HttpStatus.OK, pdfResponse.getStatusCode());
        assertEquals(HttpStatus.OK, docResponse.getStatusCode());
        assertEquals(HttpStatus.OK, txtResponse.getStatusCode());

        verify(contractAnalysisService).analyze(pdfFile);
        verify(contractAnalysisService).analyze(docFile);
        verify(contractAnalysisService).analyze(txtFile);
    }

    @Test
    void analyzeContract_HttpRequest_ShouldReturnOkResponse() throws Exception {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test contract content".getBytes()
        );

        Issues testIssues = new Issues(Collections.emptyList());
        when(contractAnalysisService.analyze(any(MultipartFile.class))).thenReturn(testIssues);

        // When & Then
        mockMvc.perform(multipart("/api/v1/contracts/analyze")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.issues").exists());

        verify(contractAnalysisService).analyze(any(MultipartFile.class));
    }

    @Test
    void analyzeContract_HttpRequest_WithoutFile_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/contracts/analyze"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(contractAnalysisService);
    }

    @Test
    void analyzeContract_HttpRequest_WithWrongParameterName_ShouldReturnBadRequest() throws Exception {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "wrongName", // Wrong parameter name
                "contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/v1/contracts/analyze")
                        .file(mockFile))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(contractAnalysisService);
    }

    @Test
    void analyzeContract_HttpRequest_WithLargeFile_ShouldHandleCorrectly() throws Exception {
        // Given
        byte[] largeContent = new byte[1024 * 1024]; // 1MB file
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                largeContent
        );

        Issues testIssues = new Issues(Collections.emptyList());
        when(contractAnalysisService.analyze(any(MultipartFile.class))).thenReturn(testIssues);

        // When & Then
        mockMvc.perform(multipart("/api/v1/contracts/analyze")
                        .file(largeFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(contractAnalysisService).analyze(any(MultipartFile.class));
    }

    @Test
    void analyzeContract_MultipleFiles_ShouldProcessEachSeparately() {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "contract1.pdf", MediaType.APPLICATION_PDF_VALUE, "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "contract2.pdf", MediaType.APPLICATION_PDF_VALUE, "content2".getBytes());

        Issues issues1 = mock(Issues.class);
        Issues issues2 = mock(Issues.class);

        when(contractAnalysisService.analyze(file1)).thenReturn(issues1);
        when(contractAnalysisService.analyze(file2)).thenReturn(issues2);

        // When
        ResponseEntity<Issues> response1 = controller.analyzeContract(file1);
        ResponseEntity<Issues> response2 = controller.analyzeContract(file2);

        // Then
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertSame(issues1, response1.getBody());
        assertSame(issues2, response2.getBody());

        verify(contractAnalysisService).analyze(file1);
        verify(contractAnalysisService).analyze(file2);
    }

    @Test
    void analyzeContract_ShouldHaveCorrectEndpointMapping() throws Exception {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        Issues testIssues = new Issues(Collections.emptyList());
        when(contractAnalysisService.analyze(any(MultipartFile.class))).thenReturn(testIssues);

        // When & Then - Test correct endpoint path
        mockMvc.perform(multipart("/api/v1/contracts/analyze")
                        .file(mockFile))
                .andExpect(status().isOk());

        // Test wrong endpoint should return 404
        mockMvc.perform(multipart("/api/v1/contracts/wrong-endpoint")
                        .file(mockFile))
                .andExpect(status().isNotFound());
    }

    @Test
    void analyzeContract_ShouldOnlyAcceptPostRequests() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/contracts/analyze")
                        .with(request -> {
                            request.setMethod("GET");
                            return request;
                        }))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void analyzeContract_WithSpecialCharactersInFilename_ShouldHandleCorrectly() throws Exception {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "contrâct_with_špecial_characters.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test content".getBytes()
        );

        Issues testIssues = new Issues(Collections.emptyList());
        when(contractAnalysisService.analyze(any(MultipartFile.class))).thenReturn(testIssues);

        // When & Then
        mockMvc.perform(multipart("/api/v1/contracts/analyze")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(contractAnalysisService).analyze(any(MultipartFile.class));
    }
}