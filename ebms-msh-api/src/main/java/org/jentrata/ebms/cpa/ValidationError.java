package org.jentrata.ebms.cpa;

import org.jentrata.ebms.EbmsError;

/**
 * Holds a ebMS validation error
 */
public class ValidationError {

    public static class Builder {

        private EbmsError error;
        private String description;
        private String refMessageID;

        public Builder error(EbmsError error) {
            this.error = error;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder refMessageID(String refMessageID) {
            this.refMessageID = refMessageID;
            return this;
        }

        public ValidationError create() {
            return new ValidationError(this);
        }

    }

    private EbmsError error;
    private String description;
    private String refMessageID;

    private ValidationError(Builder builder) {
        this.error = builder.error;
        this.refMessageID = builder.refMessageID;
        this.description = builder.description;
    }

    public EbmsError getError() {
        return error;
    }

    public void setError(EbmsError error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRefMessageID() {
        return refMessageID;
    }

    public void setRefMessageID(String refMessageID) {
        this.refMessageID = refMessageID;
    }
}
