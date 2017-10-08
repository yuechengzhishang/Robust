package com.meituan.app.patch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "打补丁专用project , 补丁patch.apk在build/outputs目录下 ", Toast.LENGTH_SHORT).show();
    }
}
