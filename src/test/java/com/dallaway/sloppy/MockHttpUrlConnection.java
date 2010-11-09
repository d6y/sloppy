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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sloppy; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.dallaway.sloppy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Mock version of HttpURLConnection for use
 * in unit tests.  Note: mostly this is
 * not implemented, but only because we don't
 * have tests that make use of many features yet.
 * 
 *
 */
public class MockHttpUrlConnection extends HttpURLConnection
{

    private HashMap<String, String> requestProps = new HashMap<String, String>();
    
    
    public int getNumRequestProperties()
    {
        return requestProps.size();
    }
    
    @Override
    public String getRequestProperty(String name)
    {
        return requestProps.get(name);
    }

    @Override
    public void setRequestProperty(String name, String value)
    {
        requestProps.put(name, value);
    }
    
    public MockHttpUrlConnection()
    {
        super(null);
    }

    
    // Methods below are not used in testing
    
    
    public MockHttpUrlConnection(URL url)
    {
        super(url);
    }

    @Override
    public void disconnect()
    {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean usingProxy()
    {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void connect() throws IOException
    {
       throw new IllegalStateException("Not implemented");

    }



}
