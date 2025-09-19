package com.ai.contractanalysis.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RagConfiguration {

    @Value("${vector.store.name:vectorstore.json}")
    private String vectorStoreName;

    @Value("${vector.store.path:/app/data}")
    private String vectorStorePath;

    @Value("classpath:/data/laws/*.json")
    private Resource[] laws;

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = getVectorStoreFile();

        if (vectorStoreFile.exists()) {
            simpleVectorStore.load(vectorStoreFile);
        } else {
            TextReader textReader;
            List<Document> allDocuments = new java.util.ArrayList<>();

            for (Resource law : laws) {
                textReader = new TextReader(law);
                textReader.getCustomMetadata().put("filename", law.getFilename());
                List<Document> documents = textReader.get();
                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocuments = textSplitter.apply(documents);
                allDocuments.addAll(splitDocuments);
            }

            simpleVectorStore.add(allDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }
        return simpleVectorStore;
    }

    private File getVectorStoreFile() {
        Path path = Paths.get(vectorStorePath, vectorStoreName);
        return path.toFile();
    }
}