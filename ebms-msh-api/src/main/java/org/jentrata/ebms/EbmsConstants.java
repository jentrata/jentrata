package org.jentrata.ebms;

import org.apache.camel.Exchange;

public class EbmsConstants {

    //Jentrata Message Header keys
    public static final String SOAP_VERSION = "JentrataSOAPVersion";
    public static final String EBMS_VERSION = "JentrataEBMSVersion";
    public static final String VALID_PARTNER_AGREEMENT = "JentrataIsValidPartnerAgreement";
    public static final String EBMS_MESSAGE_MEP = "JentrataMEP";

    public static final String SOAP_1_1_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    public static final String SOAP_1_2_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String EBXML_V2_NAMESPACE = "http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd";

    public static final String EBXML_V3_NAMESPACE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";

    public static final String EBMS_V2 = "ebMSV2";
    public static final String EBMS_V3 = "ebMSV3";

    public static final String CONTENT_ID = "Content-ID";
    public static final String CONTENT_TYPE = Exchange.CONTENT_TYPE;
    public static final String CONTENT_LENGTH = Exchange.CONTENT_LENGTH;

    public static final String MESSAGE_ID = "JentrataMessageID";
    public static final String MESSAGE_DIRECTION = "JentrataMessageDirection";
    public static final String MESSAGE_DIRECTION_INBOUND = "inbox";
}