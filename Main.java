import java.util.ArrayList;
import java.util.LinkedHashMap;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import java.awt.Image;
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

public class Main {
    
    private final int HISTORY_SIZE = 10;
    private final int ITEM_LENGTH = 40;
    private ArrayList<String> history = new ArrayList<String>();
    private JPopupMenu popup;
    private JDialog hiddenDialog;
    private ClipboardListener clip;
    
    public Main() {
        
        initSystemTray();
        
        clip = new ClipboardListener() {
            @Override
            public void clipboardUpdated(String str) {
                updated(str);
            }
        };
    }
    
    public void initSystemTray() {
        TrayIcon icon = null;
        
        if (!SystemTray.isSupported()) return;
        
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
        
        icon = new TrayIcon(image, "Clipboard History");
        icon.setImageAutoSize(true);
        
        popup = new JPopupMenu();
        JLabel popupText = new JLabel("Recent entries:");
        popup.add(popupText);
        popup.addSeparator();
        
        hiddenDialog = new JDialog();
        hiddenDialog.setSize(10, 10);
        hiddenDialog.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowLostFocus(WindowEvent we) {
                hiddenDialog.setVisible(false);
            }
            
            @Override
            public void windowGainedFocus(WindowEvent we) { }
        });
        
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                tryPopup(me);
            }
            
            @Override
            public void mousePressed(MouseEvent me) {
                tryPopup(me);
            }
            
            private void tryPopup(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    popup.setLocation(me.getX() - popup.getWidth(), me.getY() - popup.getHeight());
                    hiddenDialog.setLocation(me.getX() - 10, me.getY() - 10);
                    popup.setInvoker(hiddenDialog);
                    hiddenDialog.setVisible(true);
                    popup.setVisible(true);
                }
            }
        });
        
        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    
    public void updated(String str) {
        history.add(str);
        
        popup.removeAll();
        popup.add(new JLabel("Recent entries:"));
        
        int index = (history.size() < HISTORY_SIZE) ? 0 : (history.size() - HISTORY_SIZE) ;
        for (int i = index; i < history.size(); i++) {
            String text = history.get(i);
            JMenuItem item = new JMenuItem();
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    clip.setContents(text);
                }
            });
            if (text.length() > ITEM_LENGTH)
                item.setText(text.substring(0, ITEM_LENGTH) + "...");
            else
                item.setText(text);
            popup.add(item);
        }
    }
    
    public static void main(String[] args) {
        new Main();
    }
    
}