package com.jwriter.bootstrap.loader;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

public class WriterLoader {
	private static final Logger log = Logger.getLogger(WriterLoader.class);
	private Method addMethod;
	private URLClassLoader classLoader;

	public WriterLoader(URL... urls) throws LoaderException {
		classLoader = new URLClassLoader(urls, WriterLoader.class.getClassLoader());
		if ( log.isDebugEnabled() ) {
			log.debug("Class loader hierarquie");
			ClassLoader cl = classLoader;
			do {
				log.debug("-> "+cl);
				cl = cl.getParent();
			} while ( cl!=null );
			
		}
		
		if ( log.isDebugEnabled() ) {
			for ( URL url: urls ) {
				log.debug("Adding jar '"+url+"' to classpath");
			}
		}
		try {
			addMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		} catch (Exception e) {
			throw new LoaderException("Error trying to obtain a way for adding URLs dynamicly", e);
		} 
		addMethod.setAccessible(true); 
	}
	
	public void addJarURL (URL url) throws LoaderException {
		if ( !isURLAlreadyIn(url) ) {
			try {
				log.debug("Adding jar '"+url+"' to classpath");
				addMethod.invoke(classLoader, url);
			} catch (Exception e) {
				throw new LoaderException("Error trying to invoke dynamically addURL method", e);
			}
		}
	}
	
	public Object newWriterInstance(String className, String pathSoffice ) throws LoaderException {
		Class<?> clazzWriter = null;
		try {
			clazzWriter = classLoader.loadClass( className );
		} catch (ClassNotFoundException e) {
			throw new LoaderException("Class '"+className+"' not found on actual loader", e);
		}
		
		try {
			return clazzWriter.getConstructor(String.class).newInstance(pathSoffice);
		} catch (Throwable e) {
			throw new LoaderException("Error trying to instanciate new object for jWriter", e);
		}
	}
	
	private boolean isURLAlreadyIn(URL url) {
		URL[] urls = classLoader.getURLs();
		boolean is = false;
		for ( URL u: urls ) {
			if ( u.equals( url ) ) {
				is = true;
				break;
			}
		}
		return is;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return classLoader.loadClass(name);
	}

}
