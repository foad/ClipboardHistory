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

public class Main {
    
    private final int HISTORY_SIZE = 10;
    private final int ITEM_LENGTH = 60;
    private String DIR = "";
    
    private ArrayList<String> history = new ArrayList<String>();
    
    private JPopupMenu popup;
    private JLabel popupText;
    private JPopupMenu.Separator popupSeparator;
    private JDialog hiddenDialog;
    private Font itemFont;
    
    private ClipboardListener clip;
    
    private BufferedWriter bw;
    private File file;
    
    public Main() {
        
        initFileSystem();
        initSystemTray();
        
        clip = new ClipboardListener() {
            @Override
            public void clipboardUpdated(String str) {
                updated(str);
            }
        };
    }
    
    public void initFileSystem() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        
        String home = System.getProperty("user.home");
        DIR = home + "/clipboardhistory/" + year + "-" + (month+1);
        try {
            new File(DIR).mkdirs();
            file = new File(DIR + "/" + day + ".txt");
            if (!file.exists() && !file.isDirectory())
                file.createNewFile();
            
            bw = new BufferedWriter(new FileWriter(file, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        
        writeToFile(str);
    }
    
    public void writeToFile(String str) {
        Thread writerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized(bw) {
                    System.out.println("here bois");
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    Date currentTime = new Date();
                    try {
                        bw.write(df.format(currentTime) + "\t" + str + "\r\n");
                    } catch (IOException ioe) { ioe.printStackTrace(); }
                }
            }
        });
        writerThread.start();
    }
    
    public void close() {
        System.out.println("shuting down");
        try { bw.close(); } catch (Exception e) { }
    }
    
    public static void main(String[] args) {
        Main main = new Main();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                main.close();
            }
        });
    }
    
}