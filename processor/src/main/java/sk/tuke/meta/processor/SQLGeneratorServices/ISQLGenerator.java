package sk.tuke.meta.processor.SQLGeneratorServices;

import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;

public interface ISQLGenerator<T> {
    public String generateFrom(T sqlCompatible) throws SQLGeneratorException;
}
