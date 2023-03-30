package sk.tuke.meta.persistence.query;

import data.EntityDTO;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;

public interface IQueryManager {
    public EntityDTO dtoFromObject(Object object);
    public EntityDTO dtoFromType(Class<?> clazz);
    public Optional<ResultSet> select(String table, Condition condition, String... selectedColumns);
    public Optional<ResultSet> select(String table, String... selectedColumns);
    public Optional<ResultSet> insert(EntityDTO entityDTO);
    public ResultSet delete(EntityDTO entityDTO);
    public ResultSet update(EntityDTO entityDTO);

}
