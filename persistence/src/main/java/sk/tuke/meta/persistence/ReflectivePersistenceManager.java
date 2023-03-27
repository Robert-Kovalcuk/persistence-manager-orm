package sk.tuke.meta.persistence;

import data.EntityDTO;
import data.WhereOperation;

import javax.persistence.*;
import java.lang.reflect.Field;
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
                System.out.println(classType);
                this.createTable(classType);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ResultSet createTable(Class<?> entity) throws SQLException {
        System.out.println(entity.getSimpleName());
        return this.queryManager.createTableFromEntity(EntityDTO.fromType(entity));
    }

    @Override
    public <T> Optional<T> get(Class<T> type, long id) throws PersistenceException {
        try {
            EntityDTO entityDTO = EntityDTO.fromType(type);

            ResultSet result = this.queryManager.selectWhereId(id+"", entityDTO.getTableAnnotation().name(), entityDTO.getIdField().getName());

            if (result.next())
                return Optional.of(instanceFromResult(type, result));
            else
                return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private <T> T instanceFromResult(Class<T> type, ResultSet queryResultSet) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            ResultSetMetaData metaData = queryResultSet.getMetaData();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = queryResultSet.getObject(columnName);
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
                T instance = instanceFromResult(type, result);
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

            while (resultSet.next())
                list.add(instanceFromResult(type, resultSet));

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistenceException(e);
        }

        return list;
    }

    private String fieldNameFromColumnAnnotation(Field field) {
        Column annotation = field.getAnnotation(Column.class);

        if(annotation == null)
            return field.getName();

        return annotation.name();
    }

    private String tableNameFromTableAnnotation(Object entity) {
        Table annotation = entity.getClass().getAnnotation(Table.class);

        if(annotation == null)
            return entity.getClass().getSimpleName();

        return annotation.name();
    }

    @Override
    public long save(Object entity) throws SQLException {
        EntityDTO entityDTO = EntityDTO.fromType(entity.getClass());

        Map<String, String> map = new HashMap<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            Optional<Field> entitiesIdFieldOptional = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> val.getAnnotation(Id.class) != null).findFirst();
            Field entitiesIdField = entitiesIdFieldOptional.orElse(null);

            try {
                Object value;

                if(entitiesIdField != null) {
                    entitiesIdField.setAccessible(true);
                    value = this.save(field.get(entity));
                } else {
                    value = field.get(entity);
                }

                map.put(field.getName(), value.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        Object idFieldValue = entityDTO.getIdField().valueFrom(entity);

        if(this.get(entity.getClass(), (long) entityDTO.getIdField().valueFrom(entity)).isPresent()) {
            this.queryManager.update(entity.getClass().getSimpleName(), map, (long) idFieldValue, entityDTO.getIdField().getName());
            return (Integer) idFieldValue;
        } else {
            map.remove(entityDTO.getIdField().getName());

            try (ResultSet resultSet = queryManager.insertInto(entity.getClass().getSimpleName(), map)) {
                return (Integer) resultSet.getObject(1);
            }
        }

    }

    @Override
    public void delete(Object entity) {
        EntityDTO entityDTO = EntityDTO.fromType(entity.getClass());

        if(entityDTO.getIdField() == null)
            throw new PersistenceException("class " + entity.getClass() + "does not have @Entity annotation");

        try {
            this.queryManager.executeDelete(
                    entityDTO.getTableAnnotation().name(),
                    entityDTO.getIdField().getName(),
                    entityDTO.getIdField().valueFrom(entity).toString(),
                    WhereOperation.equals
            ).close();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
