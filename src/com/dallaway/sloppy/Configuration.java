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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

/**
 * Wrapper for the various properties of the 
 * application, such as port number to listen
 * on.  This class acts as a meeting place
 * between the user interface (graphical or
 * non-graphical) and the server code.
 * 
 * @author	$Author: richard $
 * @version $Revision: 1.5 $ $Date: 2002/05/23 08:05:33 $
 */
public class Configuration
{
	
	/** The port we listen on by default */
	public static final int DEFAULT_LISTEN_PORT = 7569;
	
	/** Default bandwidth to simulate */
	public static final int DEFAULT_BYTES_PER_SECOND = 3225;
	
	/** The bandwidth we want to limit to. */
	private int bytesPerSecond;
	
	/** The address we're proxying to. **/
	private URL destination;
	
	/** The local port we're listening on. */
	private int localPort;
	
	/** For messages back to the user, which by default will output to the console. */
	private UserInterface ui = new ConsoleLogger();
	
	/** The server handling the HTTP requests. */
	private SloppyServer server;

	
	/** Name of the setting in the properties file for the bandwidth */
	private static final String BYTES_KEY = "sloppy.bytesPerSecond";

	/** The names of the property for the port to listen on. */
	private static final  String PORT_KEY = "sloppy.listenPort";
	
	/** The name of the property for the URL to proxy to. */
	private static final String DESTINATION_KEY = "sloppy.desintationURL";

	/** Amount of space (bytes) we need in the web cache for config. */
	private static final long MUFFIN_SIZE = 2048;


	/**
	 * Create a configuration with default (factory) settings.
	 * This configuration has no destination set.
	 */
	public Configuration()
	{
		this.bytesPerSecond = DEFAULT_BYTES_PER_SECOND;
		this.destination = null;
		this.localPort = DEFAULT_LISTEN_PORT;
	}

	/**
	 * Construct a new configuration using the values supplied
	 * in the properties.  For any missing properties, default
	 * values are used.
	 * 
	 * The properties are:
	 * <ul>
	 * <li> sloppy.bytesPerSecond
	 * <li> sloppy.destinationURL
	 * <li> sloppy.listenPort
	 * </ul>
	 * 
	 * 
	 * @param	props	A properties file containing zero, one or
	 * 					more settings for sloppy.
	 * 
	 * @throws MalformedURLException Thrown if the destination URL is bad.
	 */
	public Configuration(Properties props) throws MalformedURLException
	{
		this();	// Take the default values
		init(props);			
	}	


	/**
	 * Initialize the settings from any values found in the supplied
	 * properties. 
	 * 
	 * @param	props	Properties to read from.
	 * 
	 * @throws MalformedURLException Thrown if the destination URL is bad.
	 */
	private void init(Properties props) throws MalformedURLException
	{
	    String value = (String)props.get(BYTES_KEY);
    	if (value != null) 
    	{
    		this.bytesPerSecond = Integer.parseInt(value);
    	}

	    value = (String)props.get(PORT_KEY);
    	if (value != null) 
    	{
    		this.localPort = Integer.parseInt(value);
    	}

    	value = (String)props.get(DESTINATION_KEY);
    	if (value != null) 
    	{
    		this.destination = new URL(value);
    	}
		
	}


	/**
	 * Update the configuration information from any muffins stored
	 * in the Web Start cache.
	 */
	public void loadMuffins()
	{
		ui.debug("Loading muffins");

		PersistenceService ps = null; 
    	BasicService bs = null; 

    	try 
    	{ 
        	ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService"); 
        	bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
    	} 
    	catch (UnavailableServiceException e) 
    	{ 
    		ui.error("Unable to load saved settings", e);
    		return;
	    } 	
	    	    
	    // We store a properties file in the "configuration" address.
	    try
	    {
		    URL config = new URL(bs.getCodeBase(), "configuration");
			FileContents contents = getOrCreateFile(ps, config);
			if (contents == null)
			{
				return;	
			}
						
			InputStream in =  contents.getInputStream();	    		
			Properties props = new Properties();
			props.load(in);
			in.close();
			
			init(props);
	    }
	    catch (IOException iox)
	    {
	    	ui.debug("Failed to read muffins: "+ iox);	
	    }

	}


	/**
	 * Fetch the muffin stored at the given address in the Java Web Start cache, or
	 * create it if it does not exist.
	 * 
	 * @param	ps		The persistance service to use.
	 * @param	address	The address to lookup.
	 * @return The file contents at the address.
	 * @throws IOException	Thrown if there was a problem reading the muffin.
	 */
	private FileContents getOrCreateFile(PersistenceService ps, URL address) throws IOException
	{
		ui.debug("Muffin lookup");
		FileContents toRet = null;
		try
		{
			toRet = ps.get(address);
			
			// See if the stream exists:
			InputStream in = toRet.getInputStream();
			in.close();
		}
		catch (FileNotFoundException fnf)
		{
			ui.debug("Creating muffins");
			// Doesn't exist, so create:
			long sizeAllocated = ps.create(address, MUFFIN_SIZE);	
			if (sizeAllocated < MUFFIN_SIZE)
			{
				ui.debug("Asked for "+MUFFIN_SIZE+" bytes; was allocated "+sizeAllocated);	
			}
			toRet = ps.get(address);
		}
			
		return toRet;
	}

	/**
	 * Save the current configuration information.
	 */
	public void saveMuffins()
	{
		ui.debug("Saving muffins");

		PersistenceService ps = null; 
    	BasicService bs = null; 

    	try 
    	{ 
        	ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService"); 
        	bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
    	} 
    	catch (UnavailableServiceException e) 
    	{ 
    		ui.error("Unable to save settings", e);
    		return;
	    } 	
	    	    
	    // We store a properties file in the "configuration" address.
	    try
	    {
		    URL config = new URL(bs.getCodeBase(), "configuration");
			FileContents contents = getOrCreateFile(ps, config);
			if (contents == null)
			{
				return;	
			}

			// Open for output (true means "overwrite").
			PrintStream out = new PrintStream( contents.getOutputStream(true) );
			if (destination != null)
			{
				out.println(DESTINATION_KEY + "=" + destination.toExternalForm());
			}
			out.println(PORT_KEY + "=" + localPort);
			out.println(BYTES_KEY + "=" + bytesPerSecond);
			out.close();
			
	    }
	    catch (IOException iox)
	    {
	    	ui.debug("Failed to write muffins: "+ iox);	
	    }

	}

	
	/**
	 * @return Human-readable version of this configuration object.
	 */	
	public String toString()
	{
		StringBuffer b = new StringBuffer();
		b.append("Port=").append(localPort);
		b.append(" Destination=").append(destination);
		b.append(" Bytes per second=").append(bytesPerSecond);
		return b.toString();
	}
	
	/**
	 * @return Maximum bytes per second.
	 */
	public int getBytesPerSecond()
	{
		return bytesPerSecond;
	}

	/**
	 * @param bytesPerSecond Maximum bytes per second.
	 */
	public void setBytesPerSecond(int bytesPerSecond)
	{
		this.bytesPerSecond = bytesPerSecond;
	}

	/**
	 * @return The destination URL to proxy to.
	 */
	public URL getDestination()
	{
		return destination;
	}

	/**
	 * @param destination The address to proxy to.
	 */
	public void setDestination(URL destination)
	{
		this.destination = destination;
	}

	/**
	 * @return The local port Sloppy listens on.
	 */
	public int getLocalPort()
	{
		return localPort;
	}

	/**
	 * @param localPort The port that Sloppy listens on.
	 */
	public void setLocalPort(int localPort)
	{
		this.localPort = localPort;
	}

	/**
	 * @param	ui	The user interface to use for communicating
	 * 				with the user.
	 */
	public void setUserInterface(UserInterface ui)
	{
		this.ui = ui;
	}

	/**
	 * @return	The user interface to use for communicating
	 * 			with the user.
	*/
	public UserInterface getUserInterface()
	{
		return this.ui;
	}

	/**
	 * @return The server listening for proxy requests.
	 */
	public SloppyServer getServer()
	{
		return server;
	}

	/**
	 * @param server The server listening for proxy requests.

	 */
	public void setServer(SloppyServer server)
	{
		this.server = server;
	}

}
