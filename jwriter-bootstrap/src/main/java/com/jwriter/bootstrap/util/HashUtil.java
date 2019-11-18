package com.jwriter.bootstrap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class HashUtil {
	private static final Logger log = Logger.getLogger(HashUtil.class);
	
	public static Map<String, String> generateJarsHash(String folder, String algorithm) throws NoSuchAlgorithmException, IOException {
		Map<String, String> localJarHashes = new HashMap<String, String>();
		File fileFolder = new File(folder);
		File jars[] = fileFolder.listFiles( new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		MessageDigest md = MessageDigest.getInstance(algorithm);
		for ( File jar: jars ) {
			log.info("Generanting hash for jar '"+jar.getName()+"'");
			byte[] hash = generateHashBase64(new FileInputStream(jar), md );
			String sHash = new String(hash);
			log.debug("Hash is '"+sHash+"'");
			localJarHashes.put( jar.getName().replace(".jar", "") , sHash);
		}
		return localJarHashes;
	}
	
	private static byte[] generateHashBase64 (InputStream is, MessageDigest md) throws IOException  {
		byte[] input = new byte[is.available()];
		is.read(input);
		is.close();
		byte[] hash = md.digest( input );
		return Base64.encodeBase64(hash);
	}
}
