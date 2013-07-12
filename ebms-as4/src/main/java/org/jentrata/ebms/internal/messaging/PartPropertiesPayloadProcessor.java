package org.jentrata.ebms.internal.messaging;

import org.apache.camel.Exchange;
import org.apache.camel.builder.xml.XPathBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.soap.SoapPayloadProcessor;
import org.jentrata.ebms.utils.EbmsUtils;
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

    private String partPropertyXpath = "//*[local-name()='PartInfo' and @href[.='cid:%s']]";
    private String soapPartPropertyXpath =  "//*[local-name()='PartInfo' and (not(@href) or string-length(@href)=0)]";

    @Override
    public void process(String soapMessage, String payloadId, Exchange exchange) {
        try {
            Node partInfo;
            if(payloadId.equals(EbmsConstants.SOAP_BODY_PAYLOAD_ID)) {
                partInfo = EbmsUtils.ebmsXpathNode(soapMessage,String.format(soapPartPropertyXpath,payloadId));
            }
            else {
                partInfo = EbmsUtils.ebmsXpathNode(soapMessage,String.format(partPropertyXpath,payloadId));
            }
            NodeList partProperties = EbmsUtils.ebmsXpathNodeList(partInfo,"//*[local-name()='Property']");
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

    public String getSoapPartPropertyXpath() {
        return soapPartPropertyXpath;
    }

    public void setSoapPartPropertyXpath(String soapPartPropertyXpath) {
        this.soapPartPropertyXpath = soapPartPropertyXpath;
    }
}
