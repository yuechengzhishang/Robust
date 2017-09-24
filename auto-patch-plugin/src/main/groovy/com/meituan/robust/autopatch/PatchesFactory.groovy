package com.meituan.robust.autopatch

import com.meituan.robust.Constants
import com.meituan.robust.change.AspectJUtils
import com.meituan.robust.change.RobustChangeInfo
import com.meituan.robust.change.comparator.ByteCodeUtils
import com.meituan.robust.utils.JavaUtils
import com.meituan.robust.utils.OuterClassMethodAnonymousClassUtils
import com.meituan.robust.utils.ProguardUtils
import com.meituan.robust.utils.RobustLog
import javassist.*
import javassist.bytecode.AccessFlag
import javassist.bytecode.ClassFile
/**
 * Created by zhangmeng on 16/12/2.
 * <p>
 * create patch ctclass
 */
class PatchesFactory {
    private static PatchesFactory patchesFactory = new PatchesFactory();

    private PatchesFactory() {

    }

    /****
     * @param modifiedClass
     * @param isInline
     * @param patchName
     * @param patchMethodSignureSet methods need patch,if patchMethodSignureSet length is 0,then will patch all methods in modifiedClass
     * @return
     */
    private CtClass createPatchClass(CtClass modifiedClass, boolean isInline, String patchName, Set patchMethodSignureSet, String patchPath) throws CannotCompileException, IOException, NotFoundException {
        List methodNoNeedPatchList = new ArrayList();

        String originalClassName = modifiedClass.getName()

        CtClass temPatchClass = cloneClass(modifiedClass, patchName, methodNoNeedPatchList);

        //把所有的方法访问属性都改成public
        changeMethodUnAbstract(temPatchClass)

        List<CtMethod> willDeleteCtMethods = new ArrayList<CtMethod>();
        for (CtMethod ctMethod : temPatchClass.getDeclaredMethods()) {
            if (AspectJUtils.isAjc$preClinitMethod(ctMethod.getName())
                    || AspectJUtils.isAroundBodyMethod(ctMethod.getName())){
                //ignore
            } else {
                if (RobustChangeInfo.isInvariantMethod(ctMethod) && !RobustChangeInfo.isChangedMethod(ctMethod)) {
//                temPatchClass.removeMethod(ctMethod)
                    willDeleteCtMethods.add(ctMethod)
                }
            }
        }

        List<CtConstructor> willDeleteCtConstructors = new ArrayList<CtConstructor>();
        for (CtConstructor ctConstructor : temPatchClass.getDeclaredConstructors()) {
            //删除所有的构造函数
//            temPatchClass.removeConstructor(ctConstructor)
            willDeleteCtConstructors.add(ctConstructor)
        }

        List<CtConstructor> willDeleteCtFields = new ArrayList<CtField>();
        for (CtField ctField : temPatchClass.getDeclaredFields()) {
            if (com.meituan.robust.change.RobustChangeInfo.isInvariantField(ctField)) {
//                temPatchClass.removeField(ctField)
                if (isChangeQuickRedirectFieldForPatchClass(ctField)){
                    //保留这个patch class的ChangeQuickRedirectField
                    //java.lang.NoSuchFieldError: com.meituan.sample.TestPatchActivityPatch.changeQuickRedirect
                }else if (ProguardUtils.isAspectJField(ctField)){
                    //保留
                }
                else {
                    willDeleteCtFields.add(ctField)
                }
            }
        }

        modifiedClass = Config.classPool.get(originalClassName);
        modifiedClass.defrost()
        JavaUtils.addField_OriginClass(temPatchClass, modifiedClass);

        CtMethod reaLParameterMethod = CtMethod.make(JavaUtils.getRealParamtersBody(temPatchClass.name), temPatchClass);
        temPatchClass.addMethod(reaLParameterMethod);

        dealWithSuperMethod(temPatchClass, modifiedClass, patchPath);

//        if (Config.supportProGuard&&ReadMapping.getInstance().getClassMapping(modifiedClass.getName()) == null) {
//            throw new RuntimeException(" something wrong with mappingfile ,cannot find  class  " + modifiedClass.getName() + "   in mapping file");
//        }



        temPatchClass.writeFile(patchPath)
        temPatchClass.defrost()

        //执行替换
        for (CtMethod method : temPatchClass.getDeclaredMethods()) {
            //对于内部类带来的方法修改，需要处理
            if (OuterClassMethodAnonymousClassUtils.isModifiedByAnonymous(modifiedClass.name,method)){

            } else {
                if (willDeleteCtMethods.contains(method)) {
                    continue;
                }
                if (method.name.equals("<init>")) {
                    continue;
                }
            }
            if (!Config.addedSuperMethodList.contains(method) && !reaLParameterMethod.equals(method) && !method.getName().startsWith(Constants.ROBUST_PUBLIC_SUFFIX)) {
                if (AspectJUtils.isAjc$preClinitMethod(method.getName())
                || AspectJUtils.isAroundBodyMethod(method.getName())){
                    //ignore
                } else {
                    method.instrument(
                            new RobustMethodExprEditor(modifiedClass, temPatchClass, method)
                    );
                }
            }
        }

        temPatchClass.writeFile(patchPath)
        temPatchClass.defrost()

        for (CtMethod ctMethod : willDeleteCtMethods) {
            temPatchClass.removeMethod(ctMethod)
        }

        temPatchClass.writeFile(patchPath)
        temPatchClass.defrost()

        for (CtConstructor ctConstructor : willDeleteCtConstructors) {
            temPatchClass.removeConstructor(ctConstructor)
        }

        temPatchClass.writeFile(patchPath)
        temPatchClass.defrost()
        for (CtField ctField : willDeleteCtFields) {
            temPatchClass.removeField(ctField)
        }

        //暂时先删除掉classInitMethod 解决如果改了静态字段的赋值问题
        try {
            for (CtBehavior ctBehavior: temPatchClass.getDeclaredBehaviors()){
                if (ctBehavior instanceof CtConstructor){
                    CtConstructor ctConstructor1 = (CtConstructor)ctBehavior;
                    if (ctConstructor1.name.equals(ByteCodeUtils.CLASS_INITIALIZER)){
                        temPatchClass.removeConstructor(ctConstructor1);
                    }
                }
            }
        } catch (NotFoundException e){
//            有的有clinit，有的没有这个
//            e.printStackTrace()
        }

        CtConstructor ctConstructor = CtNewConstructor.defaultConstructor(temPatchClass);
        temPatchClass.addConstructor(ctConstructor)

        temPatchClass.setSuperclass(Config.classPool.get("java.lang.Object"));
        temPatchClass.setInterfaces(null);
        for (CtMethod ctMethod:temPatchClass.getDeclaredMethods()){
            if (AspectJUtils.isAjc$preClinitMethod(ctMethod.name)){
                //need add <clinit> or call ajc$pre before init
                CtField isCall_AJCPRE_CLINITField = new CtField(Config.classPool.get("java.lang.Boolean"), "isCall_AJCPRE_CLINIT", temPatchClass);
                isCall_AJCPRE_CLINITField.setModifiers(Modifier.STATIC);
                temPatchClass.addField(isCall_AJCPRE_CLINITField);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                    stringBuilder.append("if(null == isCall_AJCPRE_CLINIT || false == isCall_AJCPRE_CLINIT.booleanValue()){");
                    stringBuilder.append(AspectJUtils.AJC$PRE_CLINIT + "();");
                    stringBuilder.append("isCall_AJCPRE_CLINIT = new java.lang.Boolean(true);");
                    stringBuilder.append("}");
                stringBuilder.append("}");
                ctConstructor.insertBeforeBody(stringBuilder.toString())
                break;
            }
        }
        CtClass patchClass = temPatchClass;
        return patchClass;
    }

    public static boolean isChangeQuickRedirectFieldForPatchClass(CtField ctField){
        if (ctField.type.getName().equals(Constants.INTERFACE_NAME)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param sourceClass
     * @param targetClassName
     * @return targetClass
     * @description targetClasses created by copy methods ,not by name
     */
    private
    static CtClass cloneClassWithoutFields(CtClass sourceClass, String patchName, List<CtMethod> exceptMethodList) throws NotFoundException, CannotCompileException {
        CtClass targetClass = cloneClass(sourceClass, patchName, exceptMethodList);
        targetClass.declaredFields.each { field ->
            targetClass.removeField(field);
        }
        //patch class shouldn`t have super class,we may be unable to initialize super class
        targetClass.setSuperclass(Config.classPool.get("java.lang.Object"));
        return targetClass;
    }

    public
    static CtClass cloneClass(CtClass sourceClass, String patchName, List<CtMethod> exceptMethodList) throws CannotCompileException, NotFoundException {

        CtClass targetClass = Config.classPool.getOrNull(patchName);
        if (targetClass != null) {
            targetClass.defrost();
        }

        CtClass sourceClassTemp = sourceClass;
        sourceClassTemp.setName(patchName);
        try {
            sourceClassTemp.writeFile(Config.robustGenerateDirectory);
        } catch (CannotCompileException e) {
            RobustLog.log("CannotCompileException ",e);
        }
        sourceClassTemp.defrost()
        sourceClassTemp.getClassFile().setMajorVersion(ClassFile.JAVA_7);
        return sourceClassTemp;
    }

    private void dealWithSuperMethod(CtClass patchClass, CtClass modifiedClass, String patchPath) throws NotFoundException, CannotCompileException, IOException {
        StringBuilder methodBuilder;
        List<CtMethod> invokeSuperMethodList = Config.invokeSuperMethodMap.getOrDefault(modifiedClass.getName(), new ArrayList());
        for (int index = 0; index < invokeSuperMethodList.size(); index++) {
            methodBuilder = new StringBuilder();
            if (invokeSuperMethodList.get(index).getParameterTypes().length > 0) {
                methodBuilder.append("public  static " + invokeSuperMethodList.get(index).getReturnType().getName() + "  " + ReflectUtils.getStaticSuperMethodName(invokeSuperMethodList.get(index).getName())
                        + "(" + patchClass.getName() + " patchInstance," + modifiedClass.getName() + " modifiedInstance," + JavaUtils.getParameterSignure(invokeSuperMethodList.get(index)) + "){");
            } else {
                methodBuilder.append("public  static  " + invokeSuperMethodList.get(index).getReturnType().getName() + "  " + ReflectUtils.getStaticSuperMethodName(invokeSuperMethodList.get(index).getName())
                        + "(" + patchClass.getName() + " patchInstance," + modifiedClass.getName() + " modifiedInstance){");
            }
            if (Constants.isLogging) {
                methodBuilder.append("android.util.Log.d(\"robust\", \" invoke  " + invokeSuperMethodList.get(index).getLongName() + " staticRobust method \");");
            }
            if (AccessFlag.isPackage(invokeSuperMethodList.get(index).getModifiers())) {
                throw new RuntimeException("autopatch does not support super method with package accessible ");
            }

            CtClass assistClass = PatchesAssistFactory.createAssistClass(modifiedClass, patchClass.getName(), invokeSuperMethodList.get(index));
            assistClass.writeFile(patchPath);

            if (invokeSuperMethodList.get(index).getReturnType().equals(CtClass.voidType)) {
                methodBuilder.append(NameManger.getInstance().getAssistClassName(patchClass.getName()) + "." + ReflectUtils.getStaticSuperMethodName(invokeSuperMethodList.get(index).getName())
                        + "(patchInstance,modifiedInstance");
            } else {
                methodBuilder.append(" return " + NameManger.getInstance().getAssistClassName(patchClass.getName()) + "." + ReflectUtils.getStaticSuperMethodName(invokeSuperMethodList.get(index).getName())
                        + "(patchInstance,modifiedInstance");
            }
            if (invokeSuperMethodList.get(index).getParameterTypes().length > 0) {
                methodBuilder.append(",");
            }
            methodBuilder.append(JavaUtils.getParameterValue(invokeSuperMethodList.get(index).getParameterTypes().length) + ");");
            methodBuilder.append("}");
            CtMethod ctMethod = CtMethod.make(methodBuilder.toString(), patchClass);
            Config.addedSuperMethodList.add(ctMethod);
            patchClass.addMethod(ctMethod);
        }
    }

    public
    static void createPublicMethodForPrivate(CtClass ctClass) throws CannotCompileException, NotFoundException {
        //内联的方法是private,需要转为public
        List<CtMethod> privateMethodList = new ArrayList<>();
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (AccessFlag.isPrivate(method.getModifiers())) {
                privateMethodList.add(method);
            }
        }
        StringBuilder private2PublicMethod;
        for (CtMethod method : privateMethodList) {
            private2PublicMethod = new StringBuilder();
            private2PublicMethod.append("public  " + getMethodStatic(method) + " " + method.getReturnType().getName() + " " + Constants.ROBUST_PUBLIC_SUFFIX + method.getName() + "(" + JavaUtils.getParameterSignure(method) + "){");
            private2PublicMethod.append("return " + method.getName() + "(" + JavaUtils.getParameterValue(method.getParameterTypes().length) + ");");
            private2PublicMethod.append("}");
            ctClass.addMethod(CtMethod.make(private2PublicMethod.toString(), ctClass));
        }

    }

    public static void changeMethodUnAbstract(CtClass ctClass) {
        //并去掉abstract
        CtMethod[] ctMethods = ctClass.getDeclaredMethods();
        if (null == ctMethods || 0 == ctMethods.length) {
            return;
        }
        for (CtMethod method : ctMethods) {
            if (RobustChangeInfo.isInvariantMethod(method)){
                return;
            }
            int originModifiers = method.getModifiers();
//            int publicModifiers = AccessFlag.setPublic(originModifiers);
            int unAbstractModifiers = AccessFlag.clear(originModifiers, AccessFlag.ABSTRACT);
            method.setModifiers(unAbstractModifiers);
        }
    }

    private static String getMethodStatic(CtMethod method) {
        //内联的方法是private,需要转为public
        if (ReflectUtils.isStatic(method.getModifiers())) {
            return " static ";
        }
        return "";
    }
    /****
     * @param modifiedClass
     * @param isInline
     * @param patchName
     * @param patchMethodSignureSet methods need patch,if patchMethodSignureSet length is 0,then will patch all methods in modifiedClass
     * @return
     */

    public
    static CtClass createPatch(String patchpath, CtClass modifiedClass, boolean isInline, String patchName, Set patchMethodSignureSet) throws NotFoundException, CannotCompileException, IOException {
        return patchesFactory.createPatchClass(modifiedClass, isInline, patchName, patchMethodSignureSet, patchpath);
    }
}
