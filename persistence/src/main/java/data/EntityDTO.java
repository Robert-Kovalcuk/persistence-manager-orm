package data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class EntityDTO {
    public Object entity;
    private Table tableAnnotation;
    private FieldDTO idField;
    private List<FieldDTO> fields = new ArrayList<>();

    private String name;

    private EntityDTO() {}

    public Table getTableAnnotation() {
        return this.tableAnnotation;
    }

    public String getName() { return this.name;}

    public List<FieldDTO> getFields() {
        return this.fields
                .stream()
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .collect(Collectors.toList());
    }

    public FieldDTO getIdField() {
        return this.idField;
    }

    public static EntityDTO fromType(Class<?> clazz) {
        EntityDTO entityDTO = new EntityDTO();

        return getEntityDTO(entityDTO, clazz);
    }

    public static EntityDTO fromTypeWithObject(Object entity) {
        EntityDTO entityDTO = new EntityDTO();
        Class<?> clazz = entity.getClass();
        entityDTO.entity = entity;
        return getEntityDTO(entityDTO, clazz);
    }

    private static EntityDTO getEntityDTO(EntityDTO entityDTO, Class<?> clazz) {
        entityDTO.tableAnnotation = clazz.getAnnotation(Table.class);
        if(entityDTO.tableAnnotation != null)
            entityDTO.name = entityDTO.tableAnnotation.name();
        else
            entityDTO.name = clazz.getSimpleName();

        for (Field declaredField : clazz.getDeclaredFields()) {
            entityDTO.fields.add(new FieldDTO(declaredField));
            if(declaredField.getAnnotation(Id.class) != null)
                entityDTO.idField = new FieldDTO(declaredField);
        }

        return entityDTO;
    }

    public boolean hasTableAnnotation() {return this.tableAnnotation != null;}
}
