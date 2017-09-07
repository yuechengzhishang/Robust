package com.meituan.sample.test;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meituan.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangyongzheng on 17/8/30.
 */

public class TestBadInnerClassActivity extends AppCompatActivity {
    private ListView listView;
    private List<BadInnerClass> badClassList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_bad);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new PatchAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BadInnerClass item = (BadInnerClass) parent.getItemAtPosition(position);
                String toastMsg = "";
                switch (item.patchType) {
                    case MODIFY_METHOD_PRIVATE:
                        toastMsg = item.privateMethod();
                        break;

                    case MODIFY_METHOD_DEFAULT:
                        toastMsg = item.defaultMethod();
                        break;

                    case MODIFY_METHOD_PROTECTED:
                        toastMsg = item.protectedMethod();
                        break;

                    case MODIFY_METHOD_PUBLIC:
                        toastMsg = item.publicMethod();
                        break;

                    case MODIFY_METHOD_MODIFIER_STATIC:
//                        toastMsg = BadInnerClass.staticMethod();
                        break;
                    case MODIFY_METHOD_MODIFIER_FINAL:
                        toastMsg = item.finalMethod();
                        break;

                    case MODIFY_METHOD_PRAM_PRIMITIVE_TYPE:
                        toastMsg = item.paramPrimitiveMethod(111);
                        break;

                    case MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE:
                        toastMsg = item.paramWrapperMethod(false);
                        break;

                    case MODIFY_METHOD_PRAM_BOOLEAN:
                        toastMsg = item.paramPrimitiveBooleanMethod(false);
                        break;

                    case MODIFY_METHOD_PRAM_ARRAY:
                        toastMsg = item.paramArrayMethod(new int[]{1, 2}, new String[]{"param", "array"});
                        break;

                    case MODIFY_METHOD_PRAM_VARIABLE_LENGTH:
                        toastMsg = item.paramVariableLengthMethod(1, "param", "array");
                        break;

                    case MODIFY_CONSTRUCTOR_PRAM_ARRAY:
                        toastMsg = item.constructorMsg;
                        break;

                    case MODIFY_CONSTRUCTOR_PRAM_NONE:
                        toastMsg = item.constructorMsg;
                        break;

                    case MODIFY_CONSTRUCTOR_PRAM_ONE:
                        toastMsg = item.constructorMsg;
                        break;

                    case MODIFY_CONSTRUCTOR_PRAM_MULTI:
                        toastMsg = item.constructorMsg;
                        break;

                    case MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH:
                        toastMsg = item.constructorMsg;
                        break;

                    case MODIFY_FIELD_PRIVATE:
                        toastMsg = item.privateField;
                        break;

                    case MODIFY_FIELD_DEFAULT:
                        toastMsg = item.defaultField;
                        break;

                    case MODIFY_FIELD_PROTECTED:
                        toastMsg = item.protectedField;
                        break;

                    case MODIFY_FIELD_PUBLIC:
                        toastMsg = item.publicField;
                        break;

                    case MODIFY_FIELD_STATIC:
//                        toastMsg = BadInnerClass.staticField;
                        break;

                    case MODIFY_FIELD_FINAL:
                        toastMsg = item.finalField;
                        break;


                    case ADD_METHOD_PRIVATE:
                        toastMsg = item.privateMethodAddTest();
                        break;

                    case ADD_METHOD_DEFAULT:
                        toastMsg = item.defaultMethodAddTest();
                        break;

                    case ADD_METHOD_PROTECTED:
                        toastMsg = item.protectedMethodAddTest();
                        break;

                    case ADD_METHOD_PUBLIC:
                        toastMsg = item.publicMethodAddTest();
                        break;

                    case ADD_METHOD_MODIFIER_STATIC:
//                        toastMsg = BadInnerClass.staticMethodAddTest();
                        break;
                    case ADD_METHOD_MODIFIER_FINAL:
                        toastMsg = item.finalMethodAddTest();
                        break;

                    case ADD_METHOD_PRAM_PRIMITIVE_TYPE:
                        toastMsg = item.paramPrimitiveMethodAddTest(111);
                        break;

                    case ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE:
                        toastMsg = item.paramWrapperMethodAddTest(false);
                        break;

                    case ADD_METHOD_PRAM_BOOLEAN:
                        toastMsg = item.paramPrimitiveBooleanMethodAddTest(false);
                        break;

                    case ADD_METHOD_PRAM_ARRAY:
                        toastMsg = item.paramArrayMethodAddTest(new int[]{1, 2}, new String[]{"param", "array"});
                        break;

                    case ADD_METHOD_PRAM_VARIABLE_LENGTH:
                        toastMsg = item.paramVariableLengthMethodAddTest(1, "param", "array");
                        break;

                    case ADD_FIELD_PRIVATE:
                        toastMsg = item.privateFieldAddTest();
                        break;

                    case ADD_FIELD_DEFAULT:
                        toastMsg = item.defaultFieldAddTest();
                        break;

                    case ADD_FIELD_PROTECTED:
                        toastMsg = item.protectedFieldAddTest();
                        break;

                    case ADD_FIELD_PUBLIC:
                        toastMsg = item.publicFieldAddTest();
                        break;

                    case ADD_FIELD_FINAL:
                        toastMsg = item.finalFieldAddTest();
                        break;

                    case ACCESS_OUT_METHOD:
                        toastMsg = item.accessOutMethod();
                        break;

                    case ACCESS_OUT_FIELD:
                        toastMsg = item.accessOutFiled();
                        break;

                    case SET_OUT_FIELD:
                        toastMsg = item.setOutFiled();
                        break;
                }

                Toast.makeText(TestBadInnerClassActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        badClassList = new ArrayList<>();
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PRIVATE));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_DEFAULT));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PROTECTED));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PUBLIC));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_MODIFIER_STATIC));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_MODIFIER_FINAL));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PRAM_PRIMITIVE_TYPE));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PRAM_BOOLEAN));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PRAM_ARRAY));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_METHOD_PRAM_VARIABLE_LENGTH));

        BadInnerClass defaultCons = new BadInnerClass();
        badClassList.add(defaultCons);
        badClassList.add(new BadInnerClass(11));
        badClassList.add(new BadInnerClass(true, new Boolean(false)));
        badClassList.add(new BadInnerClass(new String[]{"a", "b"}));
        badClassList.add(new BadInnerClass(3.14, "variable", "length"));

        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_FIELD_PRIVATE));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_FIELD_DEFAULT));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_FIELD_PROTECTED));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_FIELD_PUBLIC));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_FIELD_STATIC));
        badClassList.add(new BadInnerClass(InnerPatchType.MODIFY_FIELD_FINAL));


        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PRIVATE));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_DEFAULT));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PROTECTED));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PUBLIC));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_MODIFIER_STATIC));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_MODIFIER_FINAL));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PRAM_PRIMITIVE_TYPE));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PRAM_BOOLEAN));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PRAM_ARRAY));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_METHOD_PRAM_VARIABLE_LENGTH));

        badClassList.add(new BadInnerClass(InnerPatchType.ADD_FIELD_PRIVATE));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_FIELD_DEFAULT));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_FIELD_PROTECTED));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_FIELD_PUBLIC));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_FIELD_STATIC));
        badClassList.add(new BadInnerClass(InnerPatchType.ADD_FIELD_FINAL));

        badClassList.add(new BadInnerClass(InnerPatchType.ACCESS_OUT_METHOD));
        badClassList.add(new BadInnerClass(InnerPatchType.ACCESS_OUT_FIELD));
        badClassList.add(new BadInnerClass(InnerPatchType.SET_OUT_FIELD));
    }

    class PatchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return badClassList.size();
        }

        @Override
        public Object getItem(int position) {
            return badClassList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(TestBadInnerClassActivity.this);
                convertView.setPadding(0, dip2px(getApplicationContext(), 10), 0, dip2px(getApplicationContext(), 10));
            }
            BadInnerClass badClass = (BadInnerClass) getItem(position);
            ((TextView) convertView).setText(badClass.patchType.name());
            return convertView;
        }
    }

    /**
     * dip转换成px
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private String privateOuterField = "badPrivateOuterField";
    String defaultOuterField = "badDefaultOuterField";
    protected String protectedOuterField = "badProtectedOuterField";
    public String publicOuterField = "badPublicOuterField";
    private static String privateStaticOuterField = "privateStaticOuterField";
    protected static String protectedStaticOuterField = "protectedStaticOuterField";
    public static String publicStaticOuterField = "publicStaticOuterField";
    final String finalOuterField = "badFinalOuterField";

    private String privateOuterMethod() {
        log("enter privateOuterMethod");
        String s = "bad private OuterMethod";
//            s = "patch " + s;
        return s;
    }

    private String privateNewOuterMethod() {
        log("enter privateOuterMethod");
        String s = "bad private OuterMethod";
//            s = "patch " + s;
        return s;
    }

    String defaultOuterMethod() {
        log("enter defaultOuterMethod");
        String s = "bad default OuterMethod";
//            s = "patch " + s;
        return s;
    }

    protected String protectedOuterMethod() {
        log("enter protectedOuterMethod");
        String s = "bad protected OuterMethod";
//            s = "patch " + s;
        return s;
    }

    public String publicOuterMethod() {
        log("enter publicOuterMethod");
        String s = "bad public OuterMethod";
//            s = "patch " + s;
        return s;
    }

    private static String privateStaticOuterMethod() {
        log("enter privateStaticOuterMethod");
        String s = "bad static privateStaticOuterMethod";
//            s = "patch " + s;
        return s;
    }

    private static String privateNewStaticOuterMethod() {
        log("enter privateStaticOuterMethod");
        String s = "bad static privateStaticOuterMethod";
//            s = "patch " + s;
        return s;
    }

    protected static String protectedStaticOuterMethod() {
        log("enter protectedStaticOuterMethod2");
        String s = "bad static protectedStaticOuterMethod";
//            s = "patch " + s;
        return s;
    }

    public static String publicStaticOuterMethod() {
        log("enter publicStaticOuterMethod");
        String s = "bad static publicStaticOuterMethod";
//            s = "patch " + s;
        return s;
    }

    final String finalOuterMethod() {
        log("enter finalOuterMethod");
        String s = "bad final OuterMethod";
//            s = "patch " + s;
        return s;
    }

    String paramPrimitiveOuterMethod(int i) {
        log("enter paramPrimitiveOuterMethod");
        String s = "bad OuterMethod param primitive:" + i;
//            s = "patch " + s;
        return s;
    }

    String paramWrapperOuterMethod(Boolean b) {
        log("enter paramWrapperOuterMethod");
        String s = "bad OuterMethod param wrapper primitive :" + b;
//            s = "patch " + s;
        return s;
    }

    String paramPrimitiveBooleanOuterMethod(boolean b) {
        log("enter paramPrimitiveBooleanOuterMethod");
        String s = "bad OuterMethod param boolean primitive:" + b;
//            s = "patch " + s;
        return s;
    }

    String paramArrayOuterMethod(int[] ints, String[] strings) {
        log("enter paramArrayOuterMethod");
        String s = "bad OuterMethod param array:" + ints + strings;
//            s = "patch " + s;
        return s;
    }

    String paramVariableLengthOuterMethod(long l, String... strings) {
        log("enter paramVariableLengthOuterMethod");
        String s = "bad OuterMethod param array:" + l + strings;
//            s = "patch " + s;
        return s;
    }


    enum InnerPatchType {
        MODIFY_METHOD_PRIVATE, MODIFY_METHOD_DEFAULT, MODIFY_METHOD_PROTECTED, MODIFY_METHOD_PUBLIC, MODIFY_METHOD_MODIFIER_STATIC, MODIFY_METHOD_MODIFIER_FINAL,
        MODIFY_METHOD_PRAM_PRIMITIVE_TYPE, MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE, MODIFY_METHOD_PRAM_BOOLEAN, MODIFY_METHOD_PRAM_ARRAY, /*MODIFY_METHOD_PRAM_NONE, MODIFY_METHOD_PRAM_ONE, MODIFY_METHOD_PRAM_MULTI,*/ MODIFY_METHOD_PRAM_VARIABLE_LENGTH,
        /*MODIFY_CONSTRUCTOR_PRAM_PRIMITIVE_TYPE, MODIFY_CONSTRUCTOR_PRAM_WRAPPER_PRIMITIVE_TYPE, MODIFY_CONSTRUCTOR_PRAM_BOOLEAN,*/
        MODIFY_CONSTRUCTOR_PRAM_ARRAY, MODIFY_CONSTRUCTOR_PRAM_NONE, MODIFY_CONSTRUCTOR_PRAM_ONE, MODIFY_CONSTRUCTOR_PRAM_MULTI, MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH,
        MODIFY_FIELD_PRIVATE, MODIFY_FIELD_DEFAULT, MODIFY_FIELD_PROTECTED, MODIFY_FIELD_PUBLIC, MODIFY_FIELD_STATIC, MODIFY_FIELD_FINAL,
        ADD_METHOD_PRIVATE, ADD_METHOD_DEFAULT, ADD_METHOD_PROTECTED, ADD_METHOD_PUBLIC, ADD_METHOD_MODIFIER_STATIC, ADD_METHOD_MODIFIER_FINAL,
        ADD_METHOD_PRAM_PRIMITIVE_TYPE, ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE, ADD_METHOD_PRAM_BOOLEAN, ADD_METHOD_PRAM_ARRAY, /*ADD_METHOD_PRAM_NONE, ADD_METHOD_PRAM_ONE, ADD_METHOD_PRAM_MULTI,*/ ADD_METHOD_PRAM_VARIABLE_LENGTH,
        /*ADD_CONSTRUCTOR_PRAM_ARRAY, *//*ADD_CONSTRUCTOR_PRAM_NONE,*//* ADD_CONSTRUCTOR_PRAM_ONE, ADD_CONSTRUCTOR_PRAM_MULTI, ADD_CONSTRUCTOR_PRAM_VARIABLE_LENGTH,*/
        ADD_FIELD_PRIVATE, ADD_FIELD_DEFAULT, ADD_FIELD_PROTECTED, ADD_FIELD_PUBLIC, ADD_FIELD_STATIC, ADD_FIELD_FINAL,
        ACCESS_OUT_METHOD, ACCESS_OUT_FIELD, SET_OUT_FIELD
    }

    public class BadInnerClass {

        public InnerPatchType patchType = InnerPatchType.ADD_METHOD_PRIVATE;
        public String constructorMsg;

        private String privateField = "badPrivateField";
        String defaultField = "badDefaultField";
        protected String protectedField = "badProtectedField";
        public String publicField = "badPublicField";
        final String finalField = "badFinalField";

        public BadInnerClass(InnerPatchType patchType) {
            this.patchType = patchType;
        }

        private BadInnerClass() {
            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_NONE);
            constructorMsg = "bad private constructor with no pram";
//            constructorMsg = "patch " + constructorMsg;
        }

        BadInnerClass(int i) {
            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_ONE);
            constructorMsg = "bad default constructor with one pram int:" + i;
//            constructorMsg = "patch " + constructorMsg;
        }

        protected BadInnerClass(boolean priBoolean, Boolean wrapBoolean) {
            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_MULTI);
            constructorMsg = "bad protected constructor with two prams boolean:" + priBoolean + "  Boolean" + wrapBoolean;
//            constructorMsg = "patch " + constructorMsg;
        }

        public BadInnerClass(String[] strings) {
            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_ARRAY);
            constructorMsg = "bad public constructor with one pram String[]:" + strings;
//            constructorMsg = "patch " + constructorMsg;
        }

        public BadInnerClass(double d, String... strings) {
            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH);
            constructorMsg = "bad public constructor with variable prams double:" + d + "  String...:" + strings;
//            constructorMsg = "patch " + constructorMsg;
        }

        private String privateMethod() {
            log("enter privateMethod");
            String s = "bad private method";
//            s = "patch " + s;
            return s;
        }

        String defaultMethod() {
            log("enter defaultMethod");
            String s = "bad default method";
//            s = "patch " + s;
            return s;
        }

        protected String protectedMethod() {
            log("enter protectedMethod");
            String s = "bad protected method";
//            s = "patch " + s;
            return s;
        }

        public String publicMethod() {
            log("enter publicMethod");
            String s = "bad public method";
//            s = "patch " + s;
            return s;
        }

//        static String staticMethod() {
//            log("enter staticMethod");
//            String s = "bad static method";
////            s = "patch " + s;
//            return s;
//        }

        final String finalMethod() {
            log("enter finalMethod");
            String s = "bad final method";
//            s = "patch " + s;
            return s;
        }

        String paramPrimitiveMethod(int i) {
            log("enter paramPrimitiveMethod");
            String s = "bad method param primitive:" + i;
//            s = "patch " + s;
            return s;
        }

        String paramWrapperMethod(Boolean b) {
            log("enter paramWrapperMethod");
            String s = "bad method param wrapper primitive :" + b;
//            s = "patch " + s;
            return s;
        }

        String paramPrimitiveBooleanMethod(boolean b) {
            log("enter paramPrimitiveBooleanMethod");
            String s = "bad method param boolean primitive:" + b;
//            s = "patch " + s;
            return s;
        }

        String paramArrayMethod(int[] ints, String[] strings) {
            log("enter paramArrayMethod");
            String s = "bad method param array:" + ints + strings;
//            s = "patch " + s;
            return s;
        }

        String paramVariableLengthMethod(long l, String... strings) {
            log("enter paramVariableLengthMethod");
            String s = "bad method param array:" + l + strings;
//            s = "patch " + s;
            return s;
        }

        /* ------------------------------------增加的测试方法 start--------------------------------------*/
        private String privateMethodAddTest() {
            log("enter privateMethodAddTest");
            String s = "add test bad private method add test";
//            s = privateMethodAdded();
            return s;
        }

        String defaultMethodAddTest() {
            log("enter defaultMethodAddTest");
            String s = "add test bad default method add test";
//            s = defaultMethodAdded();
            return s;
        }

        protected String protectedMethodAddTest() {
            log("enter protectedMethodAddTest");
            String s = "add test bad protected method add test";
//            s = protectedMethodAdded();
            return s;
        }

        public String publicMethodAddTest() {
            log("enter publicMethodAddTest");
            String s = "add test bad public method add test";
//            s = publicMethodAdded();
            return s;
        }

//        static String staticMethodAddTest() {
//            log("enter staticMethodAddTest");
//            String s = "add test bad static method add test";
////            s = staticMethodAdded();
//            return s;
//        }

        final String finalMethodAddTest() {
            log("enter finalMethodAddTest");
            String s = "add test bad final method add test";
//            s = finalMethodAdded();
            return s;
        }

        String paramPrimitiveMethodAddTest(int i) {
            log("enter paramPrimitiveMethodAddTest");
            String s = "add test bad method param primitive:" + i;
//            s = paramPrimitiveMethodAdded(i);
            return s;
        }

        String paramWrapperMethodAddTest(Boolean b) {
            log("enter paramWrapperMethodAddTest");
            String s = "add test bad method param wrapper primitive :" + b;
//            s = paramWrapperMethodAdded(!b);
            return s;
        }

        String paramPrimitiveBooleanMethodAddTest(boolean b) {
            log("enter paramPrimitiveBooleanMethodAddTest");
            String s = "add test bad method param boolean primitive:" + b;
//            s = paramPrimitiveBooleanMethodAdded(!b);
            return s;
        }

        String paramArrayMethodAddTest(int[] ints, String[] strings) {
            log("enter paramArrayMethodAddTest");
            String s = "add test bad method param array:" + ints + strings;
//            s = paramArrayMethodAdded(ints, strings);
            return s;
        }

        String paramVariableLengthMethodAddTest(long l, String... strings) {
            log("enter paramVariableLengthMethodAddTest");
            String s = "add test bad method param array:" + l + strings;
//            s = paramVariableLengthMethodAdded(l, strings);
            return s;
        }

//        private String privateMethodAdded(){
//            log("enter privateMethodAdded");
//            String s = "patch add private method";
//            return s;
//        }
//
//        String defaultMethodAdded(){
//            log("enter defaultMethodAdded");
//            String s = "patch add default method";
//            return s;
//        }
//
//        protected String protectedMethodAdded(){
//            log("enter protectedMethodAdded");
//            String s = "patch add protected method";
//            return s;
//        }
//
//        public String publicMethodAdded(){
//            log("enter publicMethodAdded");
//            String s = "patch add public method";
//            return s;
//        }
//
//        static String staticMethodAdded(){
//            log("enter staticMethodAdded");
//            String s = "patch add static method";
//            return s;
//        }
//
//        final String finalMethodAdded(){
//            log("enter finalMethodAdded");
//            String s = "patch add final method";
//            return s;
//        }
//
//        String paramPrimitiveMethodAdded(int i){
//            log("enter paramPrimitiveMethodAdded");
//            String s = "patch add method param primitive:" + i;
//            return s;
//        }
//
//        String paramWrapperMethodAdded(Boolean b){
//            log("enter paramWrapperMethodAdded");
//            String s = "patch add method param wrapper primitive :" + b;
//            return s;
//        }
//
//        String paramPrimitiveBooleanMethodAdded(boolean b){
//            log("enter paramPrimitiveBooleanMethodAdded");
//            String s = "patch add method param boolean primitive:" + b;
//            return s;
//        }
//
//        String paramArrayMethodAdded(int[] ints, String[] strings){
//            log("enter paramArrayMethodAdded");
//            String s = "patch add method param array:" + ints + strings;
//            return s;
//        }
//
//        String paramVariableLengthMethodAdded(long l, String... strings){
//            log("enter paramVariableLengthMethodAdded");
//            String s = "patch add method param array:" + l + strings;
//            return s;
//        }
///* --------------------------------------测试增加方法 end-----------------------------------------------*/

/* ------------------------------------增加的测试构造函数 start--------------------------------------*/
//        BadInnerClass(boolean d){
//            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_ONE);
//            constructorMsg = "bad default constructor with one pram int:" + d;
//            constructorMsg = "patch " + constructorMsg;
//        }
//
//        protected BadInnerClass(boolean priBoolean, String s){
//            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_MULTI);
//            constructorMsg = "bad protected constructor with two prams boolean:" + priBoolean + "  Boolean"+ s;
//            constructorMsg = "patch " + constructorMsg;
//        }
//
//        public BadInnerClass(int[] strings){
//            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_ARRAY);
//            constructorMsg = "bad public constructor with one pram String[]:" + strings ;
//            constructorMsg = "patch " + constructorMsg;
//        }
//
//        public BadInnerClass(double d, long... strings){
//            this(InnerPatchType.MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH);
//            constructorMsg = "bad public constructor with variable prams double:" + d + "  String...:"+ strings;
//            constructorMsg = "patch " + constructorMsg;
//        }
/* --------------------------------------测试增构造函数 end-----------------------------------------------*/


/* ----------------------------------------增加的成员变量 start-------------------------------------------*/
        String privateFieldAddTest() {
            log("enter privateFieldAddTest");
            String s = "to add private field";
//            s = privateFieldAdded;
            return s;
        }

        String defaultFieldAddTest() {
            log("enter defaultFieldAddTest");
            String s = "to add default field";
//            s = defaultFieldAdded;
            return s;
        }

        protected String protectedFieldAddTest() {
            log("enter protectedFieldAddTest");
            String s = "to add protected field";
//            s = protectedFieldAdded;
            return s;
        }

        public String publicFieldAddTest() {
            log("enter publicFieldAddTest");
            String s = "to add public field";
//            s = publicFieldAdded;
            return s;
        }

        String finalFieldAddTest() {
            log("enter finalFieldAddTest");
            String s = "to add final field";
//            s = finalFieldAdded;
            return s;
        }

//        private String privateFieldAdded = "patchPrivateFieldAdded";
//        String defaultFieldAdded = "patchDefaultFieldAdded";
//        protected String protectedFieldAdded = "patchProtectedFieldAdded";
//        public String publicFieldAdded = "patchPublicFieldAdded";
//        //    static String staticFieldAdded = "patchStaticFieldAdded";
//        final String finalFieldAdded = "patchFinalFieldAdded";
/* --------------------------------------增加的成员变量 end-----------------------------------------------*/

/* ----------------------------------------测试外部类访问 start-------------------------------------------*/
        private String accessOutMethod() {

            StringBuilder sb = new StringBuilder();

            sb
                    .append(publicStaticOuterMethod()).append("\n")
                    .append(privateNewOuterMethod()).append("\n")
                    .append(privateNewStaticOuterMethod()).append("\n")
                    .append(protectedOuterMethod()).append("\n")
                    .append(protectedStaticOuterMethod()).append("\n")
            ;
            return sb.toString();
        }

        private String accessOutFiled() {

            StringBuilder sb = new StringBuilder();

            sb
//                    .append(privateOuterField).append("\n")
//                    .append(privateStaticOuterField).append("\n")
//                    .append(protectedStaticOuterField).append("\n")
//                    .append(publicStaticOuterField).append("\n")
                    .append(finalOuterField).append("\n");
            return sb.toString();
        }

        private String setOutFiled() {

            StringBuilder sb = new StringBuilder();
            privateOuterField = "setOutFromInnerClass" + privateOuterField;
//            privateStaticOuterField = "setOutFromInnerClass" + privateStaticOuterField;
            protectedStaticOuterField = "setOutFromInnerClass" + protectedStaticOuterField;
//            publicStaticOuterField = "setOutFromInnerClass" + publicStaticOuterField;
            sb
                    .append(privateOuterField).append("\n")
//                    .append(privateStaticOuterField).append("\n")
                    .append(protectedStaticOuterField).append("\n")
//                    .append(publicStaticOuterField).append("\n")
                    .append(finalOuterField).append("\n");
            return sb.toString();
        }
/* --------------------------------------测试外部类访问 end-----------------------------------------------*/

    }

    private static void log(String msg) {
        String name = Thread.currentThread().getName();
        Log.d(BadInnerClass.class.getSimpleName(), name + " msg:" + msg);
    }
}
