package org.testinfected.petstore.jdbc;

import org.testinfected.petstore.QueryUnitOfWork;
import org.testinfected.petstore.Transactor;
import org.testinfected.petstore.UnitOfWork;
import org.testinfected.petstore.jdbc.support.JDBCException;

import java.sql.Connection;
import java.sql.SQLException;

public class JDBCTransactor implements Transactor {
    private final Connection connection;

    public JDBCTransactor(Connection connection) {
        this.connection = connection;
    }

    public void perform(UnitOfWork unitOfWork) throws Exception {
        boolean autoCommit = connection.getAutoCommit();
        
        try {
            connection.setAutoCommit(false);
            unitOfWork.execute();
            connection.commit();
        } catch (SQLException e) {
            throw new JDBCException("Could not commit transaction", e);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            resetAutoCommitTo(autoCommit);
        }
    }

    @Override
    public <T> T performQuery(QueryUnitOfWork<T> query) throws Exception {
        perform(query);
        return query.result;
    }

    private void resetAutoCommitTo(boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException ignored) {
        }
    }
}
