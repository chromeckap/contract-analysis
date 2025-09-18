package com.ecommerce.contractanalysis.contract;


import com.ecommerce.contractanalysis.step.ComplianceCheckStep;
import com.ecommerce.contractanalysis.step.ContractAnalysisStep;
import com.ecommerce.contractanalysis.step.LawRetrievalStep;
import com.ecommerce.contractanalysis.issue.Issues;
import com.ecommerce.contractanalysis.utils.FileConverter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContractAnalysisService {
    private final ChatClient.Builder builder;
    private final SimpleVectorStore vectorStore;
    private final FileConverter fileConverter;

    public ContractAnalysisService(
            ChatClient.Builder builder,
            SimpleVectorStore vectorStore,
            FileConverter fileConverter
    ) {
        this.builder = builder;
        this.vectorStore = vectorStore;
        this.fileConverter = fileConverter;
    }

    public Issues analyze(MultipartFile file) {
        String input = fileConverter.extractText(file);

        ContractAnalysisPipeline pipeline = new ContractAnalysisPipeline(builder)
                .addStep(new ContractAnalysisStep())
                .addStep(new LawRetrievalStep(vectorStore))
                .addStep(new ComplianceCheckStep());

        return pipeline.execute(input);
    }
}
