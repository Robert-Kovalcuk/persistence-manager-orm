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

    private ResultSet execute(String sql) throws SQLException {
        final Statement statement = this.connection.createStatement();

        statement.execute(sql);

        return statement.getResultSet();
    }

    public ResultSet createTableFromEntity(Field[] fields) throws SQLException {
        final String className = fields[0].getDeclaringClass().getSimpleName();

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + className + "(");
        sql.append(sqlFromEntityFields(fields));
        sql.deleteCharAt(sql.length() - 1);
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        //System.out.println(sql);
        return this.execute(sql.toString());
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
            sql.append(field.getName()).append(" ").append(field.getType()).append(" PRIMARY KEY, ");
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

        if(!type.isPrimitive()) {
            if(type.getSimpleName().equals("String"))
                return "text";
        } else if(type.getSimpleName().equals("long")) {
            return "int";
        }
        return type.getSimpleName();
    }

    public ResultSet insertInto(String tableName, Map<String, ?> entries) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " VALUES" + "(");

        for(Map.Entry<String, ?> entry : entries.entrySet()) {
            sql.append(entry.getValue()).append(",");
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");
        System.out.println(sql);
        return this.execute(sql.toString());
    }
}
