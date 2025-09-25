package com.example.droolsapp.controller;

import com.example.droolsapp.model.RuleResponseContainer;
import com.example.droolsapp.service.RuleEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
public class ValidateController {

    private final RuleEngineService ruleEngineService;

    public ValidateController(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, Object> body) {
        RuleResponseContainer res = ruleEngineService.runRules(body);
        Map<String, Object> out = new HashMap<>();
        out.put("violations", res.getViolations());
        return ResponseEntity.ok(out);
    }
}