{
    "messageId":"${headers.JentrataMessageID}",
    "direction":"${headers.JentrataMessageDirection}",
    "cpaId":"${headers.JentrataCPAId}",
    "refMessageId":"${headers.JentrataRefMessageID!}",
    "conversationId":"${headers.JentrataConversationId}",
    "status":"${headers.JentrataMessageStatus}",
    "statusDescription":"${headers.JentrataMessageStatusDesc}",
    "messageDate":"${headers.JentrataMessageDate}",
    "headers": {
        <#assign keys = request.headers?keys/>
        <#list keys as key>
            "${key}":"${headers[key]}",
        </#list>
            "jentrataVersion":"${exchange.properties['JentrataVersion']!'2.0.0-SNAPSHOT'}"
        }
}