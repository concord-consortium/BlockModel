/****************************************************************************
*
*  Copyright (C) 1999,2000 Dallas Semiconductor Corporation.
*  All rights Reserved. Printed in U.S.A.
*  This software is protected by copyright laws of
*  the United States and of foreign countries.
*  This material may also be protected by patent laws of the United States
*  and of foreign countries.
*  This software is furnished under a license agreement and/or a
*  nondisclosure agreement and may only be used or copied in accordance
*  with the terms of those agreements.
*  The mere transfer of this software does not imply any licenses
*  of trade secrets, proprietary technology, copyrights, patents,
*  trademarks, maskwork rights, or any other form of intellectual
*  property whatsoever. Dallas Semiconductor retains all ownership rights.
*
*     Module Name: JavaKit.java
*/

/*************************************************************************************\
     File          : JavaKit.java
     Version       : 2.2.2
     Created       : 11/03/1998
     Modified      : 06/11/1999 - Added Macro capability
                     07/08/1999 - Changed to wait for a particular Prompt String after sending
                     07/09/1999 - Swingified
                     08/05/1999 - Added Dumb Terminal option, loading multiple files at once,
                                  CTRL-ALT-O toggles console output, RightClick on TextArea
                                  brings up menu to clear window, fixed Multiple keyListener
                                  problems when opening and closing ports, added Help->About
                                  window.
                     08/06/1999 - Added JDK 1.1.x support
                     08/09/1999 - Added copy/paste/select all functionality
                     08/19/1999 - Fixed Run Macro screen update problem
                     09/01/1999 - Added temporary *bad* hack to fix JFileDialog selections
                                  in the Motif look and feel.
                     09/06/1999 - Added ability to run multiple macros passed from command line
                     09/09/1999 - Fixed moveCursor to end problem where it would ignore last empty line
                                  Added <Ctrl><Alt> D to toggle debug output
                     09/16/1999 - Added Arrows that send special control characters for Slush
                                  Changed DTR to be radio group
                                  Changed load to send 'l' or 'v' even if in dumb terminal mode
                     10/23/1999 - Fixed paste to send text
                                  Made Dumb Terminal default mode
                                  Added 'Try Again' option when JavaKit tries to auto open a port
                                  that is already open
                     01/19/2000 - Fixed Linux read problem.  Was depending on available() to return
                                  bytes available to read.  This made newer versions of RxTx grumpy.
                                  Went to one global JFileChooser since it is such a memory hog
                                  and slow on creation.
                                  Added a "Trap Ctrl Keys" option to DumbEmulator mode to allow the
                                  hot keys (ctrl-F is load file, etc.) to work in that mode, too.  The
                                  user can choose not to trap the ctrl keys, in which case the actual
                                  ctrl sequence will be sent out instead of trapping and performing
                                  the JavaKit command for that ctrl key.
                                  Revamped the macro system to look for specific prompts (instead of
                                  always the TINI loader prompt) before sending a macro line.  This
                                  allows the macro to be used for everything.  (i.e. loader things like
                                  b18, f0, e, and slush things like ipconfig, etc.)
                     03/16/2000 - Made debug mode an item in the option menu and removed hotkey listener.
                                  Fixed "Port Open Retry?" to not exit program if the user answers no.  Gave
                                  a "Cancel" option that will exit the program if the user did: "java JavaKit -port COM1"
                                  and COM1 was already open, cancel will exit the program.
                                  Stopped sending 0xFF in dumb terminal mode when the user hits shift, alt, ctrl.
                                  Changed the backspace handler to watch for 0x08, 0x20, 0x08 in separate packets as well
                                  as in 1 3 byte package.
                     04/05/2000 - Added AutoZap banks, and loader protection when writing to bank 0.
                                  Added Update Loader option.
                                  Added Binary Load ability.
                     06/01/2000 - Fixed DTR capability detect on file load to look for a more complex prompt string.
                     07/12/2000 - Fixed the 'random' character glitch that occurred every time we cleared the
                                  text buffer after 20000 characters were written.
                     07/14/2000 - Fixed tbin binary loading problem when starting address
                                  is not an even multiple of 64K (always sending start address of 0x0000)
                     07/18/2000 - Implemented DTR changes as suggested by Pepe, including command line option to
                                  turn off DTR testing.
                     07/24/2000 - Changed getAbsolutePath calls to getCanonicalPath calls because I got tired
                                  of scrolling 10 pages to the right seeing ../../.. everywhere!
                     11/02/2000 - Fixed Mac problem.  Mac version of RX-TX (yeah, I know -- there is one???)
                                  would not behave on SerialPort.read(byte[512] buffer).  It would wait until 
                                  512 bytes were read, and then return all those, which is useful if you don't
                                  ever really NEED that data.  Changed to look something like
                                  SerialPort.read(byte[512] buffer,0, SerialPort.available()).
                     01/23/2001 - Fixed problem where if you sent too many character to the loader (>32) it
                                  would spit back your character + 0x08,0x20,0x08, which we didn't handle right.
                     05/08/2001 - Removed "Update Loader" from pull down menu.
                     05/09/2001 - Support 1 Meg flash.
                     05/30/2001 - Support for loading things into bank 8 (i.e. Slush too big, API bloat, your momma's
                                  recipies, etc.) on a > 512K flash enabled TINI
     Author        : Stephen Hess
     Wonder Weasel : Pepe
     New Author    : Kris Ardis
     Pinky         : Brayn
     Subverted     : Robert
     Leave me alone: Kris
\*************************************************************************************/
import gnu.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;
import java.text.*;
import java.awt.im.*;
import java.awt.datatransfer.*;
import java.awt.font.*;
import javax.swing.*;

public class JavaKit extends JFrame implements ActionListener
{
    public static final String VERSION = "2.2.2";

    //Tunable knobs to adjust
    static int     FLUSH_WAIT      = 50;
    static int     RESET_WAIT      = 100;

    //Default values
    int     height          = 550;
    int     width           = 610;
    String  baud            = "115200";
    int     xPos            = 0;
    int     yPos            = 0;
    String  PROMPT_STRING   = ">";
    int     BANK_SIZE       = 65536;
    int     ROM_SIZE        = 524288;
    String  padString       = "";
    int     padSize         = 12;
    boolean trapCtrlKeys    = true;
    
    //true if the user has enabled the option to 'Update Loader'
    static boolean i_want_to_scrog_my_tini = false;
    
    boolean PROTECT_BANK_0  = true;
    boolean AUTO_ZAP_BANKS  = true;

    private static int NO_RESPONSE_TIMEOUT = 3000; 
    private static int DTR_TOGGLE_TIMEOUT = 100;

    static String  DEFAULT_PROMPT_STRING   = ">";
//    String  DEFAULT_LONG_PROMPT     = "All rights reserved.\r\n\r\n>";
    static String  DEFAULT_LONG_PROMPT     = ".\r\n\r\n>";

    public boolean JDK11X_VM = false;
    public boolean WINDOWS   = false;

    //GUI declarations
    private JButton openCloseButton;
    private JButton resetButton;
    private JPanel dtrPanel;
    private JRadioButton dtrSet;
    private JRadioButton dtrClear;
    private JPanel buttonPanel;

    private JTextArea  inputOutputArea;

    private JComboBox portChoice;
    private JComboBox baudChoice;

    private JComboBox emulatorChoice;

    private SerialPort serialPort;

    private OutputStream outputStream;
    private InputStream  inputStream;

    private SpecialKeyListener keyListener;
    private InputHandler       inputHandler;

    private JMenuBar  menuBar;

    private JMenu fileMenu;
        private JMenuItem loadHexFileItem;
        private JMenuItem verifyHexFileItem;
        private JMenuItem enableUpdateLoaderItem;
        private JMenuItem updateLoaderItem;
        private JMenuItem loadMacroItem;
        private JMenuItem saveMacroItem;
        private JMenuItem exitItem;

    private JMenu editMenu;

    private JMenu macroMenu;
        private JMenuItem macroItem;
        private JMenuItem runMacroItem;

    private JMenu optionsMenu;
        private JMenuItem consoleOutputItem;
        private JMenuItem lineWrapItem;
        private JMenuItem generateLogItem;
        private JMenuItem trapCtrlKeysItem;
        private JMenuItem autoZapItem;
        private JMenuItem bankProtectItem;
        private JMenuItem verboseModeItem;
        private JMenuItem debugModeItem;

    private JLabel statusLabel;

    private JPopupMenu rightClickPopupMenu;

    private boolean promptReceived;
    private boolean partialPromptReceived = false;
    private int     promptPos = -1;
    private byte[]  promptBytes;

    private boolean possibleError;
    private boolean newInputReceived;

    private boolean macroModified = false;

    public static boolean dumbEmulator  = true;
    public static boolean VT100Emulator = false;

    private PrintStream logFile;

    boolean escapeChar = false;

    boolean debugMode      = false;
    boolean verboseMode    = false;
    boolean suppressOutput = false;
    boolean suppressCursorSet = false;
    static boolean allowHighBankLoading = false;

    private String userInput;

    private byte[] binaryBuffer = null;
    private static String CRC_CHECK = null;
    private boolean checkCRC = false;

    public static boolean exitAfterRun;

    private static boolean finishedLoading;

    private Vector  currentMacro;
    private String  currentMacroName;
    private boolean recordingMacro;

    boolean LOADER_UPDATE_IN_PROGRESS  = false;

    private boolean consoleOutput = false;

    private String[] hexLookup = {"0000", "000", "00", "0", ""};

    //Send after the user types a line
    private static final byte[] endCommandLine = "\r".getBytes();
    private static final String endCommandLineStr = "\r";
    //Send after every line in the hex file
    private static final byte[] endHexLine     = "\r\n".getBytes();
    private static final String endHexLineStr  = "\r\n";

    private static final byte[] TINI_RESET_STR = "\r?TINI".getBytes();  //Full reset string is: "\r\r?TINI\r"

    private static String lastUsedDir = System.getProperty("user.dir");

    public static  JFrame mainFrame;

    private static JFileChooser chooser;

    public static int charactersWritten = 0;

    /**
     * Perform all initialization.
     */
    public JavaKit(String comPort, String[] macroFiles, String newBaud,
                   int padSize, int binPause, boolean exitAfterRun, int bankSize,
                   int romSize, boolean log, boolean debugMode)
    {
    	super("JavaKit");

    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        xPos = screenSize.width/2 - width/2;
        yPos = screenSize.height/2 - height/2;

        loadINIFile();

    	setLocation(xPos,yPos);
    	setSize(width, height);

    	setBackground(Color.lightGray);

    	newInputReceived = false;
    	promptReceived = false;

    	currentMacro = new Vector();
    	recordingMacro = false;

    	mainFrame = this;

    	userInput = "";

    	this.debugMode = debugMode;
    	if (binPause != -1)
    	    FLUSH_WAIT = binPause;

        if (newBaud != null)
            baud = newBaud;

        if (romSize != -1)
            ROM_SIZE = romSize;

    	JPanel statusPanel = new JPanel();
    	statusPanel.setLayout(new BorderLayout(10,10));
    	getContentPane().add(statusPanel, BorderLayout.NORTH);

    	    emulatorChoice = new JComboBox();
    	    statusPanel.add(emulatorChoice, BorderLayout.EAST);
    	    emulatorChoice.addItem("Dumb Terminal");
    	    emulatorChoice.addItem("JavaKit Terminal");
    	    emulatorChoice.setEnabled(false);

    	    statusLabel = new JLabel();
    	    statusPanel.add(statusLabel, BorderLayout.WEST);

    	JPanel messagePanel = new JPanel();
    	messagePanel.setLayout(new BorderLayout(10,10));

        	inputOutputArea = new JTextArea();
            inputOutputArea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        	inputOutputArea.setBackground(Color.white);
            inputOutputArea.setEditable(false);
            inputOutputArea.setEnabled(false);
            JScrollPane jsp = new JScrollPane(inputOutputArea);
        	messagePanel.add(new JScrollPane(inputOutputArea), BorderLayout.CENTER);

    	getContentPane().add(messagePanel, BorderLayout.CENTER);

    	JPanel southPanel = new JPanel();
    	southPanel.setLayout(new BorderLayout());
    	getContentPane().add(southPanel,BorderLayout.SOUTH);

    	JPanel configurationPanel = new JPanel();
    	southPanel.add(configurationPanel,BorderLayout.NORTH);

  	    configurationPanel.setLayout(new GridLayout(2, 2));

    	    JLabel portNameLabel = new JLabel("Port Name:", Label.LEFT);
    	    configurationPanel.add(portNameLabel);

    	    JLabel baudLabel = new JLabel("Baud Rate:", Label.LEFT);
    	    configurationPanel.add(baudLabel);

    	    portChoice = new JComboBox();
    	    configurationPanel.add(portChoice);
    	    portChoice.addItem(new String("<Not Connected>"));
            Enumeration ports = CommPortIdentifier.getPortIdentifiers();
            while (ports.hasMoreElements())
            {
                CommPortIdentifier portId = (CommPortIdentifier) ports.nextElement();
                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL)
                {
                    portChoice.addItem(portId.getName());
                }
            }
            portChoice.setSelectedIndex(0);

    	    baudChoice = new JComboBox();
    	    configurationPanel.add(baudChoice);
    	    baudChoice.addItem("300");
    	    baudChoice.addItem("2400");
    	    baudChoice.addItem("9600");
    	    baudChoice.addItem("14400");
       	    baudChoice.addItem("19200");
    	    baudChoice.addItem("28800");
    	    baudChoice.addItem("38400");
    	    baudChoice.addItem("57600");
       	    baudChoice.addItem("115200");
       	    baudChoice.setSelectedItem(baud);
       	    if (!baudChoice.getSelectedItem().equals(baud))
    	        baudChoice.setSelectedItem("115200");

        	buttonPanel = new JPanel();
        	southPanel.add(buttonPanel, BorderLayout.SOUTH);

        	openCloseButton = new JButton("Open Port");
        	openCloseButton.addActionListener(this);
            openCloseButton.setActionCommand("Open Port");
        	buttonPanel.add(openCloseButton);

        	resetButton = new JButton("Reset");
        	resetButton.setEnabled(false);
        	resetButton.addActionListener(this);
            resetButton.setActionCommand("Reset");
        	buttonPanel.add(resetButton);

        	dtrPanel = new JPanel();
        	dtrPanel.setBorder(BorderFactory.createTitledBorder("DTR"));
        	buttonPanel.add(dtrPanel);

                ButtonGroup dtrGroup = new ButtonGroup();

                    dtrSet = new JRadioButton("Set", false);
                    dtrSet.setActionCommand("Set DTR");
                    dtrGroup.add (dtrSet);
                    dtrPanel.add(dtrSet);
                    dtrSet.addActionListener(this);
                    dtrSet.setEnabled(false);

                    dtrClear = new JRadioButton("Clear", false);
                    dtrClear.setActionCommand("Clear DTR");
                    dtrGroup.add(dtrClear);
                    dtrPanel.add(dtrClear);
                    dtrClear.addActionListener(this);
                    dtrClear.setEnabled(false);

    	menuBar = new JMenuBar();
        setJMenuBar(menuBar);

    	    fileMenu = new JMenu("File");
    	    fileMenu.setMnemonic('F');
    	    menuBar.add(fileMenu);

      	        loadHexFileItem = new JMenuItem("Load File");
       	        loadHexFileItem.addActionListener(this);
       	        loadHexFileItem.setActionCommand("Load");
                loadHexFileItem.setAccelerator(KeyStroke.getKeyStroke('F', Event.CTRL_MASK));
                loadHexFileItem.setEnabled(false);
       	        fileMenu.add(loadHexFileItem);

      	        verifyHexFileItem = new JMenuItem("Verify File");
       	        verifyHexFileItem.addActionListener(this);
       	        verifyHexFileItem.setActionCommand("Verify");
       	        verifyHexFileItem.setAccelerator(KeyStroke.getKeyStroke('H', Event.CTRL_MASK));
       	        verifyHexFileItem.setEnabled(false);
       	        fileMenu.add(verifyHexFileItem);

       	        fileMenu.addSeparator();


      	        enableUpdateLoaderItem = new JMenuItem("Enable Loader Update");
       	        enableUpdateLoaderItem.addActionListener(this);
       	        enableUpdateLoaderItem.setActionCommand("Enable Loader Update");
       	        enableUpdateLoaderItem.setEnabled(false);
      	        
      	        updateLoaderItem = new JMenuItem("Update Loader");
       	        updateLoaderItem.addActionListener(this);
       	        updateLoaderItem.setActionCommand("Update Loader");
       	        updateLoaderItem.setEnabled(false);

// Next two lines implement the "Update Loader" function.
                if (i_want_to_scrog_my_tini)
                {
       	            fileMenu.add(enableUpdateLoaderItem);
       	            fileMenu.add(updateLoaderItem);
       	            fileMenu.addSeparator();
       	        }

      	        loadMacroItem = new JMenuItem("Load Macro");
       	        loadMacroItem.addActionListener(this);
       	        loadMacroItem.setActionCommand("Load Macro");
       	        loadMacroItem.setAccelerator(KeyStroke.getKeyStroke('M', Event.CTRL_MASK));
       	        loadMacroItem.setEnabled(false);
       	        fileMenu.add(loadMacroItem);

      	        saveMacroItem = new JMenuItem("Save Macro");
       	        saveMacroItem.addActionListener(this);
       	        saveMacroItem.setActionCommand("Save Macro");
       	        saveMacroItem.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
       	        saveMacroItem.setEnabled(false);
       	        fileMenu.add(saveMacroItem);

       	        fileMenu.addSeparator();

       	        exitItem = new JMenuItem("Exit");
       	        exitItem.addActionListener(this);
                exitItem.setActionCommand("Exit");
       	        fileMenu.add(exitItem);

    	    editMenu = new JMenu("Edit");
    	    editMenu.setMnemonic('E');
    	    editMenu.setEnabled(false);
    	    menuBar.add(editMenu);

      	        JMenuItem selectAllItem = new JMenuItem("Select All");
       	        selectAllItem.addActionListener(this);
       	        selectAllItem.setActionCommand("Select All");
       	        editMenu.add(selectAllItem);

      	        JMenuItem copyItem = new JMenuItem("Copy");
       	        copyItem.addActionListener(this);
       	        copyItem.setActionCommand("Copy");
       	        editMenu.add(copyItem);

      	        JMenuItem pasteItem = new JMenuItem("Paste");
       	        pasteItem.addActionListener(this);
       	        pasteItem.setActionCommand("Paste");
       	        editMenu.add(pasteItem);

    	    macroMenu = new JMenu("Macro");
    	    macroMenu.setMnemonic('M');
    	    macroMenu.setEnabled(false);
    	    menuBar.add(macroMenu);

      	        macroItem = new JMenuItem("Record Macro");
       	        macroItem.addActionListener(this);
       	        macroItem.setActionCommand("Record");
       	        macroItem.setEnabled(false);
       	        macroMenu.add(macroItem);

      	        runMacroItem = new JMenuItem("Run Macro");
       	        runMacroItem.addActionListener(this);
       	        runMacroItem.setActionCommand("Run");
       	        runMacroItem.setEnabled(false);
       	        macroMenu.add(runMacroItem);

       	    optionsMenu = new JMenu("Options");
       	    optionsMenu.setMnemonic('O');
       	    optionsMenu.setEnabled(false);
       	    menuBar.add(optionsMenu);

       	        consoleOutputItem = new JMenuItem("Enable Console Output");
       	        consoleOutputItem.addActionListener(this);
       	        consoleOutputItem.setActionCommand("Console");
       	        consoleOutputItem.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK | Event.ALT_MASK));
       	        optionsMenu.add(consoleOutputItem);

       	        JMenuItem ctrlCItem = new JMenuItem("Send CTRL-C");
       	        ctrlCItem.addActionListener(this);
       	        ctrlCItem.setActionCommand("CTRL-C");
       	        optionsMenu.add(ctrlCItem);

       	        lineWrapItem = new JMenuItem("Enable Line Wrap");
       	        lineWrapItem.addActionListener(this);
       	        lineWrapItem.setActionCommand("Line Wrap");
       	        optionsMenu.add(lineWrapItem);

       	        trapCtrlKeysItem = new JMenuItem("Don't Trap Ctrl Keys");
       	        trapCtrlKeysItem.addActionListener(this);
       	        trapCtrlKeysItem.setActionCommand("Trap Ctrl Keys");
       	        optionsMenu.add(trapCtrlKeysItem);

       	        generateLogItem = new JMenuItem("Generate Log File");
       	        generateLogItem.addActionListener(this);
       	        generateLogItem.setActionCommand("Generate Log");
       	        optionsMenu.add(generateLogItem);

       	        autoZapItem = new JMenuItem("Disable AutoZap Mode");
       	        autoZapItem.addActionListener(this);
       	        autoZapItem.setActionCommand("AutoZap");
       	        optionsMenu.add(autoZapItem);

       	        bankProtectItem = new JMenuItem("Disable Bank 0 Protect");
       	        bankProtectItem.addActionListener(this);
       	        bankProtectItem.setActionCommand("Protect0");
       	        optionsMenu.add(bankProtectItem);

       	        debugModeItem = new JMenuItem((debugMode ? "Disable Debug Mode" : "Enable Debug Mode"));
       	        debugModeItem.addActionListener(this);
       	        debugModeItem.setActionCommand("Debug Mode");
       	        optionsMenu.add(debugModeItem);

       	        verboseModeItem = new JMenuItem("Enable Verbose Mode");
       	        verboseModeItem.addActionListener(this);
       	        verboseModeItem.setActionCommand("Verbose Mode");
       	        optionsMenu.add(verboseModeItem);

       	    JMenu helpMenu = new JMenu("Help");
       	    helpMenu.setMnemonic('H');
       	    menuBar.add(helpMenu);

       	        JMenuItem aboutItem = new JMenuItem("About");
       	        aboutItem.addActionListener(this);
       	        aboutItem.setActionCommand("About");
       	        helpMenu.add(aboutItem);

        //Determine what kind of VM we are running in (i.e. 1.1.x or 1.2.x)
        //There are several classes we'd like to use in JDK 1.2, but we don't
        //wanna restrict ONLY to JDK 1.2, so if we are in 1.1.x, use a more buggy
        //way to get the characters typed at the keyboard...
        String vmVersion = System.getProperty("java.specification.version");
        if (vmVersion != null)
        {
            if (vmVersion.startsWith("1.1"))
            {
                JDK11X_VM = true;
            }
            else
            {
                JDK11X_VM = false;
            }
        }

        String os = System.getProperty("os.name");
        if ((os != null) && (os.indexOf("Windows") == -1))
        {
            WINDOWS = false;
        }
        else
        {
            WINDOWS = true;
        }

    	addWindowListener(new CloseHandler(this));
    	ChoiceListener choiceListener = new ChoiceListener();
        baudChoice.addItemListener(choiceListener);
        emulatorChoice.addItemListener(choiceListener);
        inputOutputArea.addMouseListener(new MListener());

        rightClickPopupMenu = new JPopupMenu();
        inputOutputArea.add(rightClickPopupMenu);

            JMenuItem clearMenuItem = new JMenuItem("Clear Window");
            clearMenuItem.setActionCommand("Clear");
            clearMenuItem.addActionListener(this);
            rightClickPopupMenu.add(clearMenuItem);

            rightClickPopupMenu.addSeparator();

            JMenuItem rtClSelectAllItem = new JMenuItem("Select All");
            rtClSelectAllItem.setActionCommand("Select All");
            rtClSelectAllItem.addActionListener(this);
            rightClickPopupMenu.add(rtClSelectAllItem);

            JMenuItem rtClCopyItem = new JMenuItem("Copy");
            rtClCopyItem.setActionCommand("Copy");
            rtClCopyItem.addActionListener(this);
            rightClickPopupMenu.add(rtClCopyItem);

            JMenuItem rtClPasteItem = new JMenuItem("Paste");
            rtClPasteItem.setActionCommand("Paste");
            rtClPasteItem.addActionListener(this);
            rightClickPopupMenu.add(rtClPasteItem);

        Thread.yield();

        if (padSize == -1)
            padSize = this.padSize;

        byte[] temp = new byte[padSize];
        for (int i=0; i<temp.length; i++)
            temp[i] = 0x20;
        padString = new String(temp);

        if (comPort != null)
        {
            portChoice.setSelectedItem(comPort);
            if (!portChoice.getSelectedItem().equals(comPort))
            {
                portChoice.setSelectedIndex(0);
                JOptionPane.showMessageDialog(this, "Port: " + comPort +
                                            " is not a valid Serial Port.\nChoose a serial port.", "ERROR!", JOptionPane.ERROR_MESSAGE);
            }
            else
                openPort(true);
        }

        if (log)
        {
            initializeLogFile();
        }

        setDumbTerminalMode(true, false);

        this.exitAfterRun = exitAfterRun;
        if (macroFiles != null)
        {
            (new MacroRunner(macroFiles)).start();
        }

        chooser = new JFileChooser();
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
    }

    /**
     * Thread to load and run macros passed
     * from the command line.
     */
    public class MacroRunner extends Thread
    {
        String[] macroFiles;

        public MacroRunner(String[] macroFiles)
        {
            this.macroFiles = macroFiles;
        }

        public void run()
        {
            boolean reset = false;

            if (dumbEmulator)
            {
                reset = true;
                setDumbTerminalMode(false, false);
            }

            for (int i=0; i<macroFiles.length; i++)
            {
                if (loadMacro(macroFiles[i]))
                {
                    try
                    {
                        runCurrentMacro();
                    }
                    catch(InterruptedException ie)
                    {
                        inputOutputArea.append(ie.getMessage());
                        moveCursorToEnd();
                    }
                }
                if (escapeChar)
                {
                    if (reset)
                    {
                        setDumbTerminalMode(true, false);
                    }
                    return;
                }
            }

            if (JavaKit.exitAfterRun)
                shutdown();

            if (reset)
            {
                setDumbTerminalMode(true, false);
            }
        }
    }

    /**
     * Handle GUI action.
     */
    public void actionPerformed(ActionEvent e)
    {
    	String cmd = e.getActionCommand();

    	if (cmd.equals("Load"))
    	{
    	    File[] files = getFilesToLoad("Load");
    	    if (files != null)
    	        (new LoadFile(files,true)).start();
	    }
    	else if (cmd.equals("Verify"))
    	{
    	    File[] files = getFilesToLoad("Verify");
    	    if (files != null)
    	        (new LoadFile(files,false)).start();
	    }
	    else if (cmd.equals("Enable Loader Update"))
	    {
            int response = JOptionPane.showConfirmDialog(this, "WARNING: When updating the loader on TINI, it is possible" +
                                                        "\nto leave your hardware in an UNUSABLE state.  If this happens, your"+
                                                        "\nTINI may have to be shipped back to Dallas to re-install the Loader,"+
                                                        "\nleaving you TINI-less for some time.  Updating the loader" +
                                                        "\nis NOT RECOMMENDED.  Please only attempt to update the loader if you " +
                                                        "\nknow exactly what your are doing.  The loader is not the cause of" +
                                                        "\ndevelopment problems you may be having with TINI." +
                                                        "\n\nDo you wish to enable the Update Loader operation? (Note: this act alone"+
                                                        "\nwill not update your loader)",
                                                        "Enable Loader Update WARNING", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION)
            {
	            updateLoaderItem.setEnabled(true);
	            enableUpdateLoaderItem.setEnabled(false);
            }
	    }
    	else if (cmd.equals("Update Loader"))
    	{
            int response = JOptionPane.showConfirmDialog(this, "WARNING: A failure in this operation could result in the loss" +
                                                        "\nof the current loader on your TINI board.  Special hardware will be required" +
                                                        "\nto recover.  Proceed with caution." +
                                                        "\nNote: this operation may also clear some Flash banks." +
                                                        "\n\nDo you wish to update your loader?",
                                                        "Loader Update WARNING", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION)
            {
                File[] files = getFilesToLoad("Choose New Loader");
                if (files != null)
                {
                    LOADER_UPDATE_IN_PROGRESS = true;
    	            (new LoadFile(files, true)).start();
    	        }
            }
	    }
    	else if (cmd.equals("Exit"))
    	{
    	    //Close the port before we exit...
    	    shutdown();
    	}
    	else if (cmd.equals("About"))
    	{
            JOptionPane.showMessageDialog(this, "JavaKit\n\nVersion " + VERSION +
                                                "\n\nCopyright (C) 1999, 2000 Dallas Semiconductor Corporation",
                                                "About", JOptionPane.INFORMATION_MESSAGE);
    	}
    	else if (cmd.equals("Open Port"))
    	{
    	    openPort(false);
    	}
    	else if (cmd.equals("Clear"))
    	{
    	    inputOutputArea.setText("");
            charactersWritten = 0;
    	}
    	else if (cmd.equals("Line Wrap"))
    	{
    	    if (lineWrapItem.getText().startsWith("Enable"))
    	    {
    	        lineWrapItem.setText("Disable Line Wrap");
    	        inputOutputArea.setWrapStyleWord(false);
    	        inputOutputArea.setLineWrap(true);
    	    }
    	    else
    	    {
    	        lineWrapItem.setText("Enable Line Wrap");
    	        inputOutputArea.setLineWrap(false);
    	    }
    	}
    	else if (cmd.equals("Trap Ctrl Keys"))
    	{
    	    if (trapCtrlKeysItem.getText().startsWith("Trap"))
    	    {
    	        trapCtrlKeysItem.setText("Don't Trap Ctrl Keys");
    	        trapCtrlKeys = true;
    	    }
    	    else
    	    {
    	        trapCtrlKeysItem.setText("Trap Ctrl Keys");
    	        trapCtrlKeys = false;
    	    }
    	}
    	else if (cmd.equals("Generate Log"))
    	{
    	    if (generateLogItem.getText().startsWith("Generate"))
    	    {
    	        initializeLogFile();
    	    }
    	    else
    	    {
    	        generateLogItem.setText("Generate Log File");
    	        logFile.close();
    	        logFile = null;
    	    }
    	}
    	else if (cmd.equals("Debug Mode"))
    	{
    	    if (debugModeItem.getText().startsWith("Enable"))
    	    {
    	        debugModeItem.setText("Disable Debug Mode");
    	        debugMode = true;
    	    }
    	    else
    	    {
    	        debugModeItem.setText("Enable Debug Mode");
    	        debugMode = false;
    	    }
    	}
    	else if (cmd.equals("Verbose Mode"))
    	{
    	    if (verboseModeItem.getText().startsWith("Enable"))
    	    {
    	        verboseModeItem.setText("Disable Verbose Mode");
    	        verboseMode = true;
    	    }
    	    else
    	    {
    	        verboseModeItem.setText("Enable Verbose Mode");
    	        verboseMode = false;
    	    }
    	}
    	else if (cmd.equals("AutoZap"))
    	{
    	    if (autoZapItem.getText().startsWith("Enable"))
    	    {
    	        autoZapItem.setText("Disable AutoZap Mode");
    	        AUTO_ZAP_BANKS = true;
    	    }
    	    else
    	    {
    	        autoZapItem.setText("Enable AutoZap Mode");
    	        AUTO_ZAP_BANKS = false;
    	    }
    	}
    	else if (cmd.equals("Protect0"))
    	{
    	    if (bankProtectItem.getText().startsWith("Enable"))
    	    {
    	        bankProtectItem.setText("Disable Bank 0 Protect");
    	        PROTECT_BANK_0 = true;
    	    }
    	    else
    	    {
    	        bankProtectItem.setText("Enable Bank 0 Protect");
    	        PROTECT_BANK_0 = false;
    	    }
    	}
    	else if (cmd.equals("CTRL-C"))
    	{
    	    if (serialPort != null)
    	    {
    	        try
    	        {
    	            outputStream.write(0x03);
    	        }
    	        catch(IOException ioe)
    	        {
    	            //DRAIN
    	        }
    	    }
    	}
    	else if (cmd.equals("Console"))
    	{
    	    consoleOutput = !consoleOutput;
    	    statusLabel.setText("Console output set to: " + consoleOutput);
    	    if (consoleOutput)
    	        consoleOutputItem.setText("Disable Console Output");
    	    else
    	        consoleOutputItem.setText("Enable Console Output");
    	}
    	else if (cmd.equals("Copy"))
    	{
    	    copyText();
    	}
    	else if (cmd.equals("Paste"))
    	{
    	    pasteText();
    	}
    	else if (cmd.equals("Select All"))
    	{
    	    selectAll();
    	}
    	else if (cmd.equals("Close Port"))
    	{
    	    closePort();
    	    inputOutputArea.setText("");
    	    charactersWritten = 0;
    	}
    	else if (cmd.equals("Reset"))
    	{
    	    reset(false);
    	}
    	else if (cmd.equals("Set DTR"))
    	{
    	    setDTR();
    	}
    	else if (cmd.equals("Clear DTR"))
    	{
    	    clearDTR();
    	}
    	else if (cmd.equals("Record"))
    	{
            if (macroModified)
            {
                int response = JOptionPane.showConfirmDialog(this, "The current macro has not been saved." +
                                                            "\n\nDo you wish to save before recording a new macro?",
                                                            "Save Macro?", JOptionPane.YES_NO_CANCEL_OPTION);
                if (response == JOptionPane.YES_OPTION)
                {
                    saveMacro();
                }
                else if (response == JOptionPane.CANCEL_OPTION)
                {
                    return;
                }
            }

    	    macroModified = true;
    	    statusLabel.setText("Recording Macro...");
    	    runMacroItem.setEnabled(false);
    	    emulatorChoice.setEnabled(false);
    	    currentMacro = new Vector();
    	    recordingMacro = true;
    	    macroItem.setActionCommand("Stop");
    	    macroItem.setText("Stop Recording");
    	    optionsMenu.setEnabled(false);
    	    saveMacroItem.setEnabled(false);
    	    loadMacroItem.setEnabled(false);
    	}
    	else if (cmd.equals("Stop"))
    	{
    	    statusLabel.setText("");
    	    recordingMacro = false;
    	    emulatorChoice.setEnabled(true);
    	    runMacroItem.setEnabled(true);
    	    macroItem.setActionCommand("Record");
    	    macroItem.setText("Record Macro");
    	    optionsMenu.setEnabled(true);
    	    saveMacroItem.setEnabled(true);
    	    loadMacroItem.setEnabled(true);
    	}
    	else if (cmd.equals("Run"))
    	{
            (new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            runCurrentMacro();
                        }
                        catch(InterruptedException ie)
                        {
                            inputOutputArea.append(ie.getMessage());
                            moveCursorToEnd();
                        }
                    }
                }).start();
    	}
    	else if (cmd.equals("Load Macro"))
    	{
            if (macroModified)
            {
                int response = JOptionPane.showConfirmDialog(this, "The current macro has not been saved." +
                                                            "\n\nDo you wish to save before loading a new macro?",
                                                            "Save Macro?", JOptionPane.YES_NO_CANCEL_OPTION);
                if (response == JOptionPane.YES_OPTION)
                {
                    saveMacro();
                }
                else if (response == JOptionPane.CANCEL_OPTION)
                {
                    return;
                }
            }

	        chooser.setDialogTitle("Load Macro");
	        chooser.resetChoosableFileFilters();
	        chooser.setFileFilter(new ExtensionFileFilter("mac","Macro Files"));
            chooser.setMultiSelectionEnabled(false);
            chooser.setCurrentDirectory(new File(lastUsedDir));

            if((chooser.showOpenDialog(JavaKit.mainFrame)) == JFileChooser.APPROVE_OPTION)
            {
                lastUsedDir = chooser.getCurrentDirectory().getAbsolutePath();

                String file = chooser.getSelectedFile().getName();
                if (file != null)
                {
                    loadMacro(lastUsedDir + File.separator + file);
        	    }
        	    macroModified = false;
        	}
        }
    	else if (cmd.equals("Save Macro"))
    	{
    	    saveMacro();
        }

    	inputOutputArea.requestFocus();
    }

    /**
     * Display the hex byte data in the given String.
     */
    private void sendDebug(PrintStream out, String header, String dataStr)
    {
        if (dataStr != null)
        {
            byte[] data = dataStr.getBytes();
            sendDebug(out, header, data, 0, data.length);
        }
    }

    private void sendDebug(PrintStream out, String header, byte[] data, int start, int len)
    {
        if (data != null)
        {
    	    out.println(header + data.length + " bytes.");
    	    for (int i=0; i<len; i++)
    	    {
    	        out.print(Integer.toHexString(data[i+start] & 0x0FF) + " ");
    	    }
    	    out.println("");
    	}
    }

    /**
     * Umm, initialize the log file.
     */
    private void initializeLogFile()
    {
    	try
    	{
    	    logFile = new PrintStream(new FileOutputStream("JavaKit.log"));
    	}
    	catch(IOException ioe)
    	{
            JOptionPane.showMessageDialog(this, "IOException occured creating log file.\n\n" +
                                            "Check write permissions of current directory.", "ERROR!", JOptionPane.ERROR_MESSAGE);
            logFile = null;
            return;
    	}
    	generateLogItem.setText("Stop Generating Log");
    }

    /**
     * Get the filename and save the current macro.
     */
    private void saveMacro()
    {
	    chooser.setDialogTitle("Save Macro");
	    chooser.resetChoosableFileFilters();
	    chooser.setFileFilter(new ExtensionFileFilter("mac","Macro Files"));
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(new File(lastUsedDir));

        if((chooser.showSaveDialog(JavaKit.mainFrame)) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
              lastUsedDir = chooser.getCurrentDirectory()./*getAbsolutePath*/getCanonicalPath();
            }
            catch(IOException io)
            {
              System.out.println(io);
            }

            String file = chooser.getSelectedFile().getName();
            if (file != null)
            {
                if (!file.endsWith(".mac"))
                    file += ".mac";
                try
                {
                    //Write the macro to the file
			        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(lastUsedDir + File.separator + file));
			        for (int i=0; i<currentMacro.size(); i++)
			        {
			            String current = (String)currentMacro.elementAt(i);
			            if (current.startsWith("<JavaKit"))
			                current = current.concat("\n");
			            else
			                current = current.substring(0,current.length()-endCommandLineStr.length()) + "\n";

			            dataOut.writeBytes(current);
			        }
			        dataOut.close();
                }
                catch(IOException ioe)
                {
                    JOptionPane.showMessageDialog(this, "IOException occured reading the Macro File.\n\n" +
                                                    "Macro file may be corrupt.", "ERROR!", JOptionPane.ERROR_MESSAGE);
                }
            }
            macroModified = false;
        }
    }

    /**
     * Load the given macro file.
     */
    private boolean loadMacro(String fileName)
    {
        currentMacro = new Vector();

        try
        {
            File file = new File(fileName);
            currentMacroName = fileName;
            lastUsedDir = file./*getAbsolutePath*/getCanonicalPath();

            if (!file.exists())
            {
                JOptionPane.showMessageDialog(this, "Could not find macro file: " + fileName,
                                                "ERROR!", JOptionPane.ERROR_MESSAGE);
        		return false;
            }

        	BufferedReader inFile = new BufferedReader(new FileReader(file));

        	String line;
            while ((line = inFile.readLine()) != null)
            {
                if (!line.startsWith("<JavaKit"))
                    line = line.concat(endCommandLineStr);

                currentMacro.addElement(line);
            }
        }
        catch(IOException ioe)
        {
            JOptionPane.showMessageDialog(this, "IOException occured reading the Macro File.\n\n" +
                                            "Macro file may be corrupt.", "ERROR!", JOptionPane.ERROR_MESSAGE);
             return false;
        }

        runMacroItem.setEnabled(true);

        return true;
    }

    /**
     * Run the currently loaded macro.
     */
    private void runCurrentMacro() throws InterruptedException
    {
    	Cursor previousCursor = getCursor();
    	setNewCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	setTitle("JavaKit - Running macro");
    	statusLabel.setText("Running macro: " + currentMacroName + "...");
    	if (logFile != null)
    	    logFile.println("<Running macro: " + currentMacroName + "...>");
    	dtrSet.setEnabled(false);
    	dtrClear.setEnabled(false);
    	resetButton.setEnabled(false);
    	openCloseButton.setEnabled(false);

    	escapeChar = false;

    	int macroSize = currentMacro.size();
    	for (int i=0; i<macroSize; i++)
    	{
    	    final String current = (String)currentMacro.elementAt(i);
    	    if (debugMode)
                System.out.println("Running: " + current);

            if (current.startsWith("<JavaKit PROMPT"))
            {
                PROMPT_STRING = current.substring(15).replace((char)0x01,'\n').replace((char)0x02,'\r');
                if (debugMode)
                    System.out.println("Set prompt to:" + PROMPT_STRING + ":");
                continue;
            }
            else if ((i+1)<macroSize)
            {
                String temp = (String)currentMacro.elementAt(i+1);
                if (temp.startsWith("<JavaKit PROMPT"))
                {
                    PROMPT_STRING = temp.substring(15).replace((char)0x01,'\n').replace((char)0x02,'\r');
                    i++;
                    if (debugMode)
                        System.out.println("Set prompt to:" + PROMPT_STRING + ":");
                }
            }

    	    if (current.startsWith("<JavaKit LOAD>"))
    	    {
                finishedLoading = false;
                File[] files = new File[1];
                files[0] = new File(current.substring(14));
    	        (new LoadFile(files,true)).start();
    	        while (!finishedLoading)
    	        {
    	            try
    	            {
    	                Thread.sleep(100);
    	            }
    	            catch(InterruptedException ie){}
    	            if (escapeChar)
    	            {
    	                setNewCursor(previousCursor);
    	                statusLabel.setText("Macro load canceled.");
    	                setTitle("JavaKit");
    	                return;
    	            }
    	        }
    	    }
    	    else if (current.startsWith("<JavaKit VERIFY>"))
    	    {
                finishedLoading = false;
                File[] files = new File[1];
                files[0] = new File(current.substring(16));
    	        (new LoadFile(files,false)).start();
    	        while (!finishedLoading)
    	        {
    	            try
    	            {
    	                Thread.sleep(100);
    	            }
    	            catch(InterruptedException ie){}
    	        }
    	    }
    	    else if (current.startsWith("<JavaKit SET_DTR>"))
    	    {
    	        setDTR();
    	    }
    	    else if (current.startsWith("<JavaKit CLEAR_DTR>"))
    	    {
    	        clearDTR();
    	    }
    	    else if (current.startsWith("<JavaKit RESET>"))
    	    {
    	        String temp = PROMPT_STRING;
    	        reset(false);
    	        PROMPT_STRING = temp;
    	    }
    	    else
    	    {
    	        try
    	        {
    	            if (logFile != null)
    	            {
    	                logFile.print(current);
    	                //Make more efficient?
    	                if (debugMode)
    	                {
    	                    sendDebug(logFile, "**Sending: ", current);
    	                }
    	            }
    	            if (debugMode)
    	            {
    	                sendDebug(System.out, "**Sending: ", current);
    	            }

                    //DEBUG TEST
                    Runnable addToTextField = new Runnable() {
                        public void run()
                        {
                            inputOutputArea.append(current + "\n");
                        }
                    };
                    SwingUtilities.invokeLater(addToTextField);

    	            promptReceived = false;
    	            outputStream.write(current.getBytes());
    	        }
    	        catch(Exception ioe)
    	        {
    	            System.out.println("IOException occured while running macro...");
    	            //DEBUG ==> Handle better...
    	        }
    	    }

    	    //No need to wait for response if this is the last line in the macro.
    	    if ((i+1) != macroSize)
    	    {
                while((!promptReceived) && (!escapeChar))
                {
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch(InterruptedException ie)
                    {
                        //DRAIN
                    }
                }

                if (escapeChar)
                {
                    break;
                }
            }
    	}

    	openCloseButton.setEnabled(true);
    	inputOutputArea.setEditable(true);
    	dtrSet.setEnabled(true);
    	dtrClear.setEnabled(true);
    	resetButton.setEnabled(true);
    	setNewCursor(previousCursor);
    	statusLabel.setText("");
    	setTitle("JavaKit");
    	if (debugMode)
    	    System.out.println("Macro run finished.");
    	inputOutputArea.requestFocus();
    }

    /**
     * Send a reset to the TINI board.  (i.e. Toggle DTR, send
     * the command to start the boot loader.)
     */
    private void reset(boolean longPrompt)
    {
    	if (serialPort != null)
    	{
    	    if (debugMode)
    	    {
    	        System.out.println("Attempting reset()...");
    	    }
    	    if (logFile != null)
    	    {
    	        logFile.println("<RESET>");
    	    }
            setNewCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            serialPort.setDTR(true);
            try{Thread.sleep(DTR_TOGGLE_TIMEOUT);}catch(InterruptedException ex){}
            serialPort.setDTR(false);
            dtrClear.setSelected(true);
            try{Thread.sleep(RESET_WAIT);}catch(InterruptedException ex){}

        	if (recordingMacro)
        		currentMacro.addElement("<JavaKit RESET>" + endCommandLineStr);


            newInputReceived = false;
            escapeChar = false;

            try
            {
                if (!longPrompt)
                    sendAndWait(endCommandLine, true);
                else
                    sendAndWait(endCommandLine, DEFAULT_LONG_PROMPT);
            }
            catch(Exception e)
            {
                if (!suppressOutput)
                {
                    inputOutputArea.append(e.getMessage());
                    moveCursorToEnd();
                }
            }
            setNewCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    	}
    }

    /**
     * Copy the selected text in the text area to the system clipboard.
     */
    private void copyText()
    {
        inputOutputArea.copy();
    }

    /**
     * Paste the system clipboard to the end of the text area.
     */
    private void pasteText()
    {
        //DEBUG -- Am I sure this will always work - no race condition?
        moveCursorToEnd();
        if (!dumbEmulator)
        {
            inputOutputArea.paste();
        }

        String clipboard = null;
        try
        {
            clipboard = (String)(getToolkit().getSystemClipboard()).getContents(this).getTransferData(DataFlavor.stringFlavor);
        }
        catch(Exception e)
        {
            System.out.println("Error in getting clipboard data for paste: " + e.toString()); //DEBUG
            //Not a supported DataFlavor, or an IOException perhaps.
            return;
        }

        userInput = userInput.concat((String)clipboard);
        KeyEvent k = new KeyEvent(this, 0, 0, 0, 0, (char)KeyEvent.VK_ENTER);
        boolean backup = dumbEmulator;
        dumbEmulator = false;
        keyListener.keyReleased(k);
        dumbEmulator = backup;
    }

    /**
     * Select all of the text in the text area.
     */
    private void selectAll()
    {
        inputOutputArea.selectAll();
    }

    /**
     * Assert DTR.
     */
    private void setDTR()
    {
    	if (serialPort != null)
    	{
    	    if (logFile != null)
    	    {
    	        logFile.println("\nSet DTR");
    	    }
    	    serialPort.setDTR(true);

            if (recordingMacro)
        	    currentMacro.addElement("<JavaKit SET_DTR>" + endCommandLineStr);
    	}
    }

    /**
     * Clear DTR.
     */
    private void clearDTR()
    {
        if (serialPort != null)
        {
    	    if (logFile != null)
    	    {
    	        logFile.println("\nCleared DTR");
    	    }
    	    serialPort.setDTR(false);

            if (recordingMacro)
        	    currentMacro.addElement("<JavaKit CLEAR_DTR>" + endCommandLineStr);
        }
    }

    /**
     * Load the initialization file and set the preferences.
     */
    private void loadINIFile()
    {
		//Get the last used directory from our temp file if it exists...
		try
		{
			FileInputStream file = new FileInputStream("JavaKit.tmp");
			BufferedReader data = new BufferedReader(new InputStreamReader((InputStream)file));
			lastUsedDir = new String(data.readLine());
			baud = new String(data.readLine());
			width = Integer.parseInt(data.readLine(), 16);
			height = Integer.parseInt(data.readLine(), 16);
			xPos = Integer.parseInt(data.readLine(), 16);
			yPos = Integer.parseInt(data.readLine(), 16);
		}
		catch(Exception e)
		{
			lastUsedDir = System.getProperty("user.dir");
			baud = "115200";
            height = 550;
            width = 610;
		}
    }

    /**
     * Write the initialization file.
     */
    private void writeINIFile()
    {
        //Write the new current directory to our temp file...
		try
		{
            FileOutputStream outFile = new FileOutputStream("JavaKit.tmp");
			DataOutputStream dataOut = new DataOutputStream((OutputStream)outFile);
			dataOut.writeBytes(lastUsedDir + "\n");
			dataOut.writeBytes(baudChoice.getSelectedItem() + "\n");
			Dimension dim = getSize();
			dataOut.writeBytes(Integer.toHexString(dim.width) + "\n");
			dataOut.writeBytes(Integer.toHexString(dim.height) + "\n");
			Point point = getLocation();
			dataOut.writeBytes(Integer.toHexString(point.x) + "\n");
			dataOut.writeBytes(Integer.toHexString(point.y) + "\n");
		}
        catch(IOException ioe)
        {
            //DRAIN
        }
    }

    /**
     * Place the cursor at the end of the text area.
     */
    public void moveCursorToEnd()
    {
        inputOutputArea.setCaretPosition(inputOutputArea.getDocument().getEndPosition().getOffset() - 1);
        inputOutputArea.repaint();
    }

    class SpecialKeyListener extends KeyAdapter
    {
    	OutputStream os;

    	public SpecialKeyListener(OutputStream os)
    	{
    	    super();
    	    this.os = os;
    	}

        public void keyPressed(KeyEvent e)
        {
            if (e.getModifiers() == ActionEvent.ALT_MASK)
            {
                //DEBUG
                //This is because of Swing Bug #4159610  The accelerator
                //events are still getting fired down to the textcomponent that
                //currently has focus.  This means that if you hit alt 'f' for
                //example, the File menu would be activated, but an 'f' would
                //still appear in the current editor.  So for now, when the alt
                //key is hit, we send focus to the main menu bar.  The drawback
                //being that after the user is finished with the menu, it doesn't
                //return the cursor to the user's place in that editor.
                menuBar.grabFocus();
            }
            else if (trapCtrlKeys && (e.getModifiers() == ActionEvent.CTRL_MASK))
            {
                return;
            }

            moveCursorToEnd();

            int keyCode = e.getKeyCode();

            if ( (keyCode == KeyEvent.VK_UP) || (keyCode == KeyEvent.VK_DOWN) ||
                 (keyCode == KeyEvent.VK_RIGHT) || (keyCode == KeyEvent.VK_LEFT) )
            {
                e.consume();
                try
                {
                    //Send the control sequences that slush will look for for
                    //arrow events...
                    os.write(0x1b);
                    os.write(0x4f);
                    if (keyCode == KeyEvent.VK_UP)
                    {
                        os.write(0x41);
                    }
                    else if (keyCode == KeyEvent.VK_DOWN)
                    {
                        os.write(0x42);
                    }
                    else if (keyCode == KeyEvent.VK_LEFT)
                    {
                        os.write(0x44);
                    }
                    else if (keyCode == KeyEvent.VK_RIGHT)
                    {
                        os.write(0x43);
                    }
                }
                catch(IOException ioe)
                {
                    //DRAIN
                }
            }
            else if (dumbEmulator)
            {
                e.consume();
        	    try
        	    {
                    if (keyCode == KeyEvent.VK_ENTER)
                    {
                        os.write(endCommandLine);
                        if (logFile != null)
                        {
                            logFile.print(endCommandLineStr);
    	                    if (debugMode)
    	                    {
    	                        sendDebug(logFile, "**Sending: ", endCommandLineStr);
    	                    }
                        }
    	                if (debugMode)
    	                {
    	                    sendDebug(System.out, "**Sending: ", endCommandLineStr);
    	                }
                    }
                    else if (keyCode == KeyEvent.VK_BACK_SPACE)
                    {
                        os.write(0x08);
                        if (logFile != null)
                            logFile.write(0x08);
                        if (debugMode)
                            System.out.println("Sending Backspace: " + Integer.toHexString(0x08));
                    }
                    else if ((keyCode != KeyEvent.VK_SHIFT) && (keyCode != KeyEvent.VK_ALT) &&
                             (keyCode != KeyEvent.VK_CONTROL) && (keyCode != KeyEvent.VK_CAPS_LOCK))
                    {
                        char c = e.getKeyChar();
                        os.write(c);
                        if (logFile != null)
                            logFile.print(c);
    	                if (debugMode)
    	                {
    	                    System.out.println("**Sending: " + Integer.toHexString(c));
    	                }
                    }
                }
                catch(IOException ioe)
                {
                    System.out.println("IOException writing to Serial Port."); //DEBUG
                }
                return;
            }

            if ( (keyCode == KeyEvent.VK_BACK_SPACE) && (userInput.length() == 0))
            {
                e.consume();
            }
        }

        //DEBUG ==> I would like this to be a keyTyped function, but JDK 1.1.x seems to
        //not work unless this is a keyReleased...
        //I need to lookup the Swing Bug ID for this one.  Basically, the keyTyped event
        //is not being correctly delivered.
        public void keyReleased(KeyEvent e)
        {
            char newChar = e.getKeyChar();

            if (newChar == KeyEvent.VK_ESCAPE)
    	    {
    	        escapeChar = true;
    	    }

            if (dumbEmulator)
            {
                e.consume();
                return;
            }

            if (newChar == KeyEvent.VK_ENTER)
            {
                //Only send the data after each return when in JavaKit Terminal mode
                userInput = userInput.concat(endCommandLineStr);

        	    try
        	    {
        	    	os.write(userInput.getBytes());
                    if (logFile != null)
                    {
                        logFile.print(userInput);
    	                if (debugMode)
    	                {
    	                    sendDebug(logFile, "**Sending: ", userInput);
    	                }
                    }
    	            if (debugMode)
    	            {
    	                sendDebug(System.out, "**Sending: ", userInput);
    	            }
        	    	if (recordingMacro)
        	    	{
        	    	    currentMacro.addElement("<JavaKit PROMPT" + PROMPT_STRING.replace('\n',(char)0x01).replace('\r',(char)0x02));
        	    	    currentMacro.addElement(userInput);
        	    	}

        	    	userInput = "";
        	    }
        	    catch (IOException ioe)
        	    {
            		System.err.println("OutputStream write error: " + e);
        	    }
    	    }
            else if ( (JDK11X_VM) && ( (newChar != KeyEvent.VK_SHIFT) &&
                      (newChar != KeyEvent.CHAR_UNDEFINED)) )
            {
                userInput = userInput.concat((new Character(newChar)).toString());
            }
        }

        public void keyTyped(KeyEvent e)
        {
            if (dumbEmulator)
            {
                e.consume();
                return;
            }
            else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE)
            {
                if (userInput.length() == 0)
                    e.consume();
                else if (userInput.length() == 1)
                    userInput = "";
                else
                    userInput = userInput.substring(0,userInput.length()-1);
            }
            else if (!WINDOWS)
            {
                //DEBUG --> ARRG I hate this hack...
                userInput = userInput.concat((new Character(e.getKeyChar())).toString());
            }
        }
    }

    /**
     * This allows us to suppress local echo in the text area on key enter.
     */
    public class InputHandler implements InputMethodListener
    {
        public void inputMethodTextChanged(InputMethodEvent event)
        {
            if (dumbEmulator)
            {
                event.consume();
                return;
            }
            else
            {
                CharacterIterator ci = event.getText();
                userInput = userInput.concat(new Character(ci.current()).toString());
            }
        }

        public void caretPositionChanged(InputMethodEvent event)
        {
        }
    }

    /**
     * Mouse handler.
     */
    class MListener extends MouseAdapter
    {
        //public void mouseClicked(MouseEvent event)
        public void mousePressed(MouseEvent event)
        {
            if(event.getModifiers() == MouseEvent.BUTTON3_MASK)
            {
                if (serialPort != null)
                {
                    rightClickPopupMenu.show(inputOutputArea,event.getX(),event.getY());
                }

                event.consume();
            }
        }
        public void mouseReleased(MouseEvent event)
        {
            if(event.getModifiers() == MouseEvent.BUTTON3_MASK)
            {
                event.consume();
            }
        }

        public void mouseClicked(MouseEvent event)
        {
            if(event.getModifiers() == MouseEvent.BUTTON3_MASK)
            {
                event.consume();
            }
        }
    }

    /**
     * Handles receives from the serial port and displays in the
     * text area.
     */
    public class SerialEventListener implements SerialPortEventListener
    {
        byte[] buffer     = new byte[512];
        int    bufferSize = 0;

        public void serialEvent(SerialPortEvent e)
        {
        	String  input     = "";

            if (e.getEventType() != SerialPortEvent.DATA_AVAILABLE)
            {
                return;
            }

		   	try
		   	{
		   	    while (inputStream.available() > 0)
		   	    {
//                    bufferSize = inputStream.read(buffer);
                     int amount_to_read = inputStream.available();
		     if (amount_to_read>buffer.length)
			amount_to_read = buffer.length;
		     bufferSize = inputStream.read(buffer,0,amount_to_read);


                    if (VT100Emulator)
                    {
                        for (int i=0; i< bufferSize; i++)
                        {
                            if (buffer[i] == 0x1b)
                            {
                                handleEscape(i);
                                break;
                            }
                        }

                        if (bufferSize == 0)
                        {
                            continue;
                        }
                    }

                    if (input == null)
		   	            input = new String(buffer, 0, bufferSize);
		   	        else
		   	            input = input.concat(new String(buffer, 0, bufferSize));

	      	        if (logFile != null)
	      	        {
	      	            logFile.print(input);
	      	            if (debugMode)
	      	            {
	      	                sendDebug(logFile, "\n**Received: " + bufferSize + " bytes:", buffer, 0, bufferSize);
	      	            }
	      	        }

	      	        if (debugMode)
	      	        {
	      	            sendDebug(System.out, "\n**Received: " + bufferSize + " bytes:", buffer, 0, bufferSize);
	      	        }
		   	    }
	    	}
	    	catch (IOException ioe)
	    	{
                JOptionPane.showMessageDialog(JavaKit.mainFrame, "IOException occured reading the Serial Port.\n\n" +
                                              "Close the program and try again.", "ERROR!", JOptionPane.ERROR_MESSAGE);
	    	    return;
	      	}

	      	if (recordingMacro)
	      	{
	      	    if (input.length() > 3)
	      	        PROMPT_STRING = input.substring(input.length()-3);
	      	    else if (input.length() > 0)
	      	        PROMPT_STRING = input.substring(input.length()-1);
	      	}

            if ((consoleOutput) && (!suppressOutput))
            {
                System.out.print(input);
            }
            else if (!suppressOutput)
            {
                if (!((bufferSize == 4) && ( (buffer[1] == 0x08) && (buffer[2] == 0x20) && (buffer[3] == 0x08))) )
                {
                    if (dumbEmulator)
                    {
                        //This is still the crusty hack-o-matic way of handling the backspace event.
                        //Should really get around to doing this better.  But... it works.

                        //KLA - I added some more crusty hack-o-matic to this, if we send too much to the loader,
                        //then it spits back out the charater we sent + this stuff
    		            if (((bufferSize == 3) && ( (buffer[0] == 0x08) && (buffer[1] == 0x20) && (buffer[2] == 0x08))) ||
    //		                ((bufferSize == 4) && ( (buffer[1] == 0x08) && (buffer[2] == 0x20) && (buffer[3] == 0x08))) ||
                            ((bufferSize == 1) && (buffer[0] == 0x08)))
                        {
                            try
                            {
                                int endOffset = inputOutputArea.getLineEndOffset(inputOutputArea.getLineCount()-1);
                                inputOutputArea.replaceRange("",endOffset - 2, endOffset - 1);
                                charactersWritten--;
                            }
                            catch(BadLocationException ble)
                            {
                                //DRAIN
                            }
                            return;
                        }
                        else if (buffer[0] == 0x7F)
                        {
                            return;
                        }
                    }
                    charactersWritten += input.length();
    
                    // TextArea seems to have a problem when you keep appending text endlessly.
        	        // So, keep a 20K buffer


        		//AAAHHH! we weren't outputting after the 20000 character hack!
                    if (charactersWritten >= 20000)
                    {
                         try
                         {
                             inputOutputArea.replaceRange("",0,inputOutputArea.getLineEndOffset(inputOutputArea.getLineCount()/2));
                             charactersWritten = 0;
                         }
                         catch(BadLocationException ble)
                         {
                	        //DRAIN
                         }
                    }
                    inputOutputArea.append(input);
                }

            }

            //Now check for new input received...
            if (partialPromptReceived)
            {
                int end = Math.min(promptBytes.length-promptPos, bufferSize);
                for (int i=0; i<end; i++)
                {
                    if (promptBytes[promptPos++] != buffer[i])
                    {
                        partialPromptReceived = false;
                        promptPos = -1;
                        break;
                    }
                }
                if ((partialPromptReceived) && (promptPos == promptBytes.length))
                {
                    promptReceived = true;
                    partialPromptReceived = false;
                    promptPos = -1;
                }
            }

            if ((!promptReceived) && (!partialPromptReceived))
            {
                if (input.endsWith(PROMPT_STRING))
                {
                    promptReceived = true;
                }
                else
                {
                    promptBytes = PROMPT_STRING.getBytes();
                    int start = bufferSize - Math.min(promptBytes.length, bufferSize);
                    int partialPos = -1;
                    partialPromptReceived = false;
                    for (int i=start; i<bufferSize; i++)
                    {
                        if (promptBytes[0] == buffer[i])
                        {
                            partialPos = i;
                            break;
                        }
                    }
                    if (partialPos != -1)
                    {
                        partialPromptReceived = true;
                        int index = 0;
                        int end = Math.min(promptBytes.length, bufferSize-partialPos);
                        for (promptPos=0; promptPos<end; promptPos++)
                        {
                            if (promptBytes[promptPos] != buffer[partialPos++])
                            {
                                partialPromptReceived = false;
                                break;
                            }
                        }
                    }
                }
            }

            possibleError = (possibleError) ? true : (input.indexOf("*Err*") != -1);

            if (checkCRC)
            {
                JavaKit.CRC_CHECK += input;
            }

            newInputReceived = true;

            moveCursorToEnd();
        }

        /**
         * DEBUG ==> Not yet implemented.
         * Handle VT100 escape sequence.
         *
         * CLEAR_DISPLAY = {0x1B, 0x5B, (byte)'2', (byte)'J'};
         * CLEAR_LINE    = {0x1B, 0x5B, 0x32, (byte)'K'};
         * CLEAR_TO_EOL  = {0x1B, 0x5B, (byte)'K'};
         * BOLD_ON       = {0x1B, 0x5B, 0x31, (byte)'m'};
         * BOLD_OFF      = {0x1B, 0x5B, 0x30, (byte)'m'};
         * DELETE_CHAR   = {0x1B, 0x5B, (byte)'P'};
         * MOVE_POS      = {0x1B, 0x5B, NUM*, {UP='A', DOWN='B', RIGHT='C', LEFT='D'}};
         * SET_CURSOR    = {0x1B, 0x5B, LINE*, ';', COL*, 0x48};
         * DELETE_LINES  = {0x1B, 0x5B, NUM*, 'M'};
         * SAVE_CURSOR   = {0x1B, 0x5B, 's'};
         * RESTORE_CURSOR= {0x1B, 0x5B, 'u'};
         * INSERT_LINES  = {0x1B, 0x5B, NUM*, 'L'};
         *
         * Note: The '*' after a variable means it could be two digit.
         */
        void handleEscape(int pos)
        {
            int orig = pos;
            while(pos < (bufferSize-2))
            {
                if (buffer[pos++] == 0x1B)
                {
                    if (buffer[pos++] == 0x5B)
                    {
                        //Look for a number
                        if ((buffer[pos] >= 0x30) && (buffer[pos] <= 0x39))
                        {
                            byte num = (byte)(buffer[pos++] - 0x30);
                            //Look for a 2 digit number.
                            if ((buffer[pos] >= 0x30) && (buffer[pos] <= 0x39))
                            {
                                num = (byte)((num << 4) | (buffer[pos++] - 0x30));
                            }

                            switch(buffer[pos])
                            {
                                case ';':
                                    //Possible set cursor
                                    pos++;
                                    if ((buffer[pos] >= 0x30) && (buffer[pos] <= 0x39))
                                    {
                                        byte col = (byte)(buffer[pos++] - 0x30);
                                        //Look for a 2 digit number.
                                        if ((buffer[pos] >= 0x30) && (buffer[pos] <= 0x39))
                                        {
                                            col = (byte)((col << 4) | (buffer[pos++] - 0x30));
                                        }
                                        if (buffer[pos] == 0x48)
                                        {
                                            //Ok, a good set cursor;
                                            Element elem = inputOutputArea.getDocument().getDefaultRootElement().getElement(num);
                                            inputOutputArea.setCaretPosition(elem.getStartOffset()+col);
                                        }
                                    }
                                break;

                                case 'A':
                                    //UP
                                break;

                                case 'B':
                                    //DOWN
                                break;

                                case 'C':
                                    //RIGHT
                                break;

                                case 'D':
                                    //LEFT
                                break;

                                case 'J':
                                    //Clear display
                                    System.out.println("CLEAR DISPLAY"); //DEBUG
    	                            inputOutputArea.setText("");
                                    charactersWritten = 0;
                                break;

                                case 'M':
                                    //Delete lines
                                break;

                                case 'L':
                                    //Insert lines
                                break;

                                case 'm':
                                    //Bold
                                break;

                                default:
                                    pos--;
                                break;
                            }

                            pos++;
                        }
                        else if (buffer[pos] == 'K')
                        {
                            //Clear to EOL
                        }
                        else if (buffer[pos] == 'P')
                        {
                            //Delete char
                        }
                        else if (buffer[pos] == 's')
                        {
                            //Save cursor
                        }
                        else if (buffer[pos] == 'u')
                        {
                            //Restore cursor
                        }
                    }
                    if (orig != pos)
                    {
                        System.arraycopy(buffer, pos, buffer, orig, bufferSize-pos);
                        bufferSize -= pos-orig;
                        pos = orig;
                    }
                }
                else
                    orig++;
            }
        }
    }

    /**
     * Open the selected port for communication.
     */
    public void openPort(boolean commandLineRequest)
    {
        if (portChoice.getSelectedItem().equals("<Not Connected>"))
            return;

    	Cursor previousCursor = getCursor();
    	setNewCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        CommPortIdentifier portId = null;

        // Obtain a CommPortIdentifier object for the port to open.
        try
        {
        	portId = CommPortIdentifier.getPortIdentifier((String)portChoice.getSelectedItem());
        }
        catch (NoSuchPortException nspe)
        {
            JOptionPane.showMessageDialog(this, "Port: " + portChoice.getSelectedItem() +
                                          " is not a valid Serial Port.\nChoose a serial port.",
                                          "ERROR!", JOptionPane.ERROR_MESSAGE);
        	closePort();
        	return;
        }

        boolean done = false;
        while (!done)
        {
            // Open the port represented by the CommPortIdentifier object.
            try
            {
        	    serialPort = (SerialPort)portId.open("JavaKit", 1000);
        	    done = true;
            }
            catch (PortInUseException piue)
            {
                int response;

                if (commandLineRequest)
                {
                    response = JOptionPane.showConfirmDialog(this, "Port: " + portChoice.getSelectedItem() +
                                                             " is currently in use.\n\nRetry?",
                                                             "Retry?", JOptionPane.YES_NO_CANCEL_OPTION);
                }
                else
                {
                    response = JOptionPane.showConfirmDialog(this, "Port: " + portChoice.getSelectedItem() +
                                                             " is currently in use.\n\nRetry?",
                                                             "Retry?", JOptionPane.YES_NO_OPTION);
                }

                if (response == JOptionPane.NO_OPTION)
                {
                    closePort();
                    return;
                }
                else if (response == JOptionPane.CANCEL_OPTION)
                {
                    closePort();
                    System.exit(0);
                }
            }
        }

        // Set the parameters of the connection. If they won't set, close the
        // port before throwing an exception.
        try
        {
        	serialPort.setSerialPortParams(Integer.parseInt((String)baudChoice.getSelectedItem()),
        	    	                        SerialPort.DATABITS_8,
                                            SerialPort.STOPBITS_1,
                                            SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        }
        catch (UnsupportedCommOperationException ucoe)
        {
        	closePort();
            JOptionPane.showMessageDialog(this, "UnsupportedCommOperationException occured." +
                                          "\n\nClose the program and try again.", "ERROR!",
                                          JOptionPane.ERROR_MESSAGE);
        	return;
        }

        // Open the input and output streams for the connection. If they won't
        // open, close the port before exiting.
        try
        {
        	outputStream = serialPort.getOutputStream();
        	inputStream = serialPort.getInputStream();
        }
        catch (IOException ioe)
        {
        	closePort();
            JOptionPane.showMessageDialog(this, "Unable to open read/write streams to the Serial Port." +
                                          "\n\nClose the program and try again.", "ERROR!",
                                          JOptionPane.ERROR_MESSAGE);
        	return;
        }

        if (serialPort.isDTR())
            dtrSet.setSelected(true);
        else
            dtrClear.setSelected(true);

        // Create a new KeyHandler to respond to key strokes in the
        // inputOutpuArea. Add the KeyHandler as a keyListener to the
        // inputOutputArea.
        keyListener = new SpecialKeyListener(outputStream);
        inputOutputArea.addKeyListener(keyListener);
        if (!JDK11X_VM)
        {
            inputHandler = new InputHandler();
            inputOutputArea.addInputMethodListener(inputHandler);
        }

        try
        {
        	serialPort.addEventListener(new SerialEventListener());
        }
        catch (TooManyListenersException tmle)
        {
        	closePort();
            JOptionPane.showMessageDialog(this, "Too many listeners on the Serial Port." +
                                          "\n\nClose the program and try again.", "ERROR!",
                                          JOptionPane.ERROR_MESSAGE);
        	return;
        }

        // Set notifyOnDataAvailable to true to allow event driven input.
        serialPort.notifyOnDataAvailable(true);

        if (logFile != null)
        {
            logFile.println("Opened Port: " + (String)portChoice.getSelectedItem());
        }

    	openCloseButton.setText("Close Port");
    	openCloseButton.setActionCommand("Close Port");
        resetButton.setEnabled(true);
        dtrSet.setEnabled(true);
        dtrClear.setEnabled(true);
        portChoice.setEnabled(false);
        baudChoice.setEnabled(true);
        inputOutputArea.setEditable(true);
        inputOutputArea.setEnabled(true);
        if (!dumbEmulator)
            macroItem.setEnabled(true);
        loadMacroItem.setEnabled(true);
        emulatorChoice.setEnabled(true);
        editMenu.setEnabled(true);
        optionsMenu.setEnabled(true);
        macroMenu.setEnabled(true);
        //updateLoaderItem.setEnabled(true);
        enableUpdateLoaderItem.setEnabled(true);
        verifyHexFileItem.setEnabled(true);
        loadHexFileItem.setEnabled(true);
        userInput = "";
        inputOutputArea.requestFocus();
    	setNewCursor(previousCursor);
    }

    /**
     * Close the current port.
     */
    public void closePort()
    {
    	// Remove the key listener.
    	inputOutputArea.removeKeyListener(keyListener);
    	if (!JDK11X_VM)
    	    inputOutputArea.removeInputMethodListener(inputHandler);

    	if (serialPort != null)
    	{
    	    try
    	    {
    		    // close the i/o streams.
    	    	outputStream.close();
    	    	inputStream.close();
    	    }
    	    catch (IOException e)
    	    {
                JOptionPane.showMessageDialog(this, "IOException occured attempting to close the Serial Port." +
                                            "\n\nClose the program and try again.", "ERROR!", JOptionPane.ERROR_MESSAGE);
    	    }

    	    // Close the port.
    	    serialPort.close();
    	    serialPort = null;
    	    if (logFile != null)
    	    {
    	        logFile.println("Closed the port.");
    	    }
    	}

    	openCloseButton.setText("Open Port");
    	openCloseButton.setActionCommand("Open Port");
        resetButton.setEnabled(false);
        dtrSet.setEnabled(false);
        dtrClear.setEnabled(false);
        portChoice.setSelectedIndex(0);
    	portChoice.setEnabled(true);
    	baudChoice.setEnabled(true);
    	inputOutputArea.setEditable(false);
    	inputOutputArea.setEnabled(false);
    	macroItem.setEnabled(false);
    	runMacroItem.setEnabled(false);
    	loadMacroItem.setEnabled(false);
    	saveMacroItem.setEnabled(false);
    	emulatorChoice.setEnabled(false);
    	editMenu.setEnabled(false);
        optionsMenu.setEnabled(false);
        macroMenu.setEnabled(false);
        updateLoaderItem.setEnabled(false);
        verifyHexFileItem.setEnabled(false);
        loadHexFileItem.setEnabled(false);
    	userInput = "";
 	    setNewCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Sets the Cursor for the application.
     */
    private void setNewCursor(Cursor c)
    {
        if (!suppressCursorSet)
        {
    	    setCursor(c);
    	    inputOutputArea.setCursor(c);
    	}
    }

    /**
     * Cleanly shuts down the application by closing the open port, and
     * cleans up, then exits.
     */
    private void shutdown()
    {
        closePort();

        writeINIFile();

        if (logFile != null)
        {
            logFile.close();
        }

        if (macroModified)
        {
            int response = JOptionPane.showConfirmDialog(this, "The current macro has not been saved." +
                                                        "\n\nDo you wish to save before exiting?",
                                                        "Save Macro?", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION)
            {
                saveMacro();
            }
        }

        setVisible(false);

        System.exit(0);
    }

    /**
     * Handles closing down system. Allows application to be closed with window
     * close box.
     */
    class CloseHandler extends WindowAdapter
    {
    	JavaKit jk;

    	public CloseHandler(JavaKit jk)
    	{
    	    this.jk = jk;
    	}

    	public void windowClosing(WindowEvent e)
    	{
    	    jk.shutdown();
    	}
    }

    /**
     * Will copy the loader back into bank 0.
     */
    public void restoreLoader(boolean zapFirst) throws IOException,InterruptedException
    {
        if (zapFirst)
        {
            //Zap bank 1
            sendAndWait(0x03, true);
            if (escapeChar)
                return;
            sendAndWait('Z', "Z");
            sendAndWait(0x30, "0");
            sendAndWait(endCommandLine, "? ");
            sendAndWait('Y', true);
        }
        sendAndWait('B',"B");
        sendAndWait(0x30,"0");
        sendAndWait(endCommandLine, true);
        sendAndWait('M',"M");
        sendAndWait(endCommandLine, true);
        sendAndWait(endCommandLine, true);
    }

    /**
     * Will send the byte and block until the given String
     * is received.
     */
    byte[] temp = new byte[1];
    public void sendAndWait(int b, String forThis) throws InterruptedException
    {
        temp[0] = (byte)b;
        sendAndWait(temp, forThis);
    }

    /**
     * Will send the byte and wait for either the default
     * prompt String, or some new data to be received.
     */
    public void sendAndWait(int b, boolean forPrompt) throws InterruptedException
    {
        temp[0] = (byte)b;
        sendAndWait(temp, (forPrompt ? DEFAULT_PROMPT_STRING : null));
    }

    public void sendAndWait(short b) throws InterruptedException
    {
        escapeChar = false;
        boolean startFound = false;
        boolean send = false;
        byte t;
        for (int i=0; i<4; i++)
        {
            t = (byte)((b >>> (12-i*4)) & 0x0F);
            temp[0] = (byte)(t > 9 ? t + 55
                                   : t + 48);
            if ((temp[0] != 0x30) || (startFound) || (i==3))
            {
                startFound = true;
                sendAndWait(temp, Integer.toHexString(t & 0x0FF).toUpperCase());
            }
        }
    }

    /**
     * Will send the byte array and wait for either the default
     * prompt String, or some new data to be received.
     */
    public void sendAndWait(byte[] thisData, boolean forPrompt) throws InterruptedException
    {

        sendAndWait(thisData, (forPrompt ? DEFAULT_PROMPT_STRING : null));
    }

    /**
     * Will send the byte array and wait the given String be received.
     */
    public void sendAndWait(byte[] thisData, String forThis) throws InterruptedException
    {
        if (debugMode)
            sendDebug(System.out, "SendAndWait: ", thisData, 0, thisData.length);

        boolean forPrompt = false;
        if (forThis != null)
        {
            PROMPT_STRING = forThis;
            forPrompt = true;
        }

        newInputReceived = false;
        promptReceived = false;
        partialPromptReceived = false;
        escapeChar = false;

        if (logFile != null)
            logFile.print(thisData);

        long startTime = 0;

        boolean done = false;
        boolean needToSend = true;

        while(!done)
        {
            if (needToSend)
            {
                try
                {
                    startTime = System.currentTimeMillis();
                    outputStream.write(thisData);
                    needToSend = false;
                }
                catch(IOException ioe)
                {
                    //DRAIN
                }
            }

            try
            {
                Thread.sleep(50);
            }
            catch(InterruptedException ie)
            {
                //DRAIN
            }

            //3 seconds plenty?
            if ((System.currentTimeMillis() - startTime) > NO_RESPONSE_TIMEOUT)
            {
                throw new InterruptedException("\nNo response from TINI board!\n");
            }

            if (escapeChar)
            {
                done = true;
            }
            else
            {
                done = (forPrompt) ? promptReceived : newInputReceived;
            }
        }
    }

    /**
     * Send a carriage return and wait for the prompt to be
     * received.
     */
    public void sendBlankLine() throws InterruptedException
    {
        //Send a blank line
        sendAndWait(endCommandLine, true);
    }

    /**
     * Man, this is one of the most pathetic hacks I've ever written.
     * This allows multiple file selection from a JFileChooser since
     * for some really, really odd reason, it isn't implemented in
     * Swing.  (Even though the API says it is.  Hmm.)
     */
    static JList ref1;
    static JList ref2;
	public static void findJList(Object c)
	{
        if (c instanceof JList)
        {
            if (ref1 == null)
                ref1 = (JList)c;
            else
                ref2 = (JList)c;
        }
        else if (c instanceof Container)
        {
	        for (int i=0; i<((Container)c).getComponentCount(); i++)
	        {
	            findJList(((Container)c).getComponent(i));
	        }
        }
	}

	/**
	 * Prompt the user for files to send to TINI.
	 */
	public static File[] getFilesToLoad(String action)
	{
	    chooser.setDialogTitle(action + " File...");
	    chooser.resetChoosableFileFilters();
	    //This is kinda a hack, but I want Hex files to be the default ONLY when
	    //they are updating the loader.
	    if (!action.endsWith("Loader"))
	    {
            chooser.addChoosableFileFilter(new ExtensionFileFilter("hex","Hex Files"));
            chooser.setFileFilter(new ExtensionFileFilter("tbin","Dallas Binary Files"));
        }
        else
        {
            chooser.addChoosableFileFilter(new ExtensionFileFilter("tbin","Dallas Binary Files"));
            chooser.setFileFilter(new ExtensionFileFilter("hex","Hex Files"));
        }
        chooser.setMultiSelectionEnabled(true);
        chooser.setCurrentDirectory(new File(lastUsedDir));

        if((chooser.showOpenDialog(JavaKit.mainFrame)) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
              lastUsedDir = chooser.getCurrentDirectory()./*getAbsolutePath*/getCanonicalPath();
            }
            catch(IOException io)
            {
              System.out.println(io);
            }

            //DEBUG
            //SWING BUG : BUG ID 4102455 and 4218431
            //JFileChooser does not correctly implement Multiple selections... DOH!
            //File[] files = chooser.getSelectedFiles();
            //Annoying workaround:

            JList list = null;
            findJList(chooser);
            if (ref2 != null)
                list = ref2;
            else
                list = ref1;
            ref1 = null;
            ref2 = null;

            Object[] entries = list.getSelectedValues();
            File[] files = new File[entries.length];
            for (int k=0; k<entries.length; k++)
            {
                if (entries[k] instanceof File)
                    files[k] = (File)entries[k];
            }
            return files;
        }
        else
            return null;
	}

    /**
     * Thread to load a given hex or Dallas binary file.
     */
	public class LoadFile extends Thread
	{
	    private File[]  files;
	    private byte[]  loadCommand;  //'l' for load, 'v' for verify
	    private String  actionStr;
	    private boolean load;
	    private boolean bank0CopyInProgress = false;
	    private boolean CRITICAL_SECTION    = false;

	    private int progressPos = 0;
	    private char[] progress = {'\\', '|', '/', '-', '|', '/', '-'};

	    public LoadFile(File[] files, boolean load)
	    {
	        this.files = files;
	        this.load  = load;
	        if (load)
	        {
	            loadCommand = ("l" + endCommandLineStr).getBytes();
	            actionStr   = "Load";
	        }
	        else
	        {
	            loadCommand = ("v" + endCommandLineStr).getBytes();
	            actionStr   = "Verify";
	        }
	    }

	    public void run()
	    {
    	    if (serialPort == null)
    	    {
                JOptionPane.showMessageDialog(JavaKit.mainFrame, "You must open a Serial Port before you load." +
                                            "\nOpen the port and try again.", "ERROR!", JOptionPane.ERROR_MESSAGE);

     	        JavaKit.finishedLoading = true;
     	        return;
    	    }

        	if (files == null)
        	{
        	    //No files were passed in, so prompt user for a file list to load/verify...
        		files = JavaKit.getFilesToLoad(actionStr);
        		if (files == null)
        		{
        		    JavaKit.finishedLoading = true;
        		    statusLabel.setText("");
        		    return;
        		}
        	}

        	Cursor previousCursor = getCursor();
	        setNewCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            suppressOutput = !debugMode;

            suppressCursorSet = true;

            boolean LOADER_DEAD = false;

            //First, make sure that DTR has not been disconnected...
            statusLabel.setText("Testing board.  Please wait...");
            newInputReceived = false;
            reset(true);
            if (promptReceived)
                reset(true);
            if (!promptReceived)
            {
                JOptionPane.showMessageDialog(JavaKit.mainFrame, "ERROR: Unable to detect DTR connectivity." +
                                                "\nYou MUST have DTR toggle abilities enabled on your TINI board before attempting a load." +
                                                "\n\nSome TINI dev boards have a jumper to disable the DTR line being able to reset the TINI" +
                                                "\nboard, and some serial cables don't bring DTR through.  Please ensure that DTR control is" +
                                                "\nenabled and try your load again." +
                                                "\n\nIf you believe you have DTR connected correctly and continue to receive this message, run" +
                                                "\nwith the command line switch: -noDTRTest",
                                                "ERROR!", JOptionPane.ERROR_MESSAGE);
     	        suppressCursorSet = false;
     	        setNewCursor(previousCursor);
     	        JavaKit.finishedLoading = true;
     	        statusLabel.setText("");
     	        suppressOutput = false;
     	        return;
            }

	        for (int i=0; i<files.length; i++)
	        {
	            bank0CopyInProgress = false;
	            CRITICAL_SECTION = false;
        		try
        		{
	                String fileName = files[i]./*getAbsolutePath*/getCanonicalPath();
	                updateLoadStatus();
	                inputOutputArea.append("\n" + actionStr + "ing file: " + fileName + ".");

	                if (!LOADER_UPDATE_IN_PROGRESS)
	                {
	                    inputOutputArea.append("\n\nPlease wait... (ESC to abort.)\n");
	                }
	                else
	                {
	                    inputOutputArea.append("\n\nPlease wait... (Do NOT interrupt this process.)\n");
	                }

	                if (logFile != null)
	                {
	                    logFile.println("\n" + actionStr + "ing file: " + fileName);
	                }

        		    if (recordingMacro)
        		    {
        		        currentMacro.addElement("<JavaKit PROMPT" + DEFAULT_PROMPT_STRING);
        		        if (load)
        		            currentMacro.addElement("<JavaKit LOAD>" + fileName);
        		        else
        		            currentMacro.addElement("<JavaKit VERIFY>" + fileName);
        		    }

        		    String nc = fileName.toLowerCase();
        		    if (nc.endsWith(".hex"))
        		    {
        		        hexLoad(fileName);
        		    }
        		    else if (nc.endsWith(".tbin"))
        		    {
        		        binaryLoad(fileName);
        		    }
        		    else
        		    {
                        JOptionPane.showMessageDialog(JavaKit.mainFrame, fileName + ": Unknown file type." +
                                                      "\nFile should be a hex file (.hex) or Dallas binary file" +
                                                      "\n(.tbin).",
                                                      "ERROR!", JOptionPane.ERROR_MESSAGE);
                        break;
        		    }

                    //Test for bad load
                    if (CRITICAL_SECTION && bank0CopyInProgress)
                    {
                        inputOutputArea.append("\nERROR encountered flashing bank 0.  Restoring loader...");
                        try
                        {
                            restoreLoader(true);
                            inputOutputArea.append("\nDone.\nYou will need to try this load again.");
        	            }
                        catch(InterruptedException ie)
                        {
                            LOADER_DEAD = true;
                            inputOutputArea.append(ie.getMessage());
                            inputOutputArea.append("\n\nERROR!  Could NOT recover the current loader.");
                            inputOutputArea.append("\n<** Please DO NOT HIT RESET! **>");
                            inputOutputArea.append("\n\nTry these steps EXACTLY:");
                            inputOutputArea.append("\nChoose the \"Send Ctrl-C\" under the Options menu.");
                            inputOutputArea.append("\n\nAt this point, you should get a loader prompt");
                            inputOutputArea.append("\nback.  If you do not, try sending Ctrl-C several");
                            inputOutputArea.append("\ntimes.  If you do not get a prompt back, do NOT");
                            inputOutputArea.append("\ncontinue with these instructions.  (Contact Dallas");
                            inputOutputArea.append("\nSemiconductor for more instructions.)");
                            inputOutputArea.append("\n\nOnce you have a prompt, type \"Z0\" and hit return.");
                            inputOutputArea.append("\nThe loader will ask if you are sure.");
                            inputOutputArea.append("\nHit \"y\" to continue.");
                            inputOutputArea.append("\nThen type \"B0\" return.");
                            inputOutputArea.append("\nFinally \"M\" and return.");
                            inputOutputArea.append("\nThis should restore your loader.  Hit the reset button,");
                            inputOutputArea.append("\nand you should get your original loader prompt back.");
                            inputOutputArea.append("\nYou will then need to try the loader update again.\n");
                            moveCursorToEnd();
                        }
                        finally
                        {
                            if (verboseMode)
                                System.out.println("Trying larger pad size.  From: " + padString.length());
                            int currSize = padString.length();
                            byte[] temp = new byte[currSize+currSize/2];
                            for (int j=0; j<temp.length; j++)
                                temp[j] = 0x20;
                            padString = new String(temp);
                            if (verboseMode)
                                System.out.println("To: " + padString.length());
                        }
                    }
                    else if (LOADER_UPDATE_IN_PROGRESS)
                    {
                        if (!possibleError)
                        {
                            inputOutputArea.append("\nLoader successfully updated.  Cleaning up...");
                            reset(false);
                            JOptionPane.showMessageDialog(JavaKit.mainFrame, "Loader successfully updated.",
                                                        "Loader Update", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(JavaKit.mainFrame, "LOADER UPDATE FAILED!",
                                                          "ERROR!", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    LOADER_UPDATE_IN_PROGRESS = false;

                    if (!possibleError)
                    {
                        inputOutputArea.append("\n" + actionStr + " complete.\n");
                    }
                    else
                    {
                        possibleError = false;
                        inputOutputArea.append("\nERROR " + actionStr + "ing file: " + files[i]./*getAbsolutePath*/getCanonicalPath() + "\n");
                        outputStream.write(0x03);
                    }

                    moveCursorToEnd();
        		}
        		catch (IOException ioe)
        		{
                    JOptionPane.showMessageDialog(JavaKit.mainFrame, "IOException writing to serial port.",
                                                "ERROR!", JOptionPane.ERROR_MESSAGE);
        		}
    	    }

     	    suppressOutput = false;
     	    if (!LOADER_DEAD)
     	    {
     	        try
     	        {
                    sendAndWait(endCommandLine, true);
                }
                catch(InterruptedException ie)
                {
                    reset(false);
                }
                statusLabel.setText("");
            }
            else
                statusLabel.setText("Loader update failed.");

     	    suppressCursorSet = false;
     	    setNewCursor(previousCursor);
     	    JavaKit.finishedLoading = true;
     	    statusLabel.setText("");
	    }

        /**
         * Load an Intel hex file.
         */
	    public void hexLoad(String fileName)
	    {
	        BufferedReader inFile = null;

	        try
	        {
                inFile = new BufferedReader(new FileReader(fileName));

                String line;

                try
                {
                    sendBlankLine();
                }
                catch(InterruptedException iea)
                {
                    //Try one more time before giving up...
                    try
                    {
                        sendBlankLine();
                    }
                    catch(InterruptedException ieb)
                    {
                        JOptionPane.showMessageDialog(JavaKit.mainFrame, "Unable to communicate with TINI board." +
                                                      "\nCheck your connections and try again.",
                                                      "ERROR!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                //Send the load/verify command to the remote system and wait till some response is
                //received or user hit esc key...
                sendAndWait(loadCommand, false);

                escapeChar = false;
                newInputReceived = false;

                boolean romWrite = false;
                int     bankNum = 0;

                //Now send line by line unless the user hits the esc key or the loader
                //spits back some data.  The loader should be silent throughout the load
                //unless some problem occurs.
                while( ((line = inFile.readLine()) != null) && (!escapeChar))
                {
                    if (line.startsWith(":02000004"))  //Every Hex386 line starts with this
                    {
                        bankNum = Integer.parseInt(line.substring(9,13),16);
                        if (verboseMode)
                        {
                            inputOutputArea.append("\nAccessing bank number: " + bankNum + "\n");
                            moveCursorToEnd();
                        }
                        updateLoadStatus();

                        if (bankNum < (ROM_SIZE / BANK_SIZE))
                        {
                            if (load)
                            {
                                if (verboseMode)
                                    inputOutputArea.append("    Bank is Flash ROM...");
                                if ((bankNum == 0) && (PROTECT_BANK_0))
                                {
                                    if (verboseMode)
                                        inputOutputArea.append("\n    Attempting loader protection...");
                                    updateLoadStatus();
                                    //Zap bank 1
                                    sendAndWait(0x03, true);
                                    sendAndWait('Z', "Z");
                                    sendAndWait(0x31, "1");
                                    sendAndWait(endCommandLine, "? ");
                                    sendAndWait('Y', true);
                                    //Copy loader to bank 1...
                                    sendAndWait('B',"B");
                                    sendAndWait(0x31,"1");
                                    sendAndWait(endCommandLine, true);
                                    sendAndWait('M', "M");
                                    sendAndWait(endCommandLine, true);
                                    //Now unlock bank 0.  Wake the squirrels, Zelda, there may be trouble.
                                    sendAndWait('T', "T");
                                    sendAndWait(0x33, "3");
                                    sendAndWait(endCommandLine, "? ");
                                    sendAndWait('Y', true);
                                    bank0CopyInProgress = true;
                                    updateLoadStatus();
                                }
                                else if (bank0CopyInProgress)
                                {
                                    if (!LOADER_UPDATE_IN_PROGRESS)
                                    {
                                        //We must've just written to bank 0.
                                        sendAndWait(0x03, true);

                                        if (verboseMode)
                                            inputOutputArea.append("\n    Bank 0 update successful.   Continuing...");

                                        updateLoadStatus();

                                        //Now, Zap 1 again (since the loader put a copy of himself in bank 1)
                                        if (!AUTO_ZAP_BANKS)
                                        {
                                            sendAndWait('Z', "Z");
                                            sendAndWait(0x31, "1");
                                            sendAndWait(endCommandLine, "? ");
                                            sendAndWait('Y', true);
                                            sendAndWait(loadCommand, false);
                                            updateLoadStatus();
                                        }
                                    }
                                    LOADER_UPDATE_IN_PROGRESS = false;
                                    bank0CopyInProgress = false;
                                    updateLoadStatus();
                                }

                                if (AUTO_ZAP_BANKS)
                                {
                                    if (verboseMode)
                                        inputOutputArea.append("\n    Zapping bank...");
                                    updateLoadStatus();
                                    if (!bank0CopyInProgress)
                                    {
                                        sendAndWait(0x03, true);    //Interrupt current load
                                    }
                                    CRITICAL_SECTION = true;
                                    sendAndWait('Z', "Z");
                                    //bad steven! bankNum's can be greater than 9!!!
                                    //sendAndWait((bankNum + 0x30), false);
                                    sendAndWait(Integer.toHexString(bankNum).charAt(0), Integer.toHexString(bankNum).toUpperCase());
                                    sendAndWait(endCommandLine, "? ");
                                    sendAndWait('Y', true);
                                    updateLoadStatus();
                                    if ((!LOADER_UPDATE_IN_PROGRESS) && (bank0CopyInProgress))
                                    {
                                        if (verboseMode)
                                            inputOutputArea.append("\n    Restoring loader...");
                                        restoreLoader(false);
                                    }
                                    CRITICAL_SECTION = false;

                                    sendAndWait(loadCommand, false);
                                    if (verboseMode)
                                        inputOutputArea.append("\n    Continuing file load...");
                                    updateLoadStatus();
                                }
                                moveCursorToEnd();
                                romWrite = true;
                            }
                        }
                        else
                        {
                            if (load)
                            {
                                if (verboseMode)
                                    inputOutputArea.append("    Bank is RAM...");
                                moveCursorToEnd();
                                romWrite = false;
                            }
                        }
                        updateLoadStatus();
                    }

                    //Now write the line.
                    if (romWrite)
                        line = padString + line + endHexLineStr;
                    else
                        line += endHexLine;
                    outputStream.write(line.getBytes());
                    outputStream.flush();
                    if (logFile != null)
                        logFile.print(line);

                    //The 8/24 loader will echo back loads when
                    //T3 is enabled, so don't treat this as an
                    //error case.
                    if (possibleError)
                    {
                        escapeChar = true;    //Force an ending
                    }
                }
        	}
        	catch (FileNotFoundException fnfe)
        	{
                JOptionPane.showMessageDialog(JavaKit.mainFrame, "File " + fileName + " not found.",
                                            "ERROR!", JOptionPane.ERROR_MESSAGE);
        	}
            catch(InterruptedException ie)
            {
                possibleError = true;
                inputOutputArea.append(ie.getMessage());
                inputOutputArea.append("\nDone.\nYou will need to try this load again.");
                moveCursorToEnd();
            }
        	catch (IOException e2)
        	{
                JOptionPane.showMessageDialog(JavaKit.mainFrame, "IOException writing to serial port.",
                                            "ERROR!", JOptionPane.ERROR_MESSAGE);
                possibleError = true;
                inputOutputArea.append("\nDone.\nYou will need to try this load again.");
        	}

        	try
        	{
        	    if (inFile != null)
        	        inFile.close();
        	}
        	catch(IOException ioe)
        	{
        	    //DRAIN
        	}
	    }

	    /**
	     * A Dallas Binary file will contain any number for records with the format:
	     * Al Ah Ax Ll Lh D1...Dn Cl Ch
	     */
	    public void binaryLoad(String fileName)
	    {
	        FileInputStream inFile = null;

	        try
	        {
	            File binFile = new File(fileName);
	            inFile = new FileInputStream(binFile);

	            if (binaryBuffer == null)
	            {
	                binaryBuffer = new byte[BANK_SIZE];
	            }

	            long fileLength = binFile.length();
	            int oldBank = -1;
	            int needToSend;
	            int startAddr;
	            int bytesRead;
	            int bankNum;
	            int length;
	            int crc16;
	            boolean allCRCsPassed = true;
	            boolean unlocked = false;
	            boolean ramLoad = false;

	            boolean EOF = false;
	            while (!EOF)
	            {
	                updateLoadStatus();

	                if (fileLength < 7)
	                {
                        JOptionPane.showMessageDialog(JavaKit.mainFrame, "File does not seem to be a Dallas Binary file.",
                                                        "ERROR 1A!", JOptionPane.ERROR_MESSAGE);
	                }

	                startAddr = (inFile.read() | (inFile.read() << 8)) & 0x0FFFF;
	                bankNum = (inFile.read() & 0x0FF);
	                //This kinda assumes that we'll never have a 0 length packet...
	                length = ((inFile.read() | (inFile.read() << 8)) & 0x0FFFF) + 1;

	                fileLength -= 5;

                    if (verboseMode)
                    {
                        inputOutputArea.append("\nStartAddr: " + startAddr);
                        inputOutputArea.append("\nbankNum  : " + bankNum);
                        inputOutputArea.append("\nlength   : " + length);
                    }

    	            //DEBUG ==> Are some of these checks really needed since I force the & 0x0FFFF above?
	                if ( (bankNum < 0) || ((startAddr > 0x0FFFF) || (startAddr < 0)) || ((length > 65536) || (length < 0)))
	                {
                        JOptionPane.showMessageDialog(JavaKit.mainFrame, "File does not seem to be a valid Dallas binary file.",
                                                        "ERROR 1B!", JOptionPane.ERROR_MESSAGE);
                        inFile.close();
                        return;
	                }
	                
	                if (bankNum > 7)
	                {
	                    if (allowHighBankLoading)
	                        inputOutputArea.append("\nLoading at bank number "+bankNum+"\n");
	                    else
	                    {
                            JOptionPane.showMessageDialog(JavaKit.mainFrame, "Use the '-allow' option to allow loading to banks higher than bank 7.",
                                                            "ERROR 1C!", JOptionPane.ERROR_MESSAGE);
                            inFile.close();
                            return;
	                    }
	                }

	                //Only if this is a new bank...
                    if (oldBank != bankNum)
                    {
                        updateLoadStatus();

                        if (verboseMode)
                            inputOutputArea.append("\nAccessing bank number: " + bankNum + "\n");

                        if (bankNum < (ROM_SIZE / BANK_SIZE))
                        {
                            if (load)
                            {
                                if (verboseMode)
                                    inputOutputArea.append("    Bank is Flash ROM...");

                                if ((bankNum == 0) && (PROTECT_BANK_0))
                                {
                                    updateLoadStatus();

                                    if (verboseMode)
                                        inputOutputArea.append("\n    Attempting loader protection...");
                                    //Zap bank 1
                                    reset(false);
                                    sendAndWait('Z', "Z");
                                    sendAndWait(0x31, "1");
                                    sendAndWait(endCommandLine, "? ");
                                    sendAndWait('Y', true);
                                    //Copy loader to bank 1...
                                    sendAndWait('B', "B");
                                    sendAndWait(0x31, "1");
                                    sendAndWait(endCommandLine, true);
                                    sendAndWait('M', "M");
                                    sendAndWait(endCommandLine, true);
                                    //Now unlock bank 0.  Wake the squirrels, Zelda, there may be trouble.
                                    sendAndWait('T', "T");
                                    sendAndWait(0x33, "3");
                                    sendAndWait(endCommandLine, "? ");
                                    sendAndWait('Y', true);
                                    unlocked = true;
                                    bank0CopyInProgress = true;
                                    updateLoadStatus();
                                }
                                else if (bank0CopyInProgress)
                                {
                                    if (!LOADER_UPDATE_IN_PROGRESS)
                                    {
                                        //We must've just written to bank 0.
                                        reset(false);

                                        updateLoadStatus();

                                        if (verboseMode)
                                            inputOutputArea.append("\n    Bank 0 update successful.   Continuing...");

                                        //Now, Zap 1 again (since the loader put a copy of himself in bank 1)
                                        if (!AUTO_ZAP_BANKS)
                                        {
                                            sendAndWait('Z', "Z");
                                            sendAndWait(0x31, "1");
                                            sendAndWait(endCommandLine, "? ");
                                            sendAndWait('Y', true);
                                        }
                                    }
                                    LOADER_UPDATE_IN_PROGRESS = false;
                                    bank0CopyInProgress = false;
                                    updateLoadStatus();
                                }

                                if (AUTO_ZAP_BANKS)
                                {
                                    
                                    if (verboseMode)
                                        inputOutputArea.append("\n    Zapping bank...");
                                    if (!bank0CopyInProgress)
                                    {
                                        reset(false);    //Interrupt current load
                                    }

                                    updateLoadStatus();

                                    CRITICAL_SECTION = true;
                                    if ((bankNum & 0x07)==0)
                                    {
                                        sendAndWait('T', "T");
                                        sendAndWait(0x33, "3");
                                        sendAndWait(endCommandLine, "? ");
                                        sendAndWait('Y', true);
                                        updateLoadStatus();
                                    }
                                    
                                    sendAndWait('Z', "Z");
                                    //bad steven! bankNum's can be greater than 9!!!
                                    //sendAndWait((bankNum + 0x30), Integer.toHexString(bankNum));

                                    sendAndWait(Integer.toHexString(bankNum).charAt(0), Integer.toHexString(bankNum).toUpperCase());

                                    sendAndWait(endCommandLine, "? ");
                                    sendAndWait('Y', true);
                                    if ((!LOADER_UPDATE_IN_PROGRESS) && (bank0CopyInProgress))
                                    {
                                        if (verboseMode)
                                            inputOutputArea.append("\n    Restoring loader...");
                                        restoreLoader(false);
                                    }
                                    CRITICAL_SECTION = false;

                                    updateLoadStatus();
                                }
                                updateLoadStatus();
                                moveCursorToEnd();
                            }
                        }
                        else
                        {
                            if (load && verboseMode)
                            {
                                inputOutputArea.append("    Bank is RAM...");
                                moveCursorToEnd();
                            }
                            updateLoadStatus();
                        }
                        oldBank = bankNum;
                    }

                    if (verboseMode)
                    {
                        inputOutputArea.append("\n    Loading file segment...");
                        moveCursorToEnd();
                    }

                    updateLoadStatus();

                    //Move to the correct bank...
                    if (!bank0CopyInProgress)
                        reset(false);

                    updateLoadStatus();

                    if ((bankNum == 0) && (!unlocked))
                    {
                        sendAndWait('T', "T");
                        sendAndWait(0x33, "3");
                        sendAndWait(endCommandLine, "? ");
                        sendAndWait('Y', true);
                        updateLoadStatus();
                    }
                    else
                    {
                        unlocked = false;
                    }

                    sendAndWait('B', "B");
                    //bad steven! bankNum's can be greater than 9!!!
                    //sendAndWait((byte)(bankNum + 0x30), Integer.toHexString(bankNum & 0x0FF));
               
                    //bad whoever! this doesn't work either. yikes!!
                    //char[] tempBankNum = Integer.toHexString(bankNum).toCharArray();
                    //for (int i=0;i<tempBankNum.length;i++)
                    //{
                    //    sendAndWait((byte)tempBankNum[i], new String(tempBankNum, i, 1));
                    //}
                    sendAndWait(Integer.toHexString(bankNum).charAt(0), Integer.toHexString(bankNum).toUpperCase());
                    
                    sendAndWait(endCommandLine, true);

                    updateLoadStatus();

                    if (escapeChar)
                    {
                        inputOutputArea.append("\nUser aborted load!");
                        break;
                    }

                    //Now send load command: T1 0 addr<cr>
                    //Or, If it is a verify, it will be: T1 2 addr<cr>
                    if ((bankNum & 0x07) == 0)
                    {
                        sendAndWait('T', "T");
                        sendAndWait(0x33, "3");
                        sendAndWait(endCommandLine, "? ");
                        sendAndWait('Y', true);
                        updateLoadStatus();
                    }

                    sendAndWait('T', "T");
                    sendAndWait(0x31, "1");
                    sendAndWait(' ', " ");
                    if (load)
                    {
                        sendAndWait(0x30, "0");
                        sendAndWait(' ', " ");
                    }
                    else
                    {
                        sendAndWait(0x32, "2");
                        sendAndWait(' ', " ");
                    }
                    sendAndWait((short)startAddr);

                    sendAndWait(endCommandLine, false);

                    updateLoadStatus();

                    if (escapeChar)
                    {
                        inputOutputArea.append("\nUser aborted load!");
                        break;
                    }

	                needToSend = length;
	                escapeChar = false;
	                while ((needToSend > 0) && (!escapeChar) && (!possibleError))
	                {
	                    bytesRead = inFile.read(binaryBuffer, 0, Math.min(binaryBuffer.length, needToSend));
	                    if (debugMode)
	                    {
                            inputOutputArea.append("Desired size: " + Math.min(binaryBuffer.length, needToSend));
                            inputOutputArea.append("\nRead " + bytesRead + " bytes.");
                        }
	                    outputStream.write(binaryBuffer, 0, bytesRead);
                        outputStream.flush();
                        if (FLUSH_WAIT != -1)
                            try{Thread.sleep(FLUSH_WAIT);}catch(InterruptedException ex){}
	                    needToSend -= bytesRead;
	                    fileLength -= bytesRead;
	                    updateLoadStatus();
	                }

	                if (possibleError)
	                {
	                    inputOutputArea.append("\nERROR! Aborting load.");
	                    EOF = true;
	                }
	                else if (fileLength < 2)
	                {
                        JOptionPane.showMessageDialog(JavaKit.mainFrame, "File does not seem to be a Dallas Binary file.",
                                                        "ERROR!", JOptionPane.ERROR_MESSAGE);
                        EOF = true;
	                }
	                else if (!escapeChar)
	                {
	                    updateLoadStatus();
	                    if (verboseMode)
	                        inputOutputArea.append("\n        Checking CRC value...");
                        reset(false);
                        //Little endian
	                    crc16 = (inFile.read() | (inFile.read() << 8)) & 0x0FFFF;
                        //Reset our bank
                        sendAndWait('B', "B");

                        //bad steven! bankNum's can be greater than 9!!!
                        //sendAndWait((byte)(bankNum + 0x30), Integer.toHexString(bankNum & 0x0FF));

                        //bad whoever! this doesn't work either. yikes!!
                        //tempBankNum = Integer.toHexString(bankNum).toCharArray();
                        //for (int i=0;i<tempBankNum.length;i++)
                        //{
                        //    sendAndWait((byte)tempBankNum[i], new String(tempBankNum, i, 1));
                        //}
                        //sendAndWait((byte)(bankNum + 0x30), Integer.toHexString(bankNum&0x0FF));

                        sendAndWait(Integer.toHexString(bankNum).charAt(0), Integer.toHexString(bankNum).toUpperCase());
                        
                        sendAndWait(endCommandLine, true);
                        sendAndWait('C', "C");
                        sendAndWait(' ', " ");
	                    String crc = Integer.toHexString(startAddr) + " " + Integer.toHexString((startAddr + length - 1) & 0x0FFFF);
	                    sendAndWait((short)startAddr);
	                    sendAndWait(' ', " ");
	                    sendAndWait((short)((startAddr + length - 1) & 0x0FFFF));

	                    CRC_CHECK = "";
	                    checkCRC = true;

	                    sendAndWait(endCommandLine, true);

	                    updateLoadStatus();

	                    checkCRC = false;
                        CRC_CHECK = CRC_CHECK.substring((CRC_CHECK.indexOf('=') + 2), CRC_CHECK.length()-3);
                        String ourCRC = hexLookup[Integer.toHexString(crc16).length()] + Integer.toHexString(crc16);
                        if (CRC_CHECK.equalsIgnoreCase(ourCRC))
                        {
                            if (verboseMode)
                                inputOutputArea.append(" *Passed*");
                        }
                        else
                        {
                            inputOutputArea.append("\n CRC CHECK *FAIL*  " + CRC_CHECK + " != " + ourCRC);
                            allCRCsPassed = false;
                        }
                        updateLoadStatus();
	                }

	                fileLength -= 2;  //The CRC just read

	                if (fileLength <= 0)
	                {
	                    EOF = true;
	                }
	                else if (escapeChar)
	                {
	                    inputOutputArea.append("\nUser aborted load!");
	                    EOF = true;
	                }
	            }
	            updateLoadStatus();

	            if (!allCRCsPassed)
                    inputOutputArea.append("\n<*>Some CRC checks failed.  Try reloading.");

	            //Finish the load.
	            reset(false);
	            updateLoadStatus();
	        }
            catch(InterruptedException ie)
            {
                inputOutputArea.append(ie.getMessage());
                moveCursorToEnd();
            }
	        catch(Throwable t)
	        {
                inputOutputArea.append(t.getMessage());
                moveCursorToEnd();
                //DEBUG ==> handle?!?
	        }

	        try
	        {
	            if (inFile != null)
	                inFile.close();
	        }
	        catch(IOException ioe)
	        {
	            //DRAIN
	        }
	    }

	    /**
	     *
	     */
	    public void updateLoadStatus()
	    {
	        statusLabel.setText("Load in progress... " + progress[progressPos++]);
	        if ((progressPos % 7) == 0)
	            progressPos = 0;
	    }
	}

	/**
	 * Listener for the various pull down boxes.
	 */
    class ChoiceListener implements java.awt.event.ItemListener
    {
        public void itemStateChanged(java.awt.event.ItemEvent event)
        {
            Object object = event.getSource();
            if (object == baudChoice)
            {
                if (serialPort != null)
                {
                    try
                    {
                        serialPort.setSerialPortParams(Integer.parseInt((String)baudChoice.getSelectedItem()),
        	    	                                SerialPort.DATABITS_8,
                                                    SerialPort.STOPBITS_1,
                                                    SerialPort.PARITY_NONE);
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    }
                    catch (UnsupportedCommOperationException e)
                    {
                        //DRAIN
                    }
                }
            }
            else if (object == emulatorChoice)
            {
                if (emulatorChoice.getSelectedItem().equals("JavaKit Terminal"))
                {
                    setDumbTerminalMode(false, false);
                }
                else
                {
                    setDumbTerminalMode(true, false);
                }
            }
        }
    }

    /**
     * Switch to Dumb Terminal (i.e. send every key as it is entered, don't
     * local echo each key, etc.)
     */
    public void setDumbTerminalMode(boolean set, boolean updateChoice)
    {
        if (set)
        {
            dumbEmulator = true;
    	    macroItem.setEnabled(false);
    	    saveMacroItem.setEnabled(false);
    	    trapCtrlKeysItem.setEnabled(true);
    	    trapCtrlKeysItem.setText("Don't Trap Ctrl Keys");
    	    trapCtrlKeys = true;
    	    inputOutputArea.requestFocus();
    	    if (updateChoice)
    	        emulatorChoice.setSelectedItem("JavaKit Terminal");
        }
        else
        {
    	    macroItem.setEnabled(true);
    	    saveMacroItem.setEnabled(true);
    	    dumbEmulator = false;
    	    trapCtrlKeysItem.setEnabled(false);
    	    trapCtrlKeys = false;
    	    inputOutputArea.requestFocus();
    	    if (updateChoice)
    	        emulatorChoice.setSelectedItem("Dumb Terminal");
        }
    }

    /**
     * Display extra syntax info.
     */
    public static void showAdvancedSyntax()
    {
        System.out.println("\nJavaKit            Version " + VERSION);
        System.out.println("\nDallas Semiconductor Corporation");
        System.out.println("-==============================-");
        System.out.println("\nUsage: java JavaKit <-options>");
        System.out.println("Where options include:");
        System.out.println("-port             Specifies the COM port to auto open.");
        System.out.println("-macro            Specifies a macro file to auto load.");
        System.out.println("                    Pass multiple files separated by commas.");
        System.out.println("-baud             Specifies the baud rate to use (default is 115200.)");
        System.out.println("-padSize          Specifies the size of the pad string when");
        System.out.println("                    writing to Hex files to flash ROM (default is 12.)");
        System.out.println("-binPause         Specifies the number of milliseconds to pause after");
        System.out.println("                    a binary segment write. (default is 50ms.)");
        System.out.println("-exitAfterRun     Specifies that JavaKit should exit after running the");
        System.out.println("                    macro file specified with \"-macro\".");
        System.out.println("-bankSize         Specifies the size of each memory bank.");
        System.out.println("                    Default: 65536");
        System.out.println("-ROMSize          Specifies the total size of the Flash ROM.");
        System.out.println("                    Default: 524288 (512KB) / Use 1048576 for 1MB.");
        System.out.println("-log              Generates a log file called JavaKit.log.");
        System.out.println("-flushWait        Wait time after sending a portion of binary data.");
        System.out.println("                    Default: 50 ms.");
        System.out.println("-resetWait        Wait time after sending a DTR toggle.");
        System.out.println("                    Default: 100 ms.");
        System.out.println("-debug            Enables debug mode.");
        System.out.println("-noDTRTest        Instructs JavaKit not to test for DTR connected on");
        System.out.println("                    File->Load.");
        System.out.println("-allow            Allow loading files to a bank higher than bank 7.");
        System.out.println("-loaderdanger     Allow updating of Loader (You should probably not do this.)");
        System.out.println("-advanced         Show advanced options.");
        System.out.println("\n");
    }

    /**
     * Display syntax information.
     */
    public static void showSyntax()
    {
        System.out.println("\nJavaKit            Version " + VERSION);
        System.out.println("\nDallas Semiconductor Corporation");
        System.out.println("-==============================-");
        System.out.println("\nUsage: java JavaKit <-options>");
        System.out.println("Where basic options include:");
        System.out.println("-port             Specifies the COM port to auto open.");
        System.out.println("-baud             Specifies the baud rate to use (default is 115200.)");
        System.out.println("-macro            Specifies a macro file to auto load.");
        System.out.println("                    Pass multiple macro files separated by commas.");
        System.out.println("-exitAfterRun     Specifies that JavaKit should exit after running the");
        System.out.println("                    macro file(s) specified with \"-macro\".");
        System.out.println("-log              Generates a log file called JavaKit.log.");
        System.out.println("-advanced         Show advanced options.");
        System.out.println("\n");
    }

    /**
     *
     */
    public static void main(String[] args)
    {
        String comPort = null;
        String baud = null;
        String[] macroFiles = null;
        int padSize = -1;
        boolean exitAfterRun = false;
        int bankSize = -1;
        int romSize = -1;
        int binPause = -1;
        boolean log = false;
        boolean debugMode = false;

        for (int i=0; i<args.length; i++)
        {
            if (args[i].equalsIgnoreCase("-port"))
            {
                comPort = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-macro"))
            {
                String macroFile = args[++i] + ",";
                int prev = 0;
                int curr = 0;
                Vector macros = new Vector();
                while((curr = macroFile.indexOf(",", prev)) != -1)
                {
                    macros.addElement(macroFile.substring(prev, curr));
                    prev = curr + 1;
                }
                macroFiles = new String[macros.size()];
                macros.copyInto(macroFiles);
            }
            else if (args[i].equalsIgnoreCase("-baud"))
            {
                baud = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-padSize"))
            {
                padSize = Integer.parseInt(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-binPause"))
            {
                binPause = Integer.parseInt(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-exitAfterRun"))
            {
                exitAfterRun = true;
            }
            else if (args[i].equalsIgnoreCase("-flushWait"))
            {
                FLUSH_WAIT = Integer.parseInt(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-resetWait"))
            {
                RESET_WAIT = Integer.parseInt(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-noDTRTest"))
            {
                DEFAULT_LONG_PROMPT = DEFAULT_PROMPT_STRING;
            }
            else if (args[i].equalsIgnoreCase("-debug"))
            {
                debugMode = true;
            }
            else if (args[i].equalsIgnoreCase("-advanced"))
            {
                showAdvancedSyntax();
                System.exit(0);
            }
            else if (args[i].equalsIgnoreCase("-log"))
            {
                log = true;
            }
            else if (args[i].equalsIgnoreCase("-allow"))
            {
                allowHighBankLoading = true;
            }
            else if (args[i].equalsIgnoreCase("-ROMSize"))
            {
                romSize = Integer.parseInt(args[++i]);
            }
            else if (args[i].equalsIgnoreCase("-loaderdanger"))
            {
                i_want_to_scrog_my_tini = true;
            }
            else
            {
                System.out.println("\nUnknown option: " + args[i]);
                showSyntax();
                System.exit(1);
            }
        }

        //Attempt to set the swing (JFC) "Look And Feel".
        boolean lookAndFeelSet = false;
        int attempt = 0;
        //Loop through all of the possible look and feels until we find one that works.
        while(!lookAndFeelSet)
        {
            try
            {
                switch (attempt++)
                {
                    case 0:
                        //Try to load the Windows "Look And Feel" first since it looks the best (IMHO).
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    break;

                    case 1:
                        //Try the Motif "Look And Feel" next.
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    break;

                    case 2:
                        //Try the Metal "Look And Feel" next.
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.metal.MetalLookAndFeel");
                    break;

                    case 3:
                        //Finally, try the Basic "Look And Feel".
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.basic.BasicLookAndFeel");
                    break;
                }
                lookAndFeelSet = true;
            }
            catch(ClassNotFoundException e1)
            {
            }
            catch(InstantiationException e2)
            {
            }
            catch(IllegalAccessException e3)
            {
            }
            catch(UnsupportedLookAndFeelException e4)
            {
            }
            catch(ClassCastException e5)
            {
            }
        }

    	JavaKit javaKit = new JavaKit(comPort, macroFiles, baud, padSize, binPause,
    	                              exitAfterRun, bankSize, romSize, log, debugMode);
    	javaKit.setVisible(true);
    	javaKit.repaint();
    }
}
