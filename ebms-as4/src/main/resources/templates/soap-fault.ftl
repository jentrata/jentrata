<?xml version="1.0" encoding="utf-8"?>
<soapenv:Envelope xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:xml="http://www.w3.org/XML/1998/namespace">
    <soapenv:Body>
        <soapenv:Fault>
            <soapenv:Code>
                <soapenv:Value>soapenv:Sender</soapenv:Value>
            </soapenv:Code>
            <soapenv:Reason>
                <soapenv:Text xml:lang="en-US">${headers['CamelException']?xml!"unknown"}</soapenv:Text>
            </soapenv:Reason>
        </soapenv:Fault>
    </soapenv:Body>
</soapenv:Envelope>