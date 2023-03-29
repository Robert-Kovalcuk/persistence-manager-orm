package sk.tuke.meta.persistence.query;

import data.EntityDTO;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;

public interface IQueryManager {
    public Optional<ResultSet> select(String table, Condition condition, String... selectedColumns);
    public Optional<ResultSet> select(String table, String... selectedColumns);
    public Optional<ResultSet> insert(EntityDTO entityDTO);
    public ResultSet delete(String table);
    public ResultSet update(EntityDTO entityDTO);

}
