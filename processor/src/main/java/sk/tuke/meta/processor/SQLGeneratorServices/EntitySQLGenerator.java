package sk.tuke.meta.processor.SQLGeneratorServices;

import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.persistence.Table;

public class EntitySQLGenerator implements ISQLGenerator<Element> {

    private final FieldSQLGenerator fieldSQLGenerator;
    public EntitySQLGenerator(ProcessingEnvironment processingEnv) {
        this.fieldSQLGenerator = new FieldSQLGenerator(processingEnv);
    }

    @Override
    public String generateFrom(Element element) {
        return String.format("CREATE TABLE IF NOT EXISTS %s(%s);", this.nameOf(element), this.genSQLFromEnclosedElements(element));
    }

    private String genSQLFromEnclosedElements(Element element) {
        StringBuilder stringBuilder = new StringBuilder();

        element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .forEach(field -> {
                    try {
                        stringBuilder.append(this.genSQLFromVariableElement((VariableElement) field));
                    } catch (SQLGeneratorException e) {
                        throw new RuntimeException(e);
                    }
                });

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private String genSQLFromVariableElement(VariableElement variableElement) throws SQLGeneratorException {
        return this.fieldSQLGenerator.generateFrom(variableElement);
    }

    private String nameOf(Element element) {
        Table tableAnnotation = element.getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : element.getSimpleName().toString();
    }
}
