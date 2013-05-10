package org.jentrata.ebms.internal.messaging;

import org.apache.camel.Exchange;
import org.apache.camel.builder.xml.XPathBuilder;
import org.jentrata.ebms.soap.SoapPayloadProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Adds the PartProperties for the matching PartInfo from the ebms User Message
 * as message headers on the payload message
 *
 * @author aaronwalker
 */
public class PartPropertiesPayloadProcessor implements SoapPayloadProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(PartPropertiesPayloadProcessor.class);

    private String partPropertyXpath = "//*[local-name()='Property' and @name[.='PartID' and ../text()='%s']]/..";

    @Override
    public void process(String soapMessage, String payloadId, Exchange exchange) {
        //TODO: need to fix this to better handle the situation where partPropertyXpath doesn't find a match as the default one throws an exception
        try {
            Node node = XPathBuilder.xpath(String.format(partPropertyXpath,payloadId)).evaluate(exchange.getContext(), soapMessage, Node.class);
            NodeList partProperties = XPathBuilder.xpath("//*[local-name()='Property']").evaluate(exchange.getContext(), node, NodeList.class);
            for (int i = 0; i < partProperties.getLength(); i++) {
                Node property = partProperties.item(i);
                String name = property.getAttributes().getNamedItem("name").getTextContent();
                String value = property.getTextContent();
                exchange.getIn().setHeader(name, value);
            }
        } catch (Exception ex) {
            LOG.warn("unable to extract PartProperties for payload " + payloadId + ":" + ex);
            LOG.debug("",ex);
        }
    }

    public String getPartPropertyXpath() {
        return partPropertyXpath;
    }

    public void setPartPropertyXpath(String partPropertyXpath) {
        this.partPropertyXpath = partPropertyXpath;
    }
}
