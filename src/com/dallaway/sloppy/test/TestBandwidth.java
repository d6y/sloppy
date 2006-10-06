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
package com.dallaway.sloppy.test;


import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.dallaway.sloppy.Bandwidth;

/**
 * Test of the bandwidth class.
 * 
 * @author $Author: richard $
 * @version $Revision: 1.4 $ $Date: 2002/06/28 20:08:37 $
 */
public class TestBandwidth extends TestCase
{

	/**
	 * Test of the 28.8k bandwidth object.
	 */
	public void test28()
	{
		Bandwidth bw = new Bandwidth(28.8f);
		
		assertEquals("Wrong title", "28.8k", bw.toString());
		
		int expect = (int)Math.round( ((28.8 * 1024.0) / 8.0f) * 7.0/8.0f );
		assertEquals("Wrong bytes", expect, bw.getBytesPerSecond());		
	}

	/**
	 * Test of the 512k bandwidth object.
	 */
	public void test512()
	{
		Bandwidth bw = new Bandwidth(512.0f);
		
		assertEquals("Wrong title", "512k", bw.toString());
		
		int expect = (int)Math.round( ((512.0f * 1024.0) / 8.0f) * 7.0/8.0f );
		assertEquals("Wrong bytes", expect, bw.getBytesPerSecond());		
	}


	/**
	 * @see junit.framework.TestCase#TestCase(java.lang.String)
	 */
	public TestBandwidth(String name)
	{
		super(name);
	}

	/**
	 * @return This test as a suite.
	 */
	public static TestSuite suite()
	{
		return new TestSuite(TestBandwidth.class);	
	}

	/**
	 * Runs this test suit using JUnit's text UI.
	 * 
	 * @param	args	Ignored.
	 */
	public static void main(String[] args)
	{
		junit.textui.TestRunner.run(suite());
	}
}
