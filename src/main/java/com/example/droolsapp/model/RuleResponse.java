package com.example.droolsapp.model;

public class RuleResponse {
    private String code;
    private Integer oid;
    private Integer pid;
    private String ruleName;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getOid() { return oid; }
    public void setOid(Integer oid) { this.oid = oid; }

    public Integer getPid() { return pid; }
    public void setPid(Integer pid) { this.pid = pid; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
}