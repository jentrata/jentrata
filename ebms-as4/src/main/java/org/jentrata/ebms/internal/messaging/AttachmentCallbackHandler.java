package org.jentrata.ebms.internal.messaging;

import org.apache.wss4j.common.ext.Attachment;
import org.apache.wss4j.common.ext.AttachmentRequestCallback;
import org.apache.wss4j.common.ext.AttachmentResultCallback;

import javax.activation.DataHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to provide message attachments for signing verification
 *
 * @author aaronwalker
 */
public class AttachmentCallbackHandler implements CallbackHandler {

    Map<String,List<Attachment>> attachments;
    List<Attachment> attachmentList;
    List<Attachment> attachmentResults;
    boolean callbacked = false;

    public AttachmentCallbackHandler(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
        attachments = new LinkedHashMap<>();
        for(Attachment a :attachmentList) {
            attachments.put(a.getId(), Arrays.asList(a));
        }
        this.attachmentResults = new ArrayList<>();
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(Callback callback : callbacks) {
            if(callback instanceof AttachmentRequestCallback) {
                AttachmentRequestCallback requestCallback = (AttachmentRequestCallback) callback;
                if(requestCallback.getAttachmentId() != null) {
                    requestCallback.setAttachments(attachments.get(requestCallback.getAttachmentId()));
                } else {
                    //not sure why we aren't getting an attachmentId  so return all the attachments
                    requestCallback.setAttachments(attachmentList);
                }
            } else {
                AttachmentResultCallback attachmentResultCallback = (AttachmentResultCallback) callback;
                attachmentResults.add(attachmentResultCallback.getAttachment());
                callbacked = true;
            }
        }
    }

    public Map<String, DataHandler> getVerifiedAttachments() {
        Map<String,DataHandler> verifiedAttachments = new HashMap<>();
        for(Attachment attachment : attachmentResults) {
            verifiedAttachments.put(attachment.getId(),new DataHandler(new AttachmentDataSource(attachment)));
        }
        return verifiedAttachments;
    }

    public boolean hasCallback() {
        return callbacked;
    }
}
