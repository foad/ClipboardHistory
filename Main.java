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

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;

public class Main {
    
    private final int HISTORY_SIZE = 10;
    private ArrayList<String> history = new ArrayList<String>();
    private JPopupMenu popup;
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
        
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                tryPopup(e);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                tryPopup(e);
            }
            
            private void tryPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.setLocation(e.getX(), e.getY());
                    popup.setInvoker(popup);
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
            JMenuItem item = new JMenuItem(text);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clip.setContents(text);
                }
            });
            popup.add(item);
        }
    }
    
    public static void main(String[] args) {
        new Main();
    }
    
}