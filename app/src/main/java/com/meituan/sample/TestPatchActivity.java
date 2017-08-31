package com.meituan.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.meituan.robust.patch.RobustModify;

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
    }

    public TestPatchActivity(int x){
        this();
        Log.e("robust","qqqq");
        Log.e("robust","pppp");
        Log.e("robust","BBBB");
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

//        //write
//        publicString = "publicString";
//        //read
//        String str = publicString;
//        //show
//        Toast.makeText(this, "publicString: " + str, Toast.LENGTH_SHORT).show();

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
        findViewById(R.id.patch_text).setOnClickListener(v -> {
                    Log.d("robust", " onclick  in Listener");
                    Toast.makeText(getApplicationContext(), "T onclick Listener in lambda ", Toast.LENGTH_SHORT).show();
                }
        );

//        findViewById(R.id.patch_text).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"T onclick  in Listener",Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}
