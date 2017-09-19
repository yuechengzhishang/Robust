package com.meituan.sample.demo;

import com.meituan.robust.ChangeQuickRedirect;

/**
 * Created by hedingxu on 17/7/7.
 */

public class Test {
    public static void main(String[] args){
        OldPatch old = new OldPatch();
        OldPatch.setChangeQuickRedirect(new ChangeQuickRedirect() {
            @Override
            public Object accessDispatch(String methodName, Object[] paramArrayOfObject) {
                return null;
            }

            @Override
            public boolean isSupport(String methodName, Object[] paramArrayOfObject) {
                return false;
            }
        });
        old.method2();

        OldPatch2 changedOldPatch = new OldPatch2();
        changedOldPatch.testint2 = 22;
        changedOldPatch.testString = "changedOldPatch.testString";
        changedOldPatch.setBean(new Bean(123));
        OldPatch2 old1 = changedOldPatch;

        old1.method1();
    }
}
