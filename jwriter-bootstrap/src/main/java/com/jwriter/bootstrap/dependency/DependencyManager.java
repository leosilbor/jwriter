package com.jwriter.bootstrap.dependency;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jwriter.bootstrap.util.HashUtil;
import com.jwriter.bootstrap.util.IOUtil;
import com.jwriter.bootstrap.util.URLUtil;


public class DependencyManager {
	private static final Logger log = Logger.getLogger(DependencyManager.class);
	private static final String EXTENSION_JAR = ".jar";
	
	private String writerLocalFolder;	
	private Map<String,String> localJarHashMap;
	private boolean modified = false;
	
	public DependencyManager (String pathWriter) throws DependencyManagerException {
		this.writerLocalFolder = pathWriter;
		try {
			this.localJarHashMap = HashUtil.generateJarsHash(pathWriter, "MD5");
		} catch (Exception e) {
			throw new DependencyManagerException("Error while trying do generate hash on memory of local jars", e);
		}
	}
	
	public URL getURLJar(Jar jar, String urlLibsWriter) throws DependencyManagerException {
		String hashJarServer = jar.getHash();
		String hashJarClient = getHashJarClient(jar);
		log.info("Comparating jar hashes: jar="+jar.getName()+", server="+hashJarServer+", client="+hashJarClient);
		if ( hashJarClient==null ) {
			log.info("The jar do not exist on local directory");
			return downloadJarAndAddHash (jar, urlLibsWriter);
		} else if ( hashJarClient.equals( hashJarServer ) ) {
			log.info("The jar is up to date");
			return getURLJarLocal(jar);
		} else {
			log.warn("The jar has been modified");
			modified = true;
			return downloadJarAndAddHash (jar, urlLibsWriter);
		} 
	}
	
	public boolean isModified () {
		return modified;
	}

	private URL downloadJarAndAddHash(Jar jar, String urlLibsWriter) throws DependencyManagerException {
		localJarHashMap.put(jar.getName(), jar.getHash());
		return downloadAndSaveJar(jar, urlLibsWriter);
	}

	private URL getURLJarLocal(Jar jar) throws DependencyManagerException {
		File fileJarLocal = new File( writerLocalFolder+jar.getName()+EXTENSION_JAR );
		if ( fileJarLocal.exists() ) {
			try {
				return URLUtil.toURL( fileJarLocal );
			} catch (MalformedURLException e) {
				throw new DependencyManagerException("Error trying to create URL from local jar '"+fileJarLocal.getAbsolutePath()+"'", e);
			}
		} else {
			throw new DependencyManagerException("The local jar '"+jar.getName()+"' has hash but the jar file does not exists");
		}
	}

	private URL downloadAndSaveJar(Jar jar, String urlLibsWriter) throws DependencyManagerException {
		String urlJar = urlLibsWriter+"/"+jar.getName()+EXTENSION_JAR;
		URL urlJarServer = null;;
		try {
			urlJarServer = new URL(urlJar);
		} catch (MalformedURLException e) {
			throw new DependencyManagerException("Error trying to create URL from local jar '"+urlJar+"'", e);
		}
		String pathJarLocal = writerLocalFolder+jar.getName()+EXTENSION_JAR;
		File fileJarLocal = new File(pathJarLocal);
		try {
			fileJarLocal.createNewFile();
		} catch (IOException e) {
			throw new DependencyManagerException("Error trying to create new file to save the jar data on '"+pathJarLocal+"'", e);
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream( fileJarLocal );
		} catch (FileNotFoundException e) {
			throw new DependencyManagerException("The location to save the downloaded jar is not valid: "+pathJarLocal, e);
		}
		log.info("Downloading jar from '"+urlJar+"' and saving on '"+pathJarLocal+"'");
		InputStream is = null;
		try {
			is = urlJarServer.openStream();
		} catch (IOException e) {
			throw new DependencyManagerException("Error trying to initiate a connection to download jar on '"+urlJar+"'", e);
		}
		try {
			IOUtil.copy(is, fos);
		} catch (IOException e) {
			throw new DependencyManagerException("Error while trying to read jar data from server and save locally from "+urlJar+" to "+pathJarLocal, e);
		}
		try {
			return URLUtil.toURL( fileJarLocal );
		} catch (MalformedURLException e) {
			throw new DependencyManagerException("Error trying to create URL from local jar '"+pathJarLocal+"'", e);
		}
	}

	private String getHashJarClient(Jar jar) throws DependencyManagerException {
		return localJarHashMap.get( jar.getName() );
	}

	
}
