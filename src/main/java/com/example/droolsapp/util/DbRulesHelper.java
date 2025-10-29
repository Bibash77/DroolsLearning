package com.example.droolsapp.util;

import com.example.droolsapp.model.RuleResponse;
import com.fmr.prk.rules.domain.entity.RuleResponse;
import com.fmr.prk.rules.inbound.rest.view.v1.CatalogElement;
import com.fmr.prk.rules.inbound.rest.view.v1.PlanDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility class providing helper methods for database rules validation.
 * This class contains static methods for validating service step orders,
 * classification uniqueness, and managing catalog elements.
 */
public final class DbRulesHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbRulesHelper.class);

    private static final String SERVICE_STEP_ADD_CODE = "DBSRVADDSTPADD";
    private static final String ACCOUNT_STEP_ADD_CODE = "DBACCCFSAS";
    private static final String EARNING_STEP_ADD_CODE = "DBESAddStep";
    private static final String ACCURAL_FORMULA_STEP_ADD_CODE = "DBCRAVGACCCFGSTEP";
    private static final String CONDITION_FORMULA_STEP_ADD_CODE = "DBCCCCCCSC";
    private static final String BENEFIT_FORMULA_STEP_ADD_CODE = "DBFFCFFSC";

    private static final String SERVICE_STEP_ORDER_CODE = "DBSRVADDSTPORD";
    private static final String ACCOUNT_STEP_ORDER_CODE = "DBACCCFSASORD";
    private static final String EARNING_STEP_ORDER_CODE = "DBESAddStepOrder";
    private static final String ACCURAL_STEP_ORDER_CODE = "DBCRAVGACCCFGSTEPNUM";
    private static final String CONDITION_STEP_ORDER_CODE = "DBCCCCCCSCSON";
    private static final String BENEFIT_FORMULA_STEP_ORDER_CODE = "DBFFCFFSOOS";

    private static final String CLASSIFICATION_VALUE_CODE = "DBCLSVAL";
    private static final String CLASSIFICATION_ID_CODE = "DBCLSID";

    private static final String DUPLICATE_STEP_ORDER_MESSAGE = "Step order must be unique";
    private static final String DUPLICATE_CLASSIFICATION_COMBINATION_MESSAGE =
            "Combination of Classification ID and Classification Value must be unique";

    private DbRulesHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Validates that step orders under the same parent are unique for the given rule code.
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
                steps = dataMap.get(ACCURAL_FORMULA_STEP_ADD_CODE);
                childCode = ACCURAL_STEP_ORDER_CODE;
                break;
            case "condition":
                steps = dataMap.get(CONDITION_FORMULA_STEP_ADD_CODE);
                childCode = CONDITION_STEP_ORDER_CODE;
                break;
            case "formula":
                steps = dataMap.get(BENEFIT_FORMULA_STEP_ADD_CODE);
                childCode = BENEFIT_FORMULA_STEP_ORDER_CODE;
                break;
            default:
                LOGGER.info("Rule code not found: {}", ruleCode);
                steps = null;
                childCode = null;
                break;
        }

        if (steps != null && !steps.isEmpty() && childCode != null) {
            for (CatalogElement parentStep : steps) {
                linkChildToParent(model, childCode, parentStep, childToParentMap);
            }

            childToParentMap.forEach((child, parentId) -> {
                String orderKey = parentId + Objects.toString(child.getValue(), "");
                if (orderTracker.containsKey(orderKey)) {
                    validationResults.add(new RuleResponse(child.getCode(), child.getOid(), child.getPid(),
                            DUPLICATE_STEP_ORDER_MESSAGE));
                } else {
                    orderTracker.put(orderKey, true);
                }
            });
        }

        LOGGER.info("Time taken for {} Step Order Rule: {} ms", ruleCode, (System.currentTimeMillis() - startTime));
        return validationResults;
    }

    /**
     * Links child elements to their parent element in the provided result map.
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

    /**
     * Validates that the combination of Classification ID and Classification Value is unique.
     */
    public static List<RuleResponse> validateUniqueClassificationCombination(PlanDataModel model) {
        long startTime = System.currentTimeMillis();
        List<RuleResponse> validationResults = new ArrayList<>();
        Map<String, Boolean> combinationTracker = new HashMap<>();

        List<CatalogElement> idElements = model.getDataMap().get(CLASSIFICATION_ID_CODE);
        List<CatalogElement> valueElements = model.getDataMap().get(CLASSIFICATION_VALUE_CODE);

        if (idElements == null || valueElements == null) {
            LOGGER.info("DBCLSID or DBCLSVAL elements not found");
            return validationResults;
        }

        // Map parentId → Classification ID and Value
        Map<Long, String> idMap = new HashMap<>();
        for (CatalogElement idElem : idElements) {
            String value = Objects.toString(idElem.getValue(), "").trim();
            if (!value.isEmpty()) {
                idMap.put(idElem.getPid(), value);
            }
        }

        Map<Long, String> valueMap = new HashMap<>();
        for (CatalogElement valElem : valueElements) {
            String value = Objects.toString(valElem.getValue(), "").trim();
            if (!value.isEmpty()) {
                valueMap.put(valElem.getPid(), value);
            }
        }

        // Combine and check uniqueness
        for (Long parentId : idMap.keySet()) {
            String idVal = idMap.get(parentId);
            String valVal = valueMap.getOrDefault(parentId, "");
            if (idVal.isEmpty() && valVal.isEmpty()) {
                continue;
            }

            String combinationKey = idVal + "|" + valVal;

            if (combinationTracker.containsKey(combinationKey)) {
                validationResults.add(new RuleResponse(
                        "DBCLSDEF",
                        null,
                        parentId,
                        DUPLICATE_CLASSIFICATION_COMBINATION_MESSAGE
                ));
            } else {
                combinationTracker.put(combinationKey, true);
            }
        }

        LOGGER.info("Time taken for Unique Classification Combination Rule: {} ms",
                (System.currentTimeMillis() - startTime));
        return validationResults;
    }
}
