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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

/**
 * Test of the header handling code. 
 * 
 */
public class HeadersTest
{
    
    /**
     * Ensure we can read in headers. 
     * 
     * @throws IOException if the test fails unexpectedly.
     */
    @Test public void canReadHeaders() throws IOException
    {
        String input = 
            "Host: someplace.org\n" +
            "Foo: bar\n" +
            "Bah: ";
        
        StringReader r = new StringReader(input);
        
        final Headers headers = Headers.readFrom(new BufferedReader(r));
        assertEquals(3, headers.size());
        assertEquals("someplace.org", headers.get("Host"));
        assertEquals("bar", headers.get("Foo"));
        assertEquals("", headers.get("Bah"));
        
        
        
    }
    
    /**
     * Test of writing the right headers down a HttpURLConnection.
     */
    @Test public void canWriteHeaders()
    {
        Headers headers = new Headers();
        headers.set("Accept", "*/*");
        headers.set("Hello", "How are you?");
        
        // These headers are not set by our code.
        headers.set("Host", "Ignore me"); // Managed by HttpURLConnection
        headers.set("Connection", "Ignore me too"); // We don't do keep-alive
        
        
        MockHttpUrlConnection con = new MockHttpUrlConnection();
        headers.writeTo(con);
        
        assertEquals(2, con.getNumRequestProperties());
        assertEquals("*/*", con.getRequestProperty("Accept"));
        assertEquals("How are you?", con.getRequestProperty("Hello"));
    }

}
