package org.jentrata.ebms.cpa.pmode;

import java.util.HashMap;
import java.util.Map;

/**
 * ReceptionAwareness PMode
 *
 * @author aaronwalker
 */
public class ReceptionAwareness {

    public static final ReceptionAwareness DEFAULT = new ReceptionAwareness();

    private static final Map<String,Object> DEFAULT_RETRY = new HashMap<>();
    private static final Map<String,Object> DEFAULT_DUP_DETECTION = new HashMap<>();

    static {
        DEFAULT_RETRY.put("enabled",false);
        DEFAULT_DUP_DETECTION.put("enabled",false);
    }

    private Map<String,Object> retry = DEFAULT_RETRY;
    private Map<String,Object> duplicateDetection = DEFAULT_DUP_DETECTION;

    public boolean isRetryEnabled() {
        return retry != null && (boolean)retry.get("enabled");
    }

    public boolean isDuplicateDetectionEnabled() {
        return duplicateDetection != null && (boolean)duplicateDetection.get("enabled");
    }

    public Map<String, Object> getRetry() {
        return retry;
    }

    public void setRetry(Map<String, Object> retry) {
        this.retry = retry;
    }

    public Map<String, Object> getDuplicateDetection() {
        return duplicateDetection;
    }

    public void setDuplicateDetection(Map<String, Object> duplicateDetection) {
        this.duplicateDetection = duplicateDetection;
    }
}
