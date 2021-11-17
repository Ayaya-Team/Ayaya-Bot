package ayaya.core.utils;

import ayaya.core.exceptions.db.DBOperationNotSupportedException;
import ayaya.core.exceptions.db.DBNotConnectedException;
import ayaya.core.exceptions.general.UnsupportedTypeException;

import java.io.Serializable;
import java.sql.*;

/**
 * Class to help communicate with the database.
 */
public class SQLController {

    private static final String CREATE_DB = "create database";
    private static final String DROP_DB = "drop database";

    private boolean connected;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    /**
     * Creates an SQLController object.
     */
    public SQLController() {
        connected = false;
        connection = null;
        statement = null;
        resultSet = null;
    }

    /**
     * Opens a connection with a database.
     *
     * @param url  the url of the database
     * @param user the database user
     * @param pass the user password
     * @throws SQLException when an sql error occurs
     */
    public void open(String url, String user, String pass) throws SQLException {
        try {
            connection = DriverManager.getConnection(url, user, pass);
            connected = true;
        } catch (SQLException e) {
            connection = null;
            throw e;
        }
    }

    /**
     * Performs an insert or update request to the database.
     *
     * @param sql     the sql command
     * @param objects the arguments to insert/update/remove from the database
     * @throws SQLException             when an sql error occurs
     * @throws DBNotConnectedException  when there is no database connected to this controller
     * @throws UnsupportedTypeException if the object type isn't supported by this controller
     */
    public void sqlInsertUpdateOrDelete(String sql, Serializable[] objects, int timeout) throws SQLException,
            DBNotConnectedException, UnsupportedTypeException {
        if (connection == null) throw new DBNotConnectedException();
        if (resultSet != null && !resultSet.isClosed()) resultSet.close();
        if (statement != null && !statement.isClosed()) statement.close();
        PreparedStatement st = connection.prepareStatement(sql);
        if (timeout > 0) st.setQueryTimeout(timeout);
        for (int i = objects.length; i > 0; i--) {
            setObject(st, objects[i - 1], i);
        }
        st.executeUpdate();
        this.statement = st;
    }

    /**
     * Sets an object for a statement sql command.
     *
     * @param statement the statement to set the object for
     * @param object    the object to set
     * @param index     the index where to set
     * @throws SQLException             when an sql error occurs
     * @throws UnsupportedTypeException if the object type isn't supported by this controller
     */
    private void setObject(PreparedStatement statement, Serializable object, int index) throws SQLException,
            UnsupportedTypeException {
        if (object instanceof String) statement.setString(index, (String) object);
        else if (object instanceof Integer) statement.setInt(index, (Integer) object);
        else if (object instanceof Float) statement.setFloat(index, (Float) object);
        else if (object instanceof Double) statement.setDouble(index, (Double) object);
        else throw new UnsupportedTypeException();
    }

    /**
     * Performs a select request with input sanitization and retrieves a result set.
     *
     * @param sql     the sql command
     * @param objects the arguments to query
     * @param timeout the query timeout
     * @return a result set
     * @throws SQLException            when an sql error occurs
     * @throws DBNotConnectedException when there is no database connected to this controller
     */
    public ResultSet sqlSelect(String sql, Serializable[] objects, int timeout) throws SQLException,
            DBNotConnectedException, UnsupportedTypeException{
        PreparedStatement st = connection.prepareStatement(sql);
        if (timeout > 0) st.setQueryTimeout(timeout);
        for (int i = objects.length; i > 0; i--) {
            setObject(st, objects[i - 1], i);
        }
        return this.sqlSelect(st.toString(), timeout);
    }

    /**
     * Performs a select request and retrieves a result set.
     *
     * @param sql     the sql command
     * @param timeout the query timeout
     * @return a result set
     * @throws SQLException            when an sql error occurs
     * @throws DBNotConnectedException when there is no database connected to this controller
     */
    public ResultSet sqlSelect(String sql, int timeout) throws SQLException, DBNotConnectedException {
        if (connection == null) throw new DBNotConnectedException();
        if (resultSet != null && !resultSet.isClosed()) resultSet.close();
        if (statement != null && !statement.isClosed()) statement.close();
        statement = connection.createStatement();
        if (timeout > 0) statement.setQueryTimeout(timeout);
        resultSet = statement.executeQuery(sql);
        return resultSet;
    }

    /**
     * Performs a select request with input sanitization
     * and retrieves a result set after moving the cursor to the first row if it exists.
     *
     * @param sql     the sql command
     * @param timeout the query timeout
     * @return a result set
     * @throws SQLException            when an sql error occurs
     * @throws DBNotConnectedException when there is no database connected to this controller
     */
    public ResultSet sqlSelectNext(String sql, Serializable[] o, int timeout) throws SQLException,
            DBNotConnectedException {
        ResultSet result = this.sqlSelect(sql, o, timeout);
        result.next();
        return result;
    }

    /**
     * Performs a create request.
     *
     * @param sql     the sql command
     * @param timeout the query timeout
     * @throws SQLException                     when an sql error occurs
     * @throws DBNotConnectedException          when there is no database connected to this controller
     * @throws DBOperationNotSupportedException when the user attempts to create a new database
     */
    public void sqlCreate(String sql, int timeout) throws SQLException, DBNotConnectedException,
            DBOperationNotSupportedException {
        if (connection == null) throw new DBNotConnectedException();
        else if (sql.toLowerCase().contains(CREATE_DB)) throw new DBOperationNotSupportedException();
        if (resultSet != null && !resultSet.isClosed()) resultSet.close();
        if (statement != null && !statement.isClosed()) statement.close();
        statement = connection.createStatement();
        if (timeout > 0) statement.setQueryTimeout(timeout);
        statement.executeUpdate(sql);
    }

    /**
     * Performs an alter request.
     *
     * @param sql     the sql command
     * @param timeout the query timeout
     * @throws SQLException            when an sql error occurs
     * @throws DBNotConnectedException when there is no database connected to this controller
     */
    public void sqlAlter(String sql, int timeout) throws SQLException, DBNotConnectedException {
        if (connection == null) throw new DBNotConnectedException();
        if (resultSet != null && !resultSet.isClosed()) resultSet.close();
        if (statement != null && !statement.isClosed()) statement.close();
        statement = connection.createStatement();
        if (timeout > 0) statement.setQueryTimeout(timeout);
        statement.execute(sql);
    }

    /**
     * Performs a drop request.
     *
     * @param sql     the sql command
     * @param timeout the query timeout
     * @throws SQLException                     when an sql error occurs
     * @throws DBNotConnectedException          when there is no database connected to this controller
     * @throws DBOperationNotSupportedException when the user attempts to create a new database
     */
    public void sqlDrop(String sql, int timeout) throws SQLException, DBNotConnectedException {
        if (connection == null) throw new DBNotConnectedException();
        if (resultSet != null && !resultSet.isClosed()) resultSet.close();
        if (statement != null && !statement.isClosed()) statement.close();
        statement = connection.createStatement();
        if (timeout > 0) statement.setQueryTimeout(timeout);
        statement.executeUpdate(sql);
        if (sql.toLowerCase().contains(DROP_DB)) throw new DBOperationNotSupportedException();
    }

    /**
     * Closes the connection with the database.
     *
     * @throws SQLException when an sql error occurs
     */
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connected = false;
            connection = null;
            if (resultSet != null && !resultSet.isClosed()) resultSet.close();
            resultSet = null;
            if (statement != null && !statement.isClosed()) statement.close();
            statement = null;
        }
    }

}