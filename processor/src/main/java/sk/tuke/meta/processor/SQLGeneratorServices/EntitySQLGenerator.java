package sk.tuke.meta.processor.SQLGeneratorServices;

import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;
import sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices.AnnotationToSQLService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.persistence.Table;
import java.util.stream.Stream;

public class EntitySQLGenerator implements SQLGenerator<Element> {

    private final FieldSQLGenerator fieldSQLGenerator;
    public EntitySQLGenerator(ProcessingEnvironment processingEnv) {
        this.fieldSQLGenerator = new FieldSQLGenerator(processingEnv);
    }

    @Override
    public String generate(Element el) {
        return String.format("CREATE TABLE IF NOT EXISTS %s(%s);",nameOf(el), this.genSQLFromEnclosedElements(el));
    }

    private String genSQLFromEnclosedElements(Element element) {
        StringBuilder SQL = new StringBuilder();

        streamOfFieldKindEnclosedElements(element).forEach(fieldKind -> {
            try {
                SQL.append(this.fieldSQLGenerator.generate((VariableElement) fieldKind));
            } catch (SQLGeneratorException e) {
                throw new RuntimeException(e);
            }
        });

        SQL.deleteCharAt(SQL.length() - 1); // removes last comma
        return SQL.toString();
    }

    private static Stream<? extends Element> streamOfFieldKindEnclosedElements(Element element) {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.FIELD);
    }

    private static String nameOf(Element element) {
        Table tableAnnotation = element.getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : element.getSimpleName().toString();
    }
}
