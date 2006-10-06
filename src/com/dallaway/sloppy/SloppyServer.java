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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main wrapper class for starting the proxy.
 *
 * Each request creates a new SlowProxyThread to 
 * process the request.
 *
 * The proxy can be configured via a properties file.  
 * See default.properties for a sample.
 *
 * @author		$Author: richard $
 * @version	$Revision: 1.8 $ $Date: 2002/05/23 08:05:33 $
 */
public class SloppyServer implements Runnable
{
	
	/** Global properties for this run of sloppy.*/
	private Configuration conf;

	/** Is this proxy running? */
	private boolean running = false;

	/**
	 * @param	conf	The configuration for this run of the server.
	 */
	public SloppyServer(Configuration conf)
	{
		this.conf = conf;
	}


	/**
	 * @return True if the server is running; false otherwise.
	 */
	public boolean isRunning()
	{
		return running;	
	}

  /**
   * Start the proxy service.
   */
  public void run() 
  {

	UserInterface ui = conf.getUserInterface();

    if (running) 
    {
		return;
    }

	ReleaseInfo release = new ReleaseInfo();
    ui.notice("Starting " + release.getRelease());
	ui.notice( release.getCopyright());
	
    // Start listening for proxy requests
    ServerSocket ss = null;
    
    try
    {
    	ss = new ServerSocket( conf.getLocalPort() );
    }
    catch (IOException iox)
    {
    	ui.error("Failed to start. Is the port in use?", iox);
    	return;
    }

	ui.notice("Listening "+conf);    

    running = true;
    while (running)
    {

      // NB: if performance is an issue we can consider a pool of handler threads here

      try
      {
        // Accept an incoming request...
        Socket request = ss.accept();
        
        if (!running)
        {
        	ui.debug("Stopping...");
        	break;
        }
        
        // Hand the request off to a separate thread...
        // NB: we could start threads with different BPS to simulate clients of varying connectivity, perhaps
        SlowProxyThread handler = new SlowProxyThread(request, conf);
        handler.start();
        // Start listening again...
      }
      catch (IOException iox)
      {
        ui.debug("Error accepting request: "+iox);
      }

    }

	try
	{
		ss.close();
	}
	catch (IOException iox)
	{
		ui.error("Error while stopping listener", iox);
	}

    ui.notice("Shutdown");
  }

	/**
	 * Stop this proxy.
	 */
	public void stop()
	{
		if (!running)
		{
			return;
		}
		
		running = false;
	
		// Wake up the socket listener:
		try
		{
			Socket s = new Socket("127.0.0.1", conf.getLocalPort());
			s.close();
		}
		catch (IOException iox)
		{
			// we expect an error, because we want Sloppy to stop.
		}			

		// Listener shutdown code should have run now and
		// the thread will terminate.
	}



}