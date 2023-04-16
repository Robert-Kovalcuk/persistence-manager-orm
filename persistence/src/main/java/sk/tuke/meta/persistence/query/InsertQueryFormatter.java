package sk.tuke.meta.persistence.query;

import data.EntityDTO;
import data.FieldDTO;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class InsertQueryFormatter {
    public static String format(EntityDTO entityDTO) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + entityDTO.getName() + "(");

        for(FieldDTO entry : entityDTO.getFields()) {
            if(!entry.isId())
                sql.append(entry.getName()).append(",");
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        sql.append(" VALUES " + "(");

        for(Map.Entry<String, ?> entry : mapFromEntityFields(entityDTO).entrySet()) {
            if(Objects.equals(entry.getKey(), entityDTO.getIdField().getName()))
                continue;

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
        Map<String, Object> map = new LinkedHashMap<>();

        for (FieldDTO field : entityDTO.getFields()) {
            if(field.holdsEntity()) {
                EntityDTO embeddedEntity = EntityDTO.fromTypeWithObject(field.valueFrom(entityDTO.entity));
                try {
                    map.put(field.getName(), embeddedEntity.getIdField().valueFrom(embeddedEntity.entity).get());
                } catch (NullPointerException e) {
                    map.put(field.getName(), field.holdsEntity() ? 0 : "");
                }
            } else
                field.valueFrom(entityDTO.entity).ifPresentOrElse(o -> map.put(field.getName(), o.toString()), () -> map.put(field.getName(), field.holdsEntity() ? String.valueOf(0) : ""));
        }

        return map;
    }
}
