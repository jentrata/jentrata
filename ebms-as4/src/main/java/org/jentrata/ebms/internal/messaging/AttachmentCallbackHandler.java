package org.jentrata.ebms.internal.messaging;

import org.apache.commons.io.IOUtils;
import org.apache.wss4j.common.ext.Attachment;
import org.apache.wss4j.common.ext.AttachmentRequestCallback;
import org.apache.wss4j.common.ext.AttachmentResultCallback;
import org.jentrata.ebms.messaging.InputStreamDataSource;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to provide message attachments for signing verification
 *
 * @author aaronwalker
 */
public class AttachmentCallbackHandler implements CallbackHandler {

    List<Attachment> attachments;
    List<Attachment> attachmentResults;

    public AttachmentCallbackHandler(List<Attachment> attachments) {
        this.attachments = attachments;
        this.attachmentResults = new ArrayList<>();

    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(Callback callback : callbacks) {
            if(callback instanceof AttachmentRequestCallback) {
                AttachmentRequestCallback requestCallback = (AttachmentRequestCallback) callback;
                requestCallback.setAttachments(attachments);
            } else {
                AttachmentResultCallback attachmentResultCallback = (AttachmentResultCallback) callback;
                attachmentResults.add(attachmentResultCallback.getAttachment());
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
}
