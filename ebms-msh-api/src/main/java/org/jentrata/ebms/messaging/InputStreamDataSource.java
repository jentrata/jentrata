package org.jentrata.ebms.messaging;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of javax.activation.DataSource that wraps an InputStream with a specified content-type
 *
 * @author aaronwalker
 */
public class InputStreamDataSource implements DataSource {

    private InputStream inputStream;
    private String contentType = "*/*";

    public InputStreamDataSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStreamDataSource(InputStream inputStream, String contentType) {
        this.inputStream = inputStream;
        this.contentType = contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return "InputStreamDataSource";
    }
}
