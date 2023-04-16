package sk.tuke.meta.processor.ProxyGeneratorServices;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProxyGenerator {

    private ProcessingEnvironment processingEnv;

    public ProxyGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public String generate(Element element) {
        StringBuilder SQL = new StringBuilder();

        streamOfMethodKindEnclosedElements(element).forEach(methodKind -> {
            //TODO generate method string
            SQL.append("");
        });

        return SQL.toString();
    }


    private static Stream<? extends Element> streamOfMethodKindEnclosedElements(Element element) {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD);
    }

    private static String nameOf(Element element) {
        Table tableAnnotation = element.getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : element.getSimpleName().toString();
    }

    private String methodKindToString(Element element) {
        return  element.getModifiers().toString() + " " + element.getEnclosedElements().toString();
    }
}
