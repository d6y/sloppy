/*
 * Copyright (C) 2001 Richard Dallaway <richard@dallaway.com>
 * 
 * This file is part of Sloppy.
 * 
 * Sloppy is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Sloppy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sloppy; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.dallaway.sloppy;


/**
 * Interface for communicating with the end user.
 * 
 * Note that this interface will change for internationalization;
 * in particular the messages are likely to become keys for
 * lookup of the appropriate message.
 * 
 * @author	$Author$
 * @version $Revision$ $Date$
 */
public interface UserInterface
{
	
	/**
	 * @param	isDebug	True for debug output; false otherwise.
	 */
	void setDebug(boolean isDebug);

	/**
	 * Communicate an error situation to the end-user.
	 * 
	 * @param	message The error message to report.
	 */
	void error(String message);

	/**
	 * Communicate an error situation to the end-user.
	 * 
	 * @param	message		Summary error message.
	 * @param	exception	The related exception.
	 */
	void error(String message, Exception exception);


	/**
	 * Record an event -- this typically only has meaning for
	 * the non-GUI version of Sloppy.
	 * 
	 * @param	client		The client causing the event.
	 * @param	message		The message describing the event.
	 */
	 void event(String client, String message);
	
	/**
	 * Record an start up/shutdown or similar notice -- this typically 
	 * only has meaning for the non-GUI version of Sloppy.
	 * 
	 * @param	message		The information message.
	 */
	void notice(String message);
	
	/**
	 * @param	message		The message to show.
	 */
	void debug(String message);
	
}
