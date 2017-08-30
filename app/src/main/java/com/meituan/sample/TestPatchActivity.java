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
        // TODO: 17/8/26 需要解决 lambda表达式
//        findViewById(R.id.patch_text).setOnClickListener(v -> {
//            RobustModify.modify();
//            //test inner class accessibility
////            people.setAddr("asdasd");
////            getInfo(state, new Super(), 1l);
//            Log.d("robust", " onclick  in Listener");
//        }
//        );
        Toast.makeText(getApplicationContext(),"T onclick  in Listener 2222",Toast.LENGTH_SHORT).show();

        findViewById(R.id.patch_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"T onclick  in Listener",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
