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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 * Implementation of a graphical front end for Sloppy.
 * 
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SloppyGUI extends javax.swing.JFrame implements UserInterface, HyperlinkListener
{

    private static final long serialVersionUID = 1L;

    /** The settings for the fields on this UI. */
	private Configuration conf;

	/** Default URL string to show to the user. */
	private static final String DEFAULT_URL = "http://"; //$NON-NLS-1$
	
	/** Location of the logo resource in the Sloppy jar. */
	private static final String LOGO = "/com/dallaway/sloppy/resources/sloppy_logo64.png"; //$NON-NLS-1$

	/** Location of the 16x16 icon. */
	private static final String ICON16 = "/com/dallaway/sloppy/resources/sloppy_16.png"; //$NON-NLS-1$


	/** Are we showing debug output? */
	private boolean debug = false;


	// GUI resources:

	/** The container for the three tabs. */
    private JTabbedPane tabbedPane;

	/** The button, shown below the tabs, whihc quits the application. */
    private JButton quitButton;



	/** The "Sloppy" (first) tab. */
    private JPanel sloppyPanel;

	/** Label to prompt the user for the URL. */
    private JLabel webLabel;

	/** The type-in for the user to enter a URL. */
    private JTextField webTextBox;

	/** Prompts the user to select a bandwidth. */
    private JLabel bandwidthLabel;

	/** The drop down containing Bandwidth objects. */
    private JComboBox bandwidthDropdown;

	/** Prompt to tell the user what the button does. */
    private JLabel startLabel;

	/** The button that launches the user's web browser. */
    private JButton startButton;



	/** The "Options" (second) panel. */
    private JPanel optionsPanel;

	/** Prompt indicating that the port type-in allows the user to enter a port number. */
    private JLabel portLabel;

	/** Type-in to show the current port number, and for the user to change the port number. */
    private JTextField portTextBox;

	/** The button that applies any port changes. */
    private JButton applyButton;



	/** The "About" (third) tab. */
    private JPanel aboutPanel;

	/** Holder for the Sloppy logo. */
    private JLabel titleLabel;

	/** The body of the about text. */
    private JEditorPane aboutText;

    /** Allows the user to scroll the about text. */
	private JScrollPane aboutScrollPane;
    

    /** 
     * Create the GUI.
     * 
     * @param	conf	The system configuration/settings.
     */
    public SloppyGUI(Configuration conf) 
    {
    	
    	this.conf = conf;
    	
		// Set up the bandwidth options:        
        float[] kiloBitesPerSecond = {9.6f, 14.4f, 28.8f, 56f, 128f, 256f, 512f};

        int n = kiloBitesPerSecond.length;
        Bandwidth[] bandwidthOptions = new Bandwidth[n];
        
		// Which option should we select by default?  Default to the second one (28.8k).
        int selectedIndex = 2;
        
        for(int i=0; i<n; i++)
        {
			bandwidthOptions[i] = new Bandwidth(kiloBitesPerSecond[i]);			        	

			// If the configuration matches one of the bandwidths, make that bw the selected dropdown
			if (bandwidthOptions[i].getBytesPerSecond() == conf.getBytesPerSecond())
			{
				selectedIndex = i;
			}			
        }

        initComponents(bandwidthOptions);
        bandwidthDropdown.setSelectedIndex(selectedIndex);

		// Set the other components according the the configuration values:
        
        if (conf.getDestination() == null)
        {
        	// Default URL:
        	webTextBox.setText(DEFAULT_URL);
        }
        else
        {
        	webTextBox.setText(conf.getDestination().toExternalForm());
        }

		// Position the cursor at the end of the URL
		if (webTextBox.getText() != null)
		{
        	webTextBox.setCaretPosition( webTextBox.getText().length());
		}
        
        portTextBox.setText(Integer.toString(conf.getLocalPort()));

		aboutText.setEditable(false);
        aboutText.setText( getAboutText() );
		aboutText.setCaretPosition(0);      
	
		// Set the icon for the app:
      	setIconImage(new ImageIcon(getClass().getResource(ICON16)).getImage());

		// Extra height to see information in the "About" panel:
		setSize(350,400);
		
		// For proxy authentication:
		//Authenticator.setDefault(new GUIAuthenticator());
		

    }

	/**
	 * Update the "About" text to reflect any changes in the configuration.
	 * We do this so if anyone submits a bug, we can ask them to send us the text
	 * of the "About" box so we can see the state of the  system.
	 * 
	 * Also save the configuration settings in the web start cache for
	 * the next time we start up.
	 * 
	 */
	private void updateConfiguration()
	{
        aboutText.setText( getAboutText() );
		aboutText.setCaretPosition(0);      
		
		conf.saveMuffins();  
		
	}


	/**
	 * Build up the text for the "About" box.
	 * 
	 * @return	Information about the current Sloppy configuration.
	 */
	private String getAboutText()
	{
		StringBuilder about = new StringBuilder();

   		ReleaseInfo release = new ReleaseInfo();
		about.append("<p><b>").append(release.getRelease()).append("</b></p>"); //$NON-NLS-1$ //$NON-NLS-2$
		about.append("<p>").append( release.getCopyright()).append("</p>");				 //$NON-NLS-1$ //$NON-NLS-2$

		about.append("<p>").append(Messages.getString("credit.logo1")).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

     
		about.append("<p>"); //$NON-NLS-1$
        about.append(Messages.getString("bugReports")); //$NON-NLS-1$
        about.append("</p>"); //$NON-NLS-1$
				
        // This text not internationalized because RD needs to be able to read it
        // if submitted as part of a bug report :-/
				
		append(about, "Java", new String[] {"java.version", "java.vendor"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		append(about, "Machine", new String[] {"os.name", "os.version", "os.arch"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		append(about, "Web Start", new String[] {"javawebstart.version", "jnlpx.home", "jnlpx.jvm"	}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		append(about, "VM", new String[] {"java.vm.name","java.vm.version", "java.vm.vendor" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		append(about, "Proxy:", new String[] {"proxyHost", "proxyPort"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		
		about.append("<p>").append("Port: ").append(conf.getLocalPort()).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		about.append("<p>").append("Bandwidth: ").append(conf.getBytesPerSecond()); //$NON-NLS-1$ //$NON-NLS-2$
		Bandwidth bw = (Bandwidth)bandwidthDropdown.getSelectedItem();
		about.append(" b, ").append(bw.toString()); //$NON-NLS-1$
		about.append("</p>"); //$NON-NLS-1$


		try
		{
			about.append("<p>").append("Client: ").append(InetAddress.getLocalHost().getHostName() ).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			about.append("<p>").append("Destination: ").append(conf.getDestination().toExternalForm() ).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return about.toString();
	}

	/**
	 * Append a set of System property values to a string buffer. 
	 * 
	 * @param	buffer	The buffer to append to.
	 * @param	title	The title to prefix before the values.
	 * @param	keys	Array of System property names.
	 */
	private void append(final StringBuilder buffer, final String title, final String[] keys)
	{
		buffer.append("<p>").append(title).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
		for(int i=0, n=keys.length; i<n;i++)
		{
			buffer.append(System.getProperty(keys[i])).append(" "); //$NON-NLS-1$
		}	
		buffer.append("</p>"); //$NON-NLS-1$
	}

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * 
     * @param bandwidthOptions	The bandwidth drop-down is populated with
     * 							these values.
     */
    private void initComponents(final Bandwidth[] bandwidthOptions) 
    {
        tabbedPane = new javax.swing.JTabbedPane();
        sloppyPanel = new javax.swing.JPanel();
        webLabel = new javax.swing.JLabel();
        bandwidthDropdown = new javax.swing.JComboBox(bandwidthOptions);
        bandwidthDropdown.setEditable(false);
        webTextBox = new javax.swing.JTextField();
        startButton = new javax.swing.JButton();
        bandwidthLabel = new javax.swing.JLabel();
        startLabel = new javax.swing.JLabel();
        optionsPanel = new javax.swing.JPanel();
        portLabel = new javax.swing.JLabel();
        portTextBox = new JTextField(6);//6=extra on-screen space
        applyButton = new javax.swing.JButton();
        aboutPanel = new javax.swing.JPanel();
        aboutText = new javax.swing.JTextPane();
        titleLabel = new javax.swing.JLabel();
        quitButton = new javax.swing.JButton();
        aboutScrollPane = new javax.swing.JScrollPane();

		// Set the title and listener for the outer frame:
        setTitle(Messages.getString("name")); //$NON-NLS-1$
        addWindowListener(new java.awt.event.WindowAdapter() 
        {
            public void windowClosing(java.awt.event.WindowEvent evt) 
            {
               exit();
            }
        });

		GridBagLayout outer = new GridBagLayout();
        getContentPane().setLayout(outer);

		// Set up the tabbed pane and the always-visable QUIT button:
        
        tabbedPane.addTab(Messages.getString("tab.sloppy.title"), sloppyPanel); //$NON-NLS-1$
        tabbedPane.addTab(Messages.getString("tab.options.title"), optionsPanel); //$NON-NLS-1$
        tabbedPane.addTab(Messages.getString("tab.about.title"), aboutPanel); //$NON-NLS-1$

        quitButton.setText(Messages.getString("button.quit")); //$NON-NLS-1$
        quitButton.addActionListener(new java.awt.event.ActionListener() 
        {
            public void actionPerformed(java.awt.event.ActionEvent evt) 
            {
                exit();
            }
        });


        GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		getContentPane().add(tabbedPane, gbc);
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
		gbc.insets = new Insets(15,0,0,0);
        getContentPane().add(quitButton, gbc);

		initSloppyTab();
		initOptionsTab();
		initAboutTab();

        pack();

    }



	/**
	 * Initialize the "About" tab.
	 */
	private void initAboutTab()
	{
		

	    aboutPanel.setLayout(new GridBagLayout());
		aboutText.setContentType("text/html"); //$NON-NLS-1$
		

     
     	// LOGO:   
        titleLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(LOGO)));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,15,0);
		gbc.anchor = GridBagConstraints.CENTER;
		aboutPanel.add(titleLabel, gbc);
        
     	// Detailed information:   
        
        gbc.gridx = 0;
		gbc.gridy = 1;       
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridwidth = 2;
        aboutScrollPane.setViewportView(aboutText);
		aboutPanel.add(aboutScrollPane, gbc);

	}

	/**
	 * Initialize the "Options" tab.
	 */
	private void initOptionsTab()
	{
        optionsPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets =  new Insets(0,0,15,0);
		        
        gbc.gridx = 0;
        gbc.gridy = 0;
        portLabel.setText(Messages.getString("prompt.port")); //$NON-NLS-1$
		optionsPanel.add(portLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
		optionsPanel.add(portTextBox,  gbc);
        
        applyButton.setText(Messages.getString("button.apply")); //$NON-NLS-1$
        gbc.gridx = 1;
        gbc.gridy = 1;
        optionsPanel.add(applyButton, gbc);

		applyButton.addActionListener(new java.awt.event.ActionListener() 
		{
            public void actionPerformed(java.awt.event.ActionEvent evt) 
            {
                applyButtonActionPerformed(evt);
            }
        });
                
	}
	
	/**
	 * Initialize the layout of the "Sloppy" tab.
	 */
	private void initSloppyTab()
	{
        GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
        
        sloppyPanel.setLayout(new GridBagLayout());

		// Some default spacing:
		Insets bottomGap = new Insets(0,20,15,0);
		Insets defaultGap = new Insets(0,0,0,0);
		
		// Add a right and left spacer
		gbc.gridx=2;
		gbc.gridy=0;
		gbc.weightx=0.4;
		gbc.fill=GridBagConstraints.BOTH;
		sloppyPanel.add(new JLabel(), gbc);

		gbc.gridx=0;
		sloppyPanel.add(new JLabel(), gbc);
		
		// Defaults:
		
		gbc.weightx=0.0;
		gbc.gridx=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.insets = defaultGap;
		
		// 1. The "Address label" and text area
        
        webLabel.setText(Messages.getString("prompt.enterUrl")); //$NON-NLS-1$
        gbc.gridy=0;
		sloppyPanel.add(webLabel, gbc);

        webTextBox.setText("http://"); //$NON-NLS-1$
		gbc.gridy=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.weightx=0.2;
		gbc.insets = bottomGap;
 		sloppyPanel.add(webTextBox, gbc);

        webTextBox.addActionListener(new java.awt.event.ActionListener() 
        {
            public void actionPerformed(java.awt.event.ActionEvent evt) 
            {
                webTextBoxActionPerformed(evt);
            }
        });
      
		
		// 2.  The bandwidth label and drop down

        bandwidthLabel.setText(Messages.getString("prompt.selectSpeed")); //$NON-NLS-1$
		gbc.fill=GridBagConstraints.NONE;
		gbc.gridy=3;
		gbc.insets = defaultGap;
		sloppyPanel.add(bandwidthLabel, gbc);

		gbc.gridy=4;
		gbc.insets = bottomGap;
		sloppyPanel.add(bandwidthDropdown, gbc);

        bandwidthDropdown.addActionListener(new java.awt.event.ActionListener() 
        {
            public void actionPerformed(java.awt.event.ActionEvent evt) 
            {
                bandwidthDropdownActionPerformed(evt);
            }
        });
        

		gbc.gridy=6;
		gbc.insets = defaultGap;
        startLabel.setText(Messages.getString("prompt.launch")); //$NON-NLS-1$
        sloppyPanel.add(startLabel, gbc);

        gbc.gridy = 7;        
		gbc.insets = bottomGap;
        startButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/dallaway/sloppy/resources/play16x16.gif"))); //$NON-NLS-1$
        startButton.setText(Messages.getString("button.launch")); //$NON-NLS-1$
		sloppyPanel.add(startButton, gbc);

        startButton.addActionListener(new java.awt.event.ActionListener() 
        {
            public void actionPerformed(java.awt.event.ActionEvent evt) 
            {
                startButtonActionPerformed(evt);
            }
        });
        

	}



	/**
	 * Called when the "apply" button is pressed on the Options tab.
	 * 
	 * @param	evt		The event that triggered this called.
	 */

   private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) 
   {
		debug("Apply pressed"); //$NON-NLS-1$

		// Get the new port number:
		String portText = portTextBox.getText();
		if (portText == null)
		{
			error(Messages.getString("error.noPort")); //$NON-NLS-1$
			return;	
		}
		
		int port=-1;
		try
		{
			port = Integer.parseInt(portText);
		}
		catch (NumberFormatException nfx)
		{
			error(Messages.getString("error.badPort")); //$NON-NLS-1$
			return;	
		}
		
		// Has the port number changed?
		if (port == conf.getLocalPort())
		{
			// No change; nothing to do
			return;	
		}
		
		
		// If we get here, we need to restart the listener/server.
		JDialog progress = new JDialog(this, Messages.getString("info.wait"), false); //$NON-NLS-1$
		progress.getContentPane().setLayout( new GridBagLayout());
		JPanel panel = new JPanel(new GridBagLayout());
		progress.getContentPane().add(panel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(new JLabel(Messages.getString("info.restarting")), gbc); //$NON-NLS-1$
		progress.setSize(100,50);
		progress.setLocationRelativeTo(this);
		progress.pack();
		progress.setVisible(true);

		debug("Stopping"); //$NON-NLS-1$
		conf.getServer().stop();

		debug("Starting"); //$NON-NLS-1$
		conf.setLocalPort(port);
		Thread thread = new Thread(conf.getServer());
		thread.start();
		
		debug("Disposing of please wait message");				 //$NON-NLS-1$
		progress.setVisible(false);
		progress.dispose();
		
		// Update the about text to show the change in port:
      	updateConfiguration();
		
    }


	/**
	 * Called when there's a change to the bandwidth drop-down.
	 * 
	 * @param	evt		The event that triggered this called.
	 */
    private void bandwidthDropdownActionPerformed(java.awt.event.ActionEvent evt) 
    {
        Bandwidth bw = (Bandwidth)bandwidthDropdown.getSelectedItem();

		// Update the about text to show the change in bandwidth (if it changed)
		if (conf.getBytesPerSecond() != bw.getBytesPerSecond())
		{
	        conf.setBytesPerSecond(bw.getBytesPerSecond());
	    	updateConfiguration();
		}
        
    }

	/**
	 * Called when the "go" button is pressed, causing the web browser
	 * to be started.
	 * 
	 * @param	evt		The event that triggered this called.
	 */

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
       	debug("Go button pressed"); //$NON-NLS-1$

		// Check for a good URL from the user.
		if (updateDestination() == false)
		{
			// No good URL, so don't start the browser
			return;	
		}

		// Make sure the server code is running:
		if (conf.getServer().isRunning() == false)
		{
			error(Messages.getString("error.noRunning")); //$NON-NLS-1$
			return;
		}

		// Open the browser:
       	if (!showDocument())
       	{
       		error(Messages.getString("error.cannotLaunchBrowser")); //$NON-NLS-1$
       	}
       
    }

	/**
	 * Called when the user changes the URL text box.
	 * 
	 * @param	evt		The event that triggered this called.
	 */

    private void webTextBoxActionPerformed(java.awt.event.ActionEvent evt) 
    {
		updateDestination();	        
    }


	/**
	 * Set the configuration's Destination from the URL text box.
	 * 
	 * @return	 True, if a good URL was set; false otherwise.
	 */
	private boolean updateDestination()
	{
       String url = webTextBox.getText();

       if (url == null)
       {
			error(Messages.getString("error.noAddress")); //$NON-NLS-1$
			return false;
       }

	   // Get the URL entered by the user, and clean it up if required:       
       URL destination = clean(url);
              
       if (destination == null)
       {
       		// The destination can't be parsed as a good URL
			error(Messages.getString("error.badAddress")); //$NON-NLS-1$
			return false;
       }

		// The URL is good, so set it in the configuration, and in case clean()
		// changed the URL, we update the textbox:
		conf.setDestination(destination);
		webTextBox.setText(destination.toExternalForm());

		// Update the "about" text to show this destiation:
		updateConfiguration();
		
		return true;
	}

	/**
	 * Clean up a URL, guessing at the user's intention if possible.
	 * 
	 * @param	url		The text the user entered.
	 * @return	The URL object, or null if no valid URL
	 * 			could be constructed.
	 */
	public static URL clean(String url)
	{
		if (url == null)
		{
			return null;
		}
		
		URL toRet=null;

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
	 * Perform a clean shutdown of Sloppy. Called from the user pressing the quit
	 * button, or in some otherway requesting the application to exit.
	 */    
    private void exit()
    {
        System.exit(0);
    }



	/**
	 * @see UserInterface#debug(String)
	 */
	public void debug(String message)
	{
		if (debug)
		{
			System.out.println(message);
		}
	}

	/**
	 * @see UserInterface#error(String, Exception)
	 */
	public void error(String message, Exception ex)
	{
		JOptionPane.showMessageDialog(this, message + "\n" + ex, Messages.getString("error.title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		if (debug)
		{
			ex.printStackTrace(System.err);	
		}
	}

	/**
	 * @see UserInterface#error(String)
	 */
	public void error(String message)
	{
		JOptionPane.showMessageDialog(this, message, Messages.getString("error.title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
		
	}

	/**
	 * @see UserInterface#event(String, String)
	 */
	public void event(String client, String msg)
	{
		System.out.println(msg);
	}


	/**
	 * @see UserInterface#notice(String)
	 */
	public void notice(String message)
	{
		// Notices are really only relevant to non-GUI running.
		System.out.println(message);
	}
	
	
	/**
	 * Open the end-user's default web browser and point it at
	 * Sloppy.
	 * 
	 * @return	True if the browser was opened; false if there was
	 * 			any problem such as the JNLP service not available.
	 */
	private boolean showDocument() 
	{		
		String urlString = "http://127.0.0.1:"+conf.getLocalPort()+conf.getDestination().getFile(); //$NON-NLS-1$
		try 
		{
			// The address we want to open:
			URL url = new URL(urlString);
			
			return showDocument(url);						
		} 
		catch (MalformedURLException mx)
		{
			// Here if 127.0.0.1 is a bad URL. This will, of course, Never Happen
			error(Messages.getString("error.openBrowserFailed")+urlString); //$NON-NLS-1$
			return false;
		}
    } 		
		

	/**
	 * Open the user's browser at the given URL.
	 * 
	 * @param	url		The URL to show.
	 * @return	True if the browser was opened; false if there was
	 * 			any problem such as the JNLP service not available.
	 */
	private boolean showDocument(final URL url)
	{
		try 
		{
			// Lookup the javax.jnlp.BasicService object
			BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); //$NON-NLS-1$
			
			// Invoke the showDocument method
			return bs.showDocument(url);
		} 
		catch(UnavailableServiceException ue) 
		{
			// Not running under Web Start?
			ue.printStackTrace();
			return false;
		}
		
	}
		
	

	/**
	 * @see UserInterface#setDebug(boolean)
	 */
	public void setDebug(final boolean isDebug)
	{
		this.debug = isDebug;
	}


	/**
	 * Run the GUI in isolation from Sloppy (handy for
	 * debugging).
	 * 
	 * @param args Ignored.
	 */
	public static void main(final String[] args)
	{
		Configuration conf = new Configuration();
		SloppyGUI gui = new SloppyGUI(conf);
		gui.setVisible(true);
	}

	/**
	 * For handling links in the "About" box.
	 * 
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent event)
	{
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			showDocument(event.getURL());
		}
	}

}
