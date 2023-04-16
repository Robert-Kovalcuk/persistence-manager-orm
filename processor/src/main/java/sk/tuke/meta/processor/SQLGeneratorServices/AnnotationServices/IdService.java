package sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;

public class IdService extends AnnotationToSQLService {
    public IdService(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public String generate(VariableElement variableElement) {
        return String.format("%s %s PRIMARY KEY AUTOINCREMENT, ", variableElement.getSimpleName(), toSQLType(variableElement));
    }
}
