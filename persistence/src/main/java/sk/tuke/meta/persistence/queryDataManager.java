package sk.tuke.meta.persistence;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class queryDataManager {
    private Connection connection;

    public queryDataManager(Connection connection) {
        this.connection = connection;
    }

    public ResultSet executeSelect(String sql) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute(sql);

        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next();

        return statement.getResultSet();
    }

    public ResultSet executeUpdate(String sql) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute(sql);

        ResultSet resultSet = statement.getGeneratedKeys();
        resultSet.next();

        return resultSet;
    }

    public ResultSet createTableFromEntity(Field[] fields) throws SQLException {
        final String className = fields[0].getDeclaringClass().getSimpleName();

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + className + "(");
        sql.append(sqlFromEntityFields(fields));
        sql.deleteCharAt(sql.length() - 1);
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        //System.out.println(sql);
        return this.executeUpdate(sql.toString());
    }

    private StringBuilder sqlFromEntityFields(Field[] fields) {
        StringBuilder sql = new StringBuilder();
        
        for(Field field: fields) {
            final StringBuilder sqlFromEntityAnnotations = sqlFromEntityAnnotations(field);

            if(!sqlFromEntityAnnotations.isEmpty())
                sql.append(sqlFromEntityAnnotations);
            else {
                String key = field.getName();
                String type = sqlTypeFromField(field);
                sql.append(key).append(" ").append(type).append(", ");
            }
        }
        
        return sql;
    }

    private StringBuilder sqlFromEntityAnnotations(Field field) {
        StringBuilder sql = new StringBuilder();

        if(hasAnnotation(field, Id.class)) {
            sql.append(field.getName()).append(" ").append(sqlTypeFromField(field)).append(" PRIMARY KEY AUTOINCREMENT, ");
        } else if(hasAnnotation(field, ManyToOne.class)) {
            Optional<Field> annotatedType = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> hasAnnotation(val, Id.class)).findFirst();
            Optional<Field> annotatedName = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> hasAnnotation(val, Id.class)).findFirst();

            if(annotatedType.isEmpty() || annotatedName.isEmpty()) {
                throw new RuntimeException("annotated type is wrong");
            }

            sql
                .append(field.getName())
                .append(" ").append(sqlTypeFromField(annotatedName.get()))
                .append(", ")
                .append("FOREIGN KEY (")
                .append(field.getName())
                .append(") ")
                .append("REFERENCES ")
                .append(field.getType().getSimpleName())
                .append("(")
                .append(annotatedType.get().getName())
                .append(")")
                .append("),");
        }

        return sql;
    }

    private boolean hasAnnotation(Field field, Class<? extends Annotation> a) {
        return field.getAnnotation(a) != null;
    }

    public String sqlTypeFromField(Field field) {
        Class<?> type = field.getType();

        if(type.getSimpleName().equals("String"))
                return "TEXT";
        else if(type.getSimpleName().equals("long")) {
            return "INTEGER";
        }

        return type.getSimpleName();
    }

    public ResultSet insertInto(String tableName, Map<String, ?> entries) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName);
        sql.append("(");
        for(Map.Entry<String, ?> entry : entries.entrySet()) {
            sql.append(entry.getKey()).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        sql.append(" VALUES " + "(");

        for(Map.Entry<String, ?> entry : entries.entrySet()) {
            if(entry.getValue() instanceof String) {
                sql.append("\"");
                sql.append(entry.getValue());
                sql.append("\"").append(",");
            } else {
                sql.append(entry.getValue()).append(",");
            }
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        return this.executeUpdate(sql.toString());
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
        System.out.println(sql);
        return this.executeUpdate(sql.toString());
    }

    public ResultSet selectWhereId(String id, String table, String primaryKeyName) {
        try {
            Statement statement = this.connection.createStatement();
            StringBuilder select = new StringBuilder();

            select.append("SELECT * FROM ").append(table).append(" WHERE ").append(primaryKeyName).append("=").append(id);

            return executeSelect(select.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
