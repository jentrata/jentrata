package org.jentrata.ebms.messaging;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ValidationException;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates the Camel Message body against a schema(s)
 *
 * @author aaronwalker
 */
public class XmlSchemaValidator implements Processor {

    public static final String SCHEMA_VALID = "CamelIsSchemaValid";
    public static final String SCHEMA_ERRORS = "CamelSchemaErrors";

    private static final Logger LOG = LoggerFactory.getLogger(XmlSchemaValidator.class);

    private SchemaFactory schemaFactory;
    private Schema schema;

    public XmlSchemaValidator() {
        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    public XmlSchemaValidator(File schemaFile) throws SAXException {
        this();
        schema = schemaFactory.newSchema(schemaFile);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            Validator validator = schema.newValidator();
            StreamSource source = new StreamSource(exchange.getIn().getBody(InputStream.class));
            final List<String> errors = new ArrayList<>();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    LOG.debug("Validation warning:" + exception);
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    LOG.warn("Validation error:" + exception);
                    errors.add(exception.getMessage());
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    LOG.warn("Validation error:" + exception);
                    errors.add(exception.getMessage());
                }
            });
            validator.validate(source);
            if(errors.isEmpty()) {
                exchange.getIn().setHeader(SCHEMA_VALID,true);
            } else {
                exchange.getIn().setHeader(SCHEMA_VALID,false);
                StringBuilder error = new StringBuilder();
                for(String err : errors) {
                    error.append(err + "\n");
                }
                exchange.getIn().setHeader(SCHEMA_ERRORS,error.toString());
                throw new ValidationException("Failed Schema Validation:" + error.toString(),exchange,null);
            }

        } catch (SAXParseException ex) {
            throw new ValidationException("Failed Schema Validation:" + ex.getMessage(), exchange, ex);
        }
    }

    public void setSchemaFile(File schemaFile) throws SAXException {
        schema = schemaFactory.newSchema(schemaFile);
    }
}
