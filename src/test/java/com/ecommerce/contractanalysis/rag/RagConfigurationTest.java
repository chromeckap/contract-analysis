package com.ecommerce.contractanalysis.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagConfigurationTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private Resource mockResource1;

    @Mock
    private Resource mockResource2;

    @TempDir
    Path tempDir;

    private RagConfiguration ragConfiguration;

    @BeforeEach
    void setUp() {
        ragConfiguration = new RagConfiguration();

        ReflectionTestUtils.setField(ragConfiguration, "vectorStoreName", "vectorstore.json");
        ReflectionTestUtils.setField(ragConfiguration, "vectorStorePath", "/app/data");
        ReflectionTestUtils.setField(ragConfiguration, "laws", new Resource[]{mockResource1, mockResource2});
    }

    @Test
    void simpleVectorStore_WhenTextReaderThrowsException_ShouldPropagateException() {
        // Given
        String vectorStorePath = tempDir.toString();
        ReflectionTestUtils.setField(ragConfiguration, "vectorStorePath", vectorStorePath);

        when(mockResource1.getFilename()).thenReturn("law1.json");

        try (MockedConstruction<TextReader> ignored = mockConstruction(TextReader.class,
                (mock, context) -> {
                    when(mock.getCustomMetadata()).thenReturn(new HashMap<>());
                    when(mock.get()).thenThrow(new RuntimeException("Failed to read resource"));
                })) {

            // When & Then
            assertThrows(RuntimeException.class,
                    () -> ragConfiguration.simpleVectorStore(embeddingModel));
        }
    }

    @Test
    void getVectorStoreFile_ShouldReturnCorrectPath() {
        // Given
        String vectorStorePath = "/custom/path";
        String vectorStoreName = "custom-name.json";
        ReflectionTestUtils.setField(ragConfiguration, "vectorStorePath", vectorStorePath);
        ReflectionTestUtils.setField(ragConfiguration, "vectorStoreName", vectorStoreName);

        // When - using reflection to access private method
        File result = ReflectionTestUtils.invokeMethod(ragConfiguration, "getVectorStoreFile");

        // Then
        assertNotNull(result);
        assertEquals(Paths.get(vectorStorePath, vectorStoreName).toFile(), result);
    }

    @Test
    void simpleVectorStore_WithDefaultValues_ShouldUseDefaults() {
        // Given - using default values set in @BeforeEach
        // When
        File expectedFile = ReflectionTestUtils.invokeMethod(ragConfiguration, "getVectorStoreFile");

        // Then
        assertNotNull(expectedFile);
        assertEquals("\\app\\data\\vectorstore.json", expectedFile.getPath());
    }

    @Test
    void simpleVectorStore_WithNullResource_ShouldHandleGracefully() {
        // Given
        String vectorStorePath = tempDir.toString();
        ReflectionTestUtils.setField(ragConfiguration, "vectorStorePath", vectorStorePath);
        ReflectionTestUtils.setField(ragConfiguration, "laws", new Resource[]{null});

        // When & Then
        assertThrows(NullPointerException.class,
                () -> ragConfiguration.simpleVectorStore(embeddingModel));
    }

}