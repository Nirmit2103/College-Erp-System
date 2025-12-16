package edu.univ.erp.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongConsumer;

public final class GradeUpdateNotifier {

    private static final List<LongConsumer> listeners = new CopyOnWriteArrayList<>();

    private GradeUpdateNotifier() {}

    public static void addListener(LongConsumer listener) {
        listeners.add(listener);
    }

    public static void removeListener(LongConsumer listener) {
        listeners.remove(listener);
    }

    public static void notifyGradeUpdated(long enrollmentId) {
        for (LongConsumer l : listeners) {
            try {
                l.accept(enrollmentId);
            } catch (Exception ignored) {}
        }
    }
}
