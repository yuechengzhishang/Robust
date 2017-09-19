package com.meituan.robust.autopatch

import com.meituan.robust.Constants
import com.meituan.robust.utils.JavaUtils
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.bytecode.AccessFlag
import javassist.expr.MethodCall
import javassist.expr.NewExpr
import robust.gradle.plugin.AutoPatchTransform

class ReflectUtils {

    //FieldAccess
//            $0	The object containing the field accessed by the expression. This is not equivalent to this.
//                    this represents the object that the method including the expression is invoked on.
//            $0 is null if the field is static.
    //如果FieldAccess is non-static and outerMethod is non-static =>  FieldAccess has this
//            if (!isOuterMethodStatic && !isStatic) { //有this存在的环境
//
//                if (AccessFlag.isPublic(field.modifiers)){
//                    //没有考虑新增field,如果是新增field，需要区分出来，新增/修改的field需要使用this调用
//                    stringBuilder.append("if(\$0 == this) { ");
//                        stringBuilder.append("\$_=(\$r) " + "((" + patchClassName + ")this)." + Constants.ORIGINCLASS + "."+field.name+";");
//                    stringBuilder.append("} ")
//                    stringBuilder.append("else            { "+ "\$_ = \$proceed(\$\$);" + " }" );
//                } else {
//
//                    stringBuilder.append("if(\$0 == this) {");
//                    stringBuilder.append("\$_=(\$r) " + "((" + patchClassName + ")\$0)." + Constants.ORIGINCLASS + "."+field.name+";");
//                    stringBuilder.append("} ")
//                    stringBuilder.append("else            { "+ "\$_ = \$proceed(\$\$);" + " }" );
//                }
//            } else {
//                if (AccessFlag.isPublic(field.modifiers)){
//
//                }
//            }
    //如果在TestPatchActivity 里面 TestPatchActivity testPatchActivity =  new TestPatchActivity(); 这种情况好像没有考虑
    //如何避免TestPatchActivity testPatchActivity =  new TestPatchActivity();
    //int xx = testPatchActivity.xx; vs int xx = this.xx;
    //这种情况如何处理？其中方法里还包括这样的代码
    //int xx = testPatchActivity.getxx(); vs int xx = this.getxx();
    // 在方法的处理中？？？ 可以判断 testPatchActivity 是否 == this，来区分
//            if (AccessFlag.isPublic(field.modifiers)) {
//                if (field.declaringClass.name.equals(patchClassName)) { //如果是子类的protected属性呢？
//                    stringBuilder.append("\$_=(\$r) " + "((" + patchClassName + ")\$0)." + Constants.ORIGINCLASS + "." + field.name + ";");
//                } else {
//                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".getFieldValue(\"" + field.name + "\",instance,${field.declaringClass.name}.class);");
//                }
//            }

    public static final Boolean INLINE_R_FILE = true;
    public static int invokeCount = 0;

    public static String getFieldString2(CtField field, String patchClassName, String modifiedClassName) {
        boolean isStatic = isStatic(field.modifiers);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        if (isStatic) {
            if (AccessFlag.isPublic(field.modifiers)) {
                //deal with android R file
                if (INLINE_R_FILE && isRFile(field.declaringClass.name)) {
                    println("getFieldString static field " + field.getName() + "   is R file macthed   " + field.declaringClass.name)
                    stringBuilder.append("\$_ = " + field.constantValue + ";");
                } else {
                    if (field.declaringClass.name.equals(patchClassName)) {
                        stringBuilder.append("\$_ = " +modifiedClassName +"."+ field.name +";");
                    } else {
                        stringBuilder.append("\$_ = \$proceed(\$\$);");
                    }
                }
            } else {
                if (field.declaringClass.name.equals(patchClassName)) {
                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".getStaticFieldValue(\"" + field.name + "\"," + modifiedClassName + ".class);");

                } else {
                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".getStaticFieldValue(\"" + field.name + "\"," + field.declaringClass.name + ".class);");
                }
            }
            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"get static  value is \" +\"" + (field.getName()) + "    ${getCoutNumber()}\");");
            }
        } else {
            if (AccessFlag.isPublic(field.modifiers)) {

                stringBuilder.append(" if(\$0 instanceof " + patchClassName + "){");
                stringBuilder.append("\$_ =(\$r)((" + patchClassName + ")\$0)." + Constants.ORIGINCLASS + "." + field.name + ";");
                stringBuilder.append("}else{");
                stringBuilder.append("\$_ = \$proceed(\$\$);");
                stringBuilder.append("}");
            } else {
                stringBuilder.append("java.lang.Object instance;");
                stringBuilder.append(" if(\$0 instanceof " + patchClassName + "){");
                stringBuilder.append("instance=((" + patchClassName + ")\$0)." + Constants.ORIGINCLASS + ";")
                stringBuilder.append("}else{");
                stringBuilder.append("instance=\$0;");
                stringBuilder.append("}");
                if (field.declaringClass.name.equals(patchClassName)) {
                    //如果是子类的protected属性呢？
                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".getFieldValue(\"" + field.name + "\",instance,${modifiedClassName}.class);");
                } else {
                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".getFieldValue(\"" + field.name + "\",instance,${field.declaringClass.name}.class);");
                }
            }
            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"get value is \" +\"" + (field.getName()) + "    ${getCoutNumber()}\");");
            }
        }
        stringBuilder.append("}");
//        println field.getName() + "  get field repalce  by  " + stringBuilder.toString() + "\n"
        return stringBuilder.toString();
    }

    public static String setFieldString2(CtField field, String patchClassName, String modifiedClassName) {
        //mTopBarFragment.viewCreatedCallback = this;
        boolean isStatic = isStatic(field.modifiers)
        StringBuilder stringBuilder = new StringBuilder("{");
        if (isStatic) {
//            println("setFieldString static field " + field.getName() + "  declaringClass   " + field.declaringClass.name)
            if (AccessFlag.isPublic(field.modifiers)) {
                if (field.declaringClass.name.equals(patchClassName)) {
                    stringBuilder.append(modifiedClassName+"."+field.name +" = \$1 ;");
                } else {
                    stringBuilder.append("\$_ = \$proceed(\$\$);");
                }
            } else {
                if (field.declaringClass.name.equals(patchClassName)) {
                    stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.name + "\"," + modifiedClassName + ".class,\$1);");
                } else {
//                    A.b = this;
                    stringBuilder.append(" if(\$1 instanceof " + patchClassName + "){");
                    stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.name + "\"," + field.declaringClass.name + ".class,"+"((" + patchClassName + ")\$1)." + Constants.ORIGINCLASS+");");
                    stringBuilder.append("} else {");
                    stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setStaticFieldValue(\"" + field.name + "\"," + field.declaringClass.name + ".class,\$1);");
                    stringBuilder.append("}");
                }
            }
            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"set static  value is \" +\"" + (field.getName()) + " ${getCoutNumber()}\");");
            }
        } else {
            if (AccessFlag.isPublic(field.modifiers)) {
                if (!field.declaringClass.name.equals(patchClassName)) {
                    stringBuilder.append(" if(\$1 instanceof " + patchClassName + "){");
                    stringBuilder.append("\$0."+ field.getName() + " = " + "((" + patchClassName + ")\$1)." + Constants.ORIGINCLASS + ";");
                    stringBuilder.append("} else {");
                    stringBuilder.append("\$_ = \$proceed(\$\$);");
                    stringBuilder.append("}");
                } else {
                    stringBuilder.append(" if(\$0 instanceof " + patchClassName + "){");
                    stringBuilder.append("((" + patchClassName + ")\$0)." + Constants.ORIGINCLASS + "." + field.name + " = \$1;");
                    stringBuilder.append("} else {"); stringBuilder.append("\$_ = \$proceed(\$\$);");
                    stringBuilder.append("}");
                }
            } else {
                stringBuilder.append("java.lang.Object instance;");
                stringBuilder.append("java.lang.Class clazz;");
                stringBuilder.append(" if(\$0 instanceof " + patchClassName + "){");
                stringBuilder.append("instance=((" + patchClassName + ")\$0)." + Constants.ORIGINCLASS + ";")
                stringBuilder.append("}else{");
                stringBuilder.append("instance=\$0;");
                stringBuilder.append("}");
                if (field.declaringClass.name.equals(patchClassName)) {
                    stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.name + "\",instance,\$1,${modifiedClassName}.class);");
                } else {
                    stringBuilder.append(Constants.ROBUST_UTILS_FULL_NAME + ".setFieldValue(\"" + field.name + "\",instance,\$1,${field.declaringClass.name}.class);");
                }
            }

            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"set value is \" + \"" + (field.getName()) + "    ${getCoutNumber()}\");");
            }
        }
        stringBuilder.append("}")
//        println field.getName() + "  set  field repalce  by  " + stringBuilder.toString()
        return stringBuilder.toString();
    }

    static boolean isRFile(String s) {
        if (s.lastIndexOf("R") < 0) {
            return false;
        }
        return Constants.RFileClassSet.contains(s.substring(s.indexOf("R")));
    }

    static String getModifiedClassName(String patchName) {
        return NameManger.getInstance().getPatchNameMap().get(patchName);
    }

    static String getParameterClassSignature(String signature, String patchClassName) {
        if (signature == null || signature.length() < 1) {
            return "";
        }
        StringBuilder signureBuilder = new StringBuilder();
        String name;
        boolean isArray = false;
        for (int index = 1; index < signature.indexOf(")"); index++) {
            if (Constants.OBJECT_TYPE == signature.charAt(index) && signature.indexOf(Constants.PACKNAME_END) != -1) {
                name = signature.substring(index + 1, signature.indexOf(Constants.PACKNAME_END, index)).replaceAll("/", ".")
                if (name.equals(patchClassName)) {
                    signureBuilder.append(getModifiedClassName(patchClassName));
                } else {
                    signureBuilder.append(name);
                }
                index = signature.indexOf(";", index);
                if (isArray) {
                    signureBuilder.append("[]");
                    isArray = false;
                }
                signureBuilder.append(".class,");
            }
            if (Constants.PRIMITIVE_TYPE.contains(String.valueOf(signature.charAt(index)))) {
                switch (signature.charAt(index)) {
                    case 'Z': signureBuilder.append("boolean"); break;
                    case 'C': signureBuilder.append("char"); break;
                    case 'B': signureBuilder.append("byte"); break;
                    case 'S': signureBuilder.append("short"); break;
                    case 'I': signureBuilder.append("int"); break;
                    case 'J': signureBuilder.append("long"); break;
                    case 'F': signureBuilder.append("float"); break;
                    case 'D': signureBuilder.append("double"); break;
                    default: break;
                }
                if (isArray) {
                    signureBuilder.append("[]");
                    isArray = false;
                }
                signureBuilder.append(".class,");
            }

            if (Constants.ARRAY_TYPE.equals(String.valueOf(signature.charAt(index)))) {
                isArray = true;
            }
        }
        if (signureBuilder.length() > 0 && String.valueOf(signureBuilder.charAt(signureBuilder.length() - 1)).equals(","))
            signureBuilder.deleteCharAt(signureBuilder.length() - 1);
//        println("ggetParameterClassSignure   " + signureBuilder.toString())
        return signureBuilder.toString();
    }

    def
    static String getCreateClassString(NewExpr e, String className, String patchClassName, boolean isStatic) {
        StringBuilder stringBuilder = new StringBuilder();
        if (e.signature == null) {
            return "{\$_=(\$r)\$proceed(\$\$);}";
        }
        String signatureBuilder = getParameterClassSignature(e.signature, patchClassName);
        stringBuilder.append("{");
        if (isStatic) {
            if (signatureBuilder.length() > 1)
                stringBuilder.append("\$_= (\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",\$args,new Class[]{" + signatureBuilder + "});");
            else
                stringBuilder.append("\$_=(\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",\$args,null);");
        } else {
            if (signatureBuilder.length() > 1) {
                stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "(\$args);");
                if (Constants.isLogging)
                    stringBuilder.append("  android.util.Log.d(\"robust\",\" parameters[] from method     ${getCoutNumber()} \"+parameters);");

                stringBuilder.append("\$_= (\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",parameters,new Class[]{" + signatureBuilder + "});");

            } else
                stringBuilder.append("\$_=(\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",\$args,null);");
        }
        stringBuilder.append("}");
//        println("getCreateClassString   " + stringBuilder.toString())
        return stringBuilder.toString();
    }


    public
    static String getNewInnerClassString(String signature, String patchClassName, boolean isStatic, String className) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        String signatureBuilder = getParameterClassSignature(signature, patchClassName);
        if (isStatic) {
            if (signatureBuilder.length() > 1) {
                stringBuilder.append("\$_= (\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",\$args,new Class[]{" + signatureBuilder + "});");
            } else {
                stringBuilder.append("\$_= (\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",\$args,null);");
            }
        } else {
            if (signatureBuilder.length() > 1) {
                if (Constants.isLogging)
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"  inner Class new     ${getCoutNumber()}\");");
                stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "(\$args);");
                stringBuilder.append("\$_= (\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",parameters,new Class[]{" + signatureBuilder + "});");
            } else {
                stringBuilder.append("\$_= (\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectConstruct(\"" + className + "\",\$args,null);");
            }
        }
        stringBuilder.append("}");
//        println("getNewInnerClassString   " + stringBuilder.toString())
        return stringBuilder.toString();
    }


    public static String getParameterClassString(CtClass[] parameters) {
        if (parameters == null || parameters.length < 1) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < parameters.length; index++) {
            stringBuilder.append(parameters[index].name + ".class")
            if (index != parameters.length - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public
    static String getMethodCallString(MethodCall methodCall, CtClass patchClass, boolean isInStaticMethod) {
        String signatureBuilder = getParameterClassString(methodCall.method.parameterTypes);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        // public method or methods in patched classes
//        if (!inline && isPatchClassMethod(methodCall, outerClass)) {
////        if (AccessFlag.isPublic(methodCall.method.modifiers) || isPatchClassMethod(methodCall.method, outerClass)) {
//            println("in  getMethodCallString  before     isInStaticMethod is   " + isInStaticMethod + "  methodCall.className  " + methodCall.className + " linenumber " + methodCall.lineNumber)
//            if (isInStaticMethod) {
//                stringBuilder.append("\$_ = \$proceed(\$\$);");
//            } else {
//                stringBuilder.append("java.lang.Object parameters[]=" +  Constants.GET_REAL_PARAMETER + "(\$args);");
//                if (isStatic(methodCall.method.modifiers)) {
//                    stringBuilder.append("\$_ = \$proceed(" + getParameters(methodCall.method.parameterTypes) + ");");
//                } else {
//                    stringBuilder.append("\$_ =(\$r)((" + outerClass.name + ")(\$0) " + ")." + methodCall.methodName + "(" + getParameters(methodCall.method.parameterTypes) + ");");
//                }
//
//            }
//        } else {
        //这里面需要注意在static method中 使用static method和非static method 和在非static method中 使用static method和非static method的四种情况
//            stringBuilder.append("java.lang.Object instance;");
        stringBuilder.append(methodCall.method.declaringClass.name + " instance;");
        if (isStatic(methodCall.method.modifiers)) {
            if (isInStaticMethod) {
                //在static method使用static method
                if (AccessFlag.isPublic(methodCall.method.modifiers)) {
                    stringBuilder.append("\$_ = \$proceed(\$\$);");
                } else {
                    if (signatureBuilder.toString().length() > 1) {
                        stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\"," + methodCall.method.declaringClass.name + ".class,\$args,new Class[]{" + signatureBuilder.toString() + "});");
                    } else
                        stringBuilder.append("\$_=(\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\"," + methodCall.method.declaringClass.name + ".class,\$args,null);");
                }
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke static  method is      ${getCoutNumber()}  \" +\"" + methodCall.methodName + "\");");
                }
            } else {
                //在非static method中使用static method
                stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "(\$args);");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\"," + methodCall.method.declaringClass.name + ".class,parameters,new Class[]{" + signatureBuilder.toString() + "});");
                } else{
                    stringBuilder.append("\$_=(\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\"," + methodCall.method.declaringClass.name + ".class,parameters,null);");
                }
            }

        } else {
            if (!isInStaticMethod) {
                //在非static method中使用非static method
                stringBuilder.append(" if(\$0 == this ){");
                stringBuilder.append("instance=((" + patchClass.getName() + ")\$0)." + Constants.ORIGINCLASS + ";")
                stringBuilder.append("}else{");
                stringBuilder.append("instance=\$0;");
                stringBuilder.append("}");
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "(\$args);");
                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\",instance,parameters,new Class[]{" + signatureBuilder.toString() + "},${methodCall.method.declaringClass.name}.class);");
                } else
                    stringBuilder.append("\$_=(\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\",instance,\$args,null,${methodCall.method.declaringClass.name}.class);");
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"invoke  method is      ${getCoutNumber()} \" +\"" + methodCall.methodName + "\");");
                }
            } else {
                stringBuilder.append("instance=(" + methodCall.method.declaringClass.name + ")\$0;");
                //在static method中使用非static method
                if (signatureBuilder.toString().length() > 1) {
                    stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\",instance,\$args,new Class[]{" + signatureBuilder.toString() + "},${methodCall.method.declaringClass.name}.class);");
                } else
                    stringBuilder.append("\$_=(\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getJavaMethodSignureWithReturnType(methodCall.method) + "\",instance,\$args,null,${methodCall.method.declaringClass.name}.class);");

            }
        }
//        }
        stringBuilder.append("}");
//        println("getMethodCallString  " + stringBuilder.toString())
        return stringBuilder.toString();
    }

    public
    static String getInLineMemberString(CtMethod method, boolean isInStaticMethod, boolean isNewClass) {

        StringBuilder parameterBuilder = new StringBuilder();
        for (int i = 0; i < method.parameterTypes.length; i++) {
            parameterBuilder.append("\$" + (i + 1));
            if (i != method.parameterTypes.length - 1) {
                parameterBuilder.append(",");
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{")

        if (NameManger.getInstance().getPatchNameMap().get(NameManger.getInstance().getInlinePatchNameWithoutRecord(method.declaringClass.name)) != null) {
            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"deal inline in first   ${getCoutNumber()}  \" +\"" + method.name + "\");");
            }
            stringBuilder.append(NameManger.getInstance().getInlinePatchName(method.declaringClass.name) + " instance;");
            if (Constants.isLogging) {
                stringBuilder.append("  android.util.Log.d(\"robust\",\"deal inline method after new instance   ${getCoutNumber()}    \" +\"" + method.name + "\");");
            }
            if (isInStaticMethod || isNewClass) {
                //在static method中不需要考虑参数为this的问题
                stringBuilder.append(" instance=new " + NameManger.getInstance().getInlinePatchName(method.declaringClass.name) + "(\$0);")
                if (!isStatic(method.modifiers)) {
                    stringBuilder.append("\$_=(\$r)instance." + getInLineMethodName(method) + "(" + parameterBuilder.toString() + ");")
                } else {
                    stringBuilder.append("\$_ = (\$r)" + NameManger.getInstance().getInlinePatchName(method.declaringClass.name) + "." + getInLineMethodName(method) + "(" + parameterBuilder.toString() + ");");

                }
            } else {
                String signatureBuilder = getParameterClassString(method.parameterTypes);
                stringBuilder.append("java.lang.Object target[]=" + Constants.GET_REAL_PARAMETER + "(new java.lang.Object[]{\$0});");
                stringBuilder.append(" instance=new " + NameManger.getInstance().getInlinePatchName(method.declaringClass.name) + "(target[0]);")
                //这个需要反射来处理，处理方法的每个参数值是否为this，要不然需要很恶心的代码
                stringBuilder.append("java.lang.Object parameters[]=" + Constants.GET_REAL_PARAMETER + "(\$args);");
                if (Constants.isLogging) {
                    stringBuilder.append("  android.util.Log.d(\"robust\",\"deal inline method after new instance    ${getCoutNumber()}   \" +\"" + method.name + "\");");
                }
                if (!isStatic(method.modifiers)) {
                    if (signatureBuilder.toString().length() > 1) {
                        stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getInLineMethodName(method) + "\",instance,parameters,new Class[]{" + signatureBuilder.toString() + "},null);");
                    } else
                        stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectMethod(\"" + getInLineMethodName(method) + "\",instance,parameters,null,null);");

                } else {
                    if (signatureBuilder.toString().length() > 1) {
                        //反射内联patch中的方法
                        stringBuilder.append("\$_=(\$r) " + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getInLineMethodName(method) + "\"," + NameManger.getInstance().getInlinePatchNameWithoutRecord(method.declaringClass.name) + ".class,parameters,new Class[]{" + signatureBuilder.toString() + "});");
                    } else
                        stringBuilder.append("\$_=(\$r)" + Constants.ROBUST_UTILS_FULL_NAME + ".invokeReflectStaticMethod(\"" + getInLineMethodName(method) + "\"," + NameManger.getInstance().getInlinePatchNameWithoutRecord(method.declaringClass.name) + ".class,parameters,null);");

                }
            }

        } else {
//            throw new RuntimeException("getInLineMemberString cannot find inline class ,origin class is  " + method.declaringClass.name)
        }
        if (Constants.isLogging) {
            stringBuilder.append("  android.util.Log.d(\"robust\",\"deal inline method   ${getCoutNumber()}   \" +\"" + method.name + "\");");
        }
        stringBuilder.append("}")
//        println("getInLineMemberString  " + stringBuilder.toString())
        return stringBuilder.toString();
    }

    public static getInLineMethodName(CtMethod ctMethod) {
        if (AccessFlag.isPrivate(ctMethod.modifiers)) {
            return Constants.ROBUST_PUBLIC_SUFFIX + ctMethod.name;
        } else {
            return ctMethod.name;
        }
    }


    public static boolean isStatic(int modifiers) {
        return (modifiers & AccessFlag.STATIC) != 0;
    }

    public static String invokeSuperString(MethodCall m) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");
        if (!m.method.returnType.equals(CtClass.voidType)) {
            stringBuilder.append("\$_=(\$r)");
        }
        if (m.method.parameterTypes.length > 0) {
            stringBuilder.append(getStaticSuperMethodName(m.methodName) + "(this," + Constants.ORIGINCLASS + ",\$\$);");
        } else {
            stringBuilder.append(getStaticSuperMethodName(m.methodName) + "(this," + Constants.ORIGINCLASS + ");");
        }

        stringBuilder.append("}");
//        println("invokeSuperString  " + m.methodName + "   " + stringBuilder.toString())
        return stringBuilder.toString();
    }

    public static String getStaticSuperMethodName(String methodName) {
        return Constants.STATICFLAG + methodName;
    }

    public static String getJavaMethodSignureWithReturnType(CtMethod ctMethod) {
        //不考虑proguard的情况，这么干
        if (true) {
            return ctMethod.name;
        }
        StringBuilder methodSignure = new StringBuilder();
        methodSignure.append(ctMethod.returnType.name)
        methodSignure.append(" ")
        methodSignure.append(ctMethod.name);
        methodSignure.append("(");
        for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
            methodSignure.append(ctMethod.getParameterTypes()[i].getName());
            if (i != ctMethod.getParameterTypes().length - 1) {
                methodSignure.append(",");
            }
        }
        methodSignure.append(")")
        return methodSignure.toString();
    }


    def static getMappingValue(String name, Map memberMappingInfo) {
        if (Constants.OBSCURE) {
            String value = memberMappingInfo.get(name);
            if (value == null || value.length() < 1) {
                if (name.contains("(")) {
                    name = JavaUtils.eradicatReturnType(name)
                    value = name.substring(0, name.indexOf("("));
                } else {
                    value = name;
                }
                AutoPatchTransform.logger.warn("Warning  class name  " + name + "   can not find in mapping !! ")
//                JavaUtils.printMap(memberMappingInfo)
            }
            return value;
        } else {
            return JavaUtils.eradicatReturnType(name);
        }
    }


    private static String getCoutNumber() {
        return " No:  " + ++invokeCount;
    }
}
