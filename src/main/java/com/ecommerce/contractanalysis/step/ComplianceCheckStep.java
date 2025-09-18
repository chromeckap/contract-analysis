package com.ecommerce.contractanalysis.step;

import com.ecommerce.contractanalysis.issue.Issues;
import com.ecommerce.contractanalysis.utils.StepResult;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

public class ComplianceCheckStep extends ReasoningStep {
    public ComplianceCheckStep() {
        super("Compliance Check", "Check contract compliance and generate issues");
    }

    @Override
    public StepResult execute(String input, Map<String, Object> context, ChatClient.Builder chatClient) {
        try {
            String contractAnalysis = (String) context.get("contract_analysis");
            String legalContext = (String) context.get("legal_context");
            String originalContract = (String) context.get("original_contract");

            String systemPrompt = """
                Jste profesionální právník specializovaný na kontrolu souladu smluv.
                Vaším úkolem je identifikovat právní nepřesnosti, problémy s dodržováním předpisů
                a potenciální rizika v poskytnuté smlouvě na základě právního kontextu a analýzy.
                
                Pro každý nalezený problém uveďte:
                - Typ problému (např. "Chybějící doložka", "Porušení zákona", "Rizikový faktor")
                - Úroveň závažnosti (Kritická, Vysoká, Střední, Nízká)
                - Podrobný popis problému
                - Konkrétní doložku nebo část, které se problém týká
                - Doporučení k vyřešení
                - Přibližné číslo řádku, pokud lze určit
                
                Vraťte svá zjištění ve strukturovaném JSON formátu.
                """;

            String userPrompt = String.format("""
                Na základě analýzy smlouvy a právního kontextu identifikujte všechny problémy
                se souladem a právní nepřesnosti v této smlouvě.
                
                Původní smlouva:
                %s
                
                Analýza smlouvy:
                %s
                
                Právní kontext:
                %s
                
                Uveďte prosím všechny problémy a naformátujte je jako strukturovanou odpověď.
                """, originalContract, contractAnalysis, legalContext);

            Issues issues = chatClient.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(Issues.class);

            context.put("compliance_issues", issues);

            assert issues != null;
            return new StepResult(name, input, issues.toString(),
                    "Compliance check completed successfully", true);

        } catch (Exception e) {
            return new StepResult(name, input, "",
                    "Error during compliance check: " + e.getMessage(), false);
        }
    }
}
