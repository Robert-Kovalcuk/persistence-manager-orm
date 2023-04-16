package sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices;

import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.persistence.Id;
import java.util.Optional;

public class ManyToOneService extends AnnotationToSQLService {
    public ManyToOneService(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public String generate(VariableElement variableElement) throws SQLGeneratorException {
        Optional<? extends Element> annotatedType = IdElementFromEmbeddedEntity(variableElement);

        if (annotatedType.isEmpty())
            throw new SQLGeneratorException("Embedded entity is missing @Id field");

        String fieldName = variableElement.getSimpleName().toString();
        String fieldType = this.toSQLType(variableElement);
        String referencedTableName = super.nameOfEntity(variableElement);
        String referencedFieldName = annotatedType.get().getSimpleName().toString();

        return String.format("%s %s, FOREIGN KEY (%s) REFERENCES %s(%s),", fieldName, fieldType, fieldName, referencedTableName, referencedFieldName);
    }

    private Optional<? extends Element> IdElementFromEmbeddedEntity(VariableElement variableElement) {
        return super.processingEnvironment.getTypeUtils().asElement(variableElement.asType()).getEnclosedElements()
                .stream().filter(e -> e.getAnnotation(Id.class) != null).findFirst();
    }
}
