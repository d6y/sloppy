/*
 * Copyright (C) 2001-2007 Richard Dallaway <richard@dallaway.com>
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sloppy; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA	 02111-1307	 USA
 */
package com.dallaway.sloppy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * All requests are handed off to an instance of this class for co-ordinating
 * sender/receiver communication.
 * <p>
 *
 * This thread reads from the browser, sends to the web server, and
 * then reads from the web server and copies what it receives
 * back to the web browser.	 This final copy back is slowed down
 * to simulate the selected modem speed.
 * 
 * To simplify the code, HTTP/1.1 keep-alive connections are
 * converted into one-use connections.
 * 
 * Note that a web browser will request elements on a page
 * (e.g., GIFs) as separate requests.  To maintain a coherent bandwidth bottleneck
 * we have to record the total bytes sent to a particular client.  This means we
 * need to identify a client.  We use IP address to do this.
 *
 * 
 * @author		$Author$
 * @version $Revision$ $Date$
 */
public class SlowProxyThread extends Thread
{
	/** The interface to log to. */
	private UserInterface ui;
	
	/** The system configuration/settings. */
	private Configuration conf;
	
	/** The client request. */
	private Socket request;
	
	/** Buffer size for reading data from web server. */
	private static final int BUFFER_SIZE = 2048;
	
	/** The bottleneck to limit this user to a specific number of bytes per millisecond. */
	private Bottleneck bottleneck;

	
	/**
	 * Construct a new thread to handle a client request.
	 *
	 * @param request The client request to proxy.
	 * @param conf	The application configuration.
	 */
	public SlowProxyThread(final Socket request, final Configuration conf)
	{
		this.ui = conf.getUserInterface();
		this.request = request;
		this.conf = conf;
		
		// The client ID is a unique way to identify the browser.
		// For now we just use IP address/host name.
		String clientId = request.getInetAddress().getHostName();
		this.bottleneck = new Bottleneck(clientId, conf);	
	}
	
	/**
	 * Run the proxying thread from a single request.
	 */
	public void run()
	{
		if (conf.getDestination() == null)
		{
			ui.error("Desitnation not set");
			return;
		}
		
		if (!"http".equalsIgnoreCase(conf.getDestination().getProtocol()))
		{
			ui.error(
				"Sloppy currenty only works with HTTP addresses, not "
					+ conf.getDestination().getProtocol());
			return;
		}

		try
		{
			processRequest();							
		}
		catch (IOException iox)
		{
			// Probably a client closed connection error.
			ui.debug(iox.getMessage());
		}
		
		ui.debug("REQUEST DONE");
	}


	/**
	 * Proxy a single HTTP request from the web browser, through Sloppy, to the 
	 * web server and back again.
	 * 
	 * To understand why this method does what it does in the
	 * order that it does it, take a look at:
	 * Michael C. Daconta, "Dodge the traps hiding in the URLConnection class"
	 * http://www.javaworld.com/javaworld/jw-03-2001/jw-0323-traps.html
	 * 
	 * 
	 * @throws IOException	if there was a problem communicating with the web server
	 *						or the web browser.
	 */
	private void processRequest() throws IOException
	{
	
		ui.debug("Starting request");
	
		// The stream from the web browser:
		BufferedReader inFromWebBrowser = new BufferedReader(new InputStreamReader(request.getInputStream()));

		// The stream to the web browser:
		OutputStream outputToWebBrowser = request.getOutputStream();

		// Read the first line from the browser, which will be something like "GET /somefile HTTP/1.1"
		String firstLine = inFromWebBrowser.readLine();
        if (firstLine == null)
        {
            inFromWebBrowser.close();
            outputToWebBrowser.close();
            return;
        }
		int space = firstLine.indexOf(" ");

		String method = firstLine.substring(0, space); // GET or POST etc.
		int space2 = firstLine.indexOf(" ", space+1);
		
		String file = firstLine.substring(space+1, space2); // The file being requested

		// Read the rest of the message from the web browser:
		HashMap<String, String> headers = readHeaders(inFromWebBrowser);
		String requestBody = readBody(inFromWebBrowser);

		bottleneck.mark(); // mark an event, to record elapse time.

		// Set up the request to the server, copying over all data from the browser:
		HttpURLConnection con = getConnection(method, file);

		// Set up the request headers and body and send the request to the server
		ui.debug("Sending request to web server");
		sendRequest(con, headers, requestBody);
		con.connect();	

		// Read reply from server:
		InputStream rawInputFromWebServer = null;
		try 
		{
			rawInputFromWebServer = con.getInputStream();
		}
		catch (IOException ex) 
		{
			// Ignore - could be a FileNotFoundException for a 404
		}
		if (rawInputFromWebServer == null)
		{
			rawInputFromWebServer = con.getErrorStream();
		}
		

		// Copy the headers back to the web browser:			
		copyHeadersToWebBrowser(con, outputToWebBrowser);
		
		// Copy the body back to the web browser (if any):			
		if (rawInputFromWebServer != null) 
		{
			copyBodyToWebBrowser(rawInputFromWebServer, outputToWebBrowser);
			rawInputFromWebServer.close();
		}
		
		inFromWebBrowser.close();
		outputToWebBrowser.close();

		con.disconnect();
		request.close();
			
	}

	/**
	 * Copy the body of the http request from the web server to the web browser.
	 * @param inFromWebServer	The input stream to Sloppy from the web server.
	 * @param outputToWebBrowser	The output stream from Sloppy to the web browser.
	 * @throws IOException  if there was a communication error.
	 */
	private void copyBodyToWebBrowser(final InputStream inFromWebServer, final OutputStream outputToWebBrowser) throws IOException
	{

		byte[] buffer = new byte[BUFFER_SIZE];
		
		while (true)
		{
			bottleneck.mark(); // mark an event
			
			int bytesRead = inFromWebServer.read(buffer);

			if (bytesRead == -1)
			{
				break; // end of input
			}
			
			// Before we send the data, delay it:
			long delay = bottleneck.restrict(bytesRead);
			pause(delay);	
						
			outputToWebBrowser.write(buffer, 0, bytesRead);						
		}	

		outputToWebBrowser.flush();
				
	}


	/**
	 * Copy the HTTP headers returned by the web server to the web browser.
	 * 
	 * @param con	The connection to the web server.
	 * @param outputToWebBrowser	The output stream from Sloppy to the web browser.
	 * @throws IOException	 if there was a communication error.
	 */
	private void copyHeadersToWebBrowser(final HttpURLConnection con, final OutputStream outputToWebBrowser) throws IOException
	{	  
		int i=0;
		while (true)
		{
			String value = con.getHeaderField(i);
			if (value == null)
			{
				break; // End of headers
			}

			String name = con.getHeaderFieldKey(i);

			i++;

			if ("Transfer-Encoding".equalsIgnoreCase(name))
			{
				continue; // We drop this heading as per RFC2616			
			}


			if (name != null)
			{
				// Some headers, like the status line, have no name
				outputToWebBrowser.write(name.getBytes());
				outputToWebBrowser.write(": ".getBytes());
				
				// Change the redirection to the localhost
				if (name.equals("Location"))
				{
					URL location;
					try 
					{
						location = new URL(value);
						if (location.getHost().equals(conf.getDestination().getHost())) 
						{
							location = new URL("http", "127.0.0.1", conf.getLocalPort(), 
								location.getFile());
							value = location.toString();
						}
					}
					catch (MalformedURLException ex) 
					{
						// Ignore - don't bother changing the Location header
					}
				}
			}

			outputToWebBrowser.write(value.getBytes());
			
			outputToWebBrowser.write('\r');
			outputToWebBrowser.write('\n');

			ui.debug("< "+name+": "+value);
			
		}
        
        // Mark the end of headers/start of body with a new line.
		outputToWebBrowser.write('\r');
		outputToWebBrowser.write('\n');
		
	}



	/**
	 * Send the request received from the web browser on to the web server.
	 * 
	 * @param	con		The connection to the web server.
	 * @param	headers The headers to send to the web server.
	 * @param	body	The body to send to the web server.
	 * @throws IOException	if there was a problem communicating with the web server.
	 */
	private void sendRequest(final HttpURLConnection con, final HashMap<String, String> headers, final String body) throws IOException
	{

		// Send the headers:	
        for(Map.Entry<String,String> entry: headers.entrySet())
        {
            String name = entry.getKey();
			// Host is already set {@see getConnection}, and we don't want to do Keep-Alive
			if (!"Host".equalsIgnoreCase(name) && !"Connection".equalsIgnoreCase(name))
			{
				String value = entry.getKey();
				ui.debug("> "+name+"="+value);
				con.setRequestProperty(name, value);	
			}
		}

		// Send the body (just opening the conenction
		// seems to make some web servers think you're
		// doing a POST).
		if (body != null && body.length() > 0)
		{
			OutputStream outputToWebServer = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(outputToWebServer);
			osw.write(body);
			osw.flush();
			osw.close();
			outputToWebServer.close();	
		}

	}


	/**
	 * Set up a URLConnection to the web server.
	 * 
	 * @param	method	The HTTP metod (e.g., GET, POST).
	 * @param	file	The file being requested.
	 * @return The HttpURLConnection to the web server (not opened).
	 * @throws IOException	if there was a problem setting up the connection.
	 */
	private HttpURLConnection getConnection(final String method, final String file) throws IOException
	{
		URL url = new URL(conf.getDestination(), file);
		
		ui.debug("Method ["+method+"]");
		ui.debug("File ["+file+"]");
		ui.debug(url.toExternalForm());

		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setAllowUserInteraction(true);
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestMethod(method);
		con.setInstanceFollowRedirects(false);
		
		/* 
		 * The Host: header should be for the original request not
		 * sloppy.	E.g., if we're proxying to someplace:8080 then then
		 * header should be host:someplace:8080, not localhost:7569/ 
		 * (where Sloppy runs)
		 * 
		 * This is because some application servers use the Host: header
		 * to know what application to call.
		 */
		StringBuffer originHost = new StringBuffer();
		originHost.append(conf.getDestination().getHost());
		int port = conf.getDestination().getPort();
		if (port != -1)
		{
			originHost.append(":").append(port);	
		}
		con.setRequestProperty("Host", originHost.toString());
		
		// We don't do Keep-Alive to keep this code simple.
		con.setRequestProperty("Connection", "close");
		
		return con;
	
	}



	/**
	 * Read the HTTP body from the web browser into a string.
	 * 
	 * @param	r	The input from the web browser.
	 * @return The body of the request.
	 * @throws IOException if there was a problem reading from the web browser.
	 */
	private String readBody(final BufferedReader r) throws IOException
	{
		StringBuffer b = new StringBuffer();
		char[] buffer = new char[BUFFER_SIZE];
		while (r.ready()) 
		{
			int charsRead = r.read(buffer);
			if (charsRead == -1) 
			{
				break;
			}
			b.append(buffer, 0, charsRead);
		}
		return b.toString();		
	}


	/**
	 * Read the HTTP headers from the web browser.
	 * 
	 * @param r The input from the web browser.
	 * @return A hashtable of name/value pairs.
	 * @throws IOException	if there was a problem reading from the web browers.
	 */
	private HashMap<String, String> readHeaders(final BufferedReader r) throws IOException
	{	
		HashMap<String, String> toRet = new HashMap<String, String>();
		
		String line = r.readLine();
		while (line != null && !"".equals(line))
		{
			// Headers look like "Name: value"
	
			int colon = line.indexOf(": ");
			if (colon == -1)
			{
				toRet.put(line, "");
			}
			else
			{
				String name = line.substring(0, colon);
				String value = line.substring(colon+2); // +2 to get past the space
				toRet.put(name, value);
			}

			line = r.readLine();
		}


		return toRet;
			
	}
	
	
	/**
	 * Suspend the exchange of data on this thread
	 * for some amount of time.
	 * 
	 * @param	milliseconds	The number of milliseconds to sleep.
	 */
	private void pause(final long milliseconds)
	{
		if (milliseconds <= 0)
		{
			return;
		}
	
		try
		{
			ui.debug(getName()+" sleeping "+milliseconds);
			sleep(milliseconds);
		}
		catch (InterruptedException ix)
		{
			ui.error("Sleep interrupted", ix);
		}
	}	
	
	

}