/*
 * Copyright (C) 2008 Richard Dallaway <richard@dallaway.com>
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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility methods.
 * 
 * @author $Author:dallaway $
 * @version $Revision:12 $ $Date:2007-05-26 17:45:22 +0100 (Sat, 26 May 2007) $
 */
public class Util
{

    /**
     * Clean up a URL, guessing at the user's intention if possible.
     * 
     * @param	url the text the user entered.
     * @return	The URL object, or null if no valid URL could be constructed.
     */
    public static URL clean(String url)
    {
        if (url == null)
        {
            return null;
        }

        URL toRet = null;

        try
        {
            toRet = new URL(url);
        }
        catch (MalformedURLException mx)
        {
            // The URL entered by the user isn't valid.

            // One common misunderstanding might be to miss off the protocol:
            if (!url.startsWith("http://")) //$NON-NLS-1$
            {
                toRet = clean("http://" + url);	 //$NON-NLS-1$
            }

        }

        return toRet;
    }
    
    
    /**
     * Construct a localhost URL which will proxy the user's desitnation URL.
     * 
     * @return	 if the browser was opened; false if there was
     * 			any problem such as the JNLP service not available.
     * 
     */
    public static String makeLocalURL(final int local_port, final URL destination) 
    {
        return "http://127.0.0.1:" + local_port + destination.getFile(); //$NON-NLS-1$
    }
    
    
}
