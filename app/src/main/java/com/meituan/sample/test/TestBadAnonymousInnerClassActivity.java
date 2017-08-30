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

public class TestBadAnonymousInnerClassActivity extends AppCompatActivity {

    interface IProxy {
        String invoke(AnonymousInnerPatchType anonymousInnerPatchType);
    }

    private ListView listView;
    private List<AnonymousInnerPatchType> anonymousInnerPatchTypeList;

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
                AnonymousInnerPatchType anonymousInnerPatchType = (AnonymousInnerPatchType) parent.getItemAtPosition(position);
                String toastMsg = anonymousClass.invoke(anonymousInnerPatchType);
                Toast.makeText(TestBadAnonymousInnerClassActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        anonymousInnerPatchTypeList = new ArrayList<>();
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PRIVATE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_DEFAULT);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PROTECTED);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PUBLIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_MODIFIER_STATIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_MODIFIER_FINAL);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PRAM_PRIMITIVE_TYPE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PRAM_BOOLEAN);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PRAM_ARRAY);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_METHOD_PRAM_VARIABLE_LENGTH);

        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_FIELD_PRIVATE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_FIELD_DEFAULT);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_FIELD_PROTECTED);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_FIELD_PUBLIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_FIELD_STATIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.MODIFY_FIELD_FINAL);


        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PRIVATE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_DEFAULT);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PROTECTED);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PUBLIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_MODIFIER_STATIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_MODIFIER_FINAL);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PRAM_PRIMITIVE_TYPE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PRAM_BOOLEAN);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PRAM_ARRAY);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_METHOD_PRAM_VARIABLE_LENGTH);

        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_FIELD_PRIVATE);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_FIELD_DEFAULT);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_FIELD_PROTECTED);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_FIELD_PUBLIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_FIELD_STATIC);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ADD_FIELD_FINAL);

        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ACCESS_OUT_METHOD);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.ACCESS_OUT_FIELD);
        anonymousInnerPatchTypeList.add(AnonymousInnerPatchType.SET_OUT_FIELD);
    }

    class PatchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return anonymousInnerPatchTypeList.size();
        }

        @Override
        public Object getItem(int position) {
            return anonymousInnerPatchTypeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(TestBadAnonymousInnerClassActivity.this);
                convertView.setPadding(0, dip2px(getApplicationContext(), 10), 0, dip2px(getApplicationContext(), 10));
            }
            AnonymousInnerPatchType anonymousInnerPatchType = (AnonymousInnerPatchType) getItem(position);
            ((TextView) convertView).setText(anonymousInnerPatchType.name());
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

//    private String privateNewOuterMethod() {
//        log("enter privateOuterMethod");
//        String s = "bad private OuterMethod";
////            s = "patch " + s;
//        return s;
//    }

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

//    private static String privateNewStaticOuterMethod() {
//        log("enter privateStaticOuterMethod");
//        String s = "bad static privateStaticOuterMethod";
////            s = "patch " + s;
//        return s;
//    }

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


    enum AnonymousInnerPatchType {
        MODIFY_METHOD_PRIVATE, MODIFY_METHOD_DEFAULT, MODIFY_METHOD_PROTECTED, MODIFY_METHOD_PUBLIC, MODIFY_METHOD_MODIFIER_STATIC, MODIFY_METHOD_MODIFIER_FINAL,
        MODIFY_METHOD_PRAM_PRIMITIVE_TYPE, MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE, MODIFY_METHOD_PRAM_BOOLEAN, MODIFY_METHOD_PRAM_ARRAY, /*MODIFY_METHOD_PRAM_NONE, MODIFY_METHOD_PRAM_ONE, MODIFY_METHOD_PRAM_MULTI,*/ MODIFY_METHOD_PRAM_VARIABLE_LENGTH,
        /*MODIFY_CONSTRUCTOR_PRAM_PRIMITIVE_TYPE, MODIFY_CONSTRUCTOR_PRAM_WRAPPER_PRIMITIVE_TYPE, MODIFY_CONSTRUCTOR_PRAM_BOOLEAN,*/
        /*MODIFY_CONSTRUCTOR_PRAM_ARRAY, MODIFY_CONSTRUCTOR_PRAM_NONE, MODIFY_CONSTRUCTOR_PRAM_ONE, MODIFY_CONSTRUCTOR_PRAM_MULTI, MODIFY_CONSTRUCTOR_PRAM_VARIABLE_LENGTH,*/
        MODIFY_FIELD_PRIVATE, MODIFY_FIELD_DEFAULT, MODIFY_FIELD_PROTECTED, MODIFY_FIELD_PUBLIC, MODIFY_FIELD_STATIC, MODIFY_FIELD_FINAL,
        ADD_METHOD_PRIVATE, ADD_METHOD_DEFAULT, ADD_METHOD_PROTECTED, ADD_METHOD_PUBLIC, ADD_METHOD_MODIFIER_STATIC, ADD_METHOD_MODIFIER_FINAL,
        ADD_METHOD_PRAM_PRIMITIVE_TYPE, ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE, ADD_METHOD_PRAM_BOOLEAN, ADD_METHOD_PRAM_ARRAY, /*ADD_METHOD_PRAM_NONE, ADD_METHOD_PRAM_ONE, ADD_METHOD_PRAM_MULTI,*/ ADD_METHOD_PRAM_VARIABLE_LENGTH,
        /*ADD_CONSTRUCTOR_PRAM_ARRAY, *//*ADD_CONSTRUCTOR_PRAM_NONE,*//* ADD_CONSTRUCTOR_PRAM_ONE, ADD_CONSTRUCTOR_PRAM_MULTI, ADD_CONSTRUCTOR_PRAM_VARIABLE_LENGTH,*/
        ADD_FIELD_PRIVATE, ADD_FIELD_DEFAULT, ADD_FIELD_PROTECTED, ADD_FIELD_PUBLIC, ADD_FIELD_STATIC, ADD_FIELD_FINAL,
        ACCESS_OUT_METHOD, ACCESS_OUT_FIELD, SET_OUT_FIELD
    }


    private IProxy anonymousClass = new IProxy() {
        private String privateField = "badPrivateField";
        String defaultField = "badDefaultField";
        protected String protectedField = "badProtectedField";
        public String publicField = "badPublicField";
        final String finalField = "badFinalField";

        @Override
        public String invoke(AnonymousInnerPatchType anonymousInnerPatchType) {
            String msg = "";
            switch (anonymousInnerPatchType) {
                case MODIFY_METHOD_PRIVATE:
                    msg = privateMethod();
                    break;

                case MODIFY_METHOD_DEFAULT:
                    msg = defaultMethod();
                    break;

                case MODIFY_METHOD_PROTECTED:
                    msg = protectedMethod();
                    break;

                case MODIFY_METHOD_PUBLIC:
                    msg = publicMethod();
                    break;

                case MODIFY_METHOD_MODIFIER_STATIC:
//                        toastMsg = BadInnerClass.staticMethod();
                    break;
                case MODIFY_METHOD_MODIFIER_FINAL:
                    msg = finalMethod();
                    break;

                case MODIFY_METHOD_PRAM_PRIMITIVE_TYPE:
                    msg = paramPrimitiveMethod(111);
                    break;

                case MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE:
                    msg = paramWrapperMethod(false);
                    break;

                case MODIFY_METHOD_PRAM_BOOLEAN:
                    msg = paramPrimitiveBooleanMethod(false);
                    break;

                case MODIFY_METHOD_PRAM_ARRAY:
                    msg = paramArrayMethod(new int[]{1, 2}, new String[]{"param", "array"});
                    break;

                case MODIFY_METHOD_PRAM_VARIABLE_LENGTH:
                    msg = paramVariableLengthMethod(1, "param", "array");
                    break;

                case MODIFY_FIELD_PRIVATE:
                    msg = privateField;
                    break;

                case MODIFY_FIELD_DEFAULT:
                    msg = defaultField;
                    break;

                case MODIFY_FIELD_PROTECTED:
                    msg = protectedField;
                    break;

                case MODIFY_FIELD_PUBLIC:
                    msg = publicField;
                    break;

                case MODIFY_FIELD_STATIC:
//                        toastMsg = BadInnerClass.staticField;
                    break;

                case MODIFY_FIELD_FINAL:
                    msg = finalField;
                    break;


                case ADD_METHOD_PRIVATE:
                    msg = privateMethodAddTest();
                    break;

                case ADD_METHOD_DEFAULT:
                    msg = defaultMethodAddTest();
                    break;

                case ADD_METHOD_PROTECTED:
                    msg = protectedMethodAddTest();
                    break;

                case ADD_METHOD_PUBLIC:
                    msg = publicMethodAddTest();
                    break;

                case ADD_METHOD_MODIFIER_STATIC:
//                        toastMsg = BadInnerClass.staticMethodAddTest();
                    break;
                case ADD_METHOD_MODIFIER_FINAL:
                    msg = finalMethodAddTest();
                    break;

                case ADD_METHOD_PRAM_PRIMITIVE_TYPE:
                    msg = paramPrimitiveMethodAddTest(111);
                    break;

                case ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE:
                    msg = paramWrapperMethodAddTest(false);
                    break;

                case ADD_METHOD_PRAM_BOOLEAN:
                    msg = paramPrimitiveBooleanMethodAddTest(false);
                    break;

                case ADD_METHOD_PRAM_ARRAY:
                    msg = paramArrayMethodAddTest(new int[]{1, 2}, new String[]{"param", "array"});
                    break;

                case ADD_METHOD_PRAM_VARIABLE_LENGTH:
                    msg = paramVariableLengthMethodAddTest(1, "param", "array");
                    break;

                case ADD_FIELD_PRIVATE:
                    msg = privateFieldAddTest();
                    break;

                case ADD_FIELD_DEFAULT:
                    msg = defaultFieldAddTest();
                    break;

                case ADD_FIELD_PROTECTED:
                    msg = protectedFieldAddTest();
                    break;

                case ADD_FIELD_PUBLIC:
                    msg = publicFieldAddTest();
                    break;

                case ADD_FIELD_FINAL:
                    msg = finalFieldAddTest();
                    break;

                case ACCESS_OUT_METHOD:
                    msg = accessOutMethod();
                    break;

                case ACCESS_OUT_FIELD:
                    msg = accessOutFiled();
                    break;

                case SET_OUT_FIELD:
                    msg = setOutFiled();
                    break;
            }
            return msg;
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
//                    .append(privateNewOuterMethod()).append("\n")
//                    .append(privateNewStaticOuterMethod()).append("\n")
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

    };


    private static void log(String msg) {
        String name = Thread.currentThread().getName();
        Log.d("AnonymousClass", name + " msg:" + msg);
    }
}
