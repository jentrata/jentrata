package org.jentrata.ebms.messaging.internal.sql;

import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.messaging.DefaultMessage;
import org.jentrata.ebms.messaging.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps a JDBC Resultset to a Message
 *
 * @author aaronwalker
 */
public class JDBCMessageMapper {

    public static List<Message> getMessage(ResultSet rs) throws SQLException {
        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            DefaultMessage message = new DefaultMessage(rs.getString("message_id"));
            message.setDirection(rs.getString("message_box"));
            message.setCpaId(rs.getString("cpa_id"));
            message.setConversationId(rs.getString("conv_id"));
            message.setRefMessageId(rs.getString("ref_to_message_id"));
            String status = rs.getString("status");
            if(status != null) {
                message.setStatus(MessageStatusType.valueOf(status));
            }
            message.setStatusDescription(rs.getString("status_description"));
            message.setMessageDate(rs.getTimestamp("time_stamp"));
            messages.add(message);
        }
        return messages;
    }
}
