package com.meituan.sample;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meituan.robust.Patch;
import com.meituan.robust.PatchExecutor;
import com.meituan.robust.PatchProxy;
import com.meituan.robust.RobustCallBack;
import com.meituan.sample.extension.LogExtension;
import com.meituan.sample.test.TestBadAnonymousInnerClassActivity;
import com.meituan.sample.test.TestBadClassActivity;
import com.meituan.sample.test.TestBadInnerClassActivity;
import com.meituan.sample.test.TestBadStaticInnerClassActivity;
import com.meituan.sample.test.TestLambdaActivity;

import java.lang.reflect.Method;
import java.util.List;

/**
 * For users of Robust you may only to use MainActivity or SecondActivity,other classes are used for test.<br>
 * <br>
 * If you just want to use Robust ,we recommend you just focus on MainActivity SecondActivity and PatchManipulateImp.Especially three buttons in MainActivity<br>
 * <br>
 * in the MainActivity have three buttons; "SHOW TEXT " Button will change the text in the MainActivity,you can patch the show text.<br>
 * <br>
 * "PATCH" button will load the patch ,the patch path can be configured in PatchManipulateImp.<br>
 * <br>
 * "JUMP_SECOND_ACTIVITY" button will jump to the second ACTIVITY,so you can patch a Activity.<br>
 * <br>
 * Attention to this ,We recommend that one patch is just for one built apk ,because every  built apk has its unique mapping.txt and resource id<br>
 *
 * @author mivanzhang
 */

public class MainActivity extends AppCompatActivity {

    TextView tipsTextView;
    private String stata;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "change main activity onCreate ", Toast.LENGTH_SHORT).show();
        Log.e("change","test change222");
        System.err.println("hello change mainActivity onCreate method ");
        setContentView(R.layout.activity_main);
        PatchProxy.register(new LogExtension());

        Log.e("change","11111");
        tipsTextView = (TextView) findViewById(R.id.tips_text);
        StringBuilder tipsStringBuilder = new StringBuilder();
        Log.e("change","151515");
        tipsStringBuilder.append("Tips:");
        tipsStringBuilder.append("\n1.please click JUMP_PATCH_ACTIVITY button to see the origin and go back here");
        tipsStringBuilder.append("\n2.please click patch button to apply patch");
        tipsStringBuilder.append("\n3.please click JUMP_PATCH_ACTIVITY button to see how effective");
        Log.e("change","2222");
        tipsTextView.setText(tipsStringBuilder.toString());
        Log.e("change","2.5.5.");
        Button patch = (Button) findViewById(R.id.patch);
        Log.e("change","3333");
        patch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "patch start...", Toast.LENGTH_SHORT).show();
                if (isGrantSDCardReadPermission()) {
                    runRobust();
                } else {
                    requestPermission();
                }
            }
        });

        Log.e("change","4444");
        findViewById(R.id.jump_second_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
                String zz = getter();
                setter(zz);
                setter(xx);
                staticMethod();
                //========just add complexity =========
                boolean isMainThread = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().getMainLooper().isCurrentThread()) {
                        isMainThread = true;
                    }
                } else {
                    String currentThreadName = Thread.currentThread().getName();
                    String mainThreadName = getApplicationContext().getMainLooper().getThread().getName();
                    if (TextUtils.equals(currentThreadName, mainThreadName)) {
                        isMainThread = true;
                    }
                }

                Log.d("robust", "isMainThread :" + isMainThread);
                //=================
            }
        });

        Log.e("change","5555");

        findViewById(R.id.jump_patch_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestPatchActivity.class);
                startActivity(intent);
                //========just add complexity =========
                boolean isMainThread = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().getMainLooper().isCurrentThread()) {
                        isMainThread = true;
                    }
                } else {
                    String currentThreadName = Thread.currentThread().getName();
                    String mainThreadName = getApplicationContext().getMainLooper().getThread().getName();
                    if (TextUtils.equals(currentThreadName, mainThreadName)) {
                        isMainThread = true;
                    }
                }

                Log.d("robust", "isMainThread :" + isMainThread);
                //=================
            }
        });

        Log.e("change","6666");

        findViewById(R.id.jump_bad_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestBadClassActivity.class);
                startActivity(intent);
                //========just add complexity =========
                boolean isMainThread = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().getMainLooper().isCurrentThread()) {
                        isMainThread = true;
                    }
                } else {
                    String currentThreadName = Thread.currentThread().getName();
                    String mainThreadName = getApplicationContext().getMainLooper().getThread().getName();
                    if (TextUtils.equals(currentThreadName, mainThreadName)) {
                        isMainThread = true;
                    }
                }

                Log.d("robust", "isMainThread :" + isMainThread);
                //=================
            }
        });

        Log.e("change","777");


        findViewById(R.id.jump_bad_inner_class_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestBadInnerClassActivity.class);
                startActivity(intent);
                //========just add complexity =========
                boolean isMainThread = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().getMainLooper().isCurrentThread()) {
                        isMainThread = true;
                    }
                } else {
                    String currentThreadName = Thread.currentThread().getName();
                    String mainThreadName = getApplicationContext().getMainLooper().getThread().getName();
                    if (TextUtils.equals(currentThreadName, mainThreadName)) {
                        isMainThread = true;
                    }
                }

                Log.d("robust", "isMainThread :" + isMainThread);
                //=================
            }
        });

        Log.e("change", "888");

        findViewById(R.id.jump_bad_inner_static_class_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestBadStaticInnerClassActivity.class);
                startActivity(intent);
                //========just add complexity =========
                boolean isMainThread = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().getMainLooper().isCurrentThread()) {
                        isMainThread = true;
                    }
                } else {
                    String currentThreadName = Thread.currentThread().getName();
                    String mainThreadName = getApplicationContext().getMainLooper().getThread().getName();
                    if (TextUtils.equals(currentThreadName, mainThreadName)) {
                        isMainThread = true;
                    }
                }

                Log.d("robust", "isMainThread :" + isMainThread);
                //=================
            }
        });

        Log.e("change", "999");

        findViewById(R.id.jump_bad_anonymous_inner_class_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestBadAnonymousInnerClassActivity.class);
                startActivity(intent);
                //========just add complexity =========
                boolean isMainThread = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().getMainLooper().isCurrentThread()) {
                        isMainThread = true;
                    }
                } else {
                    String currentThreadName = Thread.currentThread().getName();
                    String mainThreadName = getApplicationContext().getMainLooper().getThread().getName();
                    if (TextUtils.equals(currentThreadName, mainThreadName)) {
                        isMainThread = true;
                    }
                }

                Log.d("robust", "isMainThread :" + isMainThread);
                //=================
            }
        });

        Log.e("change", "1010");

        findViewById(R.id.jump_bad_lambda_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestLambdaActivity.class);
                startActivity(intent);
                //========just add complexity =========
                boolean isMainThread = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getApplicationContext().getMainLooper().isCurrentThread()) {
                        isMainThread = true;
                    }
                } else {
                    String currentThreadName = Thread.currentThread().getName();
                    String mainThreadName = getApplicationContext().getMainLooper().getThread().getName();
                    if (TextUtils.equals(currentThreadName, mainThreadName)) {
                        isMainThread = true;
                    }
                }

                Log.d("robust", "isMainThread :" + isMainThread);
                //=================
            }
        });

        Log.e("change", "1111");
    }

    //patch  data report
    class Callback implements RobustCallBack {

        @Override
        public void onPatchListFetched(boolean result, boolean isNet, List<Patch> patches) {
            System.out.println(" robust arrived in onPatchListFetched");
        }

        @Override
        public void onPatchFetched(boolean result, boolean isNet, Patch patch) {
            System.out.println(" robust arrived in onPatchFetched");
        }

        @Override
        public void onPatchApplied(boolean result, Patch patch) {
            tipsTextView.post(new Runnable() {
                @Override
                public void run() {
                    if (result) {
                        Toast.makeText(getApplicationContext(), "patch applied success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "patch applied failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void logNotify(String log, String where) {
            System.out.println(" robust arrived in logNotify " + where);
        }

        @Override
        public void exceptionNotify(Throwable throwable, String where) {
            throwable.printStackTrace();
            System.out.println(" robust arrived in exceptionNotify " + where);
        }
    }

    private boolean isGrantSDCardReadPermission() {
        return PermissionUtils.isGrantSDCardReadPermission(this);
    }

    private void requestPermission() {
        PermissionUtils.requestSDCardReadPermission(this, REQUEST_CODE_SDCARD_READ);
    }

    private static final int REQUEST_CODE_SDCARD_READ = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SDCARD_READ:
                handlePermissionResult();
                break;

            default:
                break;
        }
    }

    private void handlePermissionResult() {
        if (isGrantSDCardReadPermission()) {
            runRobust();
        } else {
            Toast.makeText(this, "failure because without sd card read permission", Toast.LENGTH_SHORT).show();
        }

    }

    private void runRobust() {
        testAdd("nn");
        new PatchExecutor(getApplicationContext(), new PatchManipulateImp(), new Callback()).start();
    }

    public void testAdd(String ll){
        System.err.println("it is : " + ll);
    }



    private static String staticMethod(String str){
        Log.e("xx","xx");
        if (true){
            str = "true";
        } else {
            str = "false";
        }
        return str;
    }

    private String xx = "xx";
    private String getter(){
        return xx;
    }

    private void setter(String zz){
        this.xx = zz;
    }

    private void noNeedInsertMethod(String nn){
        nn = nn +getter();
        setter(nn);
    }

    private static void staticMethod(){
        String xx = "xx";
        System.err.println(xx);
        Object[] objects = new Object[]{3};//IDE
        Object[] objects2 = new Object[]{Integer.valueOf(3)};//class
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //主线程能拿到，其他线程拿不到？
    private static String getCurrentProcessNameByReflect() {
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method tCurrentActivityThreadMethod = clazz.getDeclaredMethod("currentActivityThread");
            tCurrentActivityThreadMethod.setAccessible(true);
            Object tCurrentActivityThread = tCurrentActivityThreadMethod.invoke(null);

            Method tGetProcessNameMethod = clazz.getDeclaredMethod("getProcessName");
            tGetProcessNameMethod.setAccessible(true);
            return  (String) tGetProcessNameMethod.invoke(tCurrentActivityThread);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
