package net.paoding.rose.jade.jadeinterface.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ModifierTests extends TestCase {

    public void testModifier() {

        Definition definition = new Definition(Character.class);
        Assert.assertEquals("java.lang.Character", definition.toString());

        Method[] methods = Character.class.getMethods();
        for (Method method : methods) {

            Modifier modifier = new Modifier(definition, method);
            Annotation annotation = modifier.getAnnotation(Deprecated.class);
            Annotation[] annotations = modifier.getParameterAnnotations(Deprecated.class);

            System.out.println(modifier);
            System.out.println("ReturnType: " + modifier.getReturnType());
            System.out.println("GenericReturnType: " + modifier.getGenericReturnType());
            System.out.println("@Deprecated Annotation: " + annotation);
            System.out.println("ParameterAnnotations: " + Arrays.toString(annotations));
            System.out.println();

            Assert.assertEquals("java.lang.Character#" + method.getName(), // NL
                    modifier.toString());

            if (method.isAnnotationPresent(Deprecated.class)) {
                Assert.assertNotNull(annotation);
                Assert.assertEquals(Deprecated.class, annotation.annotationType());
            }

            Assert.assertEquals(Deprecated.class, annotations.getClass().getComponentType());
            Assert.assertEquals(0, annotations.length);
        }
    }
}
