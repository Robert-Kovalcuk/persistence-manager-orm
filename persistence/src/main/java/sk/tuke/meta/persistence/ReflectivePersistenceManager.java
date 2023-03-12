package sk.tuke.meta.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

public class ReflectivePersistenceManager implements PersistenceManager {
    private queryDataManager queryManager;
    private Class<?>[] types;

    public ReflectivePersistenceManager(Connection connection, Class<?>... types) {
        this.queryManager = new queryDataManager(connection);
        this.types = types;
    }

    @Override
    public void createTables() {
        for (Class<?> classType : this.types) {
            try {
                this.createTable(classType);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ResultSet createTable(Class<?> entity) throws SQLException {
        return this.queryManager.createTableFromEntity(entity.getDeclaredFields());
    }

    @Override
    public <T> Optional<T> get(Class<T> type, long id) throws PersistenceException {
        try {
            String idFieldName = Arrays.stream(type.getDeclaredFields()).filter(field -> field.getAnnotation(Id.class) != null).findFirst().get().getName();
            ResultSet result = this.queryManager.selectWhereId(id+"", type.getSimpleName(), idFieldName);

            if (result.next()) {
                T instance = getInstanceFromResult(type, result);

                return Optional.of(instance);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private <T> T getInstanceFromResult(Class<T> type, ResultSet result) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            ResultSetMetaData metaData = result.getMetaData();

            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = result.getObject(columnName);
                Field field = type.getDeclaredField(columnName);

                if (isEntity(field.getType())) {
                    Object nestedEntity = this.get(field.getType(), (int) columnValue).orElse(null);
                    field.setAccessible(true); // Set the field to be accessible
                    field.set(instance, nestedEntity);
                } else {
                    field.setAccessible(true);
                    field.set(instance, columnValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    private boolean isEntity(Class<?> type) {
        return type.getAnnotation(Entity.class) != null;
    }

    private boolean isForeignKey(Field field) {
        return field.getAnnotation(ManyToOne.class) != null;
    }

    @Override
    public <T> List<T> getAll(Class<T> type) {
        List<T> list = new ArrayList<T>();

        try {
            ResultSet result = this.queryManager.executeSelect("SELECT * FROM " + type.getSimpleName());

            while (result.next()) {
                T instance = getInstanceFromResult(type, result);
                list.add(instance);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }

        return list;
    }

    @Override
    public <T> List<T> getBy(Class<T> type, String fieldName, Object value) {
        List<T> list = new ArrayList<T>();
        try {
            ResultSet resultSet = this.queryManager.executeSelect("SELECT * FROM " + type.getSimpleName() + " WHERE " + fieldName + "=" + "\"" + value.toString() + "\"");
            while (resultSet.next()) {
                list.add(getInstanceFromResult(type, resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    private Field getIdField(Object entity) {
        Optional<Field> idField = Arrays.stream(entity.getClass().getDeclaredFields()).filter(field -> field.getAnnotation(Id.class) != null).findFirst();

        return idField.orElse(null);
    }

    @Override
    public long save(Object entity) throws SQLException {
        Field idField = this.getIdField(entity);
        idField.setAccessible(true);

        Map<String, String> map = new HashMap<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            Optional<Field> entitiesIdFieldOptional = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> val.getAnnotation(Id.class) != null).findFirst();
            Field entitiesIdField = entitiesIdFieldOptional.orElse(null);

            try {
                Object value;

                if(entitiesIdField != null) {
                    entitiesIdField.setAccessible(true);
                    long id = this.save(field.get(entity));
                    value = id;
                } else {
                    value = field.get(entity);
                }

                map.put(field.getName(), value.toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            if(this.get(entity.getClass(), (long) idField.get(entity)).isPresent()) {
                this.queryManager.update(entity.getClass().getSimpleName(), map, (long) idField.get(entity), idField.getName());
                return (Integer) idField.get(entity);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        map.remove(idField.getName());

        try {
            try (ResultSet resultSet = queryManager.insertInto(entity.getClass().getSimpleName(), map)) {
                return (Integer) resultSet.getObject(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public void delete(Object entity) {
        Field field = getIdField(entity);
        field.setAccessible(true);

        try {
            this.queryManager.executeSelect("DELETE FROM " + entity.getClass().getSimpleName() + " WHERE "+field.getName()+"="+field.get(entity)).close();
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
