package com.jwriter.bootstrap.argument;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jwriter.bootstrap.util.ReflectionUtil;

public class ArgumentsBuilder {
	private static final Logger log = Logger.getLogger(ArgumentsBuilder.class);
	
	public static Map<String, List<String[]>> parseArguments (String args[]) {
		int argsSize = args.length;
		Map<String, List<String[]>> argsMap = new HashMap<String, List<String[]>>();
		String lastKey = null;
		List<String> lastValues = new ArrayList<String>();
		for ( int i=0 ; i<argsSize ; i++ ) {
			String arg = args[i];
			if ( arg.startsWith("--") ) { // IS KEY
				if ( lastKey!=null ) { // NEW KEY - ADD LAST KEY
					List<String[]> values = argsMap.get( lastKey );
					if ( values==null ) {
						values = new ArrayList<String[]>();
						argsMap.put(lastKey, values);
					}
					values.add( toArray(lastValues) );
					lastValues.clear();
				}
				lastKey = arg.replace("--", "");
			} else { // IS VALUE - ADD TO LAST VALUES
				lastValues.add( arg );
			}
		}
		
		List<String[]> values = argsMap.get( lastKey );
		if ( values==null ) {
			values = new ArrayList<String[]>();
			argsMap.put(lastKey, values);
		}
		values.add( toArray(lastValues) );
		
		return argsMap;
	}
	
	private static String[] toArray(List<String> list) {
		String[] array = new String[list.size()];
		int i = 0;
		for ( String item: list ) {
			array[i++] = item;
		}
		return array;
	}

	public static Arguments getArguments(Map<String, List<String[]>> argsMap, Class<? extends Arguments> clazz) throws ArgumentsException {
		log.info("Starting arguments translating for '"+clazz.getCanonicalName()+"'");
		
		Arguments arguments = null;
		try {
			arguments = clazz.newInstance();
		} catch (Exception e) {
			throw new ArgumentsException("Error trying to create new instance of arguments class '"+clazz.getCanonicalName()+"'", e);
		}
		
		Method[] methods = clazz.getMethods();
		
		for ( Method method: methods ) {
			String methodName = method.getName();
			if ( methodName.startsWith("set") ) {
				String key = ReflectionUtil.getAttributeNameFromSetMethod(methodName);
				int parameterTypesLength = method.getParameterTypes().length;
				List<String[]> listValues = argsMap.get( key );
				if ( listValues!=null && listValues.size()>0 ) {
					for ( String[] values: listValues ) {
						if ( parameterTypesLength!=values.length ) {
							throw new ArgumentsException("Invalid number of values found for '"+key+"' argument. "+parameterTypesLength+"' values are expected");
						}
						try {
							method.invoke(arguments, values);
						} catch (Exception e) {
							throw new ArgumentsException("Invalid values found for '"+key+"' argument", e);
						}
					}
				}
				
			}
		}
		
		arguments.validate();
		
//		for ( int i=0 ; i<argsSize ; ) {
//			String keyArg = args[i];
//			if ( keyArg.startsWith("---") && i+2<argsSize ) {
//				String key = keyArg.replace("---", "");
//				String value1 = args[i+1];
//				String value2 = args[i+2];
//				
//				log.debug("Triple of key and values found ("+key+", "+value1+", "+value2+")");
//				
//				String setMethodName = "set"+Character.toUpperCase(key.charAt(0))+key.substring(1);
//				
//				log.info("Calling method '"+setMethodName+"' to set arguments values");
//				
//				Method setMethod = findSetMethod(clazz, setMethodName);
//				
//				if ( setMethod==null ) {
//					log.warn("Argument '"+key+"' not found on class '"+clazz.getCanonicalName()+"' (is the set method properly created?)");
//				} else if ( setMethod.getParameterTypes().length!=2 ) {
//					throw new ArgumentsException("Argument '"+key+"' expects "+setMethod.getParameterTypes().length+" value(s)");
//				} else {
//					try {
//						setMethod.invoke(arguments, value1, value2);
//					} catch (Exception e) {
//						throw new ArgumentsException("Error trying to set arguments '"+value1+"' and '"+value2+"' on class '"+clazz.getCanonicalName()+"'", e);
//					}
//				}
//				
//				i=i+3;
//			} else if ( keyArg.startsWith("--") && i+1<argsSize ) {
//				String key = keyArg.replace("--", "");
//				String value = args[i+1];
//				
//				log.debug("Pair of key and value found ("+key+", "+value+")");
//				
//				String setMethodName = "set"+Character.toUpperCase(key.charAt(0))+key.substring(1);
//				
//				log.info("Calling method '"+setMethodName+"' to set argument value");
//				
//				Method setMethod = findSetMethod(clazz, setMethodName);
//				
//				if ( setMethod==null ) {
//					log.warn("Argument '"+key+"' not found on class '"+clazz.getCanonicalName()+"' (is the set method properly created?)");
//				} else if ( setMethod.getParameterTypes().length!=1 ) {
//					throw new ArgumentsException("Argument '"+key+"' expects "+setMethod.getParameterTypes().length+" value(s)");
//				} else {
//					try {
//						setMethod.invoke(arguments, value);
//					} catch (Exception e) {
//						throw new ArgumentsException("Error trying to set argument '"+value+"' on class '"+clazz.getCanonicalName()+"'", e);
//					}
//				}
//				
//				i=i+2;
//			}
//		}
//		
//		validate(arguments);
		
		return arguments;
	}
	
	private static Method findSetMethod (Class<?> clazz, String methodName) {
		for ( Method m: clazz.getMethods() ) {
			if ( methodName.equals( m.getName() ) ) {
				return m;
			}
		}
		return null;
	}
	
	private static void validate (Arguments arguments) throws ArgumentsException {
		log.info("validating arguments");
		Method[] methods = arguments.getClass().getMethods();
		for ( Method m: methods ) {
			String methodName = m.getName();
			if ( methodName.startsWith("get") && m.getParameterTypes().length==0 ) {
				Object arg = null;
				try {
					arg = m.invoke(arguments);
				} catch (Exception e) {
					throw new ArgumentsException("Erro trying do validad get method '"+methodName+"' from class '"+arguments.getClass().getCanonicalName()+"'", e);
				}
				
				log.debug("Method get found '"+methodName+"', result is '"+arg+"'");
				
				if ( arg==null ) {
					throw new ArgumentsException("Attribute '"+ReflectionUtil.getAttributeNameFromGetMethod(methodName)+"' is null");
				}
			}
		}
		log.info("arguments are correct and complete");
	}
}
