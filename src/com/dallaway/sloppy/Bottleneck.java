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

import java.util.Hashtable;

/**
 * Mechanism to work out a delay before sending data between client and server.
 * <p>
 *
 * We make use of a staic hash of {@link Usage} objects to record the amount of data
 * sent between a client and a server.  The usage is keyed by clientId (such
 * as IP address).
 * <p>
 *
 * We need to do this because a browser may request multiple files at the same
 * time, and we want to limit overall bandwidth, not just the bandwidth
 * used on each request.
 * 
 * @author		$Author$
 * @version	$Revision$ $Date$
 */
public class Bottleneck
{

  /** Hash to Usage objects, keyed by clientId. */
  private static Hashtable clients = new Hashtable();

  /** This client's id (e.g., IP Address). */
  private String clientId = null;

  /** Bandwidth usage. */
  private Usage usage = null;

  /** The configuration, so we can see the bandwidth limit. */
  private Configuration conf;

  /**
   * Construct a new bottleneck for a given client.
   *
   * @param clientId  A way to identify a client.
   * @param conf	The system configuration (i.e., bandwidth setting).
   */
  public Bottleneck(String clientId, Configuration conf)
  {
    this.clientId = clientId;
	this.conf = conf;
    usage = null;
  }


  /**
   * Delay looking up a client's usage until we have to.
   */
  private synchronized void getUsage()
  {
      usage = (Usage)clients.get(clientId);
      if (usage == null)
      {
        usage = new Usage();
        clients.put(clientId, usage);
   	  }
  }


  /**
   * Mark a "data event" such as the cient sending data or the server sending data.
   * Call this after every receive of data to keep the BPS computation fresh.
   */
  public void mark()
  {
    getUsage();
    usage.mark();
  }

  /**
   * Compute the amount of time to sleep to keep the client's bandwidth
   * usage inside the BPM measure.
   *
   * @param bytesRead	The number of bytes sent to the client.
   * 
   * @return Milliseconds to sleep for; may be -ve indicating no sleep required.
   */
  public long restrict(int bytesRead)
  {
      // Keep track of bytes sent
      getUsage();
      usage.increment(bytesRead);
      int totalBytes = usage.getTotalBytes();

      // Keep track of time spend sending those bytes
      long now = System.currentTimeMillis();
      long duration = now - usage.getStartTime();

      long expectedDuration = (long)(totalBytes / (conf.getBytesPerSecond()/1000) );

      // If we have arrived her before we are expected, we want to sleep it out
      // so we return the time difference between expected and actual
      return expectedDuration - duration;
  }


}



