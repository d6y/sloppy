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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Provides text to show in the "About" tab of the application, which contains
 * useful information for debugging and trouble shooting.
 * 
 * @author $Author: dallaway $
 * @version $Revision: 14 $ $Date: 2007-05-26 17:47:58 +0100 (Sat, 26 May 2007) $
 */
public class AboutText
{
    // The text created in the constructor.
    private String text;

    @Override
    public String toString()
    {
        return this.text;
    }
    
    public AboutText(final Configuration conf, final Bandwidth bw)
    {
        StringBuilder about = new StringBuilder();

        ReleaseInfo release = new ReleaseInfo();
        about.append("<p><b>").append(release.getRelease()).append("</b></p>"); //$NON-NLS-1$ //$NON-NLS-2$
        about.append("<p>").append(release.getCopyright()).append("</p>");				 //$NON-NLS-1$ //$NON-NLS-2$

        // credit the authors:
        about.append("<p>"); //$NON-NLS-1$
        about.append(Messages.getString("credit.software")); //$NON-NLS-1$ 
        about.append(" "); //$NON-NLS-1$
        about.append(Messages.getString("credit.softwareNames")); //$NON-NLS-1$ 
        about.append("</p>"); //$NON-NLS-1$ 
      
        
        // credit the logo creation:
        about.append("<p>"); //$NON-NLS-1$ 
        about.append(Messages.getString("credit.artwork")); //$NON-NLS-1$ 
        about.append("<a href='http://woadtoad.com'>Joanna Kleinschmidt</a>, "); //$NON-NLS-1$ 
        about.append("<a href='http://www.shshweb.com'>Shsh Web Design</a>");    //$NON-NLS-1$ s
        about.append("</p>"); //$NON-NLS-1$ 

        // credit the translators:
        about.append("<p>");
        about.append(Messages.getString("credit.tranlation")).append(" ");
        about.append(Messages.getString("credit.translatorNames"));
        about.append("</p>");

        about.append("<p>"); //$NON-NLS-1$
        about.append(Messages.getString("bugReports")); //$NON-NLS-1$
        about.append("</p>"); //$NON-NLS-1$

        // This text not internationalized because RD needs to be able to read it
        // if submitted as part of a bug report :-/

        append(about, "Java", new String[] { "java.version", "java.vendor"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        append(about, "Machine", new String[] { "os.name", "os.version", "os.arch"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        append(about, "Web Start", new String[] {"javawebstart.version", "jnlpx.home", "jnlpx.jvm"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        append(about, "VM", new String[] { "java.vm.name", "java.vm.version", "java.vm.vendor" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        append(about, "Proxy", new String[] { "proxyHost", "proxyPort" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        about.append("<p>").append("Port: ").append(conf.getLocalPort()).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        about.append("<p>").append("Bandwidth: ").append(conf.getBytesPerSecond()); //$NON-NLS-1$ //$NON-NLS-2$

        about.append(" b, ").append(bw.toString()); //$NON-NLS-1$
        about.append("</p>"); //$NON-NLS-1$


        try
        {
            about.append("<p>").append("Client: ").append(InetAddress.getLocalHost().getHostName()).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        catch (UnknownHostException ukh)
        {
            about.append("<p>").append("Client: unknown").append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (conf.getDestination() == null)
        {
            about.append("<p>").append("Destination: not set").append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else
        {
            about.append("<p>").append("Destination: ").append(conf.getDestination().toExternalForm()).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        this.text = about.toString();

    }

    /**
     * Append a set of System property values to a string buffer. 
     * 
     * @param	buffer	the buffer to append to.
     * @param	title	the title to prefix before the values.
     * @param	keys	array of System property names.
     */
    private void append(final StringBuilder buffer, final String title, final String[] keys)
    {
        buffer.append("<p>").append(title).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
        
        StringBuilder values = new StringBuilder();
        boolean non_null_found = false;       
        
        for (String key : keys)
        {
            String value = System.getProperty(key);
            non_null_found = non_null_found || value!=null;
            values.append(value).append(" "); //$NON-NLS-1$
        }
        
        if (non_null_found)
        {
            buffer.append(values.toString());
        }
        else
        {
            buffer.append("Not set");
        }
        
        buffer.append("</p>"); //$NON-NLS-1$
    }
}
