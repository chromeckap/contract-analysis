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
    public StepResult execute(String input, Map<ContextKey, Object> context, ChatClient.Builder baseChatClient) {
        try {
            String contractAnalysis = (String) context.get(ContextKey.CONTRACT_ANALYSIS);

            ChatClient ragChatClient = baseChatClient
                    .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                    .build();

            String systemPrompt = """
                Jste právní expert. Máte k dispozici POUZE níže uvedené právní předpisy,
                které jsou definované v poskytnuté databázi (JSON soubory).
                
                Neodkazujte na žádné jiné zákony, paragrafy, judikaturu ani zdroje,
                které nejsou výslovně součástí tohoto seznamu
                
                Vaším úkolem je:
                1. Na základě analýzy smlouvy vyhledat relevantní ustanovení
                2. Vysvětlit, proč jsou tato ustanovení aplikovatelná
                3. Nepřidávat žádné další právní předpisy mimo tento seznam
                """;

            String userPrompt = """
                Na základě této analýzy smlouvy určete, která ustanovení
                z POSKYTNUTÉHO seznamu právních předpisů jsou relevantní.
                
                Analýza smlouvy:
                """ + contractAnalysis + """
                
                Pokud žádné ustanovení neodpovídá, uveďte "Žádné ustanovení z poskytnutého seznamu se nevztahuje".
                """;


            String response = ragChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            context.put(ContextKey.LEGAL_CONTEXT, response);

            return new StepResult(name, contractAnalysis, response,
                    "Legal information retrieved successfully", true);

        } catch (Exception e) {
            return new StepResult(name, input, "",
                    "Error during legal information retrieval: " + e.getMessage(), false);
        }
    }
}
