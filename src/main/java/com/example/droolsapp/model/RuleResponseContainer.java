package com.example.droolsapp.model;

import java.util.ArrayList;
import java.util.List;

public class RuleResponseContainer {
    private List<RuleResponse> violations = new ArrayList<>();

    public List<RuleResponse> getViolations() { return violations; }

    public void add(RuleResponse r) { violations.add(r); }
}