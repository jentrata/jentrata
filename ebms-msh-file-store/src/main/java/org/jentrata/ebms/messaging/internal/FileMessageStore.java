package org.jentrata.ebms.messaging.internal;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.messaging.MessageStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple filesystem based message store
 *
 * @author aaronwalker
 */
public class FileMessageStore implements MessageStore {

    private String baseDir;
    private String fileNameExpression = "${date:now:yyyyMMdd-HHmmssSSS}-${headers.JentrataEBMSVersion}.msg";

    @Override
    public void store(@Body InputStream input, Exchange exchange) {
        FileOutputStream fos = null;
        try {
            String fileName = ExpressionBuilder.simpleExpression(fileNameExpression).evaluate(exchange,String.class);
            File outputFile = new File(baseDir,fileName);
            fos = new FileOutputStream(outputFile);
            IOUtils.copyLarge(input,fos);
            exchange.getIn().setHeader(MessageStore.MESSAGE_STORE_REF,outputFile.getAbsolutePath());
            exchange.getIn().setHeader(MessageStore.JENTRATA_MESSAGE_ID,exchange.getIn().getMessageId());
        } catch (IOException e) {
            //throw this so it propergates back to the sender because if we can't persist message we shouldn't accept them
            throw new RuntimeException("currently unable to persist messages in message store " + e,e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    @Override
    public InputStream findByMessageRefId(Object messageRef) {
        try {
            return new FileInputStream(new File((String) messageRef));
        } catch (FileNotFoundException e) {
            return null;
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
