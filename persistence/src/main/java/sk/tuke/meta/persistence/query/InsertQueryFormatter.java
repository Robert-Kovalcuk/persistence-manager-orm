package sk.tuke.meta.persistence.query;

import data.EntityDTO;
import data.FieldDTO;

import java.util.Map;

public class InsertQueryFormatter {
    public static String format(EntityDTO entityDTO, Object object) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + entityDTO.getName() + "(");

        for(FieldDTO entry : entityDTO.getFields())
            sql.append(entry.getName()).append(", ");

        sql.toString().deleteCharAt(sql.length() - 1);
        sql.toString().append(")");
        sql.toString().append(" VALUES " + "(");

        for(Map.Entry<String, ?> entry : fieldsNameAndValueMap.entrySet()) {
            if(entry.getValue() instanceof String) {
                sql.toString().append("\"");
                sql.toString().append(entry.getValue());
                sql.toString().append("\"").append(",");
            } else {
                sql.toString().append(entry.getValue()).append(",");
            }
        }

        sql.toString().deleteCharAt(sql.length() - 1);
        sql.toString().append(");");

        return this.executeAndGetKeys(sql.toString().toString());
    }
}
