package com.meituan.robust.autopatch;

import com.meituan.robust.Constants;
import com.meituan.robust.change.RobustChangeInfo;
import com.meituan.robust.utils.JavaUtils;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;

import static com.meituan.robust.Constants.INIT_ROBUST_PATCH;
import static com.meituan.robust.Constants.ORIGINCLASS;
import static com.meituan.robust.autopatch.Config.classPool;
import static com.meituan.robust.utils.JavaUtils.booleanPrimeType;

/**
 * Created by mivanzhang on 17/2/9.
 * <p>
 * create patch control classes,which dispatch patch methods
 */

public class PatchesControlFactory {
    private static PatchesControlFactory patchesControlFactory = new PatchesControlFactory();

    private PatchesControlFactory() {

    }

    private static CtClass createControlClass(CtClass modifiedClass) throws Exception {
        System.err.println("createControlClass name: " + modifiedClass.getName());
        CtClass patchClass = classPool.get(NameManger.getInstance().getPatchName(modifiedClass.getName()));
        patchClass.defrost();
        CtClass controlClass = classPool.getAndRename(Constants.PATCH_TEMPLATE_FULL_NAME, NameManger.getInstance().getPatchControlName(modifiedClass.getSimpleName()));
        controlClass.getDeclaredMethod("accessDispatch").insertBefore(getAccessDispatchMethodBody(patchClass, modifiedClass.getName()));
        controlClass.getDeclaredMethod("isSupport").insertBefore(getIsSupportMethodBody(patchClass, modifiedClass.getName()));
        controlClass.defrost();
        return controlClass;
    }

    private
    static String getAccessDispatchMethodBody(CtClass patchClass, String modifiedClassName) throws NotFoundException {
        //replace paramArrayOfObject to $2
        StringBuilder accessDispatchMethodBody = new StringBuilder();
        if (Config.catchReflectException) {
            accessDispatchMethodBody.append("try{");
        }
        if (Constants.isLogging) {
            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"arrivied in AccessDispatch \"+methodName+\" paramArrayOfObject  \");");
        }
        //create patch instance
        accessDispatchMethodBody.append(patchClass.getName() + " patch = new " + patchClass.getName() + "();\n");
        accessDispatchMethodBody.append(" String isStatic=$1.split(\":\")[2];");
        accessDispatchMethodBody.append(" if (isStatic.equals(\"false\")) {\n");
        accessDispatchMethodBody.append("patch." + ORIGINCLASS + " = (" + modifiedClassName + ")$2[$2.length - 1];");
//        accessDispatchMethodBody.append(" if (keyToValueRelation.get(paramArrayOfObject[paramArrayOfObject.length - 1]) == null) {\n");
//        if (Constants.isLogging) {
//            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"keyToValueRelation not contain\" );");
//        }
//        accessDispatchMethodBody.append("patch=new " + patchClass.getName() + "(("+ modifiedClassName+")paramArrayOfObject[paramArrayOfObject.length - 1]);\n");
//        accessDispatchMethodBody.append(" keyToValueRelation.put(paramArrayOfObject[paramArrayOfObject.length - 1], patch);\n");
//        accessDispatchMethodBody.append("}else{");
//        accessDispatchMethodBody.append("patch=(" + patchClass.getName() + ") keyToValueRelation.get(paramArrayOfObject[paramArrayOfObject.length - 1]);\n");
//        accessDispatchMethodBody.append("}");
        accessDispatchMethodBody.append("}\n");
//        accessDispatchMethodBody.append("else{");
//        if (Constants.isLogging) {
//            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"static method forward \" );");
//        }
//        accessDispatchMethodBody.append("patch=new " + patchClass.getName() + "();\n");
//        accessDispatchMethodBody.append("}");
        accessDispatchMethodBody.append("String methodNo=$1.split(\":\")[3];\n");
        if (Constants.isLogging) {
            accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"assemble method number  is  \" + methodNo);");
        }

        for (CtMethod method : getModifiedCtMethod(patchClass)) {
            CtClass[] parametertypes = method.getParameterTypes();
            String methodSignure = JavaUtils.getJavaMethodSignure(method).replaceAll(patchClass.getName(), modifiedClassName);
            String methodLongName = modifiedClassName + "." + methodSignure;
            String methodNumber = Config.methodMap.get(methodLongName);
            if (methodLongName.contains(INIT_ROBUST_PATCH)) {
                String tempMethodLongName = methodLongName.replace(INIT_ROBUST_PATCH, "<init>");
                methodNumber = Config.methodMap.get(tempMethodLongName);
            }
            //just Forward methods with methodNumber
            if (methodNumber != null) {
                accessDispatchMethodBody.append(" if((\"" + methodNumber + "\").equals(methodNo)){\n");
                if (Constants.isLogging) {
                    accessDispatchMethodBody.append("  android.util.Log.d(\"robust\",\"invoke method is " + method.getLongName() + " \" );");
                }
                String methodName = method.getName();
                if (AccessFlag.isPrivate(method.getModifiers())) {
                    methodName = Constants.ROBUST_PUBLIC_SUFFIX + method.getName();
                }
                if (method.getReturnType().getName().equals("void")) {
                    accessDispatchMethodBody.append("(patch." + methodName + "(");
                } else {
                    switch (method.getReturnType().getName()) {
                        case "boolean":
                            accessDispatchMethodBody.append("return Boolean.valueOf(patch." + methodName + "(");
                            break;
                        case "byte":
                            accessDispatchMethodBody.append("return Byte.valueOf(patch." + methodName + "(");
                            break;
                        case "char":
                            accessDispatchMethodBody.append("return Character.valueOf(patch." + methodName + "(");
                            break;
                        case "double":
                            accessDispatchMethodBody.append("return Double.valueOf(patch." + methodName + "(");
                            break;
                        case "float":
                            accessDispatchMethodBody.append("return Float.valueOf(patch." + methodName + "(");
                            break;
                        case "int":
                            accessDispatchMethodBody.append("return Integer.valueOf(patch." + methodName + "(");
                            break;
                        case "long":
                            accessDispatchMethodBody.append("return Long.valueOf(patch." + methodName + "(");
                            break;
                        case "short":
                            accessDispatchMethodBody.append("return Short.valueOf(patch." + methodName + "(");
                            break;
                        default:
                            accessDispatchMethodBody.append("return (patch." + methodName + "(");
                            break;
                    }
                }
                for (int index = 0; index < parametertypes.length; index++) {
                    if (booleanPrimeType(parametertypes[index].getName())) {
                        accessDispatchMethodBody.append("((" + JavaUtils.getWrapperClass(parametertypes[index].getName()) + ") (fixObj($2[" + index + "]))");
                        accessDispatchMethodBody.append(")" + JavaUtils.wrapperToPrime(parametertypes[index].getName()));
                        if (index != parametertypes.length - 1) {
                            accessDispatchMethodBody.append(",");
                        }
                    } else {
                        accessDispatchMethodBody.append("((" + JavaUtils.getWrapperClass(parametertypes[index].getName()) + ") ($2[" + index + "])");
                        accessDispatchMethodBody.append(")" + JavaUtils.wrapperToPrime(parametertypes[index].getName()));
                        if (index != parametertypes.length - 1) {
                            accessDispatchMethodBody.append(",");
                        }
                    }
                }
                accessDispatchMethodBody.append("));}\n");
            }
        }
        if (Config.catchReflectException) {
            accessDispatchMethodBody.append(" } catch (Throwable e) {");
            accessDispatchMethodBody.append(" e.printStackTrace();}");
        }
        return accessDispatchMethodBody.toString();
    }

    private static List<CtMethod> getModifiedCtMethod(CtClass patchClass) {
        List<CtMethod> modifiedCtMethodList = new ArrayList<CtMethod>();
        for (CtMethod ctMethod : patchClass.getDeclaredMethods()) {
            if (RobustChangeInfo.isChangedMethod(ctMethod)) {
                modifiedCtMethodList.add(ctMethod);
            }

            String modifiedClassName = patchClass.getName();
            if (modifiedClassName.endsWith("Patch")) {
                modifiedClassName = modifiedClassName + "ROBUST_FOR_DELETE";
                String tempStr = "Patch" + "ROBUST_FOR_DELETE";
                modifiedClassName = modifiedClassName.replace(tempStr, "");
            }

            //对于内部类带来的方法修改，需要处理
            if (AnonymousClassOuterClassMethodUtils.isModifiedByAnonymous(modifiedClassName, ctMethod)) {
                if (modifiedCtMethodList.contains(ctMethod)){
                   //ignore
                } else {
                    modifiedCtMethodList.add(ctMethod);
                }

            }
        }
        return modifiedCtMethodList;
    }

    private static String getIsSupportMethodBody(CtClass patchClass, String modifiedClassName) throws NotFoundException {
        //replace paramArrayOfObject to $2
        StringBuilder isSupportBuilder = new StringBuilder();
        StringBuilder methodsIdBuilder = new StringBuilder();
        if (Constants.isLogging) {
            isSupportBuilder.append("  android.util.Log.d(\"robust\",\"arrivied in isSupport \"+methodName+\" paramArrayOfObject  \");");
        }
        isSupportBuilder.append("String methodNo=$1.split(\":\")[3];\n");
        if (Constants.isLogging) {
            isSupportBuilder.append("  android.util.Log.d(\"robust\",\"in isSupport assemble method number  is  \" + methodNo);");
        }
        for (CtMethod method : getModifiedCtMethod(patchClass)) {
            String methodSignure = JavaUtils.getJavaMethodSignure(method).replaceAll(patchClass.getName(), modifiedClassName);
            String methodLongName = modifiedClassName + "." + methodSignure;
            String methodNumber = Config.methodMap.get(methodLongName);
            //just Forward methods with methodNumber

            if (methodNumber == null){
                if (methodLongName.contains(INIT_ROBUST_PATCH)) {
                    String tempMethodLongName = methodLongName.replace(INIT_ROBUST_PATCH, "<init>");
                    methodNumber = Config.methodMap.get(tempMethodLongName);
                }
            }

            if (methodNumber != null) {
                methodsIdBuilder.append(methodNumber + ":");
            }
        }

        if (Constants.isLogging) {
            isSupportBuilder.append("  android.util.Log.d(\"robust\",\"arrivied in isSupport \"+methodName+\" paramArrayOfObject  \" +\" isSupport result is \"+\"" + methodsIdBuilder.toString() + "\".contains(methodNo));");
        }
        isSupportBuilder.append("return \"" + methodsIdBuilder.toString() + "\".contains(methodNo);");
        return isSupportBuilder.toString();
    }


    public static CtClass createPatchesControl(CtClass modifiedClass) throws Exception {
        return createControlClass(modifiedClass);
    }

}
