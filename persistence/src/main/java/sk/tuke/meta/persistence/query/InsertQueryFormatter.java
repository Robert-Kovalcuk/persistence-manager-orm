package sk.tuke.meta.persistence.query;

import data.EntityDTO;
import data.FieldDTO;

import javax.lang.model.type.PrimitiveType;
import java.util.HashMap;
import java.util.Map;

public class InsertQueryFormatter {
    public static String format(EntityDTO entityDTO) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + entityDTO.getName() + "(");

        for(FieldDTO entry : entityDTO.getFields())
            sql.append(entry.getName()).append(",");

        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        sql.append(" VALUES " + "(");

        for(Map.Entry<String, ?> entry : mapFromEntityFields(entityDTO).entrySet()) {
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

        return sql.toString();
    }

    private static Map<String, ?> mapFromEntityFields(EntityDTO entityDTO) {
        Map<String, String> map = new HashMap<>();

        for (FieldDTO field : entityDTO.getFields()) {
            if(field.holdsEntity()) {
                EntityDTO embeddedEntity = EntityDTO.fromTypeWithObject(field.valueFrom(entityDTO.entity));
                map.put(field.getName(), embeddedEntity.getIdField().valueFrom(embeddedEntity.entity).toString());
            } else
                map.put(field.getName(), field.valueFrom(entityDTO.entity).toString());
        }

        return map;
    }
}
