package com.otheri.oop.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.otheri.commons.Utils;
import com.otheri.commons.msg.Request;
import com.otheri.commons.msg.Response;
import com.otheri.oop.server.anotation.ServiceMethod;
import com.otheri.oop.server.anotation.ServiceName;

public class OOPService {

	private HashMap<String, OOPClass> classes;

	/**
	 * 因为包含同样的type，没有注册到系统的类(用于检查错误)
	 * 
	 */
	private HashMap<String, String> classesNoBinding;

	public OOPService() {
		classes = new HashMap<String, OOPClass>();
		classesNoBinding = new HashMap<String, String>();
	}

	public HashMap<String, OOPClass> getClasses() {
		return classes;
	}

	public HashMap<String, String> getClassesNoBinding() {
		return classesNoBinding;
	}

	public Response execute(Request req) throws Exception {
		String domain = req.getDomain();
		String key = req.getMethod();

		OOPClass oopClass = classes.get(domain);
		if (null != oopClass) {
			// 找到对应的处理类

			OOPMethod oopMethod = oopClass.methods.get(key);
			if (null != oopMethod) {
				// 找到对应的处理方法
				JSONObject reqContent = JSON.parseObject(req.getContent());

				int len = oopMethod.paramNames.size();
				Object[] args = new Object[len];
				for (int i = 0; i < len; i++) {
					String paramName = oopMethod.paramNames.get(i);
					Class<?> paramType = oopMethod.realParamTypes.get(i);
					Object arg = reqContent.get(paramName);
					if (arg == null) {
						return MessageUtils.failure(req,
								"wrong method paramter : " + paramName);
					} else {
						// 自动类型转换
						if (arg instanceof JSON) {
							byte[] data = JSON.toJSONBytes(arg);
							Object obj = JSON.parseObject(data, paramType);
							args[i] = obj;
						} else {
							args[i] = arg;
						}
					}
					// System.out.println(args[i]);
				}

				Object ret = oopMethod.realMethod.invoke(
						oopClass.realClassObject, args);

				if (null == ret) {
					return MessageUtils.success(req, null);
				} else {
					return MessageUtils.success(req, JSON.toJSONString(ret));

				}
			} else {
				return MessageUtils.failure(req, "wrong key : " + key);
			}
		} else {
			return MessageUtils.failure(req, "wrong domain : " + domain);
		}
	}

	public void putClass(Class<?> clazz) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath((this.getClass())));
		CtClass c = pool.getCtClass(clazz.getName());

		Object st = c.getAnnotation(ServiceName.class);
		if (null != st) {
			String serviceDomain = ((ServiceName) st).name();
			// System.out.println("type = " + type);
			if (classes.containsKey(serviceDomain)) {
				// 已经绑定过同样的type
				classesNoBinding.put(serviceDomain, c.getName());
			} else {

				CtMethod[] methods = c.getMethods();
				HashMap<String, OOPMethod> methodsMap = new HashMap<String, OOPMethod>();
				HashMap<String, OOPMethod> methodsMapNoBinding = new HashMap<String, OOPMethod>();
				for (CtMethod m : methods) {
					// System.out.println(m.getName());
					Object sm = m.getAnnotation(ServiceMethod.class);
					if (null != sm) {
						String serviceName = ((ServiceMethod) sm).name();
						String methodType = ((ServiceMethod) sm).type();
						boolean verify = ((ServiceMethod) sm).verify();
						if (methodsMap.containsKey(serviceName)) {
							// 已经绑定同样的method
							methodsMapNoBinding.put(serviceName,
									getMethod(m, methodType, verify));
						} else {
							methodsMap.put(serviceName,
									getMethod(m, methodType, verify));
						}
					}
				}

				OOPClass oopClass = new OOPClass(c, methodsMap,
						methodsMapNoBinding);

				classes.put(serviceDomain, oopClass);

				Class<?> realClass = Class.forName(oopClass.clazz.getName());
				oopClass.realClassObject = realClass.newInstance();

				Iterator<Entry<String, OOPMethod>> itreatorMethod = oopClass.methods
						.entrySet().iterator();
				while (itreatorMethod.hasNext()) {
					Entry<String, OOPMethod> entryMethod = itreatorMethod
							.next();

					OOPMethod oopMethod = entryMethod.getValue();

					Class<?>[] parameterTypes = new Class<?>[oopMethod.paramTypes
							.size()];
					for (int i = 0; i < parameterTypes.length; i++) {
						parameterTypes[i] = buildType(oopMethod.paramTypes
								.get(i));
					}

					oopMethod.realMethod = realClass.getMethod(
							oopMethod.methodName, parameterTypes);

				}

			}

		}

	}

	private OOPMethod getMethod(CtMethod m, String methodType, boolean verify)
			throws Exception {
		// String returnType = m.getReturnType().getName();
		String returnSimpleType = m.getReturnType().getSimpleName();
		String methodName = m.getName();

		ArrayList<String> paramTypes = new ArrayList<String>();
		ArrayList<Class<?>> realParamTypes = new ArrayList<Class<?>>();
		ArrayList<String> paramSimpleTypes = new ArrayList<String>();
		ArrayList<String> paramNames = new ArrayList<String>();

		MethodInfo methodInfo = m.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);

		if (attr != null) {
			CtClass[] parameterTypes = m.getParameterTypes();
			int pos = Modifier.isStatic(m.getModifiers()) ? 0 : 1;
			for (int i = 0; i < parameterTypes.length; i++) {
				String name = parameterTypes[i].getName();
				paramTypes.add(name);
				realParamTypes.add(Class.forName(name));
				paramSimpleTypes.add(parameterTypes[i].getSimpleName());
				paramNames.add(attr.variableName(i + pos));
			}
		}

		OOPMethod ret = new OOPMethod(
				// returnType,
				returnSimpleType, methodName, methodType, verify, paramTypes,
				realParamTypes, paramSimpleTypes, paramNames);
		// System.out.println(ret.toString());
		return ret;
	}

	/**
	 * 将参数类型字符串转换成对象，需要小心的基本类型
	 * 
	 * 字符型：char 布尔型：boolean 整型：int ,short , long 浮点型：float,double,byte
	 * 
	 * @param strType
	 * @return
	 * @throws Exception
	 */
	private Class<?> buildType(String strType) throws Exception {
		if (strType.equals("char")) {
			return char.class;
		} else if (strType.equals("boolean")) {
			return boolean.class;
		} else if (strType.equals("int")) {
			return int.class;
		} else if (strType.equals("short")) {
			return short.class;
		} else if (strType.equals("long")) {
			return long.class;
		} else if (strType.equals("float")) {
			return float.class;
		} else if (strType.equals("double")) {
			return double.class;
		} else if (strType.equals("byte")) {
			return byte.class;
		} else {
			return Class.forName(strType);
		}
	}

	protected void build(HashMap<String, OOPClass> classes, String clientSrc) {
		// 删除文件夹(每次启动重新生成，不会占用多少时间)
		File codeFolder = new File(clientSrc);
		Utils.deleteFile(codeFolder);
		codeFolder.mkdirs();

		OOPClientCodeBuilder4Java javaBuilder = new OOPClientCodeBuilder4Java(
				codeFolder.getPath() + "/java/");
		OOPClientCodeBuilder4JavaScript jsBuilder = new OOPClientCodeBuilder4JavaScript(
				codeFolder.getPath() + "/js/");

		Iterator<Entry<String, OOPClass>> itreator = classes.entrySet()
				.iterator();
		while (itreator.hasNext()) {
			Entry<String, OOPClass> entry = itreator.next();

			String className = entry.getKey();
			OOPClass clazz = entry.getValue();
			javaBuilder.build(className, clazz);
			jsBuilder.build(clazz);
		}
	}
}
