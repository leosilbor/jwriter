package com.jwriter.core.odt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class OpenDocumentText {
	private Map<String, byte[]> files;
	
	public OpenDocumentText (InputStream isOdt) throws ODTException {
		ZipInputStream zis = new ZipInputStream( new BufferedInputStream( isOdt ) );
		files = new HashMap<String, byte[]>();
		try {
			for ( ZipEntry ze=zis.getNextEntry() ; ze!=null ; ze=zis.getNextEntry() ) {
				files.put( ze.getName() , getZipEntryValue(zis));
			}
			zis.close();
		} catch ( IOException e ) {
			throw new ODTException("Failed to read document. Is this a valid Open Document Text?", e);
		}
	}

	private byte[] getZipEntryValue(ZipInputStream zis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for ( int i=zis.read(buffer) ; i!=-1 ; i=zis.read(buffer) ) baos.write(buffer, 0, i);
		baos.close();
		return baos.toByteArray();
	}
	
	public void save (OutputStream osOdt) throws ODTException {
		ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( osOdt ) );
		try {
			for ( Entry<String, byte[]> entry: files.entrySet() ) {
				zos.putNextEntry( new ZipEntry( entry.getKey() ) );
				zos.write( entry.getValue() );
			}
			zos.close();
		} catch ( IOException e ) {
			throw new ODTException("Failed to save document", e);
		}
	}
	
	public byte[] save () throws ODTException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		save(baos);
		return baos.toByteArray();
	}
	
	public byte[] getFile (String name) throws ODTException {
		String real = getRealName(name);
		if ( real==null ) {
			throw new ODTException("This ODT does not contain a file named '"+name+"'");
		}
		return files.get(real);
	}
	
	private String getRealName (String name) {
		String real = null;
		for ( String key: files.keySet() ) {
			if ( key.endsWith( name ) ) {
				real = key;
				break;
			}
		}
		return real;
	}
	
	public void replaceFile (String name, byte[] file) throws ODTException {
		String real = getRealName(name);
		if ( real==null ) {
			throw new ODTException("This ODT does not contain a file named '"+name+"'");
		}
		files.put(real, file);
		
	}
}
