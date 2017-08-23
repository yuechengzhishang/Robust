package com.meituan.robust.autopatch

import com.meituan.robust.Constants
import com.meituan.robust.utils.JavaUtils
import javassist.*
import javassist.bytecode.AccessFlag
import javassist.bytecode.ClassFile
import javassist.expr.MethodCall
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
        //just keep  methods need patch
//        if (patchMethodSignureSet.size() != 0) {
            for (CtMethod method : modifiedClass.getDeclaredMethods()) {
//                if ((!patchMethodSignureSet.contains(method.getLongName()) || (!isInline && Config.methodMap.get(modifiedClass.getName() + "." + JavaUtils.getJavaMethodSignure(method)) == null))) {
//                    methodNoNeedPatchList.add(method);
//                } else {
//                    Config.methodNeedPatchSet.remove(method.getLongName());
//                }
            }
//        }
        String originalClassName = modifiedClass.getName()

        CtClass temPatchClass = cloneClass(modifiedClass, patchName, methodNoNeedPatchList);
        if (temPatchClass.getDeclaredMethods().length == 0) {
            //todo 方法被删除空了，需要做兼容
//            throw new RuntimeException("all methods in patch class are deteted,cannot find patchMethod in class " + temPatchClass.getName());
        }

        modifiedClass = Config.classPool.get(originalClassName);
        modifiedClass.defrost()
        JavaUtils.addPatchConstruct(temPatchClass, modifiedClass);
        CtMethod reaLParameterMethod = CtMethod.make(JavaUtils.getRealParamtersBody(temPatchClass.name), temPatchClass);
        temPatchClass.addMethod(reaLParameterMethod);

        dealWithSuperMethod(temPatchClass, modifiedClass, patchPath);

//        if (Config.supportProGuard&&ReadMapping.getInstance().getClassMapping(modifiedClass.getName()) == null) {
//            throw new RuntimeException(" something wrong with mappingfile ,cannot find  class  " + modifiedClass.getName() + "   in mapping file");
//        }
        List<CtMethod> invokeSuperMethodList = Config.invokeSuperMethodMap.getOrDefault(modifiedClass.getName(), new ArrayList<>());

        if (true){
            //把所有的方法访问属性都改成public
            changeMethodToPublicAndUnAbstract(temPatchClass)
        } else {
            createPublicMethodForPrivate(temPatchClass);
        }

        //执行替换
        for (CtMethod method : temPatchClass.getDeclaredMethods()) {
            if (!Config.addedSuperMethodList.contains(method) && !reaLParameterMethod.equals(method) && !method.getName().startsWith(Constants.ROBUST_PUBLIC_SUFFIX)) {
                method.instrument(
                        new RobustMethodExprEditor(modifiedClass,temPatchClass,method)
                );
//                method.instrument(
//                        new ExprEditor() {
//                            public void edit(FieldAccess f) throws CannotCompileException {
//                                if (Config.newlyAddedClassNameList.contains(f.getClassName())) {
//                                    return;
//                                }
//                                Map memberMappingInfo = getClassMappingInfo(f.getField().declaringClass.name);
//                                try {
//                                    if (f.isReader()) {
//                                        f.replace(ReflectUtils.getFieldString(f.getField(), memberMappingInfo, temPatchClass.getName(), modifiedClass.getName()));
//                                    } else if (f.isWriter()) {
//                                        f.replace(ReflectUtils.setFieldString(f.getField(), memberMappingInfo, temPatchClass.getName(), modifiedClass.getName()));
//                                    }
//                                } catch (NotFoundException e) {
//                                    e.printStackTrace();
//                                    throw new RuntimeException(e.getMessage());
//                                }
//                            }
//
//
//                            @Override
//                            void edit(NewExpr e) throws CannotCompileException {
//                                //inner class in the patched class ,not all inner class
//                                if (Config.newlyAddedClassNameList.contains(e.getClassName())||Config.noNeedReflectClassSet.contains(e.getClassName())) {
//                                    return;
//                                }
//
//                                try {
//                                    if (!ReflectUtils.isStatic(Config.classPool.get(e.getClassName()).getModifiers()) && JavaUtils.isInnerClassInModifiedClass(e.getClassName(), modifiedClass)) {
//                                        e.replace(ReflectUtils.getNewInnerClassString(e.getSignature(), temPatchClass.getName(), ReflectUtils.isStatic(Config.classPool.get(e.getClassName()).getModifiers()), getClassValue(e.getClassName())));
//                                        return;
//                                    }
//                                } catch (NotFoundException e1) {
//                                    e1.printStackTrace();
//                                }
//
//                                e.replace(ReflectUtils.getCreateClassString(e, getClassValue(e.getClassName()), temPatchClass.getName(), ReflectUtils.isStatic(method.getModifiers())));
//                            }
//
//                            @Override
//                            void edit(MethodCall m) throws CannotCompileException {
//
//                                //methods no need reflect
//                                if(Config.noNeedReflectClassSet.contains(m.method.declaringClass.name)){
//                                    return;
//                                }
//                                if (m.getMethodName().contains("lambdaFactory")) {
//                                    //method contain modifeid class
//                                    m.replace(ReflectUtils.getNewInnerClassString(m.getSignature(), temPatchClass.getName(), ReflectUtils.isStatic(method.getModifiers()), getClassValue(m.getClassName())));
//                                    return;
//                                }
//                                try {
//                                    if (!repalceInlineMethod(m, method, false)) {
//                                        Map memberMappingInfo = getClassMappingInfo(m.getMethod().getDeclaringClass().getName());
//                                        if (invokeSuperMethodList.contains(m.getMethod())) {
////                                        if (m.isSuper()) {
//                                            m.replace(ReflectUtils.invokeSuperString(m));
//                                            return;
//                                        }
//                                        m.replace(ReflectUtils.getMethodCallString(m, memberMappingInfo, temPatchClass, ReflectUtils.isStatic(method.getModifiers()), isInline));
//                                    }
//                                } catch (NotFoundException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                );
            }
        }
        //remove static code block,pay attention to the  class created by cloneClassWithoutFields which construct's
//        CtClass outerClass = cloneClassWithoutFields(temPatchClass, patchName, null);
//        outerClass = JavaUtils.addPatchConstruct(outerClass, modifiedClass);
        CtClass patchClass = temPatchClass;
        return patchClass;
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

        if (true){
            CtClass targetClass = Config.classPool.getOrNull(patchName);
            if (targetClass != null) {
                targetClass.defrost();
            }

            CtClass sourceClassTemp = sourceClass;
            sourceClassTemp.setName(patchName);
            try {
                sourceClassTemp.writeFile(Config.robustGenerateDirectory);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
            sourceClassTemp.defrost()
            return sourceClassTemp;
        }
        CtClass targetClass = Config.classPool.getOrNull(patchName);
        if (targetClass != null) {
            targetClass.defrost();
        }
        targetClass = Config.classPool.makeClass(patchName);
        targetClass.getClassFile().setMajorVersion(ClassFile.JAVA_7);
        //warning 所有的super问题均在assist class来处理,
        targetClass.setSuperclass(sourceClass.getSuperclass());
        for (CtField field : sourceClass.getDeclaredFields()) {
            targetClass.addField(new CtField(field, targetClass));
        }
        ClassMap classMap = new ClassMap();
        classMap.put(patchName, sourceClass.getName());
        classMap.fix(sourceClass);
        for (CtMethod method : sourceClass.getDeclaredMethods()) {
            if (null == exceptMethodList || !exceptMethodList.contains(method)) {
                CtMethod newCtMethod = new CtMethod(method, targetClass, classMap);
                targetClass.addMethod(newCtMethod);
            }
        }
        targetClass.setModifiers(AccessFlag.clear(targetClass.getModifiers(), AccessFlag.ABSTRACT));
        return targetClass;
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

    private Map getClassMappingInfo(String className) {
        ClassMapping classMapping = ReadMapping.getInstance().getClassMapping(className);
        if (null == classMapping) {
//            logger.warn("getClassMappingInfo~~~~~~~~~~~~~~~~class " + className + "  robust can not find in mapping ")
            classMapping = new ClassMapping();
        }
        return classMapping.getMemberMapping();
    }

    private String getClassValue(String className) {
        ClassMapping classMapping = ReadMapping.getInstance().getClassMappingOrDefault(className);
        if (classMapping.getValueName() == null) {
//            logger.warn("~~~~~~~~~~~~~~~~class " + className + "  robust can not find in mapping ")
            return className;
        } else {
            return classMapping.getValueName();
        }
    }

    public boolean repalceInlineMethod(MethodCall m, CtMethod method, boolean isNewClass) throws NotFoundException, CannotCompileException {
        ClassMapping classMapping = ReadMapping.getInstance().getClassMapping(m.getMethod().getDeclaringClass().getName());
        if (null != classMapping && classMapping.getMemberMapping().get(ReflectUtils.getJavaMethodSignureWithReturnType(m.getMethod())) == null) {
            m.replace(ReflectUtils.getInLineMemberString(m.getMethod(), ReflectUtils.isStatic(method.getModifiers()), isNewClass));
            return true;
        }
        return false;
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

    public static void changeMethodToPublicAndUnAbstract(CtClass ctClass){
        //方法访问属性转为public;并去掉abstract
        CtMethod[] ctMethods = ctClass.getDeclaredMethods();
        if (null == ctMethods || 0 == ctMethods.length){
            return;
        }
        for (CtMethod method : ctMethods) {
            int originModifiers = method.getModifiers();
            int publicModifiers = AccessFlag.setPublic(originModifiers);
            int unAbstractModifiers = AccessFlag.clear(publicModifiers,AccessFlag.ABSTRACT);
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
