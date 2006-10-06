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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Main entry point for starting Sloppy.
 * 
 * @author	$Author$
 * @version $Revision$ $Date$
 */
public class Sloppy
{

	/** By default start the GUI. */
	private static boolean startGUI = true;


	/** Do we want debug output? */
	private static boolean debug = false;

  /**
   * Start a proxy server.
   *
   * @param args Usage: Sloppy [+|-gui] [configuration.properties]
   *
   * +gui means start with a graphical user interface (default)
   * -gui means do not start a GUI
   * To override the default settings supply a configuration file.  See default.configuration for an example.
   * 
   */
  public static void main(String[] args) 
  {

	/*
	 * Set up proxy if web start tells us we're using a proxy.
	 * 
	 * NB. If proxy authentication is required, the command-line (non-GUI)
	 * version of Sloppy won't work.  It could do though, by accepting
	 * the username/password up front and using Ron Kurr's JavaWorld
	 * Tip:
	 * "Java Tip 42: Write Java apps that work with proxy-based firewalls":
	 * http://www.javaworld.com/javaworld/javatips/jw-javatip42.html
	 * 
	 * See also:
	 * "Java Tip 46: Use Java 1.2's Authenticator class"
	 * http://www.javaworld.com/javaworld/javatips/jw-javatip46.html
	 * 
	 * "Java Tip 47: URL authentication revisited"
	 * http://www.javaworld.com/javaworld/javatips/jw-javatip47.html
	 */

	String proxyHost = System.getProperty("proxyHost");
	String proxyPort = System.getProperty("proxyPort");

	if (proxyHost != null)
	{
		Properties props = System.getProperties();
		props.put("http.proxyHost", proxyHost);
		if (proxyPort != null)
		{
			props.put("http.proxyPort", proxyPort);
		}
	}



    // Read the configuration:
    Configuration conf = null;
    try
    {
    	conf = readArgs(args);
    }
    catch (IOException iox)
    {
    	System.err.println("Failed to start Sloppy: "+iox);
    	System.exit(1);	
    }

    // Construct the proxy server
    SloppyServer proxy = new SloppyServer(conf);

 	if (startGUI)
 	{
		SloppyGUI gui = new SloppyGUI(conf);
		conf.setUserInterface(gui);
	 	conf.setServer(proxy);
		gui.show(); 			
 	}	   

   	conf.getUserInterface().setDebug(debug);
    
    // Start proxying
    Thread thread = new Thread(proxy);
	thread.start();

  }

  /**
   * Read configuration properties file and command line args.
   *
   * @param args The command line arguments.
   * @return The system configuration to run with.
   * @throws IOException Thrown if there was a problem reading the properties file.
   */
  private static Configuration readArgs(String[] args) throws IOException
  {
    
    Configuration config = null;
	String url = null; // you can set the destination as -site    
    String file = null;  // name of the properties file

    for(int i=0; i<args.length; i++)
    {
      if (args[i].equalsIgnoreCase("+gui") )
      {
        startGUI = true;
      }
      else if (args[i].equalsIgnoreCase("-gui"))
      {
       startGUI = false;
      }
	  else if (args[i].equalsIgnoreCase("-debug"))
	  {
	  	debug = true;	
	  }
	  else if (args[i].equalsIgnoreCase("-site"))
	  {
	  	i++;
	  	url = args[i];	
	  }
      else
      {
        file = args[i];
      }
    } // Endfor

    if (file == null)
    {
      // no properties file supplied, so return a default config
      // plus anything in the Web Start muffins cache:
      config = new Configuration();
      config.loadMuffins();
    }
    else
    {
	    Properties props = new Properties();
    	props.load(new FileInputStream(file));
		config = new Configuration(props);
    }


	if (url != null)
	{
		config.setDestination(new URL(url));	
	}
	
    
    return config;
    
  }

	
}
