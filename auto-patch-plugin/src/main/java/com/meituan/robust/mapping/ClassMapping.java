package com.meituan.robust.mapping;

import java.util.LinkedList;
import java.util.List;

/**
 * 记录class的mapping
 * Created by hedex on 17/2/23.
 */

public class ClassMapping {
    String className;
    String newClassName;
    List<ClassFieldMapping> fieldMappings;
    List<ClassMethodMapping> methodMappings;

    public ClassMapping(String className, String newClassName) {
        this.className = className;
        this.newClassName = newClassName;
        this.fieldMappings = new LinkedList<>();
        this.methodMappings = new LinkedList<>();
    }

    public void processFieldMapping(String className, String fieldType, String fieldName, String newClassName, String newFieldName) {
        ClassFieldMapping fieldMapping = new ClassFieldMapping(className, fieldType, fieldName, newClassName, newFieldName);
        fieldMappings.add(fieldMapping);
    }

    public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newClassName, int newFirstLineNumber, int newLastLineNumber, String newMethodName) {
        ClassMethodMapping methodMapping = new ClassMethodMapping(className, firstLineNumber, lastLineNumber, methodReturnType, methodName, methodArguments, newClassName, newFirstLineNumber, newLastLineNumber, newMethodName);
        methodMappings.add(methodMapping);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(""+className + " -> " + newClassName + "\n");
        builder.append("  "+"field:"+ "\n");
        for (ClassFieldMapping fieldMapping:fieldMappings) {
            builder.append(fieldMapping.toString() + "\n");
        }
        builder.append("  "+"method:"+ "\n");
        for (ClassMethodMapping methodMapping:methodMappings){
            builder.append(methodMapping.toString() + "\n");
        }
        builder.append("\n");
        return builder.toString();
    }
}
