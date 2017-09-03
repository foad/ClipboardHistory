import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import java.awt.Toolkit;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * ClipboardListener
 * --------------------
 * Listen for changes in clipboard
 *
 * @author Dan Foad
 * @version 1.0.0
 */
public class ClipboardListener extends Thread implements ClipboardOwner {
    
    /** ClipboardListener
     * Constructor, start thread
     */
    public ClipboardListener() {
        this.start();
    }
    
    // System clipboard
    private Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    /** ClipboardListener::run
     * Take ownership of system clipboard
     */
    @Override
    public void run() {
        Transferable contents = clip.getContents(this);
        takeOwnership(contents);
    }
    
    /** ClipboardListener::lostOwnership
     * Triggered when copying in another process, wait 250ms and retake ownership
     * @param Clipboard c       The system clipboard
     * @param Transferable t    Transferable object from clipboard
     */
    @Override
    public void lostOwnership(Clipboard c, Transferable t) {
        try {
            ClipboardListener.sleep(250); // Wait 250ms for other process to finish copying
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Get contents from clipboard
        Transferable contents = clip.getContents(this);
        
        try {
            processClipboard(c, contents); // Deal with new contents
        } catch (Exception e) {
            // Log errors to look at later
            Logger.getLogger(ClipboardListener.class.getName()).log(Level.SEVERE, null, e);
        }
        
        // Retake ownership of system clipboard
        takeOwnership(contents);
    }
    
    /** ClipboardListener::takeOwnership
     * Take ownership of system clipboard
     * @param Transferable contents     Contents of clipboard
     */
    public void takeOwnership(Transferable contents) {
        clip.setContents(contents, this); // Set contents again to take ownership
    }
    
    /** ClipboatListener::setContents
     * Set contents of clipboard from string
     * @param String str    String to send to clipboard
     */
    public void setContents(String str) {
        // StringSelection implements Transferable, necessary for setContents
        StringSelection selection = new StringSelection(str);
        clip.setContents(selection, this);
    }
    
    /** ClipboardListener::processClipboard
     * Deal with new contents of clipboard, sanitation etc
     * @param Clipboard c           System clipboard
     * @param Transferable contents Contents of clipboard
     */
    public void processClipboard(Clipboard c, Transferable contents) {
        String raw;
        
        try {
            // Make sure clipboard contents isn't empty and is a string
            //if ( (contents != null) ? contents.isDataFlavorSupported(DataFlavor.stringFlavor) : false ) {
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                raw = (String)contents.getTransferData(DataFlavor.stringFlavor); // Contents as string
                clipboardUpdated(raw); // Call overrider function with contents
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** ClipboardListener::clipboardUpdated
     * Override this method with what to do with new contents
     * @param String str    New clipboard contents
     */
    public void clipboardUpdated(String str) {
        System.out.println(str); // Default action
    }
    
}