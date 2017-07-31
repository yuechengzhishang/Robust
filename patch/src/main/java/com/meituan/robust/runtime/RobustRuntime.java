package com.meituan.robust.runtime;

import com.meituan.robust.ChangeQuickRedirect;

/**
 * Created by hedingxu on 17/7/31.
 */

public class RobustRuntime {

    public static class AccessFlag {
        public static final int STATIC = 0x0008;
    }

    public static void copyFields(Object srcObj, Object descObj) {
        Class srcClass = srcObj.getClass();
        Class descClass = descObj.getClass();
        java.lang.reflect.Field[] fields = srcClass.getDeclaredFields();
        if (null == fields || 0 == fields.length) {
            return;
        }

        for (java.lang.reflect.Field field : fields) {
            if (isStatic(field)) {
                Object fieldValue = AndroidInstantRuntime.getStaticPrivateField(srcClass, field.getName());
                //ignore ChangeQuickRedirectField
                boolean isChangeQuickRedirectField = fieldValue instanceof ChangeQuickRedirect;
                if (!isChangeQuickRedirectField) {
                    AndroidInstantRuntime.setStaticPrivateField(fieldValue, descClass, field.getName());
                }

            } else {
                Object fieldValue = AndroidInstantRuntime.getPrivateField(srcObj, srcClass, field.getName());
                AndroidInstantRuntime.setPrivateField(descObj, fieldValue, descClass, field.getName());
            }
        }
    }

    static boolean isStatic(java.lang.reflect.Field field) {
        boolean isStatic = false;
        isStatic = isStatic(field.getModifiers());
        return isStatic;
    }

    static boolean isStatic(int modifiers) {
        return (modifiers & AccessFlag.STATIC) != 0;
    }
}
