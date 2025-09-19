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
    public StepResult execute(String input, Map<ContextKey, Object> context, ChatClient.Builder chatClient) {
        try {
            String contractAnalysis = (String) context.get(ContextKey.CONTRACT_ANALYSIS);
            String legalContext = (String) context.get(ContextKey.LEGAL_CONTEXT);
            String originalContract = (String) context.get(ContextKey.ORIGINAL_CONTRACT);

            String systemPrompt = """
                Jste profesionální právník specializovaný na kontrolu souladu smluv.
                Máte k dispozici výhradně následující právní kontext, který pochází z databáze (JSON soubory).
    
                Vaším úkolem je:
                - Identifikovat problémy pouze na základě POSKYTNUTÉHO právního kontextu.
                - Pokud problém není pokryt žádným ustanovením z tohoto kontextu, ignorujte jej.
                - Nikdy nepřidávejte doporučení ani chyby, které vyplývají z jiných zákonů, judikatury
                nebo obecných obchodních praktik, pokud nejsou výslovně uvedeny v právním kontextu.
    
                Pro každý nalezený problém uveďte:
                - Typ problému (např. "Chybějící doložka podle § 553")
                - Úroveň závažnosti (Kritická, Vysoká, Střední, Nízká)
                - Podrobný popis problému
                - Konkrétní doložku nebo část, které se problém týká
                - Doporučení k vyřešení
                - Přibližné číslo řádku, pokud lze určit
    
                Vraťte svá zjištění ve strukturovaném JSON formátu.
                """;


            String userPrompt = String.format("""
                Na základě analýzy smlouvy a POSKYTNUTÉHO právního kontextu
                identifikujte všechny problémy se souladem a právní nepřesnosti.

                Původní smlouva:
                %s

                Analýza smlouvy:
                %s

                Právní kontext (výhradně z JSON):
                %s

                Pokud smlouva splňuje všechny uvedené požadavky, vraťte prázdný seznam issues.
                """, originalContract, contractAnalysis, legalContext);


            Issues issues = chatClient.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(Issues.class);

            context.put(ContextKey.COMPLIANCE_ISSUES, issues);

            assert issues != null;
            return new StepResult(name, input, issues.toString(),
                    "Compliance check completed successfully", true);

        } catch (Exception e) {
            return new StepResult(name, input, "",
                    "Error during compliance check: " + e.getMessage(), false);
        }
    }
}
