package sk.tuke.meta.processor.SQLGeneratorServices;


import sk.tuke.meta.annotation.NotNull;
import sk.tuke.meta.annotation.Null;
import sk.tuke.meta.annotation.Unique;
import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public class FieldSQLGenerator implements ISQLGenerator<VariableElement> {
    ProcessingEnvironment processingEnvironment;

    public FieldSQLGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnvironment = processingEnv;
    }

    @Override
    public String generateFrom(VariableElement variableElement) throws SQLGeneratorException {
        if (this.isAnnotatedWith(variableElement, Id.class)) {
            return genSQLCodeWithIdAnnotation(variableElement);
        } else if (this.isAnnotatedWith(variableElement, Entity.class)) {
            return genSQLCodeWithEntityAnnotation(variableElement);
        } else if (this.hasSQLCompatibleFieldAnnotation(variableElement)) {

        }
    }

    private String test(VariableElement variableElement) throws ClassNotFoundException {
        StringBuilder sql = new StringBuilder();
        sql.append(this.genSQLCode(variableElement));
        sql.deleteCharAt(sql.length());
        List<? extends AnnotationMirror> compatibleAnnotations = this.typeElementFrom(variableElement).getAnnotationMirrors().stream().filter(
            annotationMirror -> {
                try {
                    return this.hasSQLCompatibleFieldAnnotation(Class.forName(annotationMirror.getAnnotationType().asElement().asType().toString()).getDeclaringClass());
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }).toList();

        for (AnnotationMirror annotationMirror : compatibleAnnotations) {
            String annotationType = annotationMirror.getAnnotationType().asElement().asType().toString();
            Class<?> annotationClass = Class.forName(annotationType).getDeclaringClass();

            if (this.isAnnotatedWith(variableElement, Unique.class))
                sql.append(this.genSQLCodeWithAnnotation(variableElement, annotationClass));
            if (this.isAnnotatedWith(variableElement, Null.class))
                sql.append(this.genSQLCodeWithAnnotation(variableElement, Null.class));
            if (this.isAnnotatedWith(variableElement, NotNull.class))
                sql.append(this.genSQLCodeWithAnnotation(variableElement, NotNull.class));
        }

        return sql.toString();
    }

    private String genSQLCode(VariableElement sqlCompatible) {
        String fieldName = sqlCompatible.getSimpleName().toString();
        String fieldType = this.getType(sqlCompatible);

        return String.format("%s %s,", fieldName, fieldType);
    }

    private String genSQLCodeWithIdAnnotation(VariableElement sqlCompatible) {
        return String.format("%s %s PRIMARY KEY AUTOINCREMENT, ", sqlCompatible.getSimpleName(), getType(sqlCompatible));
    }

    private String genSQLCodeWithEntityAnnotation(VariableElement sqlCompatible) throws SQLGeneratorException {
        Optional<? extends Element> annotatedType = IdElementFromEmbeddedEntity(sqlCompatible);

        if (annotatedType.isEmpty())
            throw new SQLGeneratorException("Embedded entity is missing @Id field");

        String fieldName = sqlCompatible.getSimpleName().toString();
        String fieldType = this.getType(sqlCompatible);
        String referencedTableName = getType(sqlCompatible);
        String referencedFieldName = annotatedType.get().getSimpleName().toString();

        return String.format("%s %s, FOREIGN KEY (%s) REFERENCES %s(%s),", fieldName, fieldType, fieldName, referencedTableName, referencedFieldName);
    }

    private <T extends Annotation> String genSQLCodeWithAnnotation(VariableElement variableElement, Class<?> annotation) {
        String fieldName = variableElement.getSimpleName().toString();
        String fieldType = this.getType(variableElement);

        return String.format("%s %s %s,", fieldName, fieldType, annotation.getSimpleName());
    }

    private Optional<? extends Element> IdElementFromEmbeddedEntity(VariableElement variableElement) {
        return variableElement.getEnclosingElement().getEnclosedElements().stream().filter(e -> this.isAnnotatedWith((VariableElement) e, Id.class)).findFirst();
    }

    private String getType(VariableElement variableElement) {
        String type = variableElement.asType().toString();

        if (type.equals("java.lang.String")) {
            return "TEXT";
        } else if (type.equals("long") || type.equals("java.lang.Long")) {
            return "INTEGER";
        }

        TypeElement typeElement = processingEnvironment.getElementUtils().getTypeElement(type);
        return typeElement != null ? typeElement.getSimpleName().toString() : type;
    }

    private <T extends Annotation> boolean isAnnotatedWith(VariableElement variableElement, Class<T> annotation) {
        TypeElement typeElement = this.typeElementFrom(variableElement);
        return typeElement != null && typeElement.getAnnotation(annotation) != null;
    }

    private boolean hasSQLCompatibleFieldAnnotation(Class<?> variableElement) {
        TypeElement typeElement = this.typeElementFrom(variableElement);

        return typeElement != null && (typeElement.getAnnotation(Unique.class) != null ||
                typeElement.getAnnotation(Null.class) != null ||
                typeElement.getAnnotation(NotNull.class) != null);
    }

    private TypeElement typeElementFrom(VariableElement variableElement) {
        return (TypeElement) processingEnvironment.getTypeUtils().asElement(variableElement.asType());
    }
}
