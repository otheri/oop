package com.otheri.oop.server;

import java.util.HashMap;

import javassist.CtClass;

public class OOPClass {
	public CtClass clazz;
	public HashMap<String, OOPMethod> methods;
	public HashMap<String, OOPMethod> methodsNoBinding;
	public Object realClassObject;

	public OOPClass(CtClass clazz, HashMap<String, OOPMethod> methods,
			HashMap<String, OOPMethod> methodsNoBinding) {
		this.clazz = clazz;
		this.methods = methods;
		this.methodsNoBinding = methodsNoBinding;
	}
}
