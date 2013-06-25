package com.otheri.oop.server.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.otheri.commons.Consts;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceMethod {

	String name() default "name";

	String type() default Consts.METHOD_POST;

	boolean verify() default false;

}