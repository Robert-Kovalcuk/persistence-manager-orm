package sk.tuke.meta.processor.CreateTableSqlService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Optional;

public class EntitySqlService {
    private final ProcessingEnvironment processingEnv;

    public EntitySqlService(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public String sqlFromTableElement(Element element) {
        return String.format("CREATE TABLE IF NOT EXISTS %s(%s);", getClassName(element), sqlFromEnclosedElements(element));
    }

    private String sqlFromEnclosedElements(Element element) {
        StringBuilder stringBuilder = new StringBuilder();

        element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .forEach(field -> stringBuilder.append(sqlFromVariableElement((VariableElement) field)));

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private String sqlFromVariableElement(VariableElement variableElement) {
        if (isIdAnnotated(variableElement)) {
            return String.format("%s %s PRIMARY KEY AUTOINCREMENT, ", variableElement.getSimpleName(), getType(variableElement));
        } else if (isEmbedded(variableElement)) {
            Optional<? extends Element> annotatedType = getIdFieldFromEmbeddedEntity(variableElement);

            if (annotatedType.isEmpty()) {
                throw new RuntimeException("Embedded entity is missing @Id field");
            }

            String fieldName = variableElement.getSimpleName().toString();
            String fieldType = getType(variableElement);
            String referencedTableName = getType(variableElement);
            String referencedFieldName = annotatedType.get().getSimpleName().toString();

            return String.format("%s %s, FOREIGN KEY (%s) REFERENCES %s(%s),", fieldName, fieldType, fieldName, referencedTableName, referencedFieldName);
        } else {
            String fieldName = variableElement.getSimpleName().toString();
            String fieldType = getType(variableElement);

            return String.format("%s %s,", fieldName, fieldType);
        }
    }

    private Optional<? extends Element> getIdFieldFromEmbeddedEntity(VariableElement variableElement) {
        return variableElement.getEnclosingElement().getEnclosedElements().stream().filter(e -> isIdAnnotated((VariableElement) e)).findFirst();
    }

    private String getClassName(Element element) {
        String className;
        Table tableAnnotation = element.getAnnotation(Table.class);

        if (tableAnnotation != null) {
            className = tableAnnotation.name();
        } else {
            className = element.getSimpleName().toString();
        }

        return className;
    }

    private String getType(VariableElement field) {
        String type = field.asType().toString();

        if (type.equals("java.lang.String")) {
            return "TEXT";
        } else if (type.equals("long") || type.equals("java.lang.Long")) {
            return "INTEGER";
        }

        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(type);
        return typeElement != null ? typeElement.getSimpleName().toString() : type;
    }

    private boolean isIdAnnotated(VariableElement field) {
        return field.getAnnotation(Id.class) != null;
    }

    private boolean isEmbedded(VariableElement variableElement) {
        TypeElement typeElement = (TypeElement) processingEnv.getTypeUtils().asElement(variableElement.asType());
        return typeElement != null && typeElement.getAnnotation(Entity.class) != null;
    }
}
