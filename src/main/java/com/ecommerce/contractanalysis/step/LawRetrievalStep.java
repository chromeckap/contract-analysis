package com.ecommerce.contractanalysis.step;

import com.ecommerce.contractanalysis.utils.StepResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Map;

public class LawRetrievalStep extends ReasoningStep {
    private final VectorStore vectorStore;

    public LawRetrievalStep(VectorStore vectorStore) {
        super("Law Retrieval", "Retrieve relevant legal information using RAG");
        this.vectorStore = vectorStore;
    }

    @Override
    public StepResult execute(String input, Map<String, Object> context, ChatClient.Builder baseChatClient) {
        try {
            String contractAnalysis = (String) context.get("contract_analysis");

            ChatClient ragChatClient = baseChatClient
                    .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                    .build();

            String systemPrompt = """
                Jste právní expert s přístupem k rozsáhlé právní databázi.
                Na základě poskytnuté analýzy smlouvy identifikujte relevantní právní ustanovení,
                předpisy a judikaturu vztahující se k tomuto typu smlouvy.
                
                Zaměřte se na:
                1. Použitelné zákony a předpisy
                2. Běžné právní požadavky pro tento typ smlouvy
                3. Potenciální problémy s dodržováním předpisů
                4. Osvědčené právní postupy
                """;

            String userPrompt = """
                Na základě této analýzy smlouvy, která právní ustanovení a předpisy jsou relevantní?
                
                Analýza smlouvy:
                """ + contractAnalysis + """
                
                Uveďte prosím relevantní právní kontext a požadavky, které budou použity pro kontrolu souladu.
                """;


            String response = ragChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            context.put("legal_context", response);

            return new StepResult(name, contractAnalysis, response,
                    "Legal information retrieved successfully", true);

        } catch (Exception e) {
            return new StepResult(name, input, "",
                    "Error during legal information retrieval: " + e.getMessage(), false);
        }
    }
}
