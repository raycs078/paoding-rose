package net.paoding.rose.web.paramresolver;

import java.util.Date;

import net.paoding.rose.web.paramresolver.ResolverFactoryImpl.DateEditor;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;

public class ThreadSafedSimpleTypeConverter implements TypeConverter {

    @SuppressWarnings("unchecked")
    @Override
    public Object convertIfNecessary(Object value, Class requiredType) throws TypeMismatchException {
        return simpleTypeConverters.get().convertIfNecessary(value, requiredType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object convertIfNecessary(Object value, Class requiredType, MethodParameter methodParam)
            throws TypeMismatchException {
        return simpleTypeConverters.get().convertIfNecessary(value, requiredType, methodParam);
    }

    ThreadLocal<SimpleTypeConverter> simpleTypeConverters = new ThreadLocal<SimpleTypeConverter>() {

        @Override
        protected SimpleTypeConverter initialValue() {
            return createSimpleTypeConverter();
        }
    };

    public SimpleTypeConverter getSimpleTypeConverter() {
        return simpleTypeConverters.get();
    }

    protected SimpleTypeConverter createSimpleTypeConverter() {
        // simpleTypeConverter is not for concurrency!
        SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();
        simpleTypeConverter.useConfigValueEditors();
        simpleTypeConverter.registerCustomEditor(Date.class, new DateEditor(Date.class));
        simpleTypeConverter.registerCustomEditor(java.sql.Date.class, new DateEditor(
                java.sql.Date.class));
        simpleTypeConverter.registerCustomEditor(java.sql.Time.class, new DateEditor(
                java.sql.Time.class));
        simpleTypeConverter.registerCustomEditor(java.sql.Timestamp.class, new DateEditor(
                java.sql.Timestamp.class));
        return simpleTypeConverter;
    }
}
