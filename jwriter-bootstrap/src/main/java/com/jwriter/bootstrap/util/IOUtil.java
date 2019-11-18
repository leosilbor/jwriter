package com.jwriter.bootstrap.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
	public static String toString(InputStream is) throws IOException {
		return new String(toByteArray(is));
	}
	
	public static byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos);
		return baos.toByteArray();
	}
	
	public static void copy (InputStream is, OutputStream os) throws IOException {
		BufferedInputStream bis = new BufferedInputStream( is );
		BufferedOutputStream bos = new BufferedOutputStream( os );
		for ( int i=bis.read() ; i!=-1 ; i=bis.read() ) {
			bos.write( i );
		}
		bis.close();
		bos.close();
	}
}
