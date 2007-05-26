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

/**
 * Records the bandwidth usage for a given client. This class is used by {@link Bottleneck}.
 * <p>
 *
 * Note that if a user does not make a request for 5 seconds, this
 * data is considered stale and is reset.  We do this because
 * we use the timestamp of the first request and total bytes transferred
 * as measures to keep BPS low.  If a user goes away for a few
 * hours, it may suddenly look as if they have had very
 * low bandwidth usage (= bytes / total time).   If the user
 * is doing nothing, bytes is constant and total time increases
 * until they have effectively unlimited bandwidth.  So we reset
 * bytes and total time if the user goes quite for 5 seconds or longer.
 * 
 * @author		$Author$
 * @version	$Revision$ $Date$
 */
public class Usage
{

	/** Time stamp of the first request. */  
	private long startTime = -1; 

	/** Total bytes exchanged since the start time. */
	private int totalBytes = 0;  

	/** The last time mark() was called. */
	private long lastMark = -1; 

	/** 
	 * Number of milliseconds between requests until we 
	 * considerour data to be stale.
	 */
 	private static final long MIN_MARK_INTERVAL = 1000L * 5;


  /**
   * Note that the client has exchanged some data.
   *
   * @param n The number of bytes exchanged.
   */
  public void increment(final int n)
  {
    totalBytes += n;
  }

  /**
   * Note that a data exchange event has occured.
   */
  public void mark()
  {

    long now =  System.currentTimeMillis();
    if (startTime == -1) 
    {
    	startTime = now;
    }

    if (lastMark == -1)
    {
      lastMark = now;
      return;
    }

    if (now - lastMark >= MIN_MARK_INTERVAL)
    {
      // Reset
      startTime = now;
      totalBytes = 0;
    }

    lastMark = now;

  }

  /**
   * @return startTime The time of the first data exchange event.
   */
  public long getStartTime()
  {
    return startTime;
  }

  /**
   * @return totalBytes Total bytes exchanged to date.
   */
  public int getTotalBytes()
  {
    return totalBytes;
  }
}