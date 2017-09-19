//package robust.gradle.plugin.asm;
//import org.objectweb.asm.*;
//
//import static org.objectweb.asm.Opcodes.*;
//
///**
// * Created by hedingxu on 17/8/21.
// * TODO 解决新增activity生命周期的函数
// */
//
//public class RobustActivityUtils {
//    private RobustActivityUtils(){
//
//    }
//
//    public static boolean isActivityClass(){
//        return false;
//    }
//
//    public static byte[] dump() throws Exception {
//
//        ClassWriter cw = new ClassWriter(0);
//        FieldVisitor fv;
//        MethodVisitor mv;
//        AnnotationVisitor av0;
//
//        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, "com/example/hedingxu/myapplication/RobustActivity", null, "android/app/Activity", null);
//
//        cw.visitSource("RobustActivity.java", null);
//
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(9, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "<init>", "()V", false);
//            mv.visitInsn(RETURN);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l1, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PROTECTED, "onCreate", "(Landroid/os/Bundle;)V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(13, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "onCreate", "(Landroid/os/Bundle;)V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(14, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l2, 0);
//            mv.visitLocalVariable("savedInstanceState", "Landroid/os/Bundle;", null, l0, l2, 1);
//            mv.visitMaxs(2, 2);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PROTECTED, "onStart", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(18, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "onStart", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(19, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PROTECTED, "onRestart", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(23, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "onRestart", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(24, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PROTECTED, "onStop", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(28, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "onStop", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(29, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PROTECTED, "onResume", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(33, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "onResume", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(34, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PROTECTED, "onPause", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(38, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "onPause", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(39, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PROTECTED, "onDestroy", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(43, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Activity", "onDestroy", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(44, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustActivity;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        cw.visitEnd();
//
//        return cw.toByteArray();
//    }
//}
//
//
//
