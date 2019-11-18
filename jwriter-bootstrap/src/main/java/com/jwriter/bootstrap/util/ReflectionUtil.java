package com.jwriter.bootstrap.util;

public class ReflectionUtil {
	public static String buildSetMethodName (String attributeName) {
		return buildMethodName("set", attributeName);
	}
	
	private static String buildMethodName(String method, String attributeName) {
		return method+Character.toUpperCase( attributeName.charAt(0) )+attributeName.substring(1);
	}
	
	public static String buildGetMethodName (String attributeName) {
		return buildMethodName("get", attributeName);
	}
	
	public static String getAttributeNameFromSetMethod (String methodName) {
		return getAttributeNameFromMethod("set", methodName);
	}
	
	private static String getAttributeNameFromMethod (String method, String methodName) {
		String att = methodName.replace(method, "");
		return Character.toLowerCase( att.charAt(0) ) + att.substring(1);
	}

	public static String getAttributeNameFromGetMethod(String methodName) {
		return getAttributeNameFromMethod("get", methodName);
	}
	
}
