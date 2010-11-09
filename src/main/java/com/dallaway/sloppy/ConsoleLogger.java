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


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility for logging events, errors and debugging information.
 *
 * This version simply writes to STDOUT and STDERR.

 * End user events are date stamped.
 * Notices, errors and debugging lines start with a hash (#).

 */
public class ConsoleLogger implements UserInterface
{
  private static final long serialVersionUID = -3589118475573004267L;

  // Date to be formated as a common log format. 
  private SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss zzzz]");

  // Set to true for debugging information. 
  private boolean debug = false;

  

  /**
   * Record an event, such as a client request, which should be logged for the end user.
   *
   * @param client A string identifying the client.
   * @param msg The message to log.  A time stamp will be included in the output.
   */
  public void event(final String client, final String msg)
  {

    StringBuilder b = new StringBuilder();
    b.append(client); // E.g., IP address
    b.append(" ");
    b.append(sdf.format(new Date())); // Does this need to be syncronized for SDF?
    b.append(" ");
    b.append(msg);

    System.out.println(b.toString());
   	System.out.flush();
    b = null; // Help the GC

  }

  /**
   * Write a debugging message.
   *
   * @param msg The message to write.
   */
  public void debug(final String msg)
  {
	if (debug) 
    {
    	System.out.print("# ");
    	System.out.println(msg);
   		System.out.flush();
    }
  }

  /**
   * Record a notice event, such as service startup.
   *
   * @param msg The message to write.
   */
  public void notice(final String msg)
  {
    System.out.print("# ");
    System.out.println(msg);
	System.err.flush();
  }

  /**
   * Record an error.
   *
   * @param msg The message to record.
   */
  public void error(final String msg)
  {
    System.err.print("# ");
    System.err.println(msg);
	System.err.flush();
  }

  /**
   * Record an error.
   *
   * @param msg The message to write.
   * @param ex Associated exception.
   */
  public void error(final String msg, final Exception ex)
  {
    System.err.print("# ");
    System.err.print(msg);
	System.err.print(": ");
	System.err.println(ex.getMessage());
	
	if (debug)
	{
		ex.printStackTrace(System.err);	
	}


	System.err.flush();
	
  }

  /**
   * Record an error event.
   *
   * @param ex Associated exception.
   */
  public void error(final Exception ex)
  {
    error(ex.getMessage());
  }


	/**
	 * @see UserInterface#setDebug(boolean)
	 */
	public void setDebug(final boolean isDebug)
	{
		this.debug = isDebug;
	}

}