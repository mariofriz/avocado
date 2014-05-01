package avocado.helpers;

import avocado.controllers.MainController;
import java.awt.Color;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Logger model
 *
 * @author Mario
 */
public class AvocadoLogger {
    
    private static AvocadoLogger instance;

    private JTextPane textPane;
    private StyledDocument doc;
    private Style style;

    public AvocadoLogger(JTextPane textPane) {
        this.textPane = textPane;
        this.doc = this.textPane.getStyledDocument();
        this.style = this.textPane.addStyle("Style", null);
    }

    public Style getStyle() {
        return style;
    }
        
    public void write(String message) {
        // Append line ending
        String text = message + "\n";
        
        // Write to text pane
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ex) {
            java.util.logging.Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Reset to black
        setColor(Color.black);
    }
    
    public void setColor(Color color) {
        StyleConstants.setForeground(this.style, color);
    }
    
    public static void setInstance(AvocadoLogger logger) {
        instance = logger;
    }
    
    public static void log(String message) {
        instance.write(message);
    }
    
    public static void info(String message) {
        instance.setColor(Color.blue);
        instance.write(message);
    }
    
    public static void error(String message) {
        instance.setColor(Color.red);
        instance.write(message);
    }
    
    public static void success(String message) {
        instance.setColor(Color.green);
        instance.write(message);
    }

}
