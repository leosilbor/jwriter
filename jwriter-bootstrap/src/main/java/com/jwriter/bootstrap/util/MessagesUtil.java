package com.jwriter.bootstrap.util;

import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessagesUtil implements Serializable {
	private static final long serialVersionUID = 3001223449795751111L;

	private static final String PROPERTIES_MESSAGES = "messages";
	
	private ResourceBundle messages;
	
	public MessagesUtil (Locale locale) throws MissingResourceException {
		messages = ResourceBundle.getBundle(PROPERTIES_MESSAGES, locale);
	}
	
	public String get(String key) {
		return messages.getString(key);
	}

}
