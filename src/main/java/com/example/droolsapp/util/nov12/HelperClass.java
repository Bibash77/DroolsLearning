package com.example.droolsapp.util.nov12;

import java.util.*;
import java.util.stream.Collectors;

public class HelperClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelperClass.class);

    // todo: call this method from drools file, passing only model.
    public static List<RuleResponse> validateAllUniqueSteps(PlanDataModel model) {
        // Define which step codes need uniqueness validation
        List<String> stepCodes = Arrays.asList(
                "DBACCCFSASSTREADSTP",  // Read Step
                "DBACCCFSASSTXSTEP"     // X Step (additional type)
        );

        // Top-level parent for all these validations is Account (DBACACA)
        String topParentCode = "DBACACA";

        return validateUniqueStepValuesByTopParent(model, stepCodes, topParentCode);
    }

    public static List<RuleResponse> validateUniqueStepValuesByTopParent(
            PlanDataModel model,
            List<String> stepCodes,
            String topParentCode
    ) {
        long startTime = System.currentTimeMillis();
        List<RuleResponse> violations = new ArrayList<>();

        // Flatten all elements for traversal
        List<CatalogElement> allElements = model.getDataMap().values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Set<String> seen = new HashSet<>();

        for (String stepCode : stepCodes) {
            List<CatalogElement> elements = model.getDataMap().get(stepCode);
            if (elements == null || elements.isEmpty()) {
                LOGGER.debug("No elements found for code {}", stepCode);
                continue;
            }

            for (CatalogElement elem : elements) {
                String value = Objects.toString(elem.getValue(), "").trim();
                if (value.isEmpty()) continue;

                Long topParentOid = findTopParentOid(elem, allElements, topParentCode);

                if (topParentOid != null) {
                    String comboKey = topParentOid + "|" + value;
                    if (!seen.add(comboKey)) {
                        violations.add(new RuleResponse(
                                elem.getCode(),
                                elem.getOid(),
                                elem.getPid(),
                                String.format("Duplicate value '%s' found under same %s", value, topParentCode)
                        ));
                    }
                } else {
                    LOGGER.warn("No top parent ({}) found for element OID {}", topParentCode, elem.getOid());
                }
            }
        }

        LOGGER.info("Time taken for {} uniqueness validation: {} ms", stepCodes, (System.currentTimeMillis() - startTime));
        return violations;
    }

    private static Long findTopParentOid(
            CatalogElement element,
            List<CatalogElement> allElements,
            String topParentCode
    ) {
        CatalogElement current = element;
        int safetyCounter = 0;

        while (current != null && safetyCounter++ < 200) {
            if (topParentCode.equals(current.getCode())) {
                return current.getOid();
            }

            CatalogElement parent = allElements.stream()
                    .filter(e -> e.getOid() != null && e.getCode() != null)
                    .filter(e -> e.getOid().equals(current.getPid())
                            && e.getCode().equals(current.getParentCode()))
                    .findFirst()
                    .orElse(null);

            current = parent;
        }

        return null; // top parent not found
    }
}
