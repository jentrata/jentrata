package org.jentrata.ebms;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes the standard EbmsErrors
 *
 * @author aaronwalker
 */
public class EbmsError {

    public enum ErrorCategory {
        Content,
        Communication,
        Unpackaging,
        Processing
    }

    public enum ErrorSeverity {
        warning,
        failure
    }

    public enum ErrorOrigin {
        ebMS,
        reliability,
        security
    }

    //6.7.1. ebMS Processing Errors
    public static final EbmsError EBMS_0001 = new EbmsError("EBMS:0001","ValueNotRecognized",ErrorCategory.Content,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0002 = new EbmsError("EBMS:0002","FeatureNotSupported",ErrorCategory.Content,ErrorSeverity.warning,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0003 = new EbmsError("EBMS:0003","ValueInconsistent",ErrorCategory.Content,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0004 = new EbmsError("EBMS:0004","Other",ErrorCategory.Content,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0005 = new EbmsError("EBMS:0005","ConnectionFailure",ErrorCategory.Communication,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0006 = new EbmsError("EBMS:0006","EmptyMessagePartitionChannel",ErrorCategory.Communication,ErrorSeverity.warning,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0007 = new EbmsError("EBMS:0007","MimeInconsistency",ErrorCategory.Unpackaging,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0008 = new EbmsError("EBMS:0008","FeatureNotSupported",ErrorCategory.Unpackaging,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0009 = new EbmsError("EBMS:0009","InvalidHeader",ErrorCategory.Unpackaging,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0010 = new EbmsError("EBMS:0010","ProcessingModeMismatch",ErrorCategory.Processing,ErrorSeverity.failure,ErrorOrigin.ebMS);
    public static final EbmsError EBMS_0011 = new EbmsError("EBMS:0011","ExternalPayloadError",ErrorCategory.Content,ErrorSeverity.failure,ErrorOrigin.ebMS);

    //6.7.2. Security Processing Errors
    public static final EbmsError EBMS_0101 = new EbmsError("EBMS:0101","FailedAuthentication",ErrorCategory.Processing,ErrorSeverity.failure,ErrorOrigin.security);
    public static final EbmsError EBMS_0102 = new EbmsError("EBMS:0102","FailedDecryption",ErrorCategory.Processing,ErrorSeverity.failure,ErrorOrigin.security);
    public static final EbmsError EBMS_0103 = new EbmsError("EBMS:0103","PolicyNoncompliance",ErrorCategory.Processing,ErrorSeverity.failure,ErrorOrigin.security);

    //6.7.3. Reliable Messaging Errors
    public static final EbmsError EBMS_0201 = new EbmsError("EBMS:0201","DysfunctionalReliability",ErrorCategory.Processing,ErrorSeverity.failure,ErrorOrigin.reliability);
    public static final EbmsError EBMS_0202 = new EbmsError("EBMS:0202","DeliveryFailure",ErrorCategory.Communication,ErrorSeverity.failure,ErrorOrigin.reliability);

    public static final EbmsError EBMS_0303 = new EbmsError("EBMS:0303","DecompressionFailure,",ErrorCategory.Communication,ErrorSeverity.failure,ErrorOrigin.reliability);

    private static final Map<String,EbmsError> ERROR_CODE_MAP = new HashMap<>();

    static {
        ERROR_CODE_MAP.put(EBMS_0001.getErrorCode(),EBMS_0001);
        ERROR_CODE_MAP.put(EBMS_0002.getErrorCode(),EBMS_0002);
        ERROR_CODE_MAP.put(EBMS_0003.getErrorCode(),EBMS_0003);
        ERROR_CODE_MAP.put(EBMS_0004.getErrorCode(),EBMS_0004);
        ERROR_CODE_MAP.put(EBMS_0005.getErrorCode(),EBMS_0005);
        ERROR_CODE_MAP.put(EBMS_0006.getErrorCode(),EBMS_0006);
        ERROR_CODE_MAP.put(EBMS_0007.getErrorCode(),EBMS_0007);
        ERROR_CODE_MAP.put(EBMS_0008.getErrorCode(),EBMS_0008);
        ERROR_CODE_MAP.put(EBMS_0009.getErrorCode(),EBMS_0009);
        ERROR_CODE_MAP.put(EBMS_0010.getErrorCode(),EBMS_0010);
        ERROR_CODE_MAP.put(EBMS_0011.getErrorCode(),EBMS_0011);

        ERROR_CODE_MAP.put(EBMS_0101.getErrorCode(),EBMS_0101);
        ERROR_CODE_MAP.put(EBMS_0102.getErrorCode(),EBMS_0102);
        ERROR_CODE_MAP.put(EBMS_0103.getErrorCode(),EBMS_0103);

        ERROR_CODE_MAP.put(EBMS_0201.getErrorCode(),EBMS_0201);
        ERROR_CODE_MAP.put(EBMS_0202.getErrorCode(),EBMS_0202);

        ERROR_CODE_MAP.put(EBMS_0303.getErrorCode(),EBMS_0303);
    }

    public static EbmsError getEbmsError(String errorCode) {
        return ERROR_CODE_MAP.get(errorCode);
    }

    private EbmsError(String errorCode, String shortDescription, ErrorCategory category, ErrorSeverity severity, ErrorOrigin origin) {
        this.errorCode = errorCode;
        this.shortDescription = shortDescription;
        this.category = category;
        this.severity = severity;
        this.origin = origin;
    }

    private final String errorCode;
    private final String shortDescription;
    private final ErrorCategory category;
    private final ErrorSeverity severity;
    private final ErrorOrigin origin;

    public String getErrorCode() {
        return errorCode;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public ErrorOrigin getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "EbmsError{" +
                "errorCode='" + errorCode + '\'' +
                ", origin=" + origin +
                ", category=" + category +
                ", severity=" + severity +
                ", shortDescription='" + shortDescription + '\'' +
                '}';
    }
}
