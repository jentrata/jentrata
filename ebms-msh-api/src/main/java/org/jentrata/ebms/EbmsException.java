package org.jentrata.ebms;

/**
 *
 */
public class EbmsException extends RuntimeException {

    private EbmsError ebmsError;

    public EbmsException() {
        this(EbmsError.EBMS_0004);
    }

    public EbmsException(EbmsError ebmsError) {
        this(ebmsError,null,null);
    }

    public EbmsException(EbmsError ebmsError,Throwable cause) {
        this(ebmsError,ebmsError.getShortDescription(),cause);
    }

    public EbmsException(EbmsError ebmsError,String msg) {
        super(msg);
        this.ebmsError = ebmsError;
    }

    public EbmsException(EbmsError ebmsError,String msg,Throwable cause) {
        super(msg,cause);
        this.ebmsError = ebmsError;
    }

    public EbmsError getEbmsError() {
        return ebmsError;
    }
}
