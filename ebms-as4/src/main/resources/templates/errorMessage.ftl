<?xml version="1.0" encoding="UTF-8"?>
<S12:Envelope
        xmlns:S12="http://www.w3.org/2003/05/soap-envelope"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:eb="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/"
        xsi:schemaLocation="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/
          http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/core/ebms-header-3_0-200704.xsd">
    <S12:Header>
        <eb:Messaging S12:mustUnderstand="1">
            <eb:SignalMessage>
                <eb:MessageInfo>
                    <eb:Timestamp>${.now?iso("UTC")}</eb:Timestamp>
                    <eb:MessageId>${headers.JentrataMessageID}</eb:MessageId>
                    <#if headers.JentrataRefToMessageInError?has_content><eb:RefToMessageId>${headers.JentrataRefToMessageInError?xml}</eb:RefToMessageId></#if>
                </eb:MessageInfo>
                <eb:Error origin="${headers.JentrataEbmsError.origin}"
                          category="${headers. JentrataEbmsError.category}"
                          errorCode="${headers.JentrataEbmsError.errorCode}"
                          severity="${headers.JentrataEbmsError.severity}"
                          shortDescription="${headers.JentrataEbmsError.shortDescription}"
                          refToMessageInError="${headers.JentrataRefToMessageInError?xml!}">
                    <#if headers.JentrataEbmsErrorDesc?has_content><eb:Description xml:lang="en">${headers.JentrataEbmsErrorDesc?xml}</eb:Description></#if>
                </eb:Error>
            </eb:SignalMessage>
        </eb:Messaging>
    </S12:Header>
    <S12:Body/>
</S12:Envelope>