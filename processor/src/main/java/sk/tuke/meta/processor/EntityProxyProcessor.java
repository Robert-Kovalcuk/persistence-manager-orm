package sk.tuke.meta.processor;

import sk.tuke.meta.processor.ProxyGeneratorServices.ProxyGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;


@SupportedAnnotationTypes("javax.persistence.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_9)
public class EntityProxyProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations.isEmpty())
            return false;
        for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString() + "Proxy";
            String packageName = "Proxy";
            try {
                JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className, element);
                Writer writer = file.openWriter();
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.append("package " + packageName + ";\n\n");
                bufferedWriter.append("public class " + className + " {\n\n");
                bufferedWriter.append("    private " + typeElement.getQualifiedName() + " delegate;\n\n");
                bufferedWriter.append("    public " + className + "(" + typeElement.getQualifiedName() + " delegate) {\n");
                bufferedWriter.append("        this.delegate = delegate;\n");
                bufferedWriter.append("    }\n\n");
                for (Element enclosedElement : typeElement.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.METHOD) {
                        String returnType = enclosedElement.asType().toString();
                        String methodName = enclosedElement.getSimpleName().toString();
                        StringBuilder parameters = new StringBuilder();
                        StringBuilder arguments = new StringBuilder();
                        for (VariableElement parameter : ((ExecutableElement) enclosedElement).getParameters()) {
                            String parameterType = parameter.asType().toString();
                            String parameterName = parameter.getSimpleName().toString();
                            parameters.append(parameterType + " " + parameterName + ", ");
                            arguments.append(parameterName + ", ");
                        }
                        if (parameters.length() > 0) {
                            parameters.delete(parameters.length() - 2, parameters.length());
                            arguments.delete(arguments.length() - 2, arguments.length());
                        }
                        bufferedWriter.append("    public " + returnType + " " + methodName + "(" + parameters.toString() + ") {\n");
                        bufferedWriter.append("        // custom code here\n");
                        bufferedWriter.append("        return delegate." + methodName + "(" + arguments.toString() + ");\n");
                        bufferedWriter.append("    }\n\n");
                    }
                }
                bufferedWriter.append("}\n");
                bufferedWriter.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void generateProxy(Element element) throws IOException {
        FileObject jfo = this.createResource(element);

        try (Writer writer = jfo.openWriter()) {
            writer.write(new ProxyGenerator(processingEnv).generate(element));
        }
    }

    private FileObject createResource(Element element) throws IOException {
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "Proxy", fileNameFrom(element) + ".java");
    }

    private static String fileNameFrom(Element element) {
        Table table = element.getAnnotation(Table.class);
        return table != null ? table.name() : element.getSimpleName().toString();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }
}
