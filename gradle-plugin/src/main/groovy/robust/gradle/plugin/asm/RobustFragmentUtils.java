//package robust.gradle.plugin.asm;
//import org.objectweb.asm.*;
//
//import static org.objectweb.asm.Opcodes.*;
///**
// * Created by hedingxu on 17/8/21.
// * TODO 解决新增Fragment生命周期的函数
// */
//
//public class RobustFragmentUtils {
//    private RobustFragmentUtils(){
//
//    }
//
//    public static boolean isFragmentClass(){
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
//        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, "com/example/hedingxu/myapplication/RobustFragment", null, "android/app/Fragment", null);
//
//        cw.visitSource("RobustFragment.java", null);
//
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(11, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "<init>", "()V", false);
//            mv.visitInsn(RETURN);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l1, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(14, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onCreate", "(Landroid/os/Bundle;)V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(15, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitLocalVariable("savedInstanceState", "Landroid/os/Bundle;", null, l0, l2, 1);
//            mv.visitMaxs(2, 2);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onViewCreated", "(Landroid/view/View;Landroid/os/Bundle;)V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(19, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitVarInsn(ALOAD, 2);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onViewCreated", "(Landroid/view/View;Landroid/os/Bundle;)V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(20, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitLocalVariable("view", "Landroid/view/View;", null, l0, l2, 1);
//            mv.visitLocalVariable("savedInstanceState", "Landroid/os/Bundle;", null, l0, l2, 2);
//            mv.visitMaxs(3, 3);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onActivityCreated", "(Landroid/os/Bundle;)V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(24, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onActivityCreated", "(Landroid/os/Bundle;)V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(25, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitLocalVariable("savedInstanceState", "Landroid/os/Bundle;", null, l0, l2, 1);
//            mv.visitMaxs(2, 2);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onStart", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(29, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onStart", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(30, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onResume", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(34, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onResume", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(35, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onStop", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(39, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onStop", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(40, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onDestroy", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(44, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onDestroy", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(45, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "onDetach", "()V", null, null);
//            mv.visitCode();
//            Label l0 = new Label();
//            mv.visitLabel(l0);
//            mv.visitLineNumber(49, l0);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "android/app/Fragment", "onDetach", "()V", false);
//            Label l1 = new Label();
//            mv.visitLabel(l1);
//            mv.visitLineNumber(50, l1);
//            mv.visitInsn(RETURN);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitLocalVariable("this", "Lcom/example/hedingxu/myapplication/RobustFragment;", null, l0, l2, 0);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
//        cw.visitEnd();
//
//        return cw.toByteArray();
//    }
//
//}
