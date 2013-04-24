package org.jentrata.ebms.messaging;

import javax.xml.soap.SOAPMessage;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A Reference to a Message in the Message Store
 *
 * @author aaronwalker
 */
public class MessageRef {

    public static class MessageRefBuilder {
        private MessageRef messageRef = new MessageRef();

        public MessageRefBuilder messageId(String messageId) {
            messageRef.setMessageId(messageId);
            return this;
        }

        public MessageRefBuilder messageDate(Date messageDate) {
            messageRef.setMessageDate(messageDate);
            return this;
        }

        public MessageRefBuilder cpdId(String cpaId) {
            messageRef.setCpaId(cpaId);
            return this;
        }

        public MessageRefBuilder from(SOAPMessage soapMessage) {
            return this;
        }

        public MessageRef build() {
            return messageRef;
        }
    }

    private String messageId;
    private String cpaId;
    private String status;
    private Date messageDate;
    private Map<String, Object> attributes;
    private List<MessagePayload> payloads;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCpaId() {
        return cpaId;
    }

    public void setCpaId(String cpaId) {
        this.cpaId = cpaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public List<MessagePayload> getPayloads() {
        return payloads;
    }

    public void setPayloads(List<MessagePayload> payloads) {
        this.payloads = payloads;
    }
}
