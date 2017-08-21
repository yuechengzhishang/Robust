package com.meituan.robust.mapping;

/**
 * Created by hedex on 17/2/23.
 */

public class MappingPrinter implements MappingProcessor{
    @Override
    public boolean processClassMapping(String className, String newClassName) {
        return true;
    }

    @Override
    public void processFieldMapping(String className, String fieldType, String fieldName, String newClassName, String newFieldName) {

    }

    @Override
    public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newClassName, int newFirstLineNumber, int newLastLineNumber, String newMethodName) {

    }
}
