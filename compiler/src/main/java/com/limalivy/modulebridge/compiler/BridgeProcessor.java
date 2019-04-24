package com.limalivy.modulebridge.compiler;

import com.google.auto.service.AutoService;
import com.limalivy.modulebridge.runtime.BridgeTarget;
import com.limalivy.modulebridge.runtime.IBridge;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * @author linmin1 on 2019/4/22.
 */
@AutoService(Processor.class)
public class BridgeProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return true;
        }

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BridgeTarget.class);
        if (elements == null || elements.isEmpty()) {
            return true;
        }
        for (Element ele : elements) {
            if (ele.getKind() == ElementKind.CLASS) {
                analysisBridgeTarget(ele);
            }
        }
        return true;
    }

    private void analysisBridgeTarget(Element element) {
        TypeName targetName = TypeName.get(element.asType());
        BridgeTarget annotation = element.getAnnotation(BridgeTarget.class);
        try {
            annotation.value();
        } catch (MirroredTypesException mte) {
            for (TypeMirror typeMirror : mte.getTypeMirrors()) {
                DeclaredType classTypeMirror = (DeclaredType) typeMirror;
                TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
                if (classTypeElement.getKind() != ElementKind.INTERFACE) {
                    throw new BridgeCompilperException();
                }
                List<ExecutableElement> methodElements = new ArrayList<>();
                readClassMethodInfo(methodElements, classTypeElement);
                TypeName qualifiedSuperClassName = TypeName.get(classTypeElement.asType());
                BridgeProxyFileCreator
                        .newInstance(targetName, qualifiedSuperClassName, methodElements, filer)
                        .makeFile();
            }
        }
    }

    private void readClassMethodInfo(List<ExecutableElement> methodElements, TypeElement classTypeElement) {
        String className = classTypeElement.getQualifiedName().toString();
        if (className.equals(IBridge.class.getCanonicalName()) ||
                className.equals(Object.class.getCanonicalName())) {
            return;
        }
        for (Element ele : classTypeElement.getEnclosedElements()) {
            if (ele instanceof ExecutableElement) {
                methodElements.add((ExecutableElement) ele);
            }
        }
        for (TypeMirror m : classTypeElement.getInterfaces()) {
            readClassMethodInfo(methodElements,
                    (TypeElement) ((DeclaredType) m).asElement());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationNameSet = new HashSet<>();
        supportedAnnotationNameSet.add(BridgeTarget.class.getCanonicalName());
        return supportedAnnotationNameSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args));
    }

    private void info(String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.NOTE,
                String.format(msg, args));
    }
}
