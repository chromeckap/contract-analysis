package com.ecommerce.contractanalysis.step;

import com.ecommerce.contractanalysis.utils.StepResult;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public class ContractAnalysisStep extends ReasoningStep {

    public ContractAnalysisStep() {
        super("Contract Analysis", "Analyze contract structure and extract key clauses");
    }

    @Override
    public StepResult execute(String input, Map<ContextKey, Object> context, ChatClient.Builder chatClient) {
        try {
            String systemPrompt = """
                Jste profesionální analytik smluv. Vaším úkolem je analyzovat poskytnutý text smlouvy a vyextrahovat:
                1. Typ smlouvy a zúčastněné strany
                2. Klíčové doložky a podmínky
                3. Důležité termíny a lhůty
                4. Finanční závazky
                5. Podmínky ukončení
                6. Rizikové oblasti, které vyžadují právní přezkum
                
                Poskytněte strukturovanou analýzu, která bude použita pro kontrolu souladu.
                """;

            String userPrompt = """
                Analyzujte tento text smlouvy a poskytněte strukturovaný rozbor:
                
                Text smlouvy:
                """ + input;

            String response = chatClient.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            context.put(ContextKey.CONTRACT_ANALYSIS, response);
            context.put(ContextKey.ORIGINAL_CONTRACT, input);

            return new StepResult(name, input, response, "Analýza smlouvy dokončena úspěšně", true);

        } catch (Exception e) {
            return new StepResult(name, input, "",
                    "Chyba při analýze smlouvy: " + e.getMessage(), false);
        }
    }
}
