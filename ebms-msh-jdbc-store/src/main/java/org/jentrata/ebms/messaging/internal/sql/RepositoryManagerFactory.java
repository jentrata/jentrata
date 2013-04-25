package org.jentrata.ebms.messaging.internal.sql;

import javax.sql.DataSource;

/**
 * Constructs a RepositoryManager based on the databaseType
 * The default is postgres
 *
 * @author aaronwalker
 */
public class RepositoryManagerFactory {

    private DataSource dataSource;
    private String databaseType = "H2";

    public RepositoryManager createRepositoryManager() {
        if(databaseType == null) {
            throw new IllegalArgumentException("databaseType can not be null");
        }
        RepositoryManager repositoryManager;
        switch (databaseType.toUpperCase()) {
            case "POSTGRES":
                repositoryManager = new PostgresRepositoryManager(dataSource);
                break;
            case "H2":
                repositoryManager = new H2RepositoryManager(dataSource);
                break;
            default:
                throw new UnsupportedOperationException(databaseType + " is currently not a supported database");
        }
        return repositoryManager;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
}
