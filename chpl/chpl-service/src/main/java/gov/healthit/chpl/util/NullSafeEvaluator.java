package gov.healthit.chpl.util;

import java.util.function.Supplier;

public final class NullSafeEvaluator {
    private NullSafeEvaluator() {
        //Not used
    }

    public static <R> R eval(Supplier<R> chainSupplier, R defaultValue) {
        try {
            R returnValue = chainSupplier.get();
            return returnValue != null ? returnValue : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
