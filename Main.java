import java.util.ArrayList;
import java.util.LinkedHashMap;

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

public class Main {
    
    private final int HISTORY_SIZE = 10;
    private final int ITEM_LENGTH = 60;
    
    private ArrayList<String> history = new ArrayList<String>();
    
    private JPopupMenu popup;
    private JLabel popupText;
    private JPopupMenu.Separator popupSeparator;
    private JDialog hiddenDialog;
    private Font itemFont;
    
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
        
        itemFont = new Font("Roboto", Font.PLAIN, 12);
        
        popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(new LineBorder(new Color(0xE0E0E0), 1));
        
        popupSeparator = new JPopupMenu.Separator();
        popupSeparator.setBackground(new Color(0, 0, 0, 0));
        popupSeparator.setForeground(new Color(0xE0E0E0));
        
        popupText = new JLabel("RECENT ENTRIES");
        popupText.setPreferredSize(new Dimension(200, 20));
        popupText.setBorder(new EmptyBorder(0, 8, 0, 0));
        popupText.setFont(itemFont);
        
        popup.add(popupText);
        popup.add(popupSeparator);

        hiddenDialog = new JDialog();
        hiddenDialog.setSize(10, 10);
        hiddenDialog.setUndecorated(true);
        hiddenDialog.setBackground(new Color(0,0,0,0));
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
                    popup.setInvoker(hiddenDialog);
                    
                    hiddenDialog.setVisible(true);
                    popup.setVisible(true);
                    
                    hiddenDialog.setLocation(me.getX() - hiddenDialog.getWidth(), me.getY() - hiddenDialog.getHeight());
                    popup.setLocation(me.getX() - popup.getWidth(), me.getY() - popup.getHeight());
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
        popup.add(popupText);
        popup.add(popupSeparator);
        
        int index = (history.size() < HISTORY_SIZE) ? 0 : (history.size() - HISTORY_SIZE) ;
        for (int i = index; i < history.size(); i++) {
            String text = history.get(i);
            JMenuItem item = new JMenuItem();
            item.setFont(itemFont);
            item.setBackground(Color.WHITE);
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