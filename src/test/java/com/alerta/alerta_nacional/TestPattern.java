package com.alerta.alerta_nacional;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.Resource;

public class TestPattern {
    public static void main(String[] args) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("file:input/images/*.png");
        System.out.println("Found " + resources.length + " resources.");
        for (Resource r : resources) {
            System.out.println(r.getFile().getAbsolutePath());
        }
    }
}
