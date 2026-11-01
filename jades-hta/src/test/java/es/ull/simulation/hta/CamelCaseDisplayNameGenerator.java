package es.ull.simulation.hta;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayNameGenerator;

public class CamelCaseDisplayNameGenerator extends DisplayNameGenerator.Standard {

    @Override
    public String generateDisplayNameForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass, Method testMethod) {
        String name = testMethod.getName();
        // Insertar espacios entre letras minúsculas y mayúsculas consecutivas (ejemplo básico)
        return splitCamelCase(name);
    }

    private String splitCamelCase(String s) {
        return s.replaceAll(String.format("%s|%s|%s",
           "(?<=[A-Z])(?=[A-Z][a-z])",
           "(?<=[^A-Z])(?=[A-Z])",
           "(?<=[A-Za-z])(?=[^A-Za-z])"
        ), " ");
    }
}