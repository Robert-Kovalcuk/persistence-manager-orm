package sk.tuke.meta.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public interface ProcessorTask {
    public void start(Element[] elements, RoundEnvironment processingEnvironment);
    public void start(Element elements, RoundEnvironment processingEnvironment);
}
