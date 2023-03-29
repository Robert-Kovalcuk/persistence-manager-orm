package sk.tuke.meta.persistence.query;

import data.EntityDTO;
import data.FieldDTO;

import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TableCreationService {

    public static String getType(FieldDTO fieldDTO) {
        Class<?> type = fieldDTO.getType();

        if(type.getSimpleName().equals("String"))
            return "TEXT";
        else if(type.getSimpleName().equals("long")) {
            return "INTEGER";
        }

        return type.getSimpleName();
    }

    private static boolean hasAnnotation(Field field, Class<? extends Annotation> a) {
        return field.getAnnotation(a) != null;
    }

    private static StringBuilder SQL_fromField(FieldDTO field) {
        StringBuilder sql = new StringBuilder();

        if(field.isId()) {
            sql.append(field.getName()).append(" ").append(getType(field)).append(" PRIMARY KEY AUTOINCREMENT, ");
        } else if(field.holdsEntity()) {
            Optional<Field> annotatedType = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> hasAnnotation(val, Id.class)).findFirst();
            Optional<Field> annotatedName = Arrays.stream(field.getType().getDeclaredFields()).filter(val -> hasAnnotation(val, Id.class)).findFirst();

            if(annotatedType.isEmpty() || annotatedName.isEmpty()) {
                throw new RuntimeException("annotated type is wrong");
            }

            sql
                    .append(field.getName())
                    .append(" ").append(getType(field))
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

    private static StringBuilder sqlFromEntityFields(List<FieldDTO> fields) {
        StringBuilder sql = new StringBuilder();

        for(FieldDTO field: fields) {
            final StringBuilder sqlFromEntityAnnotations = SQL_fromField(field);

            if(!sqlFromEntityAnnotations.isEmpty())
                sql.append(sqlFromEntityAnnotations);
            else {
                String key = field.getName();
                String type = getType(field);
                sql.append(key).append(" ").append(type).append(", ");
            }
        }

        return sql;
    }

    public static String createTable(EntityDTO entityDTO) {
        final String className = entityDTO.getTableAnnotation().name();

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + className + "(");
        sql.append(sqlFromEntityFields(entityDTO.getFields()));
        sql.deleteCharAt(sql.length() - 1);
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        //System.out.println(sql);
        return sql.toString();
    }

}
