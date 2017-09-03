import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Calendar;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.AWTException;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowEvent;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JDialog;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

/** Main
 * --------
 * Main class for clipboard history project
 *
 * @author Dan Foad
 * @version 1.0.0
 */
public class Main {
    
    /* Globals */
    // Constants
    private final int HISTORY_SIZE = 10;
    private final int ITEM_LENGTH = 60;
    private String DIR = "";
    
    // History of clipboard entries
    private ArrayList<String> history = new ArrayList<String>();
    
    // System tray components
    private JPopupMenu popup;
    private JLabel popupText;
    private JPopupMenu.Separator popupSeparator;
    private JDialog hiddenDialog;
    private Font itemFont;
    
    // Clipboard listener
    private ClipboardListener clip;
    
    // File objects
    private BufferedWriter bw;
    private File file;
    
    /** Main
     * Constructor, call init functions and add clipboard listener
     */
    public Main() {
        
        // Set up program
        initFileSystem();
        initSystemTray();
        
        // Add clipboard listener
        clip = new ClipboardListener() {
            @Override
            public void clipboardUpdated(String str) {
                updated(str); // Deal with clipboard update
            }
        };
    }
    
    /** Main::initFileSystem
     * Set up file directories and files for writing
     */
    public void initFileSystem() {
        // Get current date
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        
        // Get directory
        String home = System.getProperty("user.home");
        DIR = home + "/clipboardhistory/" + year + "-" + (month+1);
        
        try {
            // Make directory + file
            new File(DIR).mkdirs();
            file = new File(DIR + "/" + day + ".txt");
            if (!file.exists() && !file.isDirectory())
                file.createNewFile();
            
            // Create writer
            bw = new BufferedWriter(new FileWriter(file, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Main::initSystemTray
     * Set up system tray components
     */
    public void initSystemTray() {
        TrayIcon icon = null; // Icon inside of system tray
        
        // If system tray isn't a thing, back out
        if (!SystemTray.isSupported()) return;
        
        // Get system tray
        SystemTray tray = SystemTray.getSystemTray();
        
        // Get image for tray icon
        Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
        
        // Set up tray icon
        icon = new TrayIcon(image, "Clipboard History");
        icon.setImageAutoSize(true);
        
        // Font for items in popup menu
        itemFont = new Font("Roboto", Font.PLAIN, 12);
        
        // Create and set up popup menu
        popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(new LineBorder(new Color(0xE0E0E0), 1));
        
        // Style separator
        popupSeparator = new JPopupMenu.Separator();
        popupSeparator.setBackground(new Color(0, 0, 0, 0));
        popupSeparator.setForeground(new Color(0xE0E0E0));
        
        // Style title
        popupText = new JLabel("RECENT ENTRIES");
        popupText.setPreferredSize(new Dimension(200, 20));
        popupText.setBorder(new EmptyBorder(0, 8, 0, 0));
        popupText.setFont(itemFont);
        
        // Add components to popup menu
        popup.add(popupText);
        popup.add(popupSeparator);

        // Set up hidden dialog that popup menu will be attached to
        hiddenDialog = new JDialog();
        hiddenDialog.setSize(10, 10); // Small
        hiddenDialog.setUndecorated(true); // No titlebar
        hiddenDialog.setBackground(new Color(0,0,0,0)); // Transparent
        hiddenDialog.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowLostFocus(WindowEvent we) {
                hiddenDialog.setVisible(false); // Hide on focus lost
            }
            
            @Override
            public void windowGainedFocus(WindowEvent we) { }
        });
        
        // Attach listener for clicking on system tray icon
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                tryPopup(me);
            }
            
            @Override
            public void mousePressed(MouseEvent me) {
                tryPopup(me);
            }
            
            /** tryPopup
             * If triggered popup show popup
             * @param MouseEvent me     Mouse event to check
             */
            private void tryPopup(MouseEvent me) {
                if (me.isPopupTrigger()) { // If popup should exist
                    popup.setInvoker(hiddenDialog); // Attach popup to dialog so it hides on focus lost
                    
                    // Set visible
                    hiddenDialog.setVisible(true);
                    popup.setVisible(true);
                    
                    // Move to mouse location, attached via bottom right corner
                    hiddenDialog.setLocation(me.getX() - hiddenDialog.getWidth(), me.getY() - hiddenDialog.getHeight());
                    popup.setLocation(me.getX() - popup.getWidth(), me.getY() - popup.getHeight());
                }
            }
        });
        
        // Add icon to system tray
        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    
    /** Main::updated
     * Handle update to clipboard
     * @param String str    New string in clipboard
     */
    public void updated(String str) {
        history.add(str); // Add to history
        
        // Reset popup
        popup.removeAll();
        popup.add(popupText);
        popup.add(popupSeparator);
        
        // Display latest items from history
        int index = (history.size() < HISTORY_SIZE) ? 0 : (history.size() - HISTORY_SIZE);
        for (int i = index; i < history.size(); i++) {
            String text = history.get(i);
            
            // Create menu item and style
            JMenuItem item = new JMenuItem();
            item.setFont(itemFont);
            item.setBackground(Color.WHITE);
            
            // Set clipboard contents on click
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    clip.setContents(text);
                }
            });
            
            // Truncate if text is too long
            if (text.length() > ITEM_LENGTH)
                item.setText(text.substring(0, ITEM_LENGTH) + "...");
            else
                item.setText(text);
            
            // Add item to popup
            popup.add(item);
        }
        
        // Close button
        JMenuItem close = new JMenuItem("CLOSE");
        close.setFont(itemFont);
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        
        popup.add(popupSeparator);
        popup.add(close);
        
        // Write update to file
        writeToFile(str);
    }
    
    /** Main::writeToFile
     * Dispatch file writing for update to clipboard
     * @param String str    New string to write
     */
    public void writeToFile(String str) {
        // Execute on separate thread
        Thread writerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Mutex for writer
                synchronized(bw) {
                    // Get current time
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    Date currentTime = new Date();
                    
                    // Write to file under current time
                    try {
                        bw.write(df.format(currentTime) + "\t" + str + "\r\n");
                    } catch (IOException ioe) { ioe.printStackTrace(); }
                }
            }
        });
        writerThread.start(); // Dispatch thread
    }
    
    /** Main::close
     * Finish writing file at program shutdown
     *
     */
    public void close() {
        try { bw.close(); } catch (Exception e) { }
    }
    
    public static void main(String[] args) {
        Main main = new Main();
        
        // Attach shutdown hook to close writer & stream
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                main.close();
            }
        });
    }
    
}