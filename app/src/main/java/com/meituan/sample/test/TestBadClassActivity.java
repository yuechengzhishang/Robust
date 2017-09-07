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

import com.meituan.robust.utils.EnhancedRobustUtils;
import com.meituan.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangyongzheng on 17/8/30.
 */

public class TestBadClassActivity extends AppCompatActivity {
    private ListView listView;
    private List<BadClass> badClassList;

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
                BadClass item = (BadClass) parent.getItemAtPosition(position);
                String toastMsg = "";
                switch (item.patchType) {
                    case MODIFY_METHOD_PRIVATE:
                        toastMsg = (String) EnhancedRobustUtils.invokeReflectMethod("privateMethod", item, new Object[0], new Class[0], BadClass.class);
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
                        toastMsg = BadClass.staticMethod();
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
                        toastMsg = (String) EnhancedRobustUtils.getFieldValue("privateField", item, BadClass.class);
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
//                        toastMsg = BadClass.staticField;
                        break;

                    case MODIFY_FIELD_FINAL:
                        toastMsg = item.finalField;
                        break;


                    case ADD_METHOD_PRIVATE:
                        toastMsg = (String) EnhancedRobustUtils.invokeReflectMethod("privateMethodAddTest", item, new Object[0], new Class[0], BadClass.class);
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
                        toastMsg = BadClass.staticMethodAddTest();
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

                    case ADD_CLASS_METHOD:
                        toastMsg = item.addNewClassMethod();
                        break;

                    case ADD_CLASS_CONSTRUCTOR:
                        toastMsg = item.addNewClassConstructor();
                        break;

                    case ADD_CLASS_FIELD:
                        toastMsg = item.addNewClassField();
                        break;

                    case ADD_CLASS_INNER_CLASS:
                        toastMsg = item.addNewClassInnerClass();
                        break;
                }

                Toast.makeText(TestBadClassActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        badClassList = new ArrayList<>();
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PRIVATE));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_DEFAULT));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PROTECTED));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PUBLIC));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_MODIFIER_STATIC));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_MODIFIER_FINAL));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PRAM_PRIMITIVE_TYPE));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PRAM_BOOLEAN));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PRAM_ARRAY));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_METHOD_PRAM_VARIABLE_LENGTH));

        BadClass defaultCons = (BadClass) EnhancedRobustUtils.invokeReflectConstruct(BadClass.class.getName(), null, null);
        badClassList.add(defaultCons);
        badClassList.add(new BadClass(11));
        badClassList.add(new BadClass(true, new Boolean(false)));
        badClassList.add(new BadClass(new String[]{"a", "b"}));
        badClassList.add(new BadClass(3.14, "variable", "length"));

        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_FIELD_PRIVATE));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_FIELD_DEFAULT));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_FIELD_PROTECTED));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_FIELD_PUBLIC));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_FIELD_STATIC));
        badClassList.add(new BadClass(BadClass.PatchType.MODIFY_FIELD_FINAL));


        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PRIVATE));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_DEFAULT));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PROTECTED));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PUBLIC));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_MODIFIER_STATIC));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_MODIFIER_FINAL));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PRAM_PRIMITIVE_TYPE));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PRAM_WRAPPER_PRIMITIVE_TYPE));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PRAM_BOOLEAN));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PRAM_ARRAY));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_METHOD_PRAM_VARIABLE_LENGTH));

        badClassList.add(new BadClass(BadClass.PatchType.ADD_FIELD_PRIVATE));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_FIELD_DEFAULT));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_FIELD_PROTECTED));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_FIELD_PUBLIC));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_FIELD_STATIC));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_FIELD_FINAL));

        badClassList.add(new BadClass(BadClass.PatchType.ADD_CLASS_METHOD));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_CLASS_CONSTRUCTOR));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_CLASS_FIELD));
        badClassList.add(new BadClass(BadClass.PatchType.ADD_CLASS_INNER_CLASS));
    }

    class PatchAdapter extends BaseAdapter{

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
                convertView = new TextView(TestBadClassActivity.this);
                convertView.setPadding(0, dip2px(getApplicationContext(), 10), 0, dip2px(getApplicationContext(), 10));
            }
            BadClass badClass = (BadClass) getItem(position);
            ((TextView)convertView).setText(badClass.patchType.name());
            return convertView;
        }
    }

    /**
     * dip转换成px
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        Log.e("robust","******dip2px*** "+scale);
        return (int) (dipValue * scale + 0.5f);
    }


}
