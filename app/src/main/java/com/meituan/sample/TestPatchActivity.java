package com.meituan.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by hedingxu on 17/6/16.
 */

public class TestPatchActivity extends AppCompatActivity{
    public String publicString;
    public static String publicStaticString;
    protected String protectedString ;
    private String privateString;
    public TestPatchActivity(){

        super();
        Log.e("robust","555");
        System.err.println("TestPatchActivity constructor");
        Log.e("robust","666");
        System.err.println("TestPatchActivity constructor");
        Log.e("robust","777");
        Log.e("robust","TestPatchActivity super ING");
        Log.e("robust","777777");
        Log.e("robust","TestPatchActivity super ING7777777");
    }

    public TestPatchActivity(int x){
        this();
        Log.e("robust","qqqq");
        Log.e("robust","pppp");
        Log.e("robust","BBBB");
        Log.e("robust","xxxxxxxxx");
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("robust","1111");
        super.onCreate(savedInstanceState);
        Log.e("robust","222");
        setContentView(R.layout.activity_robust_compat);
        Log.e("robust","333");
        Log.e("robust","444");
        String xx = SecondActivity.ACCESSIBILITY_SERVICE;
        Log.e("robust",xx);

        Toast.makeText(this, "hello: " + hello(), Toast.LENGTH_SHORT).show();

        if (com.meituan.robust.PatchProxy.isSupport(new Object[0],this,null,false,"xxx",new Class[0],Integer.class)){
            com.meituan.robust.PatchProxy.accessDispatch(new Object[0],this,null,false,"xxx",new Class[0],Integer.class);
            return;
        }
        Toast.makeText(this, "hello: " + helloPrivate(), Toast.LENGTH_SHORT).show();

        View view11 = this.findViewById(R.id.patch_text);
        Log.e("robust",view11.toString());

        //write
        publicString = "publicString";
        //read
        String str1 = publicString;
        //show
        Toast.makeText(this, "publicString: " + str1, Toast.LENGTH_SHORT).show();

        //write
        protectedString = "protectedString";
        //read
        String str2 = protectedString;
        //show
        Toast.makeText(this, "protectedString: " + str2, Toast.LENGTH_SHORT).show();

        //write
        privateString = "privateString";
        //read
        String str3 = privateString;
        //show
        Toast.makeText(this, "privateString: " + str3, Toast.LENGTH_SHORT).show();


        //write
        publicStaticString = "publicStaticString";
        //read
        String str4 = publicStaticString;
        //show
        Toast.makeText(this, "publicStaticString: " + str4, Toast.LENGTH_SHORT).show();

//        // TODO: 17/8/26 需要解决 lambda表达式
        View view = findViewById(R.id.patch_text);
        View.OnClickListener clickListener =
                v -> {
                    Log.d("robust", " onclick  in Listener Patch" /*+ hello()*/);
//                    Toast.makeText(getApplicationContext(), "T onclick Listener in lambda Patch", Toast.LENGTH_SHORT).show();
                };

        view.setOnClickListener(clickListener);

//        EnhancedRobustUtils.invokeReflectMethod("lambda$onCreate$3",this,new Object[]{view},new Class[]{View.class},TestPatchActivity.class);
//
//        try {
//            Object obj = EnhancedRobustUtils.getFieldValue("outerPatchClassName",clickListener,Class.forName("com.meituan.sample.TestPatchActivityPatch$$Lambda$1"));
//            if (null == obj || obj instanceof  TestPatchActivity){
//                Log.e("robust","obj.toString() : "+obj.toString());
//                Toast.makeText(this, "outerPatchClassName is this :" + obj.toString(), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "outerPatchClassName is not this:" + obj.toString(), Toast.LENGTH_SHORT).show();
//            }
//
//            Log.e("robust","this.toString() : "+this.toString());
//
////            TestPatchActivityPatch.access$lambda$0(this.outerPatchClassName, var1);
//            Log.e("robust","invokeReflectStaticMethod(\"access$lambda$0\" start ");
//            Class clazz =  TestPatchActivity.class;
//            Log.e("robust","1111 : "+clazz.getName());
//            Method method = clazz.getDeclaredMethod("access$lambda$0", new Class[]{TestPatchActivity.class,View.class});
//            Log.e("robust","2222 : "+method.getName() + "," + method.toGenericString());
//            method.setAccessible(true);
//            Log.e("robust","3333 : "+method.isAccessible());
//            method.invoke(null, new Object[]{obj,view});
//            Log.e("robust","invoke : "+method.toString());
////            EnhancedRobustUtils.invokeReflectStaticMethod("access$lambda$0",,,);
//            Log.e("robust","invokeReflectStaticMethod(\"access$lambda$0\" end ");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            Log.e("robust","ClassNotFoundException",e);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//            Log.e("robust","NoSuchMethodException",e);
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//            Log.e("robust","InvocationTargetException",e);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//            Log.e("robust","IllegalAccessException",e);
//        }
        findViewById(R.id.patch_text).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(),"T onclick  in Listener in :" + getLocalClassName(),Toast.LENGTH_SHORT).show();

                return true;
            }
        });
//        findViewById(R.id.patch_text).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"T onclick  in Listener in x:" + getLocalClassName(),Toast.LENGTH_SHORT).show();
//
//            }
//        });
        AddCustomClass.visit();
        new TestPatchAddSubNonStaticClass();
        testInt(300);
        Toast.makeText(this, "TestPatchAddSubNonStaticClass", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "TestPatchAddSubNonStaticClass11100000000", Toast.LENGTH_SHORT).show();
    }

    private void testInt(int value){
        Log.e("robust","private void testInt : "+value);
        Toast.makeText(this, "TestPatchAddSubNonStaticClass3333", Toast.LENGTH_SHORT).show();
    }

    private void setPrivateString(String privateString){
        Toast.makeText(this, "TestPatchAddSubNonStaticClass2222", Toast.LENGTH_SHORT).show();
        this.privateString = privateString;
    }

    private void setProtectedString(String protectedString){
        this.protectedString = protectedString;
    }
    private static String hello(){
        return "private static String hello";
    }

    private static String helloPrivate(){
        return "private String hello";
    }

    public static void hello(Thread thread){
        thread.getName();
        Log.e("robust","***** "+thread.getName());
        Log.e("robust","proguard Test Patch");
    }

    public static class TestPatchAddSubClass{
        private static String Name = "TestPatchAddSubClass";
        public static String Sex = "0";
        public static void hello(){
            Log.e("robust","TestPatchAddSubClass" + " public static void hello");
        }

        public void voidMethod(){
            Log.e("robust","TestPatchAddSubClass" + " public void voidMethod");
        }
    }

    public  class TestPatchAddSubNonStaticClass{
        public TestPatchAddSubNonStaticClass(){
            Log.e("robust","new TestPatchAddSubNonStaticClass " + publicString + " " +privateString);
            hello();
            Log.e("robust","new TestPatchAddSubNonStaticClass: " +TestPatchActivity.this.getLocalClassName());
            setPrivateString("TestPatchAddSubNonStaticClass.set private");
            setProtectedString("TestPatchAddSubNonStaticClass.set Protected");
            Log.e("robust","TestPatchAddSubNonStaticClass.set Protected");
            setProtectedString("TestPatchAddSubNonStaticClass.set Protected");
            Log.e("robust","TestPatchAddSubNonStaticClass.set Protected");
            setProtectedString("TestPatchAddSubNonStaticClass.set Protected");
            Log.e("robust","TestPatchAddSubNonStaticClass.set Protected");
            setProtectedString("TestPatchAddSubNonStaticClass.set Protected");
            Log.e("robust","TestPatchAddSubNonStaticClass.set Protected");
            innerClassPrivateMethod();
        }
        private  String Name = "TestPatchAddSubClass";
        public  String Sex = "0";
        public  void hello(){
            Log.e("robust","TestPatchAddSubClass" + " public static void hello");
            Toast.makeText(TestPatchActivity.this, "Toast in TestPatchAddSubNonStaticClass", Toast.LENGTH_SHORT).show();
        }

        public void voidMethod(){
            Log.e("robust","TestPatchAddSubClass" + " public void voidMethod");
        }

        private void innerClassPrivateMethod(){
            Log.e("robust","TestPatchAddSubClass" + " **********");
            Log.e("robust","TestPatchAddSubClass" + " innerClassPrivateMethod");
            setPrivateString("TestPatchAddSubNonStaticClass.set private");
            setProtectedString("TestPatchAddSubNonStaticClass.set Protected");
            Log.e("robust","TestPatchAddSubNonStaticClass.set Protected");
        }
    }
}
