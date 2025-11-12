package com.example.droolsapp.util;

package.name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility class for validating database-related business rules.
 */
public final class DbRulesHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbRulesHelper.class);

    // Step Add Codes
    private static final String SERVICE_STEP_ADD_CODE = "DBSRVADDSTPADD";
    private static final String ACCOUNT_STEP_ADD_CODE = "DBACCCFSAS";
    private static final String EARNING_STEP_ADD_CODE = "DBESAddStep";
    private static final String ACCRUAL_FORMULA_STEP_ADD_CODE = "DBCRAVGACCCFGSTEP";

    // Step Order Codes
    private static final String SERVICE_STEP_ORDER_CODE = "DBSRVADDSTPORD";
    private static final String ACCOUNT_STEP_ORDER_CODE = "DBACCCFSASORD";
    private static final String EARNING_STEP_ORDER_CODE = "DBESAddStepOrder";
    private static final String ACCRUAL_STEP_ORDER_CODE = "DBCRAVGACCCFGSTEPNUM";

    // Classification Codes
    private static final String CLASSIFICATION_VALUE_CODE = "DBCLSVAL";
    private static final String CLASSIFICATION_ID_CODE = "DBCLSID";

    // Validation Messages
    private static final String DUPLICATE_STEP_ORDER_MESSAGE = "Step order must be unique";
    private static final String DUPLICATE_CLASSIFICATION_COMBINATION_MESSAGE =
            "Combination of Classification ID and Classification Value must be unique";

    private DbRulesHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Validates step order uniqueness for the input {@link PlanDataModel}.
     *
     * @param model    PlanDataModel to validate
     * @param ruleCode Type of rule to validate (e.g., service, account, earning, accrual)
     * @return List of {@link RuleResponse} containing validation results
     * @throws IllegalArgumentException if model is null
     */
    public static List<RuleResponse> validateStepOrderRule(PlanDataModel model, String ruleCode) {

        long startTime = System.currentTimeMillis();
        List<RuleResponse> validationResults = new ArrayList<>();
        Map<CatalogElement, Long> childToParentMap = new HashMap<>();
        Map<String, Boolean> orderTracker = new HashMap<>();

        Map<String, List<CatalogElement>> dataMap = model.getDataMap();
        List<CatalogElement> steps;
        String childCode;

        switch (ruleCode) {
            case "service":
                steps = dataMap.get(SERVICE_STEP_ADD_CODE);
                childCode = SERVICE_STEP_ORDER_CODE;
                break;
            case "account":
                steps = dataMap.get(ACCOUNT_STEP_ADD_CODE);
                childCode = ACCOUNT_STEP_ORDER_CODE;
                break;
            case "earning":
                steps = dataMap.get(EARNING_STEP_ADD_CODE);
                childCode = EARNING_STEP_ORDER_CODE;
                break;
            case "accrual":
                steps = dataMap.get(ACCRUAL_FORMULA_STEP_ADD_CODE);
                childCode = ACCRUAL_STEP_ORDER_CODE;
                break;
            default:
                LOGGER.info("Rule code '{}' not recognized.", ruleCode);
                steps = null;
                childCode = null;
                break;
        }

        if (steps != null && !steps.isEmpty() && childCode != null) {
            for (CatalogElement parentStep : steps) {
                linkChildToParent(model, childCode, parentStep, childToParentMap);
            }

            childToParentMap.forEach((child, parentId) -> {
                String orderKey = parentId + "P" + Objects.toString(child.getValue(), "");
                if (orderTracker.containsKey(orderKey)) {
                    validationResults.add(new RuleResponse(
                            child.getCode(),
                            child.getOid(),
                            child.getPid(),
                            DUPLICATE_STEP_ORDER_MESSAGE
                    ));
                } else {
                    orderTracker.put(orderKey, true);
                }
            });
        }

        LOGGER.info("Time taken for {} Step Order Rule: {}ms", ruleCode, (System.currentTimeMillis() - startTime));
        return validationResults;
    }

    /**
     * Links child elements to their parent element in the provided result map.
     *
     * @param model              PlanDataModel containing the data
     * @param childCode          Code identifying child elements
     * @param parent             Parent CatalogElement
     * @param childToParentResult Map to store child-parent relationships
     */
    private static void linkChildToParent(PlanDataModel model, String childCode, CatalogElement parent,
                                          Map<CatalogElement, Long> childToParentResult) {

        List<CatalogElement> children = model.getDataMap().get(childCode);
        if (children != null) {
            children.stream()
                    .filter(node -> Objects.equals(node.getPid(), parent.getOid()))
                    .findFirst()
                    .ifPresent(element -> childToParentResult.put(element, parent.getPid()));
        }
    }
}
