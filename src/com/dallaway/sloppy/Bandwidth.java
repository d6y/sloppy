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

import java.text.NumberFormat;

/**
 * Conntainer for information about a particular bandwidth setting: i.e.,
 * a label such as "28.8k" and a corresponding bytes-per-second measure.
 * 
 * @author	$Author$
 * @version $Revison$ $Date$
 */
public class Bandwidth
{
	
	/** The label shown to the user for this bandwidth setting. */
	private String label;
	
	/** The maximum bytes per second for this bandwidth setting. */
	private int bytesPerSecond;

	
	/**
	 * Construct a new bandwidth setting.
	 * 
	 * @param	kiloBitsPerSecond	Kilobits per second.
	 */
	public Bandwidth(float kiloBitsPerSecond)
	{
	
       	/*
       	 * Convert Kb into KBs
       	 * 
    	 * Here's the thinking:
    	 * 	2.8k = 28.8 * 1024 = 29491.2 bits per second
    	 * 	29491.2 / 8 = 3686.4 bytes per second
    	 *  Take just 7/8ths of that to allow for control bits
    	 *    3686.4 * 7/8 = 3225.6 bytes per second
		 */
       	bytesPerSecond = Math.round( ((kiloBitsPerSecond*1024.0f) / 8.0f) * (7.0f/8.0f));

		/*
		 * Turn the KB into a pretty label for the user
		 * to select.
		 * 
		 * 512.0 KB -> "512k"
		 * 28.8KB -> "28.8k"
		 */

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);
		this.label = nf.format(kiloBitsPerSecond) + "k";
	}
	
	/**
	 * @return The label.
	 */
	public String toString()
	{
		return this.label;
	}

	/**
	 * @return Bytes per second for this bandwidth setting.
	 */
	public int getBytesPerSecond()
	{
		return this.bytesPerSecond;
	}
	
}
