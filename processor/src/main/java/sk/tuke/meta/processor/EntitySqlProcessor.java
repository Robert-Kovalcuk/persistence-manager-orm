package sk.tuke.meta.processor;

import sk.tuke.meta.processor.SQLGeneratorServices.EntitySQLGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("javax.persistence.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_9)
public class EntitySqlProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations.isEmpty())
            return true;

        for (Element validatedElement : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            try {
                this.generateAndSaveSQL(validatedElement);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot generate SQL file: " + e.getMessage());
            }
        }

        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    private void generateAndSaveSQL(Element element) throws IOException {
        FileObject jfo = this.createResource(element);

        try (Writer writer = jfo.openWriter()) {
            writer.write(new EntitySQLGenerator(processingEnv).generate(element));
        }
    }

    private FileObject createResource(Element element) throws IOException {
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "SQL", fileNameFrom(element) + ".sql");
    }

    private static String fileNameFrom(Element element) {
        Table table = element.getAnnotation(Table.class);
        return table != null ? table.name() : element.getSimpleName().toString();
    }

}