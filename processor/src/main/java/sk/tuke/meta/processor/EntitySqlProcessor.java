package sk.tuke.meta.processor;

import sk.tuke.meta.processor.SQLGeneratorServices.EntitySQLGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.persistence.Table;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("javax.persistence.Table")
@SupportedSourceVersion(SourceVersion.RELEASE_9)
public class EntitySqlProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element validatedElement : roundEnv.getElementsAnnotatedWith(Table.class)) {
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
        FileObject jfo = createResource(element);

        try (Writer writer = jfo.openWriter()) {
            writer.write(new EntitySQLGenerator(processingEnv).generateFrom(element));
        }
    }

    private FileObject createResource(Element element) throws IOException {
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "sql", this.fullClassName(element) + ".sql");
    }

    private String fullClassName(Element element) {
        return element.toString() + "TableCreateSQL";
    }

}