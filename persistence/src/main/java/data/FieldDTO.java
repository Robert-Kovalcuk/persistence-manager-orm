package data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.lang.reflect.Field;

public class FieldDTO {
    private final Field field;
    private final Class<?> type;
    private final String name;
    private final Column columnAnnotation;

    public Field getField() {
        return field;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Column getColumnAnnotation() {
        return columnAnnotation;
    }

    public FieldDTO(Field field) {
        this.field = field;
        this.type = field.getType();
        this.name = field.getName();
        this.columnAnnotation = field.getAnnotation(Column.class);
    }

    public boolean isId() {
        return this.field.getAnnotation(Id.class) != null;
    }

    public boolean holdsEntity() {
        return this.field.getAnnotation(ManyToOne.class) != null;
    }

    public Object valueFrom(Object object) {
        try {
            this.field.setAccessible(true);
            return this.field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new Object(); // TODO illegalAccessException
        }
    }
}
