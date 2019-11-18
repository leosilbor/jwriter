package com.jwriter.bootstrap.argument;

import java.io.Serializable;

/**
 * arguments abstraction
 * @author leonardo.borges
 *
 */
public interface Arguments extends Serializable {
	public void validate() throws ArgumentsException;
}
