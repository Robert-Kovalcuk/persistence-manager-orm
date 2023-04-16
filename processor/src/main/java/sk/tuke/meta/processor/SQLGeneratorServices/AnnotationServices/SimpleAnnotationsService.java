package sk.tuke.meta.processor.SQLGeneratorServices.AnnotationServices;

import sk.tuke.meta.annotation.NotNull;
import sk.tuke.meta.annotation.Null;
import sk.tuke.meta.annotation.Unique;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.persistence.Column;

public class SimpleAnnotationsService extends AnnotationToSQLService {

    ColumnService columnService = new ColumnService(super.processingEnvironment);
    public SimpleAnnotationsService(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public String generate(VariableElement variableElement) {
        StringBuilder SQL = new StringBuilder();

        if (hasAnnotation(variableElement, Column.class))
            SQL.append(this.columnService.generate(variableElement));
        else SQL.append(super.withoutAnnotation(variableElement));

        if (hasAnnotation(variableElement, Unique.class))
            SQL.insert(SQL.length() -1, uniqueToStr());
        if (hasAnnotation(variableElement, Null.class))
            SQL.insert(SQL.length() -1, nullToStr());
        if (hasAnnotation(variableElement, NotNull.class))
            SQL.insert(SQL.length() -1, notNullToStr());

        return SQL.toString();
    }

    private static String uniqueToStr() {return " " + Unique.class.getSimpleName().toUpperCase() + " ";}
    private static String nullToStr() {return " " + Null.class.getSimpleName().toUpperCase() + " ";}
    private static String notNullToStr() {return " " + new StringBuilder(NotNull.class.getSimpleName().toUpperCase()).insert(3, " ") + " ";}
}
