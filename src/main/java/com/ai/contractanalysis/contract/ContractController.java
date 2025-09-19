package com.ai.contractanalysis.contract;

import com.ai.contractanalysis.issue.Issues;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {
    private final ContractAnalysisService contractAnalysisService;

    public ContractController(ContractAnalysisService contractAnalysisService) {
        this.contractAnalysisService = contractAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Issues> analyzeContract(@RequestParam("file") MultipartFile file) {
        Issues issues = contractAnalysisService.analyze(file);
        return ResponseEntity.ok(issues);
    }

}