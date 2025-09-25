package com.example.droolsapp.util;

import java.util.List;
import java.util.Map;

public class RuleUtils {

    public static boolean hasActiveStatus(Object statusValue) {
        if (!(statusValue instanceof Map)) return false;
        Object lv = ((Map) statusValue).get("listValues");
        if (!(lv instanceof List)) return false;
        for (Object item : (List) lv) {
            if (item instanceof Map) {
                Object code = ((Map) item).get("lovValueCode");
                if ("FIDB_STATUSA".equals(code)) return true;
            }
        }
        return false;
    }


    public static boolean descriptionPresentAndNotEmpty(Object value) {
        if (value == null) return false;
        if (value instanceof String) {
            return ((String) value).trim().length() > 0;
        }
        return false;
    }
}