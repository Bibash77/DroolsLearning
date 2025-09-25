package com.example.droolsapp.model;

public class CatalogElement {
    private String code;
    private Integer oid;
    private Integer pid;
    private Object value;

    public CatalogElement() {}

    public CatalogElement(String code, Integer oid, Integer pid, Object value) {
        this.code = code;
        this.oid = oid;
        this.pid = pid;
        this.value = value;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getOid() { return oid; }
    public void setOid(Integer oid) { this.oid = oid; }

    public Integer getPid() { return pid; }
    public void setPid(Integer pid) { this.pid = pid; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}