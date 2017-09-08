package com.meituan.sample.test;

import android.util.Log;

/**
 * Created by huangyongzheng on 17/8/30.
 */

public class BadClass {
    enum PatchType {
        MODIFY_METHOD_PRIVATE, MODIFY_METHOD_DEFAULT, MODIFY_METHOD_PROTECTED, MODIFY_METHOD_PUBLIC, MODIFY_METHOD_MODIFIER_STATIC, MODIFY_METHOD_MODIFIER_FINAL,
        MODIFY_METHOD_PRAM_PRIMITIVE_TYPE, MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE, MODIFY_METHOD_PRAM_BOOLEAN, MODIFY_METHOD_PRAM_ARRAY, /*MODIFY_METHOD_PRAM_NONE, MODIFY_METHOD_PRAM_ONE, MODIFY_METHOD_PRAM_MULTI,*/ MODIFY_METHOD_PRAM_VARIABLE_LENGTH,
        /*MODIFY_CONSTRUCTOR_PRAM_PRIMITIVE_TYPE, MODIFY_CONSTRUCTOR_PRAM_WRAPPER_PRIMITIVE_TYPE, MODIFY_CONSTRUCTOR_PRAM_BOOLEAN,*/
        MODIFY_CONSTRUCTOR_PRAM_ARRAY, MODIFY_CONSTRUCTOR_PRAM_NONE, MODIFY_CONSTRUCTOR_PRAM_ONE, MODIFY_CONSTRUCTOR_PRAM_MULTI, MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH,
        MODIFY_FIELD_PRIVATE, MODIFY_FIELD_DEFAULT, MODIFY_FIELD_PROTECTED, MODIFY_FIELD_PUBLIC, MODIFY_FIELD_STATIC, MODIFY_FIELD_FINAL,
        ADD_METHOD_PRIVATE, ADD_METHOD_DEFAULT, ADD_METHOD_PROTECTED, ADD_METHOD_PUBLIC, ADD_METHOD_MODIFIER_STATIC, ADD_METHOD_MODIFIER_FINAL,
        ADD_METHOD_PRAM_PRIMITIVE_TYPE, ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE, ADD_METHOD_PRAM_BOOLEAN, ADD_METHOD_PRAM_ARRAY, /*ADD_METHOD_PRAM_NONE, ADD_METHOD_PRAM_ONE, ADD_METHOD_PRAM_MULTI,*/ ADD_METHOD_PRAM_VARIABLE_LENGTH,
        /*ADD_CONSTRUCTOR_PRAM_ARRAY, *//*ADD_CONSTRUCTOR_PRAM_NONE,*//* ADD_CONSTRUCTOR_PRAM_ONE, ADD_CONSTRUCTOR_PRAM_MULTI, ADD_CONSTRUCTOR_PRAM_VARIABLE_LENGTH,*/
        ADD_FIELD_PRIVATE, ADD_FIELD_DEFAULT, ADD_FIELD_PROTECTED, ADD_FIELD_PUBLIC, ADD_FIELD_STATIC, ADD_FIELD_FINAL,
        ADD_CLASS_METHOD, ADD_CLASS_CONSTRUCTOR, ADD_CLASS_FIELD, ADD_CLASS_INNER_CLASS,
        ACCESS_INNER_PRIVATE_METHOD, ACCESS_INNER_PRIVATE_CONSTRUCTOR, ACCESS_INNER_STATIC_METHOD, ACCESS_INNER_PRIVATE_FIELD, ACCESS_INNER_STATIC_FIELD
    }

    public PatchType patchType = PatchType.ADD_METHOD_PRIVATE;
    public String constructorMsg;

    private String privateField = "badPrivateField";
    String defaultField = "badDefaultField";
    protected String protectedField = "badProtectedField";
    public String publicField = "badPublicField";
//    static String staticField = "badStaticField";
    final String finalField = "badFinalField";

    public BadClass(PatchType patchType) {
        this.patchType = patchType;
    }
    private BadClass() {
        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_NONE);
        constructorMsg = "bad private constructor with no pram";
//        constructorMsg = "patch " + constructorMsg;
    }

    BadClass(int i){
        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_ONE);
        constructorMsg = "bad default constructor with one pram int:" + i;
//        constructorMsg = "patch " + constructorMsg;
    }

    protected BadClass(boolean priBoolean, Boolean wrapBoolean){
        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_MULTI);
        constructorMsg = "bad protected constructor with two prams boolean:" + priBoolean + "  Boolean"+ wrapBoolean;
//        constructorMsg = "patch " + constructorMsg;
    }

    public BadClass(String[] strings){
        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_ARRAY);
        constructorMsg = "bad public constructor with one pram String[]:" + strings ;
//        constructorMsg = "patch " + constructorMsg;
    }

    public BadClass(double d, String... strings){
        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH);
        constructorMsg = "bad public constructor with variable prams double:" + d + "  String...:"+ strings;
//        constructorMsg = "patch " + constructorMsg;
    }

    String accessInnerPrivateMethod() {
        log("enter accessInnerPrivateMethod");
        String s = "to accessInnerPrivateMethod";
//        s = "patch " + new InnerClass().privateInnerMethod();
        return s;
    }

    String accessInnerPrivateConstructor() {
        log("enter callInnerPrivateConstructor");
        String s = "to callInnerPrivateConstructor";
//        s = "patch " + new InnerClass("callInnerPrivateConstructor").constructorMsg;
        return s;
    }

    String accessInnerPrivateFiled() {
        log("enter getInnerFiled");
        String s = "to getInnerFiled";
//        s = "patch " + new InnerClass().privateInnerString;
        return s;
    }

    String accessInnerStaticMethod() {
        log("enter callInnerStaticMethod");
        String s = "to callInnerStaticMethod";
//        s = "patch " + InnerStaticClass.staticMethod() + new InnerStaticClass().privateInnerMethod();
        return s;
    }

    String accessInnerStaticFiled() {
        log("enter getInnerStaticFiled");
        String s = "to getInnerStaticFiled";
//        s = "patch " + InnerStaticClass.privateStaticInnerString + new InnerStaticClass().privateInnerString;;
        return s;
    }


    private String privateMethod(){
        log("enter privateMethod");
        String s = "bad private method";
//        s = "patch " + s;
        return s;
    }

    String defaultMethod(){
        log("enter defaultMethod");
        String s = "bad default method";
//        s = "patch " + s;
        return s;
    }

    protected String protectedMethod(){
        log("enter protectedMethod");
        String s = "bad protected method";
//        s = "patch " + s;
        return s;
    }

    public String publicMethod(){
        log("enter publicMethod");
        String s = "bad public method";
//        s = "patch " + s;
        return s;
    }

    static String staticMethod(){
        log("enter staticMethod");
        String s = "bad static method";
//        s = "patch " + s;
        return s;
    }

    final String finalMethod(){
        log("enter finalMethod");
        String s = "bad final method";
//        s = "patch " + s;
        return s;
    }

    String paramPrimitiveMethod(int i){
        log("enter paramPrimitiveMethod");
        String s = "bad method param primitive:" + i;
//        s = "patch " + s;
        return s;
    }

    String paramWrapperMethod(Boolean b){
        log("enter paramWrapperMethod");
        String s = "bad method param wrapper primitive :" + b;
//        s = "patch " + s;
        return s;
    }

    String paramPrimitiveBooleanMethod(boolean b){
        log("enter paramPrimitiveBooleanMethod");
        String s = "bad method param boolean primitive:" + b;
//        s = "patch " + s;
        return s;
    }

    String paramArrayMethod(int[] ints, String[] strings){
        log("enter paramArrayMethod");
        String s = "bad method param array:" + ints + strings;
//        s = "patch " + s;
        return s;
    }

    String paramVariableLengthMethod(long l, String... strings){
        log("enter paramVariableLengthMethod");
        String s = "bad method param array:" + l + strings;
//        s = "patch " + s;
        return s;
    }

    /* ------------------------------------增加的测试方法 start--------------------------------------*/
    private String privateMethodAddTest(){
        log("enter privateMethodAddTest");
        String s = "add test bad private method add test";
//        s = privateMethodAdded();
        return s;
    }

    String defaultMethodAddTest(){
        log("enter defaultMethodAddTest");
        String s = "add test bad default method add test";
//        s = defaultMethodAdded();
        return s;
    }

    protected String protectedMethodAddTest(){
        log("enter protectedMethodAddTest");
        String s = "add test bad protected method add test";
//        s = protectedMethodAdded();
        return s;
    }

    public String publicMethodAddTest(){
        log("enter publicMethodAddTest");
        String s = "add test bad public method add test";
//        s = publicMethodAdded();
        return s;
    }

    static String staticMethodAddTest(){
        log("enter staticMethodAddTest");
        String s = "add test bad static method add test";
//        s = staticMethodAdded();
        return s;
    }

    final String finalMethodAddTest(){
        log("enter finalMethodAddTest");
        String s = "add test bad final method add test";
//        s = finalMethodAdded();
        return s;
    }

    String paramPrimitiveMethodAddTest(int i){
        log("enter paramPrimitiveMethodAddTest");
        String s = "add test bad method param primitive:" + i;
//        s = paramPrimitiveMethodAdded(i);
        return s;
    }

    String paramWrapperMethodAddTest(Boolean b){
        log("enter paramWrapperMethodAddTest");
        String s = "add test bad method param wrapper primitive :" + b;
//        s = paramWrapperMethodAdded(!b);
        return s;
    }

    String paramPrimitiveBooleanMethodAddTest(boolean b){
        log("enter paramPrimitiveBooleanMethodAddTest");
        String s = "add test bad method param boolean primitive:" + b;
//        s = paramPrimitiveBooleanMethodAdded(!b);
        return s;
    }

    String paramArrayMethodAddTest(int[] ints, String[] strings){
        log("enter paramArrayMethodAddTest");
        String s = "add test bad method param array:" + ints + strings;
//        s = paramArrayMethodAdded(ints, strings);
        return s;
    }

    String paramVariableLengthMethodAddTest(long l, String... strings){
        log("enter paramVariableLengthMethodAddTest");
        String s = "add test bad method param array:" + l + strings;
//        s = paramVariableLengthMethodAdded(l, strings);
        return s;
    }

//    private String privateMethodAdded(){
//        log("enter privateMethodAdded");
//        String s = "patch add private method";
//        return s;
//    }
//
//    String defaultMethodAdded(){
//        log("enter defaultMethodAdded");
//        String s = "patch add default method";
//        return s;
//    }
//
//    protected String protectedMethodAdded(){
//        log("enter protectedMethodAdded");
//        String s = "patch add protected method";
//        return s;
//    }
//
//    public String publicMethodAdded(){
//        log("enter publicMethodAdded");
//        String s = "patch add public method";
//        return s;
//    }
//
//    static String staticMethodAdded(){
//        log("enter staticMethodAdded");
//        String s = "patch add static method";
//        return s;
//    }
//
//    final String finalMethodAdded(){
//        log("enter finalMethodAdded");
//        String s = "patch add final method";
//        return s;
//    }
//
//    String paramPrimitiveMethodAdded(int i){
//        log("enter paramPrimitiveMethodAdded");
//        String s = "patch add method param primitive:" + i;
//        return s;
//    }
//
//    String paramWrapperMethodAdded(Boolean b){
//        log("enter paramWrapperMethodAdded");
//        String s = "patch add method param wrapper primitive :" + b;
//        return s;
//    }
//
//    String paramPrimitiveBooleanMethodAdded(boolean b){
//        log("enter paramPrimitiveBooleanMethodAdded");
//        String s = "patch add method param boolean primitive:" + b;
//        return s;
//    }
//
//    String paramArrayMethodAdded(int[] ints, String[] strings){
//        log("enter paramArrayMethodAdded");
//        String s = "patch add method param array:" + ints + strings;
//        return s;
//    }
//
//    String paramVariableLengthMethodAdded(long l, String... strings){
//        log("enter paramVariableLengthMethodAdded");
//        String s = "patch add method param array:" + l + strings;
//        return s;
//    }
/* --------------------------------------测试增加方法 end-----------------------------------------------*/


/* ------------------------------------增加的测试构造函数 start--------------------------------------*/
//    BadClass(boolean d){
//        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_ONE);
//        constructorMsg = "bad default constructor with one pram int:" + d;
//        constructorMsg = "patch " + constructorMsg;
//    }
//
//    protected BadClass(boolean priBoolean, String s){
//        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_MULTI);
//        constructorMsg = "bad protected constructor with two prams boolean:" + priBoolean + "  Boolean"+ s;
//        constructorMsg = "patch " + constructorMsg;
//    }
//
//    public BadClass(int[] strings){
//        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_ARRAY);
//        constructorMsg = "bad public constructor with one pram String[]:" + strings ;
//        constructorMsg = "patch " + constructorMsg;
//    }
//
//    public BadClass(double d, long... strings){
//        this(PatchType.MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH);
//        constructorMsg = "bad public constructor with variable prams double:" + d + "  String...:"+ strings;
//        constructorMsg = "patch " + constructorMsg;
//    }
/* --------------------------------------测试增构造函数 end-----------------------------------------------*/

    /* ----------------------------------------增加的成员变量 start-------------------------------------------*/
    String privateFieldAddTest() {
        log("enter privateFieldAddTest");
        String s = "to add private field";
//        s = privateFieldAdded;
        return s;
    }

    String defaultFieldAddTest() {
        log("enter defaultFieldAddTest");
        String s = "to add default field";
//        s = defaultFieldAdded;
        return s;
    }

    protected String protectedFieldAddTest() {
        log("enter protectedFieldAddTest");
        String s = "to add protected field";
//        s = protectedFieldAdded;
        return s;
    }

    public String publicFieldAddTest() {
        log("enter publicFieldAddTest");
        String s = "to add public field";
//        s = publicFieldAdded;
        return s;
    }

    String finalFieldAddTest() {
        log("enter finalFieldAddTest");
        String s = "to add final field";
//        s = finalFieldAdded;
        return s;
    }

//    private String privateFieldAdded = "patchPrivateFieldAdded";
//    String defaultFieldAdded = "patchDefaultFieldAdded";
//    protected String protectedFieldAdded = "patchProtectedFieldAdded";
//    public String publicFieldAdded = "patchPublicFieldAdded";
//    //    static String staticFieldAdded = "patchStaticFieldAdded";
//    final String finalFieldAdded = "patchFinalFieldAdded";
/* --------------------------------------增加的成员变量 end-----------------------------------------------*/


    /* ----------------------------------------新增类 start-------------------------------------------*/
    String addNewClassMethod() {
        log("enter addNewClassMethod");
        String s = "to add new class method";
//        NewClass newClass = new NewClass();
//        StringBuilder sb = new StringBuilder("patch ");
//        sb.append(newClass.publicStaticMethod(123, new String[]{"h", "y"})).append("\n")
//                .append(NewClass.defaultStaticMethod(true, "a ", "b")).append("\n")
//                .append(newClass.accessOldClassMethod()).append("\n")
//                .append(newClass.accessOldClassField());
//        s = sb.toString();
        return s;
    }

    String addNewClassConstructor() {
        log("enter addNewClassConstructor");
        String s = "to add new class constructor";
//        NewClass newClass = new NewClass(789);
//        s = "patch " + newClass.constructorMsg + newClass.packageInteger;
        return s;
    }

    String addNewClassField() {
        log("enter addNewClassField");
        String s = "to add new class field";
//        NewClass newClass = new NewClass(555);
//        s = "patch " + newClass.constructorMsg + newClass.packageInteger + NewClass.staticString + NewClass.FINAL_STATIC_BOOLEAN;
        return s;
    }

    String addNewClassInnerClass() {
        log("enter addNewClassMethod");
        String s = "to add new class inner class";
//        NewClass.NewInnerClass newInnerClass = new NewClass.NewInnerClass(3.14f);
//        s = "patch " + newInnerClass.toString();
        return s;
    }

/* --------------------------------------新增类 end-----------------------------------------------*/

    private static void log(String msg){
        String name = Thread.currentThread().getName();
        Log.d(BadClass.class.getSimpleName(), name +" msg:" + msg);
    }


    private class InnerClass {
        private String privateInnerString = "privateInnerString";
        private String constructorMsg;

        private InnerClass() {

        }

        private InnerClass(String constructorMsg) {
            this.constructorMsg = constructorMsg;
        }

        private String privateInnerMethod() {
            log("enter privateInnerMethod");
            String s = "private Inner method";
            return s;
        }
    }

    private static class InnerStaticClass {
        private static String privateStaticInnerString = "privateStaticInnerString";
        private String privateInnerString = "privateInnerString";

        private String privateInnerMethod() {
            log("enter privateInnerMethod");
            String s = "private Inner method";
            return s;
        }

        final static String staticMethod() {
            log("enter staticMethod");
            String s = "bad static method";
//        s = "patch " + s;
            return s;
        }
    }

}
