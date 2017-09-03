import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import java.awt.Toolkit;

import java.util.logging.Logger;
import java.util.logging.Level;

public class ClipboardListener extends Thread implements ClipboardOwner {
    
    private Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    @Override
    public void run() {
        Transferable contents = clip.getContents(this);
        takeOwnership(contents);
    }
    
    @Override
    public void lostOwnership(Clipboard c, Transferable t) {
        try {
            ClipboardListener.sleep(250);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Transferable contents = clip.getContents(this);
        
        try {
            processClipboard(c, contents);
        } catch (Exception e) {
            Logger.getLogger(ClipboardListener.class.getName()).log(Level.SEVERE, null, e);
        }
        
        takeOwnership(contents);
    }
    
    public void takeOwnership(Transferable contents) {
        clip.setContents(contents, this);
    }
    
    public void processClipboard(Clipboard c, Transferable contents) {
        String raw;
        
        try {
            //if ( (contents != null) ? contents.isDataFlavorSupported(DataFlavor.stringFlavor) : false ) {
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                raw = (String)contents.getTransferData(DataFlavor.stringFlavor);
                System.out.println(raw);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}