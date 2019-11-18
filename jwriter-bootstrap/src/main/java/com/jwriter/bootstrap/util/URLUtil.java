package com.jwriter.bootstrap.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class URLUtil {
	public static URL toURL (File file) throws MalformedURLException {
		return file.toURI().toURL();
	}
	
	public static String toString (URL url) {
		return url.toString().replace('\\', '/');
	}
	
	public static String toString (File file) throws MalformedURLException {
		return toString( toURL( file ) );
	}
}
