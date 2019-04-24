package com.limalivy.modulebridge.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author linmin1 on 2019/4/22.
 */
public class BridgeProxyFileCreator {
    public static final String PROXY_SUFFIX = "$BridgeProxy";

    private TypeName targetName;
    private TypeName bridgeName;
    private List<ExecutableElement> methodElements;
    private Filer outFiler;

    private String packageName;
    private String className;

    private BridgeProxyFileCreator(TypeName targetName, TypeName bridgeName, List<ExecutableElement> methodElements, Filer outFiler) {
        this.targetName = targetName;
        this.bridgeName = bridgeName;
        this.methodElements = methodElements;
        this.outFiler = outFiler;
        String targetNameStr = bridgeName.toString();
        int index = targetNameStr.lastIndexOf(".");
        if (index > 0) {
            packageName = targetNameStr.substring(0, index);
            className = targetNameStr.substring(index + 1);
        } else {
            packageName = "";
            className = targetNameStr;
        }
    }

    public static BridgeProxyFileCreator newInstance(TypeName targetName,
                                                     TypeName bridgeName,
                                                     List<ExecutableElement> methodElements,
                                                     Filer outFiler) {
        return new BridgeProxyFileCreator(targetName, bridgeName, methodElements, outFiler);
    }

    public void makeFile() {

        CodeBlock staticBlock = CodeBlock.builder()
                .addStatement("isCreated = new AtomicInteger(0)").build();

        TypeSpec.Builder builder = TypeSpec.classBuilder(className + PROXY_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.addSuperinterface(bridgeName);
        builder.addField(targetName, "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE);
        builder.addField(TypeName.get(AtomicInteger.class), "isCreated", Modifier.PRIVATE, Modifier.STATIC, Modifier
                .FINAL);
        builder.addStaticBlock(staticBlock);
        for (MethodSpec ms : makeMethodList()) {
            builder.addMethod(ms);
        }
        builder.addMethod(makeOnCreateMethod());
        builder.addMethod(makeGetInstanceMethod());
        TypeSpec ts = builder.build();
        try {
            JavaFile.builder(packageName, ts).build().writeTo(outFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<MethodSpec> makeMethodList() {
        ArrayList<MethodSpec> methodList = new ArrayList<>();
        for (ExecutableElement eele : methodElements) {
            MethodSpec ms = makeMethod(eele);
            if (ms != null) {
                methodList.add(ms);
            }
        }
        return methodList;
    }

    private MethodSpec makeMethod(ExecutableElement eele) {
        Set<Modifier> modifiers = eele.getModifiers();
        if (modifiers.contains(Modifier.STATIC) ||
                modifiers.contains(Modifier.PRIVATE)) {
            return null;
        }

        TypeName returnType = TypeName.get(eele.getReturnType());
        List<String> paramNames = new ArrayList<>();
        String methodName = eele.getSimpleName().toString();
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
        builder.addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returnType);
        for (VariableElement vele : eele.getParameters()) {
            String paramName = vele.getSimpleName().toString();
            paramNames.add(paramName);
            builder.addParameter(TypeName.get(vele.asType()), paramName);
        }
        for (TypeMirror tm : eele.getThrownTypes()) {
            builder.addException(TypeName.get(tm));
        }
        builder.addCode(makeMethodBody(returnType, methodName, paramNames).toString());
        return builder.build();
    }

    private StringBuilder makeMethodBody(TypeName returnType, String methodName, List<String> paramNames) {
        StringBuilder sb = new StringBuilder();
        if (returnType != TypeName.VOID) {
            sb.append("    return ");
        } else {
            sb.append("    ");
        }
        sb.append("getInstance().")
                .append(methodName)
                .append("(");
        for (int i = 0, size = paramNames.size(); i < size; ++i) {
            if (i == size - 1) {
                sb.append(paramNames.get(i));
            } else {
                sb.append(paramNames.get(i)).append(", ");
            }
        }
        sb.append(");\n");
        return sb;
    }

    private MethodSpec makeOnCreateMethod() {
        return MethodSpec.methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .build();

    }

    private MethodSpec makeGetInstanceMethod() {
        return MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PRIVATE)
                .addModifiers(Modifier.STATIC)
                .returns(targetName)
                .addCode(createInstanceCode(className, targetName.toString()).toString())
                .build();
    }

    private StringBuilder createInstanceCode(String className, String targetClassName) {
        StringBuilder sb = new StringBuilder();
        //append new instance
        sb.append("if (instance == null) {\n")
                .append("    synchronized (")
                .append(className).append(".class) {\n")
                .append("        if (instance == null) {\n")
                .append("            instance = new ")
                .append(targetClassName)
                .append("();\n")
                .append("        }\n")
                .append("    }\n")
                .append("}\n");
        // append call onCreate
        sb.append("while (true) {\n")
                .append("    if (isCreated.get() == 2) {\n")
                .append("        break;\n")
                .append("    }\n")
                .append("    if (isCreated.compareAndSet(0, 1)) {\n")
                .append("        instance.onCreate();\n")
                .append("        isCreated.set(2);\n")
                .append("        break;\n")
                .append("    }\n")
                .append("    Thread.yield();\n")
                .append("}\n");
        // append return
        sb.append("return instance;\n");
        return sb;
    }
}
