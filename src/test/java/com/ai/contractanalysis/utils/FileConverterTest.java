package com.ai.contractanalysis.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileConverterTest {

    private FileConverter fileConverter;

    @BeforeEach
    void setUp() {
        fileConverter = new FileConverter();
    }

    @Test
    void extractText_ShouldThrowException_WhenFileIsNull() {
        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileConverter.extractText(null));

        assertEquals("Soubor je prázdný nebo chybí", ex.getMessage());
    }

    @Test
    void extractText_ShouldThrowException_WhenFileIsEmpty() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileConverter.extractText(file));

        assertEquals("Soubor je prázdný nebo chybí", ex.getMessage());
    }

    @Test
    void extractText_ShouldThrowException_WhenUnsupportedExtension() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("file.xyz");

        // Mock statickou metodu SupportedFileType
        try (MockedStatic<SupportedFileType> mocked = mockStatic(SupportedFileType.class)) {
            mocked.when(() -> SupportedFileType.fromExtension("xyz"))
                    .thenThrow(new IllegalArgumentException("Nepodporovaný typ souboru"));

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> fileConverter.extractText(file));

            assertEquals("Nepodporovaný typ souboru", ex.getMessage());
        }
    }

    @Test
    void extractText_ShouldThrowException_WhenIOExceptionOccurs() throws Exception {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("file.pdf");
        when(file.getInputStream()).thenThrow(new IOException("IO chyba"));

        try (MockedStatic<SupportedFileType> mocked = mockStatic(SupportedFileType.class)) {
            mocked.when(() -> SupportedFileType.fromExtension("pdf"))
                    .thenReturn(SupportedFileType.PDF);

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> fileConverter.extractText(file));

            assertTrue(ex.getMessage().contains("Chyba při čtení souboru: file.pdf"));
            assertNotNull(ex.getCause());
            assertEquals("IO chyba", ex.getCause().getMessage());
        }
    }
}
