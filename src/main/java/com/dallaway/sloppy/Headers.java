/*
 * Copyright (C) 2001-2010 Richard Dallaway <richard@dallaway.com>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for reading, writing and managing HTTP Headers.
 * 
 */
public class Headers
{

    private static final Logger LOG = Logger.getLogger(Headers.class.getName());

    // Internal map of header name associated with header value
    private HashMap<String, String> headers;

    /**
     * Set a name value pair in the headers.  Used in unit testing; not thread safe.
     * 
     * @param name the name of the header.
     * @param value the value of the header.
     */
    public void set(final String name, final String value)
    {
       if (headers == null)
       {
           headers = new HashMap<String, String>();
       }
       
       headers.put(name, value);
    }
    
    
    /**
     * Read a header value.
     * @param name the name of the header.
     * @return the value, or null if not set.
     */
    public String get(final String name)
    {
        String value = null;
        
        if (headers != null)
        {
            value = headers.get(name);
        }
        
        return value;
    }
    
    /**
     * @return the number of headers.
     */
    public int size()
    {
        if (headers == null)
        {
            return 0;
        }
        else
        {
            return headers.size();
        }
    }
    
    /**
     * Output headers down the given connection.
     * 
     * @param con the connection to write to.
     */
    public void writeTo(final HttpURLConnection con)
    {
        for(Map.Entry<String,String> entry: headers.entrySet())
        {
            String name = entry.getKey();
            // Host is already set {@see getConnection}, and we don't want to do
            // Keep-Alive
            if (!"Host".equalsIgnoreCase(name) && !"Connection".equalsIgnoreCase(name))
            {
                String value = entry.getValue();

                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine("> "+name+": "+value);
                }
                con.setRequestProperty(name, value);    
            }
        }
    }



    /**
     * Read the HTTP headers from the web browser.
     * 
     * @param r the input from the web browser.
     * @return the set of headers.
     * 
     * @throws IOException
     *             if there was a problem reading from the web browers.
     */
    public static Headers readFrom(final BufferedReader r) throws IOException
    {

        final HashMap<String, String> toRet = new HashMap<String, String>();

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


        Headers headers = new Headers();
        headers.headers = toRet;
        return headers;

    }
}



