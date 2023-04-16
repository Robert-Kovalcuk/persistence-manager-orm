package sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices;

import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.Table;
import java.lang.annotation.Annotation;

public abstract class AnnotationToSQLService {
    protected final ProcessingEnvironment processingEnvironment;

    protected AnnotationToSQLService(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    /**
     *
     * @param variableElement element representing a field (of FIELD kind)
     * @return SQL-ready string in "name type constrains," format
     * @throws SQLGeneratorException
     */
    public abstract String generate(VariableElement variableElement) throws SQLGeneratorException;

    protected String toSQLType(VariableElement variableElement) {
        String type = variableElement.asType().toString();

        if (type.equals("java.lang.String")) {
            return "TEXT";
        } else if (type.equals("long") || type.equals("java.lang.Long")) {
            return "INTEGER";
        }

        TypeElement typeElement = processingEnvironment.getElementUtils().getTypeElement(type);
        return typeElement != null ? typeElement.getSimpleName().toString() : type;
    }

    public String withoutAnnotation(VariableElement variableElement) {
        String fieldName = variableElement.getSimpleName().toString();
        String fieldType = this.toSQLType(variableElement);

        return String.format("%s %s,", fieldName, fieldType);
    }

    public static <T extends Annotation> boolean hasAnnotation(VariableElement variableElement, Class<T> annotation) {
        return variableElement != null && variableElement.getAnnotation(annotation) != null;
    }

    public String nameOfEntity(VariableElement variableElement) {
        Element element = processingEnvironment.getTypeUtils().asElement(variableElement.asType());
        Table table = element.getAnnotation(Table.class);

        return table != null ? table.name() : element.getSimpleName().toString();
    }
}
