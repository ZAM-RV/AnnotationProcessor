package com.sain.processor;

import com.google.auto.service.AutoService;
import com.sain.annotation.Factory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types typeUtils; // Types: A utils class to work with TypeMirror
    private Elements elementUtils; // Elements: Element represents a program element such as a package, class, or method.
    private Filer filer; // Filer: Like the name suggests with Filer you can create files.
    private Messager messager;
    private Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Factory.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Iterate over all @Factory annotated elements

        for (Element annotated : roundEnv.getElementsAnnotatedWith(Factory.class)) {

            // Checks if the element annotation with Factory is a class
            if (annotated.getKind() != ElementKind.CLASS) {

                error(annotated, "Only classes annotatated with @%s",
                        Factory.class.getSimpleName());

                return true;
            }

            TypeElement typeElement = (TypeElement) annotated;

            try {
                FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement, messager);

                if (!isValidClass(annotatedClass)) {
                    return true;
                }

                FactoryGroupedClasses factoryClass = factoryClasses.get(annotatedClass.getQualifiedSuperClassName());

                if (factoryClass == null) {
                    String qualifiedGroupName = annotatedClass.getQualifiedSuperClassName();
                    factoryClass = new FactoryGroupedClasses(qualifiedGroupName);
                    factoryClasses.put(qualifiedGroupName, factoryClass);
                }

                factoryClass.add(annotatedClass);
            } catch (IllegalArgumentException | ProcessingException e) {
                // @Factory.id() is empty
                error(typeElement, e.getMessage());
                return true;
            }

        }
        try {
            for (FactoryGroupedClasses factoryClass: factoryClasses.values()) {
                factoryClass.generateCode(elementUtils, filer);

                factoryClasses.clear();
            }
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "HELLLOOOOOO!!!!!!");
        return true;
    }

    private boolean isValidClass(FactoryAnnotatedClass factoryAnnotatedClass) {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = factoryAnnotatedClass.getTypeElement();

        if(!classElement.getModifiers().contains(Modifier.PUBLIC)){
            error(classElement, "The class %s is not public", classElement.getQualifiedName().toString());
            return false;
        }

        // Check if it's an abstract class
        if(classElement.getModifiers().contains(Modifier.ABSTRACT)){
            error(classElement, "The class %s is abstract, you can't annotate abstract classes with @%", classElement.getQualifiedName().toString(), Factory.class.getSimpleName());
            return false;
        }

        // This checks that the class that we has implments that interface. Essentially two things are done
        // 1. Check if the type used in the annotation is indeed an interface
        // 2. Checks if the class that we have extends that interface
        TypeElement superClassElement = elementUtils.getTypeElement(factoryAnnotatedClass.getQualifiedSuperClassName());

        // This then checks if it was indeed an interface which was used
        if(superClassElement.getKind() == ElementKind.INTERFACE) {

            if(!classElement.getInterfaces().contains(superClassElement.asType())) {
                error(classElement, "The class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        factoryAnnotatedClass.getQualifiedSuperClassName());
                return false;
            }
        } else {
            // Where in the if statement we check if it implements an interface. Here we check that our current class is a subclass of the class that was set as type in the annotation

            TypeElement currentClass = classElement;

            while (true) {

                TypeMirror superClassType = currentClass.getSuperclass();

                if (superClassType.getKind() == TypeKind.NONE) {
                    // Basis class (java.lang.Object) reached, so exit
                    error(classElement, "The class %s annotated with @%s must inherit from %s",
                            classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                            factoryAnnotatedClass.getQualifiedSuperClassName());
                    return false;
                }

                if (superClassType.toString().equals(factoryAnnotatedClass.getQualifiedSuperClassName())) {
                    // Required super class found. Break
                    break;
                }

                currentClass = (TypeElement) typeUtils.asElement(superClassType);
            }
            }

            // Check if an empty public constructor is given
            for (Element enclosed : classElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement constructorElement = (ExecutableElement) enclosed;
                    if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                            .contains(Modifier.PUBLIC)) {
                        // Found an empty constructor
                        return true;
                    }
                }
            }

            // No empty constructor found
            error(classElement, "The class %s must provide an public empty default constructor",
                    classElement.getQualifiedName().toString());
            return false;
        }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e
        );
    }
}
