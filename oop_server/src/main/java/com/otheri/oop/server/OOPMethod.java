package com.otheri.oop.server;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class OOPMethod {
	// public String returnType;
	public String returnSimpleType;
	public String methodName;
	public String methodType;
	public boolean verify;
	public ArrayList<String> paramTypes;
	public ArrayList<Class<?>> realParamTypes;
	public ArrayList<String> paramSimpleTypes;
	public ArrayList<String> paramNames;
	public Method realMethod;

	public OOPMethod(
			// String returnType,
			String returnSimpleType, String methodName, String methodType,
			boolean verify, ArrayList<String> paramTypes,
			ArrayList<Class<?>> realParamTypes,
			ArrayList<String> paramSimpleTypes, ArrayList<String> paramNames) {
		// this.returnType = returnType;
		this.returnSimpleType = returnSimpleType;
		this.methodName = methodName;
		this.methodType = methodType;
		this.verify = verify;
		this.paramTypes = paramTypes;
		this.realParamTypes = realParamTypes;
		this.paramSimpleTypes = paramSimpleTypes;
		this.paramNames = paramNames;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(returnSimpleType).append(' ').append(methodName).append('(');
		for (int i = 0; i < paramTypes.size(); i++) {
			sb.append(paramSimpleTypes.get(i));
			sb.append(" ");
			sb.append(paramNames.get(i));
			sb.append(", ");
		}
		if (paramNames.size() > 0) {
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")");
		return sb.toString();
	}
}