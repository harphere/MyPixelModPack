package dev.chet.mypixelmodpack;

/** One failed component entry must not prevent later components from starting. */
final class FeatureInstallation {
    interface Action { void run() throws Throwable; }
    interface Reporter { void report(String feature, String status, Throwable error); }
    static void run(String feature, boolean enabled, Action action, Reporter reporter) {
        if (!enabled) { reporter.report(feature, "OFF", null); return; }
        try {
            action.run();
            reporter.report(feature, "ON; entry point returned (see component logs for hook availability)", null);
        } catch (Throwable error) {
            reporter.report(feature, "INSTALL FAILED: " + error, error);
        }
    }
    private FeatureInstallation() { }
}
