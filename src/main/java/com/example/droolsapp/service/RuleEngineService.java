package com.example.droolsapp.service;

import com.example.droolsapp.model.CatalogElement;
import com.example.droolsapp.model.RuleResponseContainer;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RuleEngineService {

    private final KieContainer kieContainer;

    public RuleEngineService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RuleResponseContainer runRules(Map<String, Object> requestBody) {
        KieSession ksession = null;
        try {
            try {
                // Prefer the explicitly named KieSession defined in META-INF/kmodule.xml
                ksession = kieContainer.newKieSession("rulesSession");
            } catch (RuntimeException ex) {
                // Fallback to the default session if the named session is not available
                ksession = kieContainer.newKieSession();
            }
            if (ksession == null) {
                throw new IllegalStateException("Failed to create KieSession (named or default)");
            }

            RuleResponseContainer container = new RuleResponseContainer();
            ksession.insert(container);

            Object dataMapObj = requestBody.get("dataMap");
            if (dataMapObj instanceof Map) {
                Map<String, Object> dataMap = (Map) dataMapObj;

                for (Map.Entry<String, Object> e : dataMap.entrySet()) {
                    Object listObj = e.getValue();
                    if (listObj instanceof List) {
                        for (Object item : (List) listObj) {
                            if (item instanceof Map) {
                                Map m = (Map) item;
                                String code = (String) m.get("code");
                                Integer oid = null;
                                Integer pid = null;
                                Object oidObj = m.get("oid");
                                Object pidObj = m.get("pid");
                                try { if (oidObj != null) oid = Integer.valueOf(String.valueOf(oidObj)); } catch(Exception ex) {}
                                try { if (pidObj != null) pid = Integer.valueOf(String.valueOf(pidObj)); } catch(Exception ex) {}
                                Object value = m.get("value");

                                CatalogElement ce = new CatalogElement(code, oid, pid, value);
                                ksession.insert(ce);
                            }
                        }
                    }
                }
            }

            ksession.fireAllRules();
            return container;
        } finally {
            if (ksession != null) {
                ksession.dispose();
            }
        }
    }
}