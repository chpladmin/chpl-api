package gov.healthit.chpl.util;

import java.util.function.Supplier;

public final class NullSafeEvaluator {
    private NullSafeEvaluator() {
        //Not used
    }

    public static <R> R eval(Supplier<R> chainSupplier, R defaultValue) {
        try {
            return chainSupplier.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
