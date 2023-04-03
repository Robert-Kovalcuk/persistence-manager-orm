package sk.tuke.meta.processor.SQLGeneratorServices;


import sk.tuke.meta.annotation.NotNull;
import sk.tuke.meta.annotation.Null;
import sk.tuke.meta.annotation.Unique;
import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class FieldSQLGenerator implements ISQLGenerator<VariableElement> {
    ProcessingEnvironment processingEnvironment;

    public FieldSQLGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnvironment = processingEnv;
    }

    @Override
    public String generateFrom(VariableElement variableElement) throws SQLGeneratorException {
        /*if (this.hasAnnotation(variableElement, Id.class))
            return genSQLCodeWithIdAnnotation(variableElement);
        else if (this.hasAnnotation(variableElement, Entity.class))
            return genSQLCodeWithEntityAnnotation(variableElement);
        else if (this.hasSimpleSQLAnnotation(variableElement))
            return this.genSQLWithCompatibleAnnotations(variableElement);
        else return this.genSQLWithoutAnnotations(variableElement);*/
        return "";
    }

    private String genSQLWithCompatibleAnnotations(VariableElement variableElement) {
        StringBuilder sql = new StringBuilder();

        if (this.hasAnnotation(variableElement, Column.class))
            sql.append(this.genSQLWithColumnAnnotation(variableElement));
        else sql.append(this.genSQLWithoutAnnotations(variableElement));

        if (this.hasAnnotation(variableElement, Unique.class))
            sql.insert(sql.length() -1, Unique.class.getSimpleName());
        if (this.hasAnnotation(variableElement, Null.class))
            sql.insert(sql.length() -1, Null.class.getSimpleName());
        if (this.hasAnnotation(variableElement, NotNull.class))
            sql.insert(sql.length() -1, NotNull.class.getSimpleName());

        return sql.toString();
    }

    private String genSQLWithoutAnnotations(VariableElement sqlCompatible) {
        String fieldName = sqlCompatible.getSimpleName().toString();
        String fieldType = this.SQLTypeOf(sqlCompatible);

        return String.format("%s %s,", fieldName, fieldType);
    }

    private String genSQLWithColumnAnnotation(VariableElement sqlCompatible) {
        String fieldName = sqlCompatible.getSimpleName().toString();
        String fieldType = this.SQLTypeOf(sqlCompatible);
        Column column = sqlCompatible.getAnnotation(Column.class);

        return String.format("%s %s,", column.name() != null ? column.name() : fieldName, fieldType);
    }

    private String genSQLCodeWithIdAnnotation(VariableElement sqlCompatible) {
        return String.format("%s %s PRIMARY KEY AUTOINCREMENT, ", sqlCompatible.getSimpleName(), SQLTypeOf(sqlCompatible));
    }

    private String genSQLCodeWithEntityAnnotation(VariableElement sqlCompatible) throws SQLGeneratorException {
        Optional<? extends Element> annotatedType = IdElementFromEmbeddedEntity(sqlCompatible);

        if (annotatedType.isEmpty())
            throw new SQLGeneratorException("Embedded entity is missing @Id field");

        String fieldName = sqlCompatible.getSimpleName().toString();
        String fieldType = this.SQLTypeOf(sqlCompatible);
        String referencedTableName = SQLTypeOf(sqlCompatible);
        String referencedFieldName = annotatedType.get().getSimpleName().toString();

        return String.format("%s %s, FOREIGN KEY (%s) REFERENCES %s(%s),", fieldName, fieldType, fieldName, referencedTableName, referencedFieldName);
    }

    private Optional<? extends Element> IdElementFromEmbeddedEntity(VariableElement variableElement) {
        return variableElement.getEnclosingElement().getEnclosedElements().stream().filter(e -> this.hasAnnotation((VariableElement) e, Id.class)).findFirst();
    }

    private String SQLTypeOf(VariableElement variableElement) {
        String type = variableElement.asType().toString();

        if (type.equals("java.lang.String")) {
            return "TEXT";
        } else if (type.equals("long") || type.equals("java.lang.Long")) {
            return "INTEGER";
        }

        TypeElement typeElement = processingEnvironment.getElementUtils().getTypeElement(type);
        return typeElement != null ? typeElement.getSimpleName().toString() : type;
    }

    private TypeElement typeElementFrom(VariableElement variableElement) {
        return (TypeElement) processingEnvironment.getTypeUtils().asElement(variableElement.asType());
    }

    private <T extends Annotation> boolean hasAnnotation(VariableElement variableElement, Class<T> annotation) {
        TypeElement typeElement = this.typeElementFrom(variableElement);
        return typeElement != null && typeElement.getAnnotation(annotation) != null;
    }

    /**
     * Checks if variableElement is annotated with either @Unique, @Null, @Column or @NotNull annotation
     * @param variableElement
     * @return
     */
    private boolean hasSimpleSQLAnnotation(VariableElement variableElement) {
        TypeElement typeElement = this.typeElementFrom(variableElement);

        return typeElement != null && (typeElement.getAnnotation(Unique.class) != null ||
                typeElement.getAnnotation(Null.class) != null ||
                typeElement.getAnnotation(NotNull.class) != null);
    }
}
