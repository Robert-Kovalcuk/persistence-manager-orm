package sk.tuke.meta.persistence;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public <T> Optional<T> get(Class<T> type, long id) {
        return Optional.empty();
    }

    @Override
    public <T> List<T> getAll(Class<T> type) {
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> getBy(Class<T> type, String fieldName, Object value) {
        return Collections.emptyList();
    }

    @Override
    public long save(Object entity) throws SQLException {
        Map<String, String> map = new HashMap<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            String type;
            field.setAccessible(true);

            Optional<Field> entitiesIdFieldOptional = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> val.getAnnotation(Id.class) != null).findFirst();
            Field entitiesIdField = entitiesIdFieldOptional.orElse(null);

            try {
                Object value;

                if(entitiesIdField != null) {
                    entitiesIdField.setAccessible(true);
                    type = entitiesIdField.getType().getSimpleName();
                    value = entitiesIdField.get();
                } else {
                    value = field.get(entity);
                    type = field.getType().getSimpleName();
                }
                map.put(type, value.toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        final ResultSet resultSet = queryManager.insertInto(entity.getClass().getSimpleName(), map);

        return 0;
    }

    @Override
    public void delete(Object entity) {
    }
}
