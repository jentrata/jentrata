package org.jentrata.ebms.internal.messaging;

import org.apache.wss4j.common.ext.Attachment;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A DataSource wrapper around an Attachment
 *
 * @author aaronwalker
 */
public class AttachmentDataSource implements DataSource {

    private Attachment attachment;
    private boolean verified;

    public AttachmentDataSource(Attachment attachment) {
        this(attachment,false);
    }

    public AttachmentDataSource(Attachment attachment, boolean verified) {
        this.attachment = attachment;
        this.verified = verified;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return attachment.getSourceStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return attachment.getMimeType();
    }

    @Override
    public String getName() {
        return attachment.getId();
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
