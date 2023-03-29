package sk.tuke.meta.persistence.query;

import data.EntityDTO;
import data.WhereOperator;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class QueryManager implements IQueryManager {
    private Connection connection;

    public QueryManager(Connection connection) {
        this.connection = connection;
    }

    public ResultSet selectCondition(String tableName, String columnName, String value, WhereOperator operation) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute("SELECT * FROM " + tableName + " WHERE " + columnName + operation + value);

        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next();

        return statement.getResultSet();
    }

    public ResultSet selectAll(String tableName) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute("SELECT * FROM " + tableName);

        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next();

        return statement.getResultSet();
    }

    public ResultSet executeAndGetKeys(String sql) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute(sql);

        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next();

        return resultSet;
    }

    public ResultSet delete(String tableName, String columnName, String value, WhereOperator operation) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute("DELETE * FROM " + tableName + " WHERE " + value + stringFromOperation(operation) + columnName);

        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next();

        return resultSet;
    }

    private String stringFromOperation(WhereOperator operation) {
        switch (operation) {
            case equals -> {
                return "=";
            }
            case lessThan -> {
                return "<";
            }
            case biggerThan -> {
                return ">";
            }
        }
        return "";
    }

    public ResultSet createTable(EntityDTO entityDTO) throws SQLException {
        return this.executeAndGetKeys(TableCreationService.createTable(entityDTO));
    }


    public ResultSet update(String tableName, Map<String, ?> entries, long id, String idName) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName);

        sql.append(" SET ");
        for(Map.Entry<String, ?> entry : entries.entrySet()) {
            sql.append(entry.getKey()).append(" = ");
            sql.append("\"").append(entry.getValue()).append("\"").append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ").append(idName).append("=").append(id);

        sql.append(";");

        return this.executeAndGetKeys(sql.toString());
    }

    @Override
    public Optional<ResultSet> select(String table, Condition condition, String... selectedColumns) {
        try {
            String query = "SELECT " + stringFromVararg(selectedColumns) + " " + "FROM " + table + " WHERE " + conditionToString(condition);

            return Optional.of(this.connection.prepareStatement(query).executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<ResultSet> select(String table, String... selectedColumns) {
        try {
            String query = "SELECT " + stringFromVararg(selectedColumns) + " " + "FROM " + table;

            return Optional.of(this.connection.prepareStatement(query).executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static String stringFromVararg(String[] selectedColumns) {
        return Arrays.toString(selectedColumns).replace("[", "").replace("]", "");
    }

    private static String conditionToString(Condition condition) {
        return condition.getOperand1() + condition.operatorToString() + condition.getOperand2();
    }

    @Override
    public Optional<ResultSet> insert(EntityDTO entityDTO) {
        try {
            String insertQuery = InsertQueryFormatter.format(tableName, entries);

            return Optional.of(this.connection.prepareStatement(insertQuery).executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public ResultSet delete(String table) {
        return null;
    }

    @Override
    public ResultSet update() {
        return null;
    }
}
