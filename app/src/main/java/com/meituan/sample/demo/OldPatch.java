package com.meituan.sample.demo;

import android.util.Log;
import android.view.View;

import com.meituan.robust.ChangeQuickRedirect;

/**
 * Created by hedingxu on 17/7/7.
 */

public class OldPatch extends Old {

    public OldPatch(){
        super();
    }

    public OldPatch(String hello,String xx){
        super(hello);
        Log.e("robust","hello click");
        Log.e("robust","hello click");
        Log.e("robust","hello click");
    }

    public OldPatch(String hello,String xx,String yy){
        this(hello,xx);
        Log.e("robust","hello click + " + yy);
    }

    private String test = "old patch test";
    private int testint1 = 1;
    public int testint2 = 2;
    public static int testint3 = 3;
    public String testString = "string";
    private String testString2 = "string2";
    private static String testString3 = "string3";
    private static ChangeQuickRedirect changeQuickRedirect ;
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.e("robust","hello click");
        }
    };

    private Bean bean = new Bean();

    public static void setChangeQuickRedirect(ChangeQuickRedirect changeQuickRedirect) {
        OldPatch.changeQuickRedirect = changeQuickRedirect;
    }

    public void setBean(Bean bean){
        this.bean = bean;
    }


    public void method1() {
        //source code 1 patch
    }

    public void method2() {
        //source code 2 patch
        method3();
    }

    public void method3() {
        System.err.println("method 3 old patch" + test);
        //source code 3 patch
    }

//    public static void main(String[] args){
//        Old old = new OldPatch();
//        old.method3();
//    }
}
