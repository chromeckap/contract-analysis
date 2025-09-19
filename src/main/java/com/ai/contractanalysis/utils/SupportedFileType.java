package com.ai.contractanalysis.utils;

import java.util.Arrays;
import java.util.List;

public enum SupportedFileType {
    PDF("pdf"),
    DOCX("docx");

    private final String extension;

    SupportedFileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static SupportedFileType fromExtension(String extension) {
        return Arrays.stream(values())
                .filter(type -> type.extension.equalsIgnoreCase(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(
                                "Nepodporovaný typ souboru: %s. Podporované formáty: %s",
                                extension,
                                String.join(", ", getSupportedExtensions())
                        )
                ));
    }

    public static List<String> getSupportedExtensions() {
        return Arrays.stream(values())
                .map(SupportedFileType::getExtension)
                .toList();
    }
}
