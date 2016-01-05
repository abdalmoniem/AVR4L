package mainPckg;

import com.sun.glass.ui.Cursor;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 *
 * @author mn3m
 *
 * @E-mail hifnawy_moniem@hotmail.com
 *
 */
public class Studio extends javax.swing.JFrame {

    private boolean chooseFile = false;
    private boolean error = false;

    private String mmcu = null;
    private String parentPath = null;
    private String cPath = null;
    private String oPath = null;
    private String elfPath = null;
    private String hexPath = null;

    private File fileToOpen;

    private TabStop[] getTabs(int n) {
        int noOftabs = n;
        TabStop[] tabs = new TabStop[noOftabs];
        int tabSize = 10;
        for (int i = 0; i < noOftabs; i++) {
            tabSize += 20;
            tabs[i] = new TabStop(tabSize, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
        }
        return tabs;
    }

    private int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }
        return index;
    }

    private int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }

    private void appendToPane(JTextPane pane, String msg, int mode) throws BadLocationException {
        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        if (mode == 0) { //no errors
            StyleConstants.setForeground(attr, Color.GREEN);
            StyleConstants.setBold(attr, true);
            StyleConstants.setItalic(attr, true);
            doc.insertString(doc.getLength(), msg, attr);
        } else if (mode == 1) { //warning
            StyleConstants.setForeground(attr, Color.YELLOW);
            StyleConstants.setBold(attr, true);
            StyleConstants.setItalic(attr, true);
            doc.insertString(doc.getLength(), msg, attr);
        } else if (mode == 2) { //error
            StyleConstants.setForeground(attr, Color.RED);
            StyleConstants.setBold(attr, true);
            StyleConstants.setItalic(attr, true);
            doc.insertString(doc.getLength(), msg, attr);
        } else if (mode == 3) { //for editing
            StyleConstants.setForeground(attr, Color.BLACK);
            doc.insertString(doc.getLength(), msg, attr);
        } else { //normal text in console area
            StyleConstants.setForeground(attr, Color.WHITE);
            StyleConstants.setItalic(attr, true);
            doc.insertString(doc.getLength(), msg, attr);
        }
    }

    private void checkForErrors(JTextPane pane, BufferedReader br) {
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("warning")
                        || line.toLowerCase().contains("disable")) {
                    appendToPane(pane, line + "\n", 1);
                } else if (line.toLowerCase().contains("error")
                        || line.toLowerCase().contains("no such file")
                        || line.toLowerCase().contains("in function")
                        || line.toLowerCase().contains("not found")
                        || line.toLowerCase().contains("undefined reference")
                        || line.toLowerCase().contains("invalid")
                        || line.toLowerCase().contains("failed")
                        || line.toLowerCase().contains(";")
                        || line.toLowerCase().contains("^")
                        || line.toLowerCase().contains("{")
                        || line.toLowerCase().contains("}")) {
                    error = true;
                    appendToPane(pane, line + "\n", 2);
                } else if (line.toLowerCase().contains("are: at43usb320")) {
                    appendToPane(pane, "Note: Please select a valid microcontroller from the list" + "\n", 1);
                } else if (line.toLowerCase().contains("valid parts are:")) {
                    appendToPane(pane, "Note: Please revise the valid parts" + "\n", 1);
                    break;
                } else if (line.toLowerCase().contains("note")) {
                    appendToPane(pane, line + "\n", 1);
                } else if (line.toLowerCase().contains("avrdude done") && error) {
                    appendToPane(pane, line + "\n", 2);
                } else {
                    appendToPane(pane, line + "\n", 0);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadLocationException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveAsFunction() {
        FileDialog fd = new FileDialog(this, "Save As...", FileDialog.SAVE);
        fd.setTitle("Save As...");
        fd.setFile(tabFileLabel.getText().toLowerCase().replace("*", ""));
        fd.setVisible(true);
        String selected = fd.getDirectory() + fd.getFile();
        if (!selected.contains("null")) {
            try {
                fileToOpen = fd.getFiles()[0];
                PrintWriter writer = new PrintWriter(fileToOpen, "UTF-8");
                writer.println(editingPane.getText());
                writer.close();
                cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
                parentPath = "\"" + fileToOpen.getParent() + "\"";
                tabFileLabel.setText(fileToOpen.getName());
                tabFileLabel.setForeground(Color.BLACK);

                if (!(fileToOpen.toString().charAt(fileToOpen.toString().length() - 1) == 'c')) {
                    JOptionPane.showMessageDialog(this,
                            "changed file name to \""
                            + fileToOpen.getName().toString().split("\\.")[0]
                            + ".c\"", "Notice",
                            JOptionPane.WARNING_MESSAGE);
                    fileToOpen.renameTo(new File(fileToOpen.toString().split("\\.")[0] + ".c"));
                }
                chooseFile = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void saveFunction() {
        try {
            editingPane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
            PrintWriter writer = new PrintWriter(fileToOpen, "UTF-8");
            writer.println(editingPane.getText());
            writer.close();
            cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
            parentPath = "\"" + fileToOpen.getParent() + "\"";
            tabFileLabel.setText(fileToOpen.getName());
            tabFileLabel.setForeground(Color.BLACK);

            for (int i = 0; i < 500; i++) {
                editingPane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
            }
            editingPane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_TEXT));
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void compileFile() {
        oPath = cPath.replace(".c", ".o");
        elfPath = cPath.replace(".c", ".elf");
        hexPath = cPath.replace(".c", ".hex");
        consolePane.setText(null);
        try {
            String[] cmd = {"cmd", "/c", "avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + cPath + " -o " + oPath};
            appendToPane(consolePane, cmd[2] + "\n", 4);
            System.out.println(cmd[2]);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            checkForErrors(consolePane, br);

            if (!error) {
                cmd = new String[]{"cmd", "/c", "avr-gcc -g -mmcu=" + mmcu + " -o " + elfPath + " " + oPath};
                System.out.println(cmd[2]);
                appendToPane(consolePane, cmd[2] + "\n", 4);
                pb = new ProcessBuilder(cmd);
                p = pb.start();
                br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                checkForErrors(consolePane, br);
            }

            if (!error) {
                cmd = new String[]{"cmd", "/c", "avr-objcopy -j .text -j .data -O ihex " + elfPath + " " + hexPath};
                System.out.println(cmd[2]);
                appendToPane(consolePane, cmd[2] + "\n", 4);
                pb = new ProcessBuilder(cmd);
                p = pb.start();
                br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                checkForErrors(consolePane, br);
            }

            if (!error) {
                cmd = new String[]{"cmd", "/c", "rm -f " + parentPath + "/*.o " + parentPath + "/*.elf"};
                System.out.println(cmd[2]);
                appendToPane(consolePane, cmd[2] + "\n", 4);
                pb = new ProcessBuilder(cmd);
                p = pb.start();
                br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                checkForErrors(consolePane, br);
                appendToPane(consolePane, "Compiled Successfully for device " + mmcu + " !!!\n", 0);
            } else {
                appendToPane(consolePane, "Errors Occured During Compilation !!!\n", 2);
                error = false;
            }
        } catch (IOException | BadLocationException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void uploadHex() {
        try {
            consolePane.setText(null);
            appendToPane(consolePane, "Uploading...\n", 4);
            appendToPane(consolePane, "Please Be Patient...\n", 4);
            editingPane.setCaretPosition(editingPane.getDocument().getLength());
            oPath = cPath.replace(".c", ".o");
            elfPath = cPath.replace(".c", ".elf");
            hexPath = cPath.replace(".c", ".hex");
            String[] cmd = {"cmd", "/c", "avrdude -p " + mmcu + " -c usbasp -u -U flash:w:" + hexPath.replace("\\", "/") + ":i"};
            System.out.println(cmd[2]);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            checkForErrors(consolePane, br);

            if (!error) {
                appendToPane(consolePane, "Uploaded Successfully !!!\n", 0);
            } else {
                appendToPane(consolePane, "Errors Occured During Upload !!!\n", 2);
                error = false;
            }
        } catch (IOException | BadLocationException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Studio(String[] arguments) {
        initComponents();

        TextLineNumber tln = new TextLineNumber(editingPane);
        editingScrollPane.setRowHeaderView(tln);

        this.setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        mcuCombo.setSelectedItem("atmega16");
        mmcu = mcuCombo.getSelectedItem().toString();

        DateFormat dateFormat = new SimpleDateFormat("MMMdd_YY");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        tabFileLabel.setText("sketch_" + date.toLowerCase() + ".c");

        final StyleContext cont = StyleContext.getDefaultStyleContext();
        final SimpleAttributeSet attrMagenta = new SimpleAttributeSet();
        final SimpleAttributeSet attrPurble = new SimpleAttributeSet();
        final SimpleAttributeSet attrBlue = new SimpleAttributeSet();
        final SimpleAttributeSet attrBlack = new SimpleAttributeSet();

        StyleConstants.setForeground(attrPurble, new Color(0x9932CC));
        StyleConstants.setForeground(attrMagenta, Color.MAGENTA);
        StyleConstants.setForeground(attrBlue, Color.BLUE);
        StyleConstants.setForeground(attrBlack, Color.BLACK);

        StyleConstants.setBold(attrMagenta, true);
        StyleConstants.setBold(attrBlue, true);
        StyleConstants.setBold(attrPurble, true);
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
                        if (text.substring(wordL, wordR).matches("(\\W)*(#|define|include|static|void|return|if|else|while|do|for|continue|break|struct|case|switch|typedef|default|const|sizeof)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(signed|unsigned|volatile|int|long|short|double|float|char)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attrPurble, false);
                        } else if (text.substring(wordL, wordR).matches("(.*)(\\d)(.*)")) {
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

                if (text.substring(before, after).matches("(\\W)*(#|define|include|static|void|return|if|else|while|do|for|continue|break|struct|case|switch|typedef|default|const|sizeof)")) {
                    setCharacterAttributes(before, after - before, attrBlue, false);
                } else if (text.substring(before, after).matches("(\\W)*(signed|unsigned|volatile|int|long|short|double|float|char)")) {
                    setCharacterAttributes(before, after - before, attrPurble, false);
                } else if (text.substring(before, after).matches("(.*)(\\d)(.*)")) {
                    setCharacterAttributes(before, after - before, attrMagenta, false);
                } else {
                    setCharacterAttributes(before, after - before, attrBlack, false);
                }
            }
        };

        TabSet tabset = new TabSet(getTabs(1000));
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabset);
        editingPane.setStyledDocument(doc);
        editingPane.setParagraphAttributes(aset, false);

        ActionMap am = editingPane.getActionMap();
        am.put(DefaultEditorKit.insertBreakAction, new IndentBreakAction());
        editingPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                tabFileLabel.setText("*" + tabFileLabel.getText().toLowerCase().replace("*", ""));
                tabFileLabel.setForeground(Color.ORANGE);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                tabFileLabel.setText("*" + tabFileLabel.getText().toLowerCase().replace("*", ""));
                tabFileLabel.setForeground(Color.ORANGE);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                tabFileLabel.setText("*" + tabFileLabel.getText().toLowerCase().replace("*", ""));
                tabFileLabel.setForeground(Color.ORANGE);
            }
        });

        if (arguments.length > 0) {
            try {
                fileToOpen = new File(arguments[0]);
                if (fileToOpen.getAbsolutePath().charAt(fileToOpen.getAbsolutePath().length() - 2) == '.'
                        && fileToOpen.getAbsolutePath().charAt(fileToOpen.getAbsolutePath().length() - 1) == 'c') {
                    cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
                    parentPath = "\"" + fileToOpen.getParent() + "\"";
                    editingPane.setText(null);
                    Scanner scan = new Scanner(fileToOpen);
                    while (scan.hasNext()) {
                        String line = scan.nextLine();
                        appendToPane(editingPane, line + "\n", 3);
                    }

                    if (editingPane.getText().length() > 0) {
                        editingPane.setText(editingPane.getText().substring(0, editingPane.getText().length() - 1));
                    } else {
                        editingPane.setText(null);
                    }
                    tabFileLabel.setText(fileToOpen.getName());
                    tabFileLabel.setForeground(Color.BLACK);
                    chooseFile = true;
                } else {
                    JOptionPane.showMessageDialog(this, "AVR-Studio can only open its own sketches and other files ending in .c",
                            "Bad file selected", JOptionPane.WARNING_MESSAGE);
                    JOptionPane.showMessageDialog(this, "AVR-Studio will no exit",
                            "Exiting...", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            } catch (FileNotFoundException | BadLocationException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

      // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
      private void initComponents() {

            splitPane = new javax.swing.JSplitPane();
            consoleScrollPane = new javax.swing.JScrollPane();
            consolePane = new javax.swing.JTextPane();
            editingScrollPane = new javax.swing.JScrollPane();
            editingPane = new javax.swing.JTextPane();
            toolBar = new javax.swing.JToolBar();
            verifyButton = new javax.swing.JButton();
            uploadButton = new javax.swing.JButton();
            searchField = new javax.swing.JTextField();
            mcuCombo = new javax.swing.JComboBox();
            tabFileLabel = new javax.swing.JLabel();
            menuBar = new javax.swing.JMenuBar();
            fileMenu = new javax.swing.JMenu();
            openMenuItem = new javax.swing.JMenuItem();
            saveMenuItem = new javax.swing.JMenuItem();
            saveAsMenuItem = new javax.swing.JMenuItem();
            aboutMenuItem = new javax.swing.JMenuItem();
            exitMenuItem = new javax.swing.JMenuItem();
            jMenu1 = new javax.swing.JMenu();
            verifyMenuItem = new javax.swing.JMenuItem();
            uploadMenuItem = new javax.swing.JMenuItem();

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            setTitle("AVR Studio");
            setMinimumSize(new java.awt.Dimension(525, 550));
            addWindowStateListener(new java.awt.event.WindowStateListener() {
                  public void windowStateChanged(java.awt.event.WindowEvent evt) {
                        formWindowStateChanged(evt);
                  }
            });
            addWindowListener(new java.awt.event.WindowAdapter() {
                  public void windowClosing(java.awt.event.WindowEvent evt) {
                        formWindowClosing(evt);
                  }
            });

            splitPane.setBorder(null);
            splitPane.setDividerLocation(350);
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setContinuousLayout(true);

            consolePane.setEditable(false);
            consolePane.setBackground(new java.awt.Color(1, 1, 1));
            consolePane.setBorder(null);
            consolePane.setForeground(new java.awt.Color(254, 254, 254));
            consolePane.setCaretColor(new java.awt.Color(254, 254, 254));
            consolePane.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
            consoleScrollPane.setViewportView(consolePane);

            splitPane.setRightComponent(consoleScrollPane);

            editingScrollPane.setViewportView(editingPane);

            splitPane.setLeftComponent(editingScrollPane);

            toolBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            toolBar.setEnabled(false);

            verifyButton.setText("Verify");
            verifyButton.setFocusable(false);
            verifyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            verifyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            verifyButton.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        verifyButtonActionPerformed(evt);
                  }
            });
            toolBar.add(verifyButton);

            uploadButton.setText("Upload");
            uploadButton.setFocusable(false);
            uploadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            uploadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            uploadButton.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        uploadButtonActionPerformed(evt);
                  }
            });
            toolBar.add(uploadButton);

            searchField.setToolTipText("Search Microcontrollers");
            searchField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
            searchField.setPreferredSize(new java.awt.Dimension(200, 27));
            searchField.addKeyListener(new java.awt.event.KeyAdapter() {
                  public void keyTyped(java.awt.event.KeyEvent evt) {
                        searchFieldKeyTyped(evt);
                  }
            });
            toolBar.add(searchField);

            mcuCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "at43usb320", "at43usb355", "at76c711", "at86rf401", "at90c8534", "at90can128", "at90can32", "at90can64", "at90pwm1", "at90pwm161", "at90pwm2", "at90pwm216", "at90pwm2b", "at90pwm3", "at90pwm316", "at90pwm3b", "at90pwm81", "at90s1200", "at90s2313", "at90s2323", "at90s2333", "at90s2343", "at90s4414", "at90s4433", "at90s4434", "at90s8515", "at90s8535", "at90scr100", "at90usb1286", "at90usb1287", "at90usb162", "at90usb646", "at90usb647", "at90usb82", "at94k", "ata5272", "ata5505", "ata5702m322", "ata5782", "ata5790", "ata5790n", "ata5795", "ata5831", "ata6285", "ata6286", "ata6289", "ata6612c", "ata6613c", "ata6614q", "ata6616c", "ata6617c", "ata664251", "atmega103", "atmega128", "atmega1280", "atmega1281", "atmega1284", "atmega1284p", "atmega1284rfr2", "atmega128a", "atmega128rfa1", "atmega128rfr2", "atmega16", "atmega161", "atmega162", "atmega163", "atmega164a", "atmega164p", "atmega164pa", "atmega165", "atmega165a", "atmega165p", "atmega165pa", "atmega168", "atmega168a", "atmega168p", "atmega168pa", "atmega168pb", "atmega169", "atmega169a", "atmega169p", "atmega169pa", "atmega16a", "atmega16hva", "atmega16hva2", "atmega16hvb", "atmega16hvbrevb", "atmega16m1", "atmega16u2", "atmega16u4", "atmega2560", "atmega2561", "atmega2564rfr2", "atmega256rfr2", "atmega32", "atmega323", "atmega324a", "atmega324p", "atmega324pa", "atmega325", "atmega3250", "atmega3250a", "atmega3250p", "atmega3250pa", "atmega325a", "atmega325p", "atmega325pa", "atmega328", "atmega328p", "atmega329", "atmega3290", "atmega3290a", "atmega3290p", "atmega3290pa", "atmega329a", "atmega329p", "atmega329pa", "atmega32a", "atmega32c1", "atmega32hvb", "atmega32hvbrevb", "atmega32m1", "atmega32u2", "atmega32u4", "atmega32u6", "atmega406", "atmega48", "atmega48a", "atmega48p", "atmega48pa", "atmega48pb", "atmega64", "atmega640", "atmega644", "atmega644a", "atmega644p", "atmega644pa", "atmega644rfr2", "atmega645", "atmega6450", "atmega6450a", "atmega6450p", "atmega645a", "atmega645p", "atmega649", "atmega6490", "atmega6490a", "atmega6490p", "atmega649a", "atmega649p", "atmega64a", "atmega64c1", "atmega64hve", "atmega64hve2", "atmega64m1", "atmega64rfr2", "atmega8", "atmega8515", "atmega8535", "atmega88", "atmega88a", "atmega88p", "atmega88pa", "atmega88pb", "atmega8a", "atmega8hva", "atmega8u2", "attiny10", "attiny11", "attiny12", "attiny13", "attiny13a", "attiny15", "attiny1634", "attiny167", "attiny20", "attiny22", "attiny2313", "attiny2313a", "attiny24", "attiny24a", "attiny25", "attiny26", "attiny261", "attiny261a", "attiny28", "attiny4", "attiny40", "attiny4313", "attiny43u", "attiny44", "attiny441", "attiny44a", "attiny45", "attiny461", "attiny461a", "attiny48", "attiny5", "attiny828", "attiny84", "attiny841", "attiny84a", "attiny85", "attiny861", "attiny861a", "attiny87", "attiny88", "attiny9", "atxmega128a1", "atxmega128a1u", "atxmega128a3", "atxmega128a3u", "atxmega128a4u", "atxmega128b1", "atxmega128b3", "atxmega128c3", "atxmega128d3", "atxmega128d4", "atxmega16a4", "atxmega16a4u", "atxmega16c4", "atxmega16d4", "atxmega16e5", "atxmega192a3", "atxmega192a3u", "atxmega192c3", "atxmega192d3", "atxmega256a3", "atxmega256a3b", "atxmega256a3bu", "atxmega256a3u", "atxmega256c3", "atxmega256d3", "atxmega32a4", "atxmega32a4u", "atxmega32c3", "atxmega32c4", "atxmega32d3", "atxmega32d4", "atxmega32e5", "atxmega384c3", "atxmega384d3", "atxmega64a1", "atxmega64a1u", "atxmega64a3", "atxmega64a3u", "atxmega64a4u", "atxmega64b1", "atxmega64b3", "atxmega64c3", "atxmega64d3", "atxmega64d4", "atxmega8e5", "avr1", "avr2", "avr25", "avr3", "avr31", "avr35", "avr4", "avr5", "avr51", "avr6", "avrtiny", "avrxmega2", "avrxmega4", "avrxmega5", "avrxmega6", "avrxmega7", "m3000" }));
            mcuCombo.setSelectedIndex(62);
            mcuCombo.setPreferredSize(new java.awt.Dimension(200, 27));
            toolBar.add(mcuCombo);

            tabFileLabel.setForeground(new java.awt.Color(1, 1, 1));

            fileMenu.setText("File");

            openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
            openMenuItem.setBackground(new java.awt.Color(235, 235, 235));
            openMenuItem.setMnemonic('o');
            openMenuItem.setText("Open");
            openMenuItem.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        openMenuItemActionPerformed(evt);
                  }
            });
            fileMenu.add(openMenuItem);

            saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
            saveMenuItem.setMnemonic('s');
            saveMenuItem.setText("Save");
            saveMenuItem.setToolTipText("");
            saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        saveMenuItemActionPerformed(evt);
                  }
            });
            fileMenu.add(saveMenuItem);

            saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
            saveAsMenuItem.setMnemonic('a');
            saveAsMenuItem.setText("Save As");
            saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        saveAsMenuItemActionPerformed(evt);
                  }
            });
            fileMenu.add(saveAsMenuItem);

            aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
            aboutMenuItem.setMnemonic('a');
            aboutMenuItem.setText("About");
            aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        aboutMenuItemActionPerformed(evt);
                  }
            });
            fileMenu.add(aboutMenuItem);

            exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
            exitMenuItem.setMnemonic('e');
            exitMenuItem.setText("Exit");
            exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        exitMenuItemActionPerformed(evt);
                  }
            });
            fileMenu.add(exitMenuItem);

            menuBar.add(fileMenu);

            jMenu1.setText("Tools");

            verifyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, java.awt.event.InputEvent.SHIFT_MASK));
            verifyMenuItem.setText("Verify");
            verifyMenuItem.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        verifyMenuItemActionPerformed(evt);
                  }
            });
            jMenu1.add(verifyMenuItem);

            uploadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, java.awt.event.InputEvent.SHIFT_MASK));
            uploadMenuItem.setText("Upload");
            uploadMenuItem.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        uploadMenuItemActionPerformed(evt);
                  }
            });
            jMenu1.add(uploadMenuItem);

            menuBar.add(jMenu1);

            setJMenuBar(menuBar);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(splitPane)
                  .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tabFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE))
            );

            pack();
      }// </editor-fold>//GEN-END:initComponents

      private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
          saveAsFunction();
      }//GEN-LAST:event_saveAsMenuItemActionPerformed

      private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
          if (chooseFile) {
              saveFunction();
          } else {
              saveAsFunction();
          }
      }//GEN-LAST:event_saveMenuItemActionPerformed

      private void verifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verifyButtonActionPerformed
          if (cPath == null) {
              JOptionPane.showMessageDialog(this, "Please Select a file to compile first", "No Selected File Found", JOptionPane.WARNING_MESSAGE);
          } else {
              compileFile();
          }
      }//GEN-LAST:event_verifyButtonActionPerformed

      private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
          if (cPath == null) {
              JOptionPane.showMessageDialog(this, "Please Select a file to upload first", "No Selected File Found", JOptionPane.WARNING_MESSAGE);
          } else {
              uploadHex();
          }
      }//GEN-LAST:event_uploadButtonActionPerformed

      private void searchFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyTyped
          String s = "" + evt.getKeyChar();
          Pattern regex = Pattern.compile("[~`!@#$%^&*()_+=;'\"/|.,><-{}A-Za-z0-9]");
          Matcher matcher = regex.matcher(s);
          if (matcher.find()) {
              for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                  if (mcuCombo.getItemAt(i).toString().toLowerCase().contains(searchField.getText().toLowerCase())) {
                      mcuCombo.setSelectedIndex(i);
                      mmcu = mcuCombo.getSelectedItem().toString();
                  }
              }
          } else {
              for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                  if (mcuCombo.getItemAt(i).toString().toLowerCase().equals(searchField.getText().toLowerCase())) {
                      mcuCombo.setSelectedIndex(i);
                      mmcu = mcuCombo.getSelectedItem().toString();
                  }
              }
          }
      }//GEN-LAST:event_searchFieldKeyTyped

      private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
          FileDialog fd = new FileDialog(this, "Open...", FileDialog.LOAD);
          fd.setTitle("Open...");
          fd.setFile(tabFileLabel.getText().replace("*", ""));
          fd.setVisible(true);
          String selected = fd.getDirectory() + fd.getFile();

          if (!selected.contains("null")) {
              if (!selected.substring(selected.length() - 2, selected.length()).toLowerCase().equals(".c")) {
                  JOptionPane.showMessageDialog(this, "AVR-Studio can only open its own sketches and other files ending in .c",
                          "Bad file selected", JOptionPane.WARNING_MESSAGE);
              } else if (!selected.contains("null")) {
                  try {
                      fileToOpen = fd.getFiles()[0];
                      cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
                      parentPath = "\"" + fileToOpen.getParent() + "\"";
                      editingPane.setText(null);
                      Scanner scan = new Scanner(fileToOpen);
                      while (scan.hasNext()) {
                          String line = scan.nextLine();
                          appendToPane(editingPane, line + "\n", 3);
                      }
                      if (editingPane.getText().length() > 0) {
                          editingPane.setText(editingPane.getText().substring(0, editingPane.getText().length() - 1));
                      } else {
                          editingPane.setText(null);
                      }
                      tabFileLabel.setText(fileToOpen.getName());
                      tabFileLabel.setForeground(Color.BLACK);
                      chooseFile = true;
                  } catch (FileNotFoundException | BadLocationException ex) {
                      Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }
          }
      }//GEN-LAST:event_openMenuItemActionPerformed

      private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
          // maximized
          if ((evt.getNewState() & Studio.MAXIMIZED_BOTH) == Studio.MAXIMIZED_BOTH)
              splitPane.setResizeWeight(0.9);
          
          // minimized
          else if ((evt.getNewState() & Studio.ICONIFIED) == Studio.ICONIFIED)
              System.out.println(evt.getID());
      }//GEN-LAST:event_formWindowStateChanged

      private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
          JOptionPane.showMessageDialog(this, "Compiled, Edited and Designed by\nA Moniem AlHifnawy", "About", JOptionPane.INFORMATION_MESSAGE);
      }//GEN-LAST:event_aboutMenuItemActionPerformed

      private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
          if (tabFileLabel.getText().toLowerCase().contains("*")) {
              int result = JOptionPane.showOptionDialog(this,
                      "Changes has occured in file  \"" + tabFileLabel.getText().toLowerCase().replace("*", "") + "\".\nDo you want to save these changes?",
                      "File \"" + tabFileLabel.getText().toLowerCase().replace("*", "") + "\" Modified.",
                      JOptionPane.YES_NO_CANCEL_OPTION,
                      JOptionPane.QUESTION_MESSAGE,
                      null,
                      new String[]{"Save Changes", "Close Without Saving", "Cancel"},
                      "Save Changes");
              if (result == JOptionPane.YES_OPTION) {
                  if (chooseFile) {
                      saveFunction();
                      System.exit(0);
                  } else {
                      saveAsFunction();
                      if (fileToOpen != null) {
                          System.exit(0);
                      }
                  }
              } else if (result == JOptionPane.NO_OPTION) {
                  System.exit(0);
              }
          } else {
              System.exit(0);
          }
      }//GEN-LAST:event_formWindowClosing

      private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
          formWindowClosing(new WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING));
      }//GEN-LAST:event_exitMenuItemActionPerformed

    private void verifyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verifyMenuItemActionPerformed
        verifyButtonActionPerformed(new ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "verify"));
    }//GEN-LAST:event_verifyMenuItemActionPerformed

    private void uploadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadMenuItemActionPerformed
        uploadButtonActionPerformed(new ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "upload"));
    }//GEN-LAST:event_uploadMenuItemActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if (info.getName().toLowerCase().equals("windows")) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Studio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Studio(args).setVisible(true);
            }
        });
    }

      // Variables declaration - do not modify//GEN-BEGIN:variables
      private javax.swing.JMenuItem aboutMenuItem;
      private javax.swing.JTextPane consolePane;
      private javax.swing.JScrollPane consoleScrollPane;
      public static javax.swing.JTextPane editingPane;
      public static javax.swing.JScrollPane editingScrollPane;
      private javax.swing.JMenuItem exitMenuItem;
      private javax.swing.JMenu fileMenu;
      private javax.swing.JMenu jMenu1;
      private javax.swing.JComboBox mcuCombo;
      private javax.swing.JMenuBar menuBar;
      private javax.swing.JMenuItem openMenuItem;
      private javax.swing.JMenuItem saveAsMenuItem;
      private javax.swing.JMenuItem saveMenuItem;
      private javax.swing.JTextField searchField;
      private javax.swing.JSplitPane splitPane;
      private javax.swing.JLabel tabFileLabel;
      private javax.swing.JToolBar toolBar;
      private javax.swing.JButton uploadButton;
      private javax.swing.JMenuItem uploadMenuItem;
      private javax.swing.JButton verifyButton;
      private javax.swing.JMenuItem verifyMenuItem;
      // End of variables declaration//GEN-END:variables
public class IndentBreakAction extends TextAction {

        /**
         * Creates this object with the appropriate identifier.
         */
        public IndentBreakAction() {
            super(DefaultEditorKit.insertBreakAction);
        }

        /**
         * The operation to perform when this action is triggered.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);

            if (target == null) {
                return;
            }

            if ((!target.isEditable()) || (!target.isEnabled())) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
                return;
            }

            try {
                //  Determine which line we are on
                Document doc = target.getDocument();
                Element rootElement = doc.getDefaultRootElement();
                int selectionStart = target.getSelectionStart();
                int line = rootElement.getElementIndex(selectionStart);

                //  Get the text for this line
                int start = rootElement.getElement(line).getStartOffset();
                int end = rootElement.getElement(line).getEndOffset();
                int length = end - start;
                String text = doc.getText(start, length);
                int offset = 0;

                //  Get the number of white spaces characters at the start of the line
                for (offset = 0; offset < length; offset++) {
                    char c = text.charAt(offset);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                }

                //  When splitting the text include white space at start of line
                //  else do default processing
                if (selectionStart - start > offset) {
                    target.replaceSelection("\n" + text.substring(0, offset));
                } else {
                    target.replaceSelection("\n");
                }
            } catch (BadLocationException ble) {
            }
        }
    }
}

/**
 * This class will display line numbers for a related text component. The text
 * component must use the same line height for each line. TextLineNumber
 * supports wrapped lines and will highlight the line number of the current line
 * in the text component.
 *
 * This class was designed to be used as a component added to the row header of
 * a JScrollPane.
 */
class TextLineNumber extends JPanel
        implements CaretListener, DocumentListener, PropertyChangeListener {

    public final static float LEFT = 0.0f;
    public final static float CENTER = 0.5f;
    public final static float RIGHT = 1.0f;

    private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

    //  Text component this TextTextLineNumber component is in sync with
    private JTextComponent component;

    //  Properties that can be changed
    private boolean updateFont;
    private int borderGap;
    private Color currentLineForeground;
    private float digitAlignment;
    private int minimumDisplayDigits;

    //  Keep history information to reduce the number of times the component
    //  needs to be repainted
    private int lastDigits;
    private int lastHeight;
    private int lastLine;

    private HashMap<String, FontMetrics> fonts;

    /**
     * Create a line number component for a text component. This minimum display
     * width will be based on 3 digits.
     *
     * @param component the related text component
     */
    public TextLineNumber(JTextComponent component) {
        this(component, 3);
    }

    /**
     * Create a line number component for a text component.
     *
     * @param component the related text component
     * @param minimumDisplayDigits the number of digits used to calculate the
     * minimum width of the component
     */
    public TextLineNumber(JTextComponent component, int minimumDisplayDigits) {
        this.component = component;

        setFont(component.getFont());

        setBorderGap(5);
        setCurrentLineForeground(Color.RED);
        setDigitAlignment(RIGHT);
        setMinimumDisplayDigits(minimumDisplayDigits);

        component.getDocument().addDocumentListener(this);
        component.addCaretListener(this);
        component.addPropertyChangeListener("font", this);
    }

    /**
     * Gets the update font property
     *
     * @return the update font property
     */
    public boolean getUpdateFont() {
        return updateFont;
    }

    /**
     * Set the update font property. Indicates whether this Font should be
     * updated automatically when the Font of the related text component is
     * changed.
     *
     * @param updateFont when true update the Font and repaint the line numbers,
     * otherwise just repaint the line numbers.
     */
    public void setUpdateFont(boolean updateFont) {
        this.updateFont = updateFont;
    }

    /**
     * Gets the border gap
     *
     * @return the border gap in pixels
     */
    public int getBorderGap() {
        return borderGap;
    }

    /**
     * The border gap is used in calculating the left and right insets of the
     * border. Default value is 5.
     *
     * @param borderGap the gap in pixels
     */
    public void setBorderGap(int borderGap) {
        this.borderGap = borderGap;
        Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
        setBorder(new CompoundBorder(OUTER, inner));
        lastDigits = 0;
        setPreferredWidth();
    }

    /**
     * Gets the current line rendering Color
     *
     * @return the Color used to render the current line number
     */
    public Color getCurrentLineForeground() {
        return currentLineForeground == null ? getForeground() : currentLineForeground;
    }

    /**
     * The Color used to render the current line digits. Default is Coolor.RED.
     *
     * @param currentLineForeground the Color used to render the current line
     */
    public void setCurrentLineForeground(Color currentLineForeground) {
        this.currentLineForeground = currentLineForeground;
    }

    /**
     * Gets the digit alignment
     *
     * @return the alignment of the painted digits
     */
    public float getDigitAlignment() {
        return digitAlignment;
    }

    /**
     * Specify the horizontal alignment of the digits within the component.
     * Common values would be:
     * <ul>
     * <li>TextLineNumber.LEFT
     * <li>TextLineNumber.CENTER
     * <li>TextLineNumber.RIGHT (default)
     * </ul>
     *
     * @param currentLineForeground the Color used to render the current line
     */
    public void setDigitAlignment(float digitAlignment) {
        this.digitAlignment
                = digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
    }

    /**
     * Gets the minimum display digits
     *
     * @return the minimum display digits
     */
    public int getMinimumDisplayDigits() {
        return minimumDisplayDigits;
    }

    /**
     * Specify the mimimum number of digits used to calculate the preferred
     * width of the component. Default is 3.
     *
     * @param minimumDisplayDigits the number digits used in the preferred width
     * calculation
     */
    public void setMinimumDisplayDigits(int minimumDisplayDigits) {
        this.minimumDisplayDigits = minimumDisplayDigits;
        setPreferredWidth();
    }

    /**
     * Calculate the width needed to display the maximum line number
     */
    private void setPreferredWidth() {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

        //  Update sizes when number of digits in the line number changes
        if (lastDigits != digits) {
            lastDigits = digits;
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int width = fontMetrics.charWidth('0') * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;

            Dimension d = getPreferredSize();
            d.setSize(preferredWidth, HEIGHT);
            setPreferredSize(d);
            setSize(d);
        }
    }

    /**
     * Draw the line numbers
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //	Determine the width of the space available to draw the line number
        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;

        //  Determine the rows to draw within the clipped bounds.
        Rectangle clip = g.getClipBounds();
        int rowStartOffset = component.viewToModel(new Point(0, clip.y));
        int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));

        while (rowStartOffset <= endOffset) {
            try {
                if (isCurrentLine(rowStartOffset)) {
                    g.setColor(getCurrentLineForeground());
                } else {
                    g.setColor(getForeground());
                }

                //  Get the line number as a string and then determine the
                //  "X" and "Y" offsets for drawing the string.
                String lineNumber = getTextLineNumber(rowStartOffset);
                int stringWidth = fontMetrics.stringWidth(lineNumber);
                int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                int y = getOffsetY(rowStartOffset, fontMetrics);
                g.drawString(lineNumber, x, y);

                //  Move to the next row
                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
            } catch (Exception e) {
                break;
            }
        }
    }

    /*
       *  We need to know if the caret is currently positioned on the line we
       *  are about to paint so the line number can be highlighted.
     */
    private boolean isCurrentLine(int rowStartOffset) {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        if (root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition)) {
            return true;
        } else {
            return false;
        }
    }

    /*
       *	Get the line number to be drawn. The empty string will be returned
       *  when a line of text has wrapped.
     */
    protected String getTextLineNumber(int rowStartOffset) {
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex(rowStartOffset);
        Element line = root.getElement(index);

        if (line.getStartOffset() == rowStartOffset) {
            return String.valueOf(index + 1);
        } else {
            return "";
        }
    }

    /*
       *  Determine the X offset to properly align the line number when drawn
     */
    private int getOffsetX(int availableWidth, int stringWidth) {
        return (int) ((availableWidth - stringWidth) * digitAlignment);
    }

    /*
       *  Determine the Y offset for the current row
     */
    private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics)
            throws BadLocationException {
        //  Get the bounding rectangle of the row

        Rectangle r = component.modelToView(rowStartOffset);
        int lineHeight = fontMetrics.getHeight();
        int y = r.y + r.height;
        int descent = 0;

        //  The text needs to be positioned above the bottom of the bounding
        //  rectangle based on the descent of the font(s) contained on the row.
        if (r.height == lineHeight) // default font is being used
        {
            descent = fontMetrics.getDescent();
        } else // We need to check all the attributes for font changes
        {
            if (fonts == null) {
                fonts = new HashMap<String, FontMetrics>();
            }

            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex(rowStartOffset);
            Element line = root.getElement(index);

            for (int i = 0; i < line.getElementCount(); i++) {
                Element child = line.getElement(i);
                AttributeSet as = child.getAttributes();
                String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
                Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
                String key = fontFamily + fontSize;

                FontMetrics fm = fonts.get(key);

                if (fm == null) {
                    Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    fm = component.getFontMetrics(font);
                    fonts.put(key, fm);
                }

                descent = Math.max(descent, fm.getDescent());
            }
        }

        return y - descent;
    }

//
//  Implement CaretListener interface
//
    @Override
    public void caretUpdate(CaretEvent e) {
        //  Get the line the caret is positioned on

        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex(caretPosition);

        //  Need to repaint so the correct line number can be highlighted
        if (lastLine != currentLine) {
            repaint();
            lastLine = currentLine;
        }
    }

//
//  Implement DocumentListener interface
//
    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged();
    }

    /*
       *  A document change may affect the number of displayed lines of text.
       *  Therefore the lines numbers will also change.
     */
    private void documentChanged() {
        //  View of the component has not been updated at the time
        //  the DocumentEvent is fired

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    int endPos = component.getDocument().getLength();
                    Rectangle rect = component.modelToView(endPos);

                    if (rect != null && rect.y != lastHeight) {
                        setPreferredWidth();
                        repaint();
                        lastHeight = rect.y;
                    }
                } catch (BadLocationException ex) {
                    /* nothing to do */ }
            }
        });
    }

//
//  Implement PropertyChangeListener interface
//
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof Font) {
            if (updateFont) {
                Font newFont = (Font) evt.getNewValue();
                setFont(newFont);
                lastDigits = 0;
                setPreferredWidth();
            } else {
                repaint();
            }
        }
    }
}
