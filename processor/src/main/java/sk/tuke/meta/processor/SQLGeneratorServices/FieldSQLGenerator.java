package sk.tuke.meta.processor.SQLGeneratorServices;


import sk.tuke.meta.annotation.NotNull;
import sk.tuke.meta.annotation.Null;
import sk.tuke.meta.annotation.Unique;
import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;
import sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices.AnnotationToSQLService;
import sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices.IdService;
import sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices.ManyToOneService;
import sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices.SimpleAnnotationsService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class FieldSQLGenerator implements SQLGenerator<VariableElement> {
    private final ManyToOneService manyToOneService;
    private final IdService idService;
    private final SimpleAnnotationsService simpleAnnotationsService;

    public FieldSQLGenerator(ProcessingEnvironment processingEnv) {
        super();

        this.manyToOneService = new ManyToOneService(processingEnv);
        this.idService = new IdService(processingEnv);
        this.simpleAnnotationsService = new SimpleAnnotationsService(processingEnv);
    }

    @Override
    public String generate(VariableElement el) throws SQLGeneratorException {
        if (AnnotationToSQLService.hasAnnotation(el, Id.class))
            return idService.generate(el);

        else if (AnnotationToSQLService.hasAnnotation(el, ManyToOne.class))
            return manyToOneService.generate(el);

        else if (hasSimpleSQLAnnotation(el))
            return this.simpleAnnotationsService.generate(el);

        else
            return simpleAnnotationsService.withoutAnnotation(el);
    }

    /**
     * Checks if variableElement is annotated with either @Unique, @Null, @Column or @NotNull annotation
     * @param variableElement representing a field
     * @return true if element has either Unique, Null or NotNull annotation false otherwise
     */
    private static boolean hasSimpleSQLAnnotation(VariableElement variableElement) {
        return variableElement != null && (variableElement.getAnnotation(Unique.class) != null ||
               variableElement.getAnnotation(Null.class) != null ||
               variableElement.getAnnotation(NotNull.class) != null ||
                variableElement.getAnnotation(Column.class) != null
        );
    }
}
