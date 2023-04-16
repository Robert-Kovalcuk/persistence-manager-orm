package sk.tuke.meta.processor.SQLGeneratorServices;

import sk.tuke.meta.processor.Exceptions.SQLGeneratorException;

import javax.lang.model.element.VariableElement;

public interface SQLGenerator<T> {
    /**
     *
     * @param el sql-compatible element
     * @return SQL-compatible string
     * @throws SQLGeneratorException
     */
    public String generate(T el) throws SQLGeneratorException;
}
