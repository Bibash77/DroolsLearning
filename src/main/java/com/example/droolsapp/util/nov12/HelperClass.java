package com.example.droolsapp.util.nov12;

/**
 * @author Bibash Bogati
 * @created 2025-11-12
 */
public class HelperClass {


    // todo sudarshan  :: call this method from drools file .. pas the model .
    //  . also replace x step with another step
    /**
     * Wrapper method to validate all unique step types (e.g., ReadStep, XStep) under same top-level parent (Account).
     * This is the only method you’ll need to call from Drools.
     */
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




    /**
     * Validates that the given step codes have unique values under the same top parent (like Account).
     */
    public static List<RuleResponse> validateUniqueStepValuesByTopParent(
            PlanDataModel model,
            List<String> stepCodes,
            String topParentCode
    ) {
        long startTime = System.currentTimeMillis();
        List<RuleResponse> violations = new ArrayList<>();

        // Flatten all elements by OID for parent traversal
        Map<Long, CatalogElement> elementByOid = new HashMap<>();
        model.getDataMap().values().forEach(list -> list.forEach(e -> elementByOid.put(e.getOid(), e)));

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

                Long topParentOid = findTopParentOid(elem, elementByOid, topParentCode);

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

    /**
     * Traverses parent chain upward until reaching the specified top parent code.
     */
    private static Long findTopParentOid(CatalogElement element, Map<Long, CatalogElement> elementByOid, String topParentCode) {
        CatalogElement current = elementByOid.get(element.getPid());
        int safetyCounter = 0;

        while (current != null && safetyCounter++ < 100) {
            if (topParentCode.equals(current.getCode())) {
                return current.getOid();
            }
            current = elementByOid.get(current.getPid());
        }

        return null;
    }

}
