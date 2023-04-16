package data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.lang.reflect.Field;
import java.util.Optional;

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
        return this.columnAnnotation == null ? this.field.getName() : this.columnAnnotation.name();
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

    public boolean isUnique() {return this.columnAnnotation != null && this.columnAnnotation.unique();}

    public boolean holdsEntity() {
        return this.field.getAnnotation(ManyToOne.class) != null;
    }

    public Optional<Object> valueFrom(Object object) {
        if(object == null) {
            return Optional.empty();
        }

        try {
            this.field.setAccessible(true);
            return Optional.of(this.field.get(object));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return Optional.empty(); // TODO illegalAccessException
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    public boolean setValueAt(Object o, Object value) {
        try {
            this.field.setAccessible(true);
            System.out.print("fieldType " + o.getClass());
            System.out.println(" to value " + value.toString());
            this.field.set(o, value);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();// TODO illegalAccessException
            return false;
        }
    }
}
