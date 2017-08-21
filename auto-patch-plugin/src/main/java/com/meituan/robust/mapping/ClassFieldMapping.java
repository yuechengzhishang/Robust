package com.meituan.robust.mapping;

/**
 * 记录class的Field的mapping
 * Created by hedex on 17/2/23.
 */

public class ClassFieldMapping {
    String className;
    String fieldType;
    String fieldName;
    String newClassName;
    String newFieldName;

    public ClassFieldMapping(String className, String fieldType, String fieldName, String newClassName, String newFieldName) {
        this.className = className;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.newClassName = newClassName;
        this.newFieldName = newFieldName;
    }

    @Override
    public String toString() {
        return "    " + fieldType + " " + fieldName + " -> " + newFieldName;
    }
}
