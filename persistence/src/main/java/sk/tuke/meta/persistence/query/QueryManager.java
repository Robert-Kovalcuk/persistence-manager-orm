package sk.tuke.meta.persistence.query;

import data.EntityDTO;
import data.FieldDTO;
import data.WhereOperator;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QueryManager implements IQueryManager {
    private Connection connection;

    public QueryManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public EntityDTO dtoFromType(Class<?> clazz) {
        return EntityDTO.fromType(clazz);
    }

    @Override
    public EntityDTO dtoFromObject(Object object) {
        return EntityDTO.fromTypeWithObject(object);
    }

    @Override
    public Optional<ResultSet> select(String table, Condition condition, String... selectedColumns) {
        try {
            String query = "SELECT " + stringFromVararg(selectedColumns) + " " + "FROM " + table + " WHERE " + conditionToString(condition);
            System.out.println(query);
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
            System.out.println(query);
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
            String insertQuery = InsertQueryFormatter.format(entityDTO);

            PreparedStatement statement = this.connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1); // assuming the auto-generated key is a long
                return Optional.of(this.selectCondition(entityDTO.getName(), entityDTO.getIdField().getName(), String.valueOf(id), WhereOperator.equals));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public ResultSet delete(EntityDTO entityDTO) {
        ResultSet resultSet;
        try {
            Statement statement;
            statement = this.connection.createStatement();
            statement.execute("DELETE * FROM " + entityDTO.getName() + " WHERE " + entityDTO.getIdField().valueFrom(entityDTO.entity) + stringFromOperation(WhereOperator.equals) + entityDTO.getIdField().getName());

            resultSet = statement.getGeneratedKeys();
            resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return resultSet;
    }

    @Override
    public ResultSet update(EntityDTO entityDTO) throws SQLException {
        Map<String, Object> map = new HashMap<>();

        for (FieldDTO field : entityDTO.getFields()) {
            if (field.holdsEntity()) {
                Object o = field.valueFrom(entityDTO.entity).get();
                EntityDTO entityDTO1 = EntityDTO.fromTypeWithObject(o);

                map.put(field.getName(), entityDTO1.getIdField().valueFrom(o).get());
            } else {
                map.put(field.getName(), field.valueFrom(entityDTO.entity).get());
            }
        }

        String tableName = entityDTO.getName();
        long id = (long) entityDTO.getIdField().valueFrom(entityDTO.entity).orElse(0);
        String idName = entityDTO.getIdField().getName();

        StringBuilder sql = new StringBuilder("UPDATE " + tableName);

        sql.append(" SET ");
        for(Map.Entry<String, ?> entry : map.entrySet()) {
            sql.append(entry.getKey()).append(" = ");
            sql.append("\"").append(entry.getValue()).append("\"").append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ").append(idName).append("=").append(id);

        sql.append(";");

        return this.executeAndGetKeys(sql.toString());
    }

    public ResultSet selectCondition(String tableName, String columnName, String value, WhereOperator operation) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute("SELECT * FROM " + tableName + " WHERE " + columnName + stringFromOperation(operation) + value);

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
        System.out.println(sql);
        Statement statement = this.connection.createStatement();
        statement.execute(sql);

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

}
