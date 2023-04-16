package sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.persistence.Column;

public class ColumnService extends AnnotationToSQLService {
    public ColumnService(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public String generate(VariableElement variableElement) {
        String fieldName = variableElement.getSimpleName().toString();
        String fieldType = this.toSQLType(variableElement);
        Column column = variableElement.getAnnotation(Column.class);

        return String.format("%s %s,", column.name() != null ? column.name() : fieldName, fieldType);
    }
}
