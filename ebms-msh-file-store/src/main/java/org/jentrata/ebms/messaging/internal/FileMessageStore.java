package org.jentrata.ebms.messaging.internal;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.messaging.DefaultMessage;
import org.jentrata.ebms.messaging.Message;
import org.jentrata.ebms.messaging.MessageStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * A simple filesystem based message store
 *
 * @author aaronwalker
 */
public class FileMessageStore implements MessageStore {

    private String baseDir;
    private String fileNameExpression = "${headers.JentrataMessageID}-${headers.JentrataMessageType}.msg";

    @Override
    public void store(@Body InputStream input, Exchange exchange) {
        FileOutputStream fos = null;
        try {
            String fileName = ExpressionBuilder.simpleExpression(fileNameExpression).evaluate(exchange,String.class);
            File outputFile = new File(baseDir,fileName);
            fos = new FileOutputStream(outputFile);
            IOUtils.copyLarge(input,fos);
            exchange.getIn().setHeader(MessageStore.MESSAGE_STORE_REF,outputFile.getAbsolutePath());
        } catch (IOException e) {
            //throw this so it propergates back to the sender because if we can't persist message we shouldn't accept them
            throw new RuntimeException("currently unable to persist messages in message store " + e,e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    @Override
    public void storeMessage(Exchange exchange) {
        String messageId = exchange.getIn().getHeader(EbmsConstants.MESSAGE_ID,String.class);
        String messageDirection = exchange.getIn().getHeader(EbmsConstants.MESSAGE_DIRECTION,String.class);
        MessageStatusType status = exchange.getIn().getHeader(EbmsConstants.MESSAGE_STATUS,MessageStatusType.class);
        String statusDesc = exchange.getIn().getHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION,String.class);
        updateMessage(messageId,messageDirection,status,statusDesc);
    }

    @Override
    public Message findByMessageId(final String messageId) {
        return new DefaultMessage(messageId);
    }

    @Override
    public InputStream findPayloadById(String messageId) {
        try {
            return new FileInputStream(getFilename(messageId));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private String getFilename(final String messageId) {
        File repo = new File(baseDir);
        File [] files = repo.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(messageId) && name.endsWith(".msg");
            }
        });
        if(files != null && files.length > 0) {
            return files[0].getAbsolutePath();
        } else {
            return null;
        }
    }

    @Override
    public void updateMessage(final String messageId, String messageDirection, MessageStatusType status, String statusDescription) {
        File update = new File(baseDir,messageId + "." + status);
        try (FileOutputStream outputStream = new FileOutputStream(update)) {
            IOUtils.write(statusDescription,outputStream);
        } catch (IOException e) {
            throw new RuntimeException("unable to update message " + messageId + " with status " + status,e);
        }
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
        File f = new File(baseDir);
        if(!f.exists()) {
            f.mkdirs();
        }
    }

    public String getFileNameExpression() {
        return fileNameExpression;
    }

    public void setFileNameExpression(String fileNameExpression) {
        this.fileNameExpression = fileNameExpression;
    }

    @Override
    public String toString() {
        return "FileMessageStore{" +
                "baseDir='" + baseDir + '\'' +
                ", fileNameExpression='" + fileNameExpression + '\'' +
                '}';
    }
}
