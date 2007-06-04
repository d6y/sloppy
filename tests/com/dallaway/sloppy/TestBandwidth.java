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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test of the bandwidth class.
 * 
 * @author $Author:dallaway $
 * @version $Revision:12 $ $Date:2007-05-26 17:45:22 +0100 (Sat, 26 May 2007) $
 */
public class TestBandwidth 
{

	/**
	 * Test of the 28.8k bandwidth object.
	 */
	@Test public void test28()
	{
		Bandwidth bw = new Bandwidth(28.8f);
		
		assertEquals("Wrong title", "28.8k", bw.toString());
		
		int expect = (int)Math.round( ((28.8 * 1024.0) / 8.0f) * 7.0/8.0f );
		assertEquals("Wrong bytes", expect, bw.getBytesPerSecond());		
	}

	/**
	 * Test of the 512k bandwidth object.
	 */
	@Test public void test512()
	{
		Bandwidth bw = new Bandwidth(512.0f);
		
		assertEquals("Wrong title", "512k", bw.toString());
		
		int expect = (int)Math.round( ((512.0f * 1024.0) / 8.0f) * 7.0/8.0f );
		assertEquals("Wrong bytes", expect, bw.getBytesPerSecond());		
	}


	
}
