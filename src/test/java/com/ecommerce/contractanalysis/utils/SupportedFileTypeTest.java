package com.ecommerce.contractanalysis.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupportedFileTypeTest {

    @Test
    void getExtension_ShouldReturnCorrectValues() {
        assertEquals("pdf", SupportedFileType.PDF.getExtension());
        assertEquals("docx", SupportedFileType.DOCX.getExtension());
    }

    @Test
    void fromExtension_ShouldReturnEnum_WhenExtensionIsValid() {
        assertEquals(SupportedFileType.PDF, SupportedFileType.fromExtension("pdf"));
        assertEquals(SupportedFileType.DOCX, SupportedFileType.fromExtension("docx"));
    }

    @Test
    void fromExtension_ShouldBeCaseInsensitive() {
        assertEquals(SupportedFileType.PDF, SupportedFileType.fromExtension("PDF"));
        assertEquals(SupportedFileType.DOCX, SupportedFileType.fromExtension("DoCx"));
    }

    @Test
    void fromExtension_ShouldThrowException_WhenExtensionIsNotSupported() {
        String invalidExtension = "txt";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> SupportedFileType.fromExtension(invalidExtension)
        );

        assertTrue(ex.getMessage().contains("Nepodporovaný typ souboru: txt"));
        assertTrue(ex.getMessage().contains("pdf, docx"));
    }

    @Test
    void getSupportedExtensions_ShouldReturnAllExtensions() {
        List<String> extensions = SupportedFileType.getSupportedExtensions();

        assertNotNull(extensions);
        assertEquals(2, extensions.size());
        assertTrue(extensions.contains("pdf"));
        assertTrue(extensions.contains("docx"));
    }
}
