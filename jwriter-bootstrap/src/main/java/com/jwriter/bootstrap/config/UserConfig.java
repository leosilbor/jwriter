package com.jwriter.bootstrap.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;

import com.jwriter.bootstrap.util.ReflectionUtil;

public class UserConfig implements Serializable {

	private static final long serialVersionUID = -8646388281097086592L;
	
//	public static final String[] JARS_NAMES = new String[]{"ridl.jar", "juh.jar", "jurt.jar", "unoil.jar", "officebean.jar"};
	public static final String[] JARS_NAMES = new String[]{"ridl.jar", "juh.jar", "jurt.jar", "unoil.jar"};
	public static final String SOFFICE_NAME = "soffice";
	
	private URL pathJuh;
	private URL pathJurt;
	private URL pathUnoil;
	private URL pathRidl;
//	private URL pathOfficebean;
	private String pathSoffice;	
	
	public boolean hasAllPaths() {
//		if ( pathSoffice==null || pathJuh==null || pathJurt==null || pathRidl==null || pathUnoil==null || pathOfficebean==null ) {
//			return false;
//		}
		if ( pathSoffice==null || pathJuh==null || pathJurt==null || pathRidl==null || pathUnoil==null ) {
			return false;
		}
		return true;
	}
	
	public void setJarPath(String fileName, File actualFile) throws ConfigException {
		String setMethodName = ReflectionUtil.buildSetMethodName( "path"+Character.toUpperCase(fileName.charAt(0))+fileName.substring(1, fileName.length()-4) );
		Method setMethod = null;
		try {
			setMethod = this.getClass().getDeclaredMethod(setMethodName, URL.class);
		} catch (Exception e) {
			throw new ConfigException("Error trying to set file path to file '"+fileName+"'", e);
		}
		
		try {
			setMethod.invoke(this, actualFile.toURI().toURL());
		} catch (Exception e) {
			throw new ConfigException("Error trying to set file path to file '"+fileName+"'", e);
		}
		
	}
	
	public void toStream (File file) throws ConfigException {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this);
			oos.close();
		} catch ( Exception e ) {
			throw new ConfigException("Error while trying to serializer user configuration", e);
		}
	}
	
	public static UserConfig fromStream(InputStream is) throws ConfigException {
		UserConfig userConfig = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			userConfig = (UserConfig) ois.readObject();
			ois.close();
		} catch ( Exception e ) {
			throw new ConfigException("Error while trying to desseralize user configuration", e);
		}
		return userConfig;
	}
	
	public URL getPathJuh() {
		return pathJuh;
	}
	public URL getPathJurt() {
		return pathJurt;
	}
	public URL getPathUnoil() {
		return pathUnoil;
	}
	public URL getPathRidl() {
		return pathRidl;
	}
//	public URL getPathOfficebean() {
//		return pathOfficebean;
//	}
	public String getPathSoffice() {
		return pathSoffice;
	}

	public void setPathJuh(URL pathJuh) {
		this.pathJuh = pathJuh;
	}

	public void setPathJurt(URL pathJurt) {
		this.pathJurt = pathJurt;
	}

	public void setPathUnoil(URL pathUnoil) {
		this.pathUnoil = pathUnoil;
	}

	public void setPathRidl(URL pathRidl) {
		this.pathRidl = pathRidl;
	}

//	public void setPathOfficebean(URL pathOfficebean) {
//		this.pathOfficebean = pathOfficebean;
//	}

	public void setPathSoffice(String pathSoffice) {
		this.pathSoffice = pathSoffice;
	}


}
