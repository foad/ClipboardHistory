import java.util.ArrayList;

public class Main {
    
    private static ArrayList<String> contents = new ArrayList<String>();
    
    public static void main(String[] args) {
        ClipboardListener clip = new ClipboardListener() {
            @Override
            public void clipboardUpdated(String str) {
                contents.add(str);
                updated();
            }
        };
        
        while (true) {
            
        }
    }
    
    public static void updated() {
        System.out.println("-----------------");
        for (String s : contents) {
            System.out.println(" > " + s);
        }
    }
}