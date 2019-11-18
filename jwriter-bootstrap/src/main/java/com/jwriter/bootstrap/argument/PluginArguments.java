package com.jwriter.bootstrap.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jwriter.bootstrap.dependency.Jar;
import com.jwriter.bootstrap.loader.WriterLoader;
import com.jwriter.bootstrap.xml.OfficeInstalationTransformer;

public class PluginArguments implements Arguments {
	private static final long serialVersionUID = 5631202723493574549L;
	
	private List<Jar> jars;
	private List<String> transformersClassNames;

	private String pluginClass;
	private String urlLib;
	
	public String getUrlLib() {
		return urlLib;
	}
	public String getPluginClass() {
		return pluginClass;
	}
	public List<Jar> getJars() {
		return jars;
	}
	public List<OfficeInstalationTransformer> getTransformers(Map<String, List<String[]>> argsMap, WriterLoader classLoader) throws ArgumentsException {
		List<OfficeInstalationTransformer> transformers = null;
		if ( transformersClassNames!=null && transformersClassNames.size()>0 ) {
			transformers = new ArrayList<OfficeInstalationTransformer>();
			for ( String tcn: transformersClassNames ) {
				try {
					transformers.add( (OfficeInstalationTransformer) classLoader.loadClass(tcn).getConstructor(Map.class).newInstance(argsMap) );
				} catch (Exception e) {
					throw new ArgumentsException("Error while trying to instantiate new transform from '"+tcn+"'", e);
				}
			}
		}
		return transformers;
	}
	
	public void setUrlLib(String urlLib) {
		this.urlLib = urlLib;
	}
	public void setPluginClass(String pluginClass) {
		this.pluginClass = pluginClass;
	}	
	public void setJar(String name, String hash) throws ArgumentsException {
		if ( jars==null ) {
			jars = new ArrayList<Jar>();
		}
		if ( hash==null ) {
			throw new ArgumentsException("Invalid hash of jar '"+name+"'");
		}
		jars.add(new Jar(name, hash));
	}
	public void setTransformer(String transformerClassName) throws ArgumentsException {
		if ( transformersClassNames==null ) {
			transformersClassNames = new ArrayList<String>();
		}
		transformersClassNames.add( transformerClassName );
	}
	
	public void validate() throws ArgumentsException {
		if ( getJars()==null || getJars().size()==0 ) {
			throw new ArgumentsException("Argument 'jar' not found");
		}
		
		if ( getPluginClass()==null ) {
			throw new ArgumentsException("Argument 'pluginClass' not found");
		}
		
		if ( getUrlLib()==null ) {
			throw new ArgumentsException("Argument 'urlLib' not found");
		}
		
	}
	
}
