package org.jentrata.ebms.cpa;

/**
 * An invalid Partner Agreement
 *
 * @author aaronwalker
 */
public class InvalidPartnerAgreementException extends Exception {

    public InvalidPartnerAgreementException() {
    }

    public InvalidPartnerAgreementException(String message) {
        super(message);
    }

    public InvalidPartnerAgreementException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPartnerAgreementException(Throwable cause) {
        super(cause);
    }

    public InvalidPartnerAgreementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
