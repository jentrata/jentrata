package org.jentrata.ebms.messaging;

import org.jentrata.ebms.MessageStatusType;

import java.util.Date;

/**
 * Holds information about a message
 *
 * @author aaronwalker
 */
public interface Message {

    String getMessageId();
    String getDirection();
    String getCpaId();
    String getRefMessageId();
    String getConversationId();
    MessageStatusType getStatus();
    String getStatusDescription();
    Date getMessageDate();

}
