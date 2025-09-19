package com.ai.contractanalysis.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileConverter {
    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Soubor je prázdný nebo chybí");
        }

        final String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        SupportedFileType.fromExtension(extension);

        try {
            return tika.parseToString(file.getInputStream());
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Chyba při čtení souboru: " + file.getOriginalFilename(), ex
            );
        }
    }

}
