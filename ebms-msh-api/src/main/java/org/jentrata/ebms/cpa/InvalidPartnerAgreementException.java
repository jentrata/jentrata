package org.jentrata.ebms.cpa;

import org.jentrata.ebms.EbmsError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An invalid Partner Agreement
 *
 * @author aaronwalker
 */
public class InvalidPartnerAgreementException extends Exception {

    List<ValidationError> validationErrors;

    public InvalidPartnerAgreementException(String message) {
        super(message);
        validationErrors = Arrays.asList(new ValidationError.Builder()
                .error(EbmsError.EBMS_0001)
                .description(message)
                .refMessageID(null)
                .create()
        );
    }

    public InvalidPartnerAgreementException(String refMessageID, String message, Throwable cause) {
        super(message, cause);
        validationErrors = Arrays.asList(new ValidationError.Builder()
                .error(EbmsError.EBMS_0001)
                .description(message)
                .refMessageID(refMessageID)
                .create()
        );
    }

    public InvalidPartnerAgreementException(Throwable cause) {
        super(cause);
    }

    public InvalidPartnerAgreementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvalidPartnerAgreementException(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
