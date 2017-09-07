package com.meituan.sample.test;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

public class TestLambdaActivity extends AppCompatActivity {
    interface FunctionInterface {
        String getMessage();
    }

    private ListView listView;
    private List<LambdaPatchType> lambdaPatchTypes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_bad);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new PatchAdapter());
        listView.setOnItemClickListener((parent, view, position, id) -> {
            LambdaPatchType lambdaPatchType = (LambdaPatchType) parent.getItemAtPosition(position);
            FunctionInterface functionInterface = null;
            String toastMsg = "";
            switch (lambdaPatchType) {
                case ORIGINAL_METHOD_PRIVATE:
//                    functionInterface = () -> "bad originalPrivateMethod";
                        functionInterface = () -> "patch" + originalPrivateMethod();
                    break;

                case ORIGINAL_METHOD_STATIC:
                    functionInterface = () -> "bad originalStaticMethod";
//                        functionInterface = () -> "patch" + originalStaticMethod();
                    break;

                case MODIFY_METHOD_PRIVATE:
                    functionInterface = () -> "bad modifyPrivateMethod";
//                        functionInterface = () -> "patch" + modifyPrivateMethod();
                    break;

                case MODIFY_METHOD_STATIC:
                    functionInterface = () -> "bad modifyStaticMethod";
//                        functionInterface = () -> "patch" + modifyStaticMethod();
                    break;

                case ADD_METHOD_PRIVATE:
                    functionInterface = () -> "bad addPrivateMethod";
//                        functionInterface = () -> "patch" + addPrivateMethod();
                    break;

                case ADD_METHOD_STATIC:
                    functionInterface = () -> "bad addStaticMethod";
//                        functionInterface = () -> "patch" + addStaticMethod();
                    break;
            }

            toastMsg = functionInterface.getMessage();
            Toast.makeText(TestLambdaActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
        });
    }

    private void initData() {
        lambdaPatchTypes = new ArrayList<>();
        lambdaPatchTypes.add(LambdaPatchType.ORIGINAL_METHOD_PRIVATE);
        lambdaPatchTypes.add(LambdaPatchType.ORIGINAL_METHOD_STATIC);
        lambdaPatchTypes.add(LambdaPatchType.MODIFY_METHOD_PRIVATE);
        lambdaPatchTypes.add(LambdaPatchType.MODIFY_METHOD_STATIC);
        lambdaPatchTypes.add(LambdaPatchType.ADD_METHOD_PRIVATE);
        lambdaPatchTypes.add(LambdaPatchType.ADD_METHOD_STATIC);
    }

    class PatchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return lambdaPatchTypes.size();
        }

        @Override
        public Object getItem(int position) {
            return lambdaPatchTypes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(TestLambdaActivity.this);
                convertView.setPadding(0, dip2px(getApplicationContext(), 10), 0, dip2px(getApplicationContext(), 10));
            }
            LambdaPatchType lambdaPatchType = (LambdaPatchType) getItem(position);
            ((TextView) convertView).setText(lambdaPatchType.name());
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


    private String originalPrivateMethod() {
        log("enter originalPrivateMethod");
        String s = "originalPrivateMethod";
        return s;
    }

    public static String originalStaticMethod() {
        log("enter privateStaticOuterMethod");
        String s = "originalStaticMethod";
        return s;
    }

    private String modifyPrivateMethod() {
        log("enter modifyPrivateMethod");
        String s = "modify modifyPrivateMethod";
//            s = "already  " + s;
        return s;
    }

    public static String modifyStaticMethod() {
        log("enter modifyStaticMethod");
        String s = "modifyStaticMethod";
//            s = "already  " + s;
        return s;
    }

//    private String addPrivateMethod() {
//        log("enter addPrivateMethod");
//        String s = " addPrivateMethod";
//        return s;
//    }
//
//    public static String addStaticMethod() {
//        log("enter addPrivateMethod");
//        String s = "addPrivateMethod";
//        return s;
//    }

    enum LambdaPatchType {
        ORIGINAL_METHOD_PRIVATE, ORIGINAL_METHOD_STATIC,
        MODIFY_METHOD_PRIVATE, MODIFY_METHOD_STATIC,
        ADD_METHOD_PRIVATE, ADD_METHOD_STATIC,
        COMBINED_CALL_METHOD

    }


    private static void log(String msg) {
        String name = Thread.currentThread().getName();
        Log.d("TestLambda", name + " msg:" + msg);
    }
}
