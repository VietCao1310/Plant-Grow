package service;

import model.PotSession;

/**
 * The labelling rule engine. It reads one session and returns a label code.
 * Thresholds live in one place so a staff member can tune them later without
 * touching any other class. The order of the checks matters: the narrower cases
 * are tested first and the normal label is the fallback.
 */
public class PotRuleEngine {

    public String classify(PotSession o) {
        if (o.getWaterMl() >= 130
                && o.getRetainRatio() <= 0.42
                && o.getMoistAfter() >= 780) {
            return "OVERWATER";
        }

        if ((o.getRetainRatio() >= 0.6 && o.getRetainRatio() <= 0.9)
                && o.getMoistAfter() <= 430
                && (o.getMoistBefore() >= 270 && o.getMoistBefore() <= 410)) {
            return "NO_UPTAKE";
        }

        if (o.getMoistBefore() <= 200
                && o.getWeightBefore() <= 370
                && o.getDryRate() >= 6) {
            return "DRY_STRESS";
        }

        if (o.getDryRate() >= 14
                && o.getRetainRatio() <= 0.8) {
            return "LEAK";
        }

        return "OK";
    }

    /**
     * Severity shown on the dashboard for a given label.
     */
    /** Severity shown on the dashboard. WARN = yellow, CRITICAL = red (as the subject says). */
    public String severityOf(String label) {
        if ("OVERWATER".equals(label)) return "WARN";
        if ("NO_UPTAKE".equals(label)) return "CRITICAL";
        if ("DRY_STRESS".equals(label)) return "WARN";
        if ("LEAK".equals(label)) return "CRITICAL";
        return "INFO";
    }

    /**
     * Text stored on the alert row. Kept in English like the rest of the code.
     */
    public String messageOf(String label) {
        return "Rule engine classified this session as " + label;
    }
}
