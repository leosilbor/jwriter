package com.jwriter.bootstrap.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jwriter.bootstrap.argument.ArgumentsBuilder;
import com.jwriter.bootstrap.argument.ArgumentsException;
import com.jwriter.bootstrap.argument.PluginArguments;
import com.jwriter.bootstrap.config.UserConfig;
import com.jwriter.bootstrap.dependency.DependencyManager;
import com.jwriter.bootstrap.dependency.DependencyManagerException;
import com.jwriter.bootstrap.dependency.Jar;
import com.jwriter.bootstrap.loader.LoaderException;
import com.jwriter.bootstrap.loader.WriterLoader;
import com.jwriter.bootstrap.office.OfficeService;
import com.jwriter.bootstrap.plugin.PluginException;
import com.jwriter.bootstrap.plugin.Writer;
import com.jwriter.bootstrap.util.MessagesUtil;
import com.jwriter.bootstrap.util.UIUtil;
import com.jwriter.bootstrap.xml.OfficeInstalationTransformer;
import com.jwriter.bootstrap.xml.TransformerException;

public class PluginInitializer extends Thread {
	private static final Logger log = Logger.getLogger(PluginInitializer.class);
	private static final int MAX_OFFICE_FOLDER_DEEP = 3;
	
	private Socket socketNewArgs;
	
	public PluginInitializer (Socket socketNewArgs) {
		this.socketNewArgs = socketNewArgs;
	}
	
	public void run() {
		DependencyManager dependencyManager = BootstrapContext.getInstance().getDependencyManager();
		WriterLoader loader = BootstrapContext.getInstance().getLoader();
		MessagesUtil messages = BootstrapContext.getInstance().getMessages();
		UserConfig userConfig = BootstrapContext.getInstance().getUserConfig();
		OfficeService officeService = BootstrapContext.getInstance().getOfficeService();
		String title = BootstrapContext.getInstance().getTitle();
		
		log.info("New connection received, reading data");
		Map<String, List<String[]>> argsMap = null;
		try {
			ObjectInputStream ois = new ObjectInputStream( socketNewArgs.getInputStream() );
			argsMap = (Map<String, List<String[]>>) ois.readObject();
			ois.close();
		} catch ( Exception e ) {
			String msg = messages.get("error.read.connection");
			log.fatal(msg, e);
			UIUtil.showError( null, title, msg );
			officeService.stopOfficeService();
			System.exit(0);
		}
		
		log.info("New arguments readed with success: "+argsMap);
		log.info("Creating new plugin instance to execute");
		log.info("Parsing plugin arguments");
		PluginArguments pluginArgs = null;
		try {
			pluginArgs = (PluginArguments) ArgumentsBuilder.getArguments(argsMap, PluginArguments.class);
		} catch (ArgumentsException e) {
			endPlugin(title, messages.get("error.plugin.args"), e);
			return;
		}
		
		List<Jar> jars = pluginArgs.getJars();
		String pluginClass = pluginArgs.getPluginClass();
		String urlLibsWriter = pluginArgs.getUrlLib();
		
		for ( Jar jar: jars ) {
			URL urlJar = null;
			try {
				log.info("Managing dependency for jar '"+jar.getName()+"'");
				urlJar = dependencyManager.getURLJar(jar, urlLibsWriter);
			} catch (DependencyManagerException e) {
				endPlugin(title, messages.get("error.url.jar"), e);
				return;
			}
			
			try {
				loader.addJarURL(urlJar);
			} catch (LoaderException e) {
				endPlugin(title, messages.get("error.add.loader"), e);
				return;
			}
		}
		
		if ( dependencyManager.isModified() ) {
			UIUtil.showInfo(null, title, messages.get("jar.modified.msg"));
			officeService.stopOfficeService();
			System.exit(0);
		}
		
		List<OfficeInstalationTransformer> transformers = null;
		try {
			transformers = pluginArgs.getTransformers( argsMap, loader );
		} catch (ArgumentsException e) {
			endPlugin(title, messages.get("error.plugin.gettansformers"), e);
			return;
		}
		
		if ( transformers!=null) {
			log.info(transformers.size()+" transformer found on plugin. starting transformation...");
			boolean transformed = false;
			try {
				transformed = transformOfficeInstalation(transformers);
			} catch (TransformerException e) {
				endPlugin(title, messages.get("error.plugin.transform"), e);
				return;
			}
			if ( transformed ) {
				officeService.stopOfficeService();
			}
		}
		
		log.info("Creating plugin instance from '"+pluginClass+"'");
		Writer plugin = null;
		try {
			plugin = (Writer) loader.newWriterInstance( pluginClass, userConfig.getPathSoffice() );
		} catch (LoaderException e) {
			endPlugin(title, messages.get("error.create.plugin"), e);
			return;
		} catch ( ClassCastException e ) {
			endPlugin(title, messages.get("error.class.plugin"), e);
			return;
		}
		log.info("Starting plugin execution");
		try {
			plugin.start(argsMap);
		} catch (PluginException e) {
			plugin.stop();
			endPlugin(title, e.getMessage(), e);
		} catch (Throwable e) {
			plugin.stop();
			endPlugin(title, messages.get("error.plugin.nottreated"), e);
		}
	}
	
//	public static void main(String[] args) throws TransformerException {
//		List<OfficeInstalationTransformer> list = new ArrayList<OfficeInstalationTransformer>();
//		list.add( new OfficeInstalationTransformer(null) {
//			
//			public boolean transform(InputStream is, OutputStream os) throws TransformerException {
//				return true;
//			}
//			
//			public String getFileName() {
//				return "UserProfile.xcu";
//			}
//		});
//		long time = System.currentTimeMillis();
//		new PluginInitializer(null).transformOfficeInstalation(list);
//		System.out.println(System.currentTimeMillis()-time);
//	}
	
	private boolean transformOfficeInstalation(List<OfficeInstalationTransformer> transformers) throws TransformerException {
		Map<String, OfficeInstalationTransformer> oitMap = new HashMap<String, OfficeInstalationTransformer>();
		for ( OfficeInstalationTransformer oit: transformers ) {
			oitMap.put( oit.getFileName() , oit);
		}
		File file = new File(System.getProperty("user.home")) ;
		return transformFolder (0, file, oitMap);
	}

	private boolean transformFolder(int deep, File file, Map<String, OfficeInstalationTransformer> oitMap) throws TransformerException {
		if ( file.isDirectory() && ( deep<MAX_OFFICE_FOLDER_DEEP || file.getAbsolutePath().contains("Office") ) ) {
			File[] subFiles = file.listFiles();
			boolean someTransformed = false;
			deep++;
			if ( subFiles!=null ) {
				for ( File sf: subFiles ) {
					if ( transformFolder( deep, sf , oitMap) ) {
						someTransformed = true;
					}
				}
			}
			return someTransformed;
		} else if ( file.isFile() ) {
			OfficeInstalationTransformer oit = oitMap.get( file.getName() );
			if ( oit==null ) {
				return false;
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InputStream fis = null;
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					throw new TransformerException("The file '"+file.getName()+"' does not exists", e);
				}
				
				log.info("Transforming file '"+file.getAbsolutePath()+"'");
				
				if ( oit.transform(fis, baos) ) {
					byte[] bytes = baos.toByteArray();
					OutputStream fos = null;
					try {
						fos = new FileOutputStream( file );
					} catch (FileNotFoundException e) {
						throw new TransformerException("The file '"+file.getName()+"' does not exists", e);
					}
					try {
						fos.write( bytes );
					} catch (IOException e) {
						throw new TransformerException("Error while trying to write transformed content ("+bytes.length+" bytes) on '"+file.getName()+"'", e);
					}
					log.info("The file has been modified");
					return true;
				} else {
					log.info("The file has not been modified");
					return false;
				}
			}
		} else {
			return false;
		}
	}

	private void endPlugin(String title, String msg, Throwable e) {
		log.error(msg, e);
		UIUtil.showError(null, title, msg);
		interrupt();
	}


}
