package sk.tuke.meta.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.Table;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.logging.Logger;

@SupportedSourceVersion(SourceVersion.RELEASE_9)
public class TableSqlGenerationProcessor extends AbstractProcessor {
    private static final Logger logger = Logger.getLogger(TableSqlGenerationProcessor.class.getName());
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getRootElements();
        for (Element validatedElement : elements) {
            try {
                saveToStandardLocation("",validatedElement);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Cannot generate validator: " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    private boolean saveToStandardLocation(String sql, Element element) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject jfo = filer.createSourceFile(fullClassName(element));
        try (Writer writer = jfo.openWriter()) {
            writer.write("asdasd");
        }

        return false;
    }
    private String fullClassName(Element validatedEntity) {
        return validatedEntity.toString() + "Validator";
    }

}