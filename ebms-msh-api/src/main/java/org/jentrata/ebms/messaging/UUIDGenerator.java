package org.jentrata.ebms.messaging;

import java.util.UUID;

/**
 * Used to generate message Id
 *
 * @author aaronwalker
 */
public class UUIDGenerator {

    private String prefix = "";
    private String sufffix = "@jentrata.org";

    public String generateId() {
        String uuid = UUID.randomUUID().toString();
        return prefix + uuid + sufffix;
    }
}
