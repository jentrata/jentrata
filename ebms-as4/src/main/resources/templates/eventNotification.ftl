{
    "messageId":"${headers.JentrataMessageID}",
    "direction":"${headers.JentrataMessageDirection}",
    "cpaId":"${headers.JentrataCPAId!'UNKNOWN'}",
    "refMessageId":"${headers.JentrataRefMessageID!}",
    "duplicateMessageId":"${headers.JentrataDuplicateMessageId!}",
    "conversationId":"${headers.JentrataConversationId!}",
    "status":"${headers.JentrataMessageStatus}",
    "statusDescription":"${headers.JentrataMessageStatusDesc!}",
    "messageDate":"${headers.JentrataMessageDate!}",
    "headers": {
        <#assign keys = request.headers?keys/>
        <#list keys as key>
            <#if headers[key]?has_content><#if headers[key]?is_string>"${key}":"${headers[key]?json_string}",<#elseif headers[key]?is_date>"${key}":"${headers[key]?datetime?iso_local}",</#if></#if>
        </#list>
            "jentrataVersion":"${exchange.properties['JentrataVersion']!'2.0.0-SNAPSHOT'}"
        }
}