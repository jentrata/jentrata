------=_Jentrata_Mime_Message_
Content-Type: application/soap+xml; charset=UTF-8
Content-Id: <soapPart@jentrata.org>

${body}

<#list headers.JentrataPayloads as payload>
------=_Jentrata_Mime_Message_
Content-Type: ${payload.contentType}; charset=${payload.charset}
Content-Id: ${payload.payloadId}

${payload.content}

</#list>
------=_Jentrata_Mime_Message_--