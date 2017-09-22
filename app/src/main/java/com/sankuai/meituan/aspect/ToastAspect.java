package com.sankuai.meituan.aspect;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;

/**
 * Created by letty on 2016/12/13.
 */
@Aspect
public class ToastAspect {
    private static final String TAG = "toastAspect";

    @Pointcut("call(* android.widget.Toast+.show(..)) && (within(com.meituan..*)|| within(com.sankuai..*))")
    public void toastShow() {

    }

    @Pointcut("toastShow() && !cflowbelow(toastShow())")
    public void realToastShow() {
    }

    @Around("realToastShow()")
    public void toastShow(ProceedingJoinPoint point) {
        try {
            Toast toast = (Toast) point.getTarget();
            Context context = (Context) getValue(toast, "mContext");

            if (context == null || Build.VERSION.SDK_INT >= 19 && NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                //use system function
                point.proceed(point.getArgs());
            } else {
                //Toast params
                int mDuration = toast.getDuration();
                View mNextView = toast.getView();
                int mGravity = toast.getGravity();
                int mX = toast.getXOffset();
                int mY = toast.getYOffset();
                float mHorizontalMargin = toast.getHorizontalMargin();
                float mVerticalMargin = toast.getVerticalMargin();

                new MToast(context instanceof Application ? context : context.getApplicationContext())
                        .setDuration(mDuration)
                        .setView(mNextView)
                        .setGravity(mGravity, mX, mY)
                        .setMargin(mHorizontalMargin, mVerticalMargin).show();
            }
        } catch (Throwable exception) {
            //ignore
        }
    }
    // TODO: 2016/12/14  toast.cancel() can't be work with MToast

    /**
     * 通过字段名从对象或对象的父类中得到字段的值
     *
     * @param object    对象实例
     * @param fieldName 字段名
     * @return 字段对应的值
     * @throws Exception
     */
    public static Object getValue(Object object, String fieldName) throws Exception {
        if (object == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }
        Field field = null;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (Exception e) {
                //ignore
            }
        }

        return null;
    }
}