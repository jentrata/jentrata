package org.jentrata.ebms.cpa.pmode;

/**
 * Payload Service PModes
 *
 * @author aaronwalker
 */
public class PayloadService {

    public static final PayloadService DEFAULT_PAYLOAD_SERVICE = new PayloadService();

    public enum CompressionType {
        NONE(""),
        GZIP("application/gzip");

        private String type;

        CompressionType(String type)  {
            this.type=type;
        }

        public String getType() {
            return type;
        }
    }

    private CompressionType compressionType = CompressionType.NONE;
    private String payloadId;

    public CompressionType getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(CompressionType compressionType) {
        this.compressionType = compressionType;
    }

    public String getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(String payloadId) {
        this.payloadId = payloadId;
    }
}
