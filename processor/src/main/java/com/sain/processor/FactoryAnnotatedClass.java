package com.sain.processor;

import com.sain.annotation.Factory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

public class FactoryAnnotatedClass {

    private TypeElement annotatedClassElement;
    private String qualifiedSuperClassName;
    private String simpleTypeName;
    private String id;

    public FactoryAnnotatedClass(TypeElement classElement) throws IllegalArgumentException{
        this.annotatedClassElement = classElement;
        Factory annotation = classElement.getAnnotation(Factory.class);
        id = annotation.id();

        if(id == null || id.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("id() in @%s for class %s is null or empty! that's not allowed",
                            Factory.class.getSimpleName(), classElement.getQualifiedName().toString()));

        }

        /**
         * Since annotation processing runs before compiling java source code we have to consider two cases:
         */

        try {
            /**
             * The class is already compiled: This is the case if a third party .jar contains compiled .class files with @Factory annotations.
             * In that case we can directly access the class like we do in the try-block.
             */
            Class<?> clazz = annotation.type();
            qualifiedSuperClassName = clazz.getCanonicalName();
            simpleTypeName = clazz.getSimpleName();
        } catch (MirroredTypeException mte) {
            /**
             * The class is not compiled yet: This will be the case if we try to compile our source code which has @Factory annotations.
             * Trying to access the class directly throws a MirroredTypeException.
             * Fortunately MirroredTypeException contains a TypeMirror representation of our not yet compiled class.
             * Since we know that it must be type of class (we have already checked that before) we can cast it to DeclaredType and access TypeElement to read the qualified name.
             */
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }

    /**
     * The original element that was annotated with @Factory
     */
    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }

    /**
     * Get the full qualified name of the type specified in  {@link Factory#type()}.
     *
     * @return qualified name
     */
    public String getQualifiedSuperClassName() {
        return qualifiedSuperClassName;
    }

    /**
     * Get the simple name of the type specified in  {@link Factory#type()}.
     *
     * @return qualified name
     */
    public String getSimpleTypeName() {
        return simpleTypeName;
    }

    /**
     * Get the id as specified in {@link Factory#id()}.
     * return the id
     */
    public String getId() {
        return id;
    }
}
