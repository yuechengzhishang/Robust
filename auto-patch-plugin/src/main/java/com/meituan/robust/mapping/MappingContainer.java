package com.meituan.robust.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hedex on 17/2/23.
 */

public class MappingContainer implements MappingProcessor {
    Map<String, ClassMapping> classMappings;

    Map<String,String> oldNameMap = new HashMap<>();
    Map<String,String> newNameMap = new HashMap<>();

    public MappingContainer(Map<String, ClassMapping> classMappings) {
        this.classMappings = classMappings;
    }

    @Override
    public boolean processClassMapping(String className, String newClassName) {
        oldNameMap.put(className,newClassName);
        newNameMap.put(newClassName,className);
        return true;
    }

    @Override
    public void processFieldMapping(String className, String fieldType, String fieldName, String newClassName, String newFieldName) {
        newClassName = oldNameMap.get(className);
        ClassMapping classMapping = classMappings.get(className);
        if (null == classMapping) {
            classMapping = new ClassMapping(className, newClassName);
            classMappings.put(className, classMapping);
        }
        classMapping.processFieldMapping(className, fieldType, fieldName, newClassName, newFieldName);
    }

    @Override
    public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newClassName, int newFirstLineNumber, int newLastLineNumber, String newMethodName) {
        newClassName = oldNameMap.get(className);
        ClassMapping classMapping = classMappings.get(className);
        if (null == classMapping) {
            classMapping = new ClassMapping(className, newClassName);
            classMappings.put(className, classMapping);
        }
        classMapping.processMethodMapping(className,
                firstLineNumber,
                lastLineNumber,
                methodReturnType,
                methodName,
                methodArguments,
                newClassName,
                newFirstLineNumber,
                newLastLineNumber,
                newMethodName);
    }
}
