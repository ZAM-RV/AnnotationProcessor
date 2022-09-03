package com.sain.processor;

import com.sain.annotation.Factory;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

// Groups all FactoryAnnotatedClasses together
public class FactoryGroupedClasses {

    private String qualifiedClassName;

    private Map<String, FactoryAnnotatedClass> itemsMap = new LinkedHashMap<>();

    public FactoryGroupedClasses(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public void add(FactoryAnnotatedClass toInsert) throws ProcessingException {

        FactoryAnnotatedClass existing = itemsMap.get(toInsert.getId());
        if (existing != null) {

            // Already existing
            throw new ProcessingException(toInsert.getTypeElement(),
                    "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
                    toInsert.getTypeElement().getQualifiedName().toString(), Factory.class.getSimpleName(),
                    toInsert.getId(), existing.getTypeElement().getQualifiedName().toString());
        }

        itemsMap.put(toInsert.getId(), toInsert);
    }

    public void generateCode(Elements elementUtils, Filer file) throws IOException {

    }


}
