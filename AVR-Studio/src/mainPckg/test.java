
import java.awt.Color;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;

public class test extends JFrame {
    private static int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
//            System.out.print(String.valueOf(text.charAt(index)));
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }
        return index;
    }

    private static int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            System.out.print(String.valueOf(text.charAt(index)));
            System.out.print(index);
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }
    
    public static void main(String[] args) {
        final StyleContext cont = StyleContext.getDefaultStyleContext();
        final SimpleAttributeSet attrMagenta = new SimpleAttributeSet();
        final SimpleAttributeSet attrBlue = new SimpleAttributeSet();
        final SimpleAttributeSet attrBlack = new SimpleAttributeSet();

        StyleConstants.setForeground(attrMagenta, Color.MAGENTA);
        StyleConstants.setForeground(attrBlue, Color.BLUE);
        StyleConstants.setForeground(attrBlack, Color.BLACK);

        StyleConstants.setBold(attrMagenta, true);
        StyleConstants.setBold(attrBlue, true);
        StyleConstants.setBold(attrBlack, false);

        DefaultStyledDocument doc = new DefaultStyledDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);
                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offset) + 1;
                if (before < 0) {
                    before = 0;
                }
                int after = findFirstNonWordChar(text, offset + str.length());
                int wordL = before;
                int wordR = before;

                while (wordR <= after) {
                    if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
                        if (text.substring(wordL, wordR).matches("(\\W)*(#define|#include|static|void|return|if|else|while|do|for|continue|break|struct|case|switch|typedef|default|const|sizeof)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(signed|unsigned|volatile|int|long|short|double|float|char"
                                + "|0x[A-F]" + "|0x[a-f]" + "|0x[0-9]" + "|0x[A-F][A-F]"
                                + "|0x[A-F][a-f]" + "|0x[A-F][0-9]" + "|0x[a-f][a-f]" + "|0x[a-f][A-F]"
                                + "|0x[a-f][0-9]" + "|0x[0-9][A-F]" + "|0x[0-9][a-f]" + "|0x[0-9][0-9])")) {
                            setCharacterAttributes(wordL, wordR - wordL, attrMagenta, false);
                        } else {
                            setCharacterAttributes(wordL, wordR - wordL, attrBlack, false);
                        }

                        wordL = wordR;
                    }
                    wordR++;
                }
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offs) + 1;
                if (before < 0) {
                    before = 0;
                }
                int after = findFirstNonWordChar(text, offs);

                if (text.substring(before, after).matches("(\\W)*(#define|#include|static|void|return|if|else|while|do|for|continue|break|struct|case|switch|typedef|default|const|sizeof)")) {
                    setCharacterAttributes(before, after - before, attrBlue, false);
                } else if (text.substring(before, after).matches("(\\W)*(signed|unsigned|volatile|int|long|short|double|float|char"
                        + "|0x[A-F]" + "|0x[a-f]" + "|0x[0-9]" + "|0x[A-F][A-F]"
                        + "|0x[A-F][a-f]" + "|0x[A-F][0-9]" + "|0x[a-f][a-f]" + "|0x[a-f][A-F]"
                        + "|0x[a-f][0-9]" + "|0x[0-9][A-F]" + "|0x[0-9][a-f]" + "|0x[0-9][0-9])")) {
                    setCharacterAttributes(before, after - before, attrMagenta, false);
                } else {
                    setCharacterAttributes(before, after - before, attrBlack, false);
                }
            }
        };
        JTextPane textPane = new JTextPane();
        JFrame frame = new JFrame("Test");
        textPane.setStyledDocument(doc);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBounds(200, 200, 500, 550);
        frame.add(textPane);
        frame.setVisible(true);
    }
}
