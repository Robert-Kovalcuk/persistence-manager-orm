package sk.tuke.meta.persistence;

import data.EntityDTO;
import data.FieldDTO;
import data.WhereOperator;
import sk.tuke.meta.persistence.query.QueryManager;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class ReflectivePersistenceManager implements PersistenceManager {
    private QueryManager queryManager;
    private Class<?>[] types;

    public ReflectivePersistenceManager(Connection connection, Class<?>... types) {
        this.queryManager = new QueryManager(connection);
        this.types = types;
    }

    @Override
    public void createTables() {
        for (Class<?> classType : this.types) {
            try {
                this.createTable(classType);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new PersistenceException(e);
            }
        }
    }

    private ResultSet createTable(Class<?> entity) throws SQLException {
        return this.queryManager.createTable(EntityDTO.fromType(entity));
    }

    @Override
    public <T> Optional<T> get(Class<T> type, long id) throws PersistenceException {
        try {
            EntityDTO entityDTO = EntityDTO.fromType(type);
            ResultSet result = this.queryManager.selectCondition(entityDTO.getName(), entityDTO.getIdField().getName(), id+"", WhereOperator.equals);

            return result.next() ? Optional.of(instanceFromResultSet(type, result)) : Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private boolean isEntity(Class<?> type) {
        return type.getAnnotation(Entity.class) != null;
    }

    @Override
    public <T> List<T> getAll(Class<T> type) {
        List<T> instances = new ArrayList<T>();

        try {
            EntityDTO entityDTO = EntityDTO.fromType(type);
            ResultSet result = this.queryManager.selectAll(entityDTO.getName());

            while (result.next()) {
                T instance = instanceFromResultSet(type, result);
                instances.add(instance);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }

        return instances;
    }

    @Override
    public <T> List<T> getBy(Class<T> type, String fieldName, Object value) {
        List<T> list = new ArrayList<T>();

        try {
            EntityDTO entityDTO = EntityDTO.fromType(type);
            ResultSet resultSet = this.queryManager.selectCondition(entityDTO.getName(), fieldName, value.toString(), WhereOperator.equals);

            while (resultSet.next())
                list.add(instanceFromResultSet(type, resultSet));

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistenceException(e);
        }

        return list;
    }

    @Override
    public long save(Object entity) throws SQLException {
        EntityDTO entityDTO = this.queryManager.dtoFromObject(entity);

        Map<String, String> map = new HashMap<>();

        for (FieldDTO field : entityDTO.getFields()) {
            Optional<Field> entitiesIdFieldOptional = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> val.getAnnotation(Id.class) != null).findFirst();
            Field entitiesIdField = entitiesIdFieldOptional.orElse(null);

            Object value;

            if (entitiesIdField != null) {
                entitiesIdField.setAccessible(true);
                value = this.save(field.valueFrom(entity));
            } else {
                value = field.valueFrom(entity);
            }

            map.put(field.getName(), value.toString());
        }

        if (this.get(entity.getClass(), (long) entityDTO.getIdField().valueFrom(entity))
                .isPresent()) {
            this.queryManager.update(entityDTO);
            return (Integer) entityDTO.getIdField().valueFrom(entity);
        } else {
            map.remove(entityDTO.getIdField().getName());

            Optional<ResultSet> resultSet = queryManager.insert(entityDTO);
            if (resultSet.isPresent())
                return (Integer) resultSet.get().getObject(1);
        }
        return 0;
    }

    @Override
    public void delete(Object entity) {
        EntityDTO entityDTO = EntityDTO.fromType(entity.getClass());

        if(entityDTO.getIdField() == null)
            throw new PersistenceException("class " + entity.getClass() + "does not have @Entity annotation");

        try {
            this.queryManager.delete(entityDTO).close();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private <T> T instanceFromResultSet(Class<T> type, ResultSet queryResultSet) {
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
}
