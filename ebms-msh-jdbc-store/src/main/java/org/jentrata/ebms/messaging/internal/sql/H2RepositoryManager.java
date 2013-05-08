package org.jentrata.ebms.messaging.internal.sql;

import javax.sql.DataSource;
import java.io.InputStream;

/**
 * A implementation of the RepositoryManager using H2
 *
 * @author aaronwalker
 */
public class H2RepositoryManager extends AbstractRepositoryManager implements RepositoryManager {

    private static final String [] CREATE_TABLES_SQL = {
            "CREATE TABLE IF NOT EXISTS repository (\n" +
                    " message_id varchar,\n" +
                    " content_type varchar,\n" +
                    " content bytea,\n" +
                    " time_stamp timestamp,\n" +
                    " message_box varchar,\n" +
                    " PRIMARY KEY (message_id, message_box)\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS message (\n" +
                    " message_id varchar,\n" +
                    " message_box varchar,\n" +
                    " cpa_id varchar,\n" +
                    " ref_to_message_id varchar,\n" +
                    " conv_id varchar,\n" +
                    " status varchar,\n" +
                    " status_description varchar,\n" +
                    " time_stamp timestamp,\n" +
                    " PRIMARY KEY (message_id, message_box)\n" +
                    ");"

    };

    public H2RepositoryManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String [] getCreateSQL() {
        return CREATE_TABLES_SQL;
    }


}
