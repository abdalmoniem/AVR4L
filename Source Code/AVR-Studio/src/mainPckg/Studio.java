package mainPckg;

import com.sun.glass.ui.Cursor;
import gnu.io.CommPortIdentifier;
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
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
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
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.UIManager.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.syntaxkits.CSyntaxKit;
import jsyntaxpane.util.Configuration;

/**
 *
 * @author mn3m
 *
 * @e-mail hifnawy_moniem@hotmail.com
 *
 */
public class Studio extends javax.swing.JFrame {

      private boolean choseFile = false;
      private boolean verified = false;
      private boolean error = false;
      private boolean temporary = false;
      private int warning_count = 0;

      private String prog_option = null;
      private String mmcu = null;
      private String parentPath = null;
      private String cPath = null;
      private String oPath = null;
      private String elfPath = null;
      private String hexPath = null;

      private File fileToOpen;
      private File temporaryFileToOpen;

      private DocumentListener listener;
      private Configuration config;
      private CSyntaxKit kit;

      private final int default_font_size = 15;
      private String default_font = "";
      private int font_size = default_font_size;

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
            switch (mode) {
                  case 0:
                        //no errors
                        StyleConstants.setForeground(attr, Color.GREEN);
                        StyleConstants.setBold(attr, true);
                        StyleConstants.setItalic(attr, true);
                        doc.insertString(doc.getLength(), msg, attr);
                        break;
                  case 1:
                        //warning
                        StyleConstants.setForeground(attr, Color.YELLOW);
                        StyleConstants.setBold(attr, true);
                        StyleConstants.setItalic(attr, true);
                        doc.insertString(doc.getLength(), msg, attr);
                        break;
                  case 2:
                        //error
                        StyleConstants.setForeground(attr, Color.RED);
                        StyleConstants.setBold(attr, true);
                        StyleConstants.setItalic(attr, true);
                        doc.insertString(doc.getLength(), msg, attr);
                        break;
                  case 3:
                        //for editing
                        StyleConstants.setForeground(attr, Color.BLACK);
                        doc.insertString(doc.getLength(), msg, attr);
                        break;
                  default:
                        //normal text in console area
                        StyleConstants.setForeground(attr, Color.WHITE);
                        StyleConstants.setItalic(attr, true);
                        doc.insertString(doc.getLength(), msg, attr);
                        break;
            }
      }

      private void appendToPane(JEditorPane pane, String msg, int mode) throws BadLocationException {
            try {
                  HTMLEditorKit kit = new HTMLEditorKit();
                  HTMLDocument doc2 = new HTMLDocument();
                  pane.setEditorKit(kit);
                  pane.setDocument(doc2);
                  HTMLDocument doc = (HTMLDocument) pane.getDocument();
                  kit.insertHTML(doc, doc2.getLength(), msg, 0, 0, null);
            } catch (BadLocationException | IOException exc) {
                  System.err.println(exc.getMessage());
            }
      }

      private void checkForErrors(JTextPane pane, BufferedReader br) {
            try {
                  boolean warning = false;
                  String line = br.readLine();
                  while (line != null) {
                        if (line.toLowerCase().contains("in function")) {
                              String line_before_that_maybe_error = line;
                              line = br.readLine();
                              if (line.toLowerCase().contains("error")
                                      || line.toLowerCase().contains("no such file")
                                      || line.toLowerCase().contains("not found")
                                      || line.toLowerCase().contains("undefined reference")
                                      || line.toLowerCase().contains("double check chip")
                                      || line.toLowerCase().contains("invalid")
                                      || line.toLowerCase().contains("failed")
                                      || line.toLowerCase().contains(";")
                                      || line.toLowerCase().contains("^")
                                      || line.toLowerCase().contains("{")
                                      || line.toLowerCase().contains("}")) {
                                    appendToPane(pane, line_before_that_maybe_error + "\n", 2);
                                    appendToPane(pane, line + "\n", 2);
                                    error = true;
                              } else if (line.toLowerCase().contains("avrdude done") && error) {
                                    appendToPane(pane, line + "\n", 2);
                              } else if (line.toLowerCase().contains("warning") || line.toLowerCase().contains("disable")) {
                                    appendToPane(pane, line_before_that_maybe_error + "\n", 1);
                                    appendToPane(pane, line + "\n", 1);
                                    LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                                    if (!info.getName().toLowerCase().contains("windows")) {
                                          warning = true;
                                    }
                                    warning_count++;
                              } else if (line.toLowerCase().contains("note")) {
                                    appendToPane(pane, line + "\n", 1);
                              } else {
                                    appendToPane(pane, line + "\n", 0);
                              }
                        } else if (line.toLowerCase().contains("error")
                                || line.toLowerCase().contains("no such file")
                                || line.toLowerCase().contains("not found")
                                || line.toLowerCase().contains("undefined reference")
                                || line.toLowerCase().contains("double check chip")
                                || line.toLowerCase().contains("invalid")
                                || line.toLowerCase().contains("failed")) {
                              appendToPane(pane, line + "\n", 2);
                              error = true;
                        } else if (line.toLowerCase().contains(";")
                                || line.toLowerCase().contains("{")
                                || line.toLowerCase().contains("}")) {
                              if (warning) {
                                    appendToPane(pane, line + "\n", 1);
                                    LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                                    if (!info.getName().toLowerCase().contains("windows")) {
                                          line = br.readLine();
                                          if (line != null && line.toLowerCase().contains("^")) {
                                                appendToPane(pane, line + "\n", 1);
                                          }
                                    }
                                    warning_count++;
                                    warning = false;
                              } else {
                                    appendToPane(pane, line + "\n", 2);
                                    error = true;
                              }
                        } else if (line.toLowerCase().contains("^") && error) {
                              appendToPane(pane, line + "\n", 2);
                        } else if (line.toLowerCase().contains("avrdude done") && error) {
                              appendToPane(pane, line + "\n", 2);
                        } else if (line.toLowerCase().contains("warning") || line.toLowerCase().contains("disable")) {
                              appendToPane(pane, line + "\n", 1);
                              warning_count++;
                              warning = true;
                        } else if (line.toLowerCase().contains("note")) {
                              appendToPane(pane, line + "\n", 1);
                        } else {
                              appendToPane(pane, line + "\n", 0);
                        }
                        consolePane.setCaretPosition(consolePane.getDocument().getLength());
                        line = br.readLine();
                  }
            } catch (IOException | BadLocationException ex) {
                  System.err.println(ex.toString());
            }
      }

      private void saveAsFunction() {
            if (temporary) {
                  try {
                        LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                        String[] cmd = (info.getName().toLowerCase().equals("windows"))
                                ? new String[]{"cmd", "/c", "rm -rf " + temporaryFileToOpen.getParentFile()}
                                : new String[]{"/bin/sh", "-c", "rm -rf " + temporaryFileToOpen.getParentFile()};
                        System.out.println(cmd[2]);
                        new ProcessBuilder(cmd).start();
                  } catch (IOException ex) {
                        System.err.println(ex.toString());
                  }
            }
            FileDialog fd = new FileDialog(this, "Save As...", FileDialog.SAVE);
            fd.setTitle("Save As...");
            fd.setFile(tabFileLabel.getText().toLowerCase().replace("*", "").replace(".c", ""));
            fd.setVisible(true);
            String selected = fd.getDirectory() + fd.getFile();
            if (!selected.contains("null")) {
                  try {
                        temporary = false;
                        File x = new File(fd.getFiles()[0].getPath().replace(".c", ""));
                        x.mkdir();
                        LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                        if (info.getName().toLowerCase().equals("windows")) {
                              fileToOpen = new File(x.getPath() + "\\" + fd.getFile() + ".c");
                        } else {
                              fileToOpen = new File(x.getPath() + "/" + fd.getFile() + ".c");
                              if (fileToOpen.getParentFile().getName().equals(fileToOpen.getParentFile().getParentFile().getName())) {
                                    fileToOpen.getParentFile().delete();
                                    fileToOpen = new File(fileToOpen.getParentFile().getParentFile() + "/" + fd.getFile().replace(".c", "") + ".c");
                              }
                        }
                        PrintWriter writer = new PrintWriter(fileToOpen, "UTF-8");
                        writer.println(editingPane.getText());
                        writer.close();
                        cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
                        parentPath = "\"" + fileToOpen.getParent() + "\"";
                        tabFileLabel.setText(fileToOpen.getName());
                        tabFileLabel.setForeground(Color.BLACK);
                        choseFile = true;
                  } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        System.err.println(ex.toString());
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
                  editingPane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
                  editingPane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_TEXT));
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                  Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
      }

      private void compileFile() {
            saveFunction();

            editingPane.setEnabled(false);
            oPath = cPath.replace(".c", ".o");
            elfPath = cPath.replace(".c", ".elf");
            hexPath = cPath.replace(".c", ".hex");
            consolePane.setText(null);
            warning_count = 0;
            error = false;

            LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
            if (info.getName().toLowerCase().equals("windows")) {       //Windows
                  try {
                        String[] cmd = {"cmd", "/c", "avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + cPath + " -o " + oPath};
                        System.out.println(cmd[2]);
                        appendToPane(consolePane, cmd[2] + "\n", 4);
                        consolePane.setCaretPosition(consolePane.getDocument().getLength());
                        Process p = new ProcessBuilder(cmd).start();
                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        checkForErrors(consolePane, br);

                        if (!error) {
                              cmd = new String[]{"cmd", "/c", "avr-gcc -g -mmcu=" + mmcu + " -o " + elfPath + " " + oPath};
                              System.out.println(cmd[2]);
                              appendToPane(consolePane, cmd[2] + "\n", 4);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              p = new ProcessBuilder(cmd).start();
                              br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              checkForErrors(consolePane, br);
                        }
                        if (!error) {
                              cmd = new String[]{"cmd", "/c", "avr-objcopy -j .text -j .data -O ihex " + elfPath + " " + hexPath};
                              System.out.println(cmd[2]);
                              appendToPane(consolePane, cmd[2] + "\n", 4);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              p = new ProcessBuilder(cmd).start();
                              br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              checkForErrors(consolePane, br);
                        }

                        if (!error) {
                              cmd = new String[]{"cmd", "/c", "rm -f \"" + fileToOpen.getAbsolutePath().replace(".c", ".o") + "\" \""
                                    + fileToOpen.getAbsolutePath().replace(".c", ".elf") + "\""};
                              System.out.println(cmd[2]);
                              appendToPane(consolePane, cmd[2] + "\n", 4);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              p = new ProcessBuilder(cmd).start();
                              br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              checkForErrors(consolePane, br);
                        }

                        if (!error) {
                              File f = new File(hexPath.replace("\"", ""));
                              if (f.exists()) {
                                    if (warning_count == 1) {
                                          System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                          appendToPane(consolePane, String.format("\n%d errors, %d warning\n", 0, warning_count), 1);
                                    } else if (warning_count > 0) {
                                          System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                          appendToPane(consolePane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 1);
                                    } else {
                                          System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                          appendToPane(consolePane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 0);
                                    }
                                    System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                                    appendToPane(consolePane, "Compiled Successfully for device " + mmcu + " !!!\n", 0);
                                    consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                    verified = true;
                              } else {
                                    if (warning_count == 1) {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          appendToPane(consolePane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                    } else if (warning_count > 0) {
                                          System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                          appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    } else {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    }
                                    System.err.println("Compilation Terminated, could not generate hex file !!!");
                                    appendToPane(consolePane, "Compilation Terminated, could not generate hex file !!!\n", 2);
                                    consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              }
                        } else {
                              if (warning_count == 1) {
                                    System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                    appendToPane(consolePane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                              } else if (warning_count > 0) {
                                    System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                    appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                              } else {
                                    System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                    appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                              }
                              System.err.println("Errors Occured During Compilation !!!");
                              appendToPane(consolePane, "Errors Occured During Compilation !!!\n", 2);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                        }
                  } catch (IOException | BadLocationException ex) {
                        System.err.println(ex.toString());
                  }
            } else {        //Linux
                  try {
                        String[] cmd = {"/bin/sh", "-c", "avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + cPath + " -o " + oPath};
                        System.out.println(cmd[2]);
                        appendToPane(consolePane, cmd[2] + "\n", 4);
                        consolePane.setCaretPosition(consolePane.getDocument().getLength());
                        Process p = new ProcessBuilder(cmd).start();
                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        checkForErrors(consolePane, br);

                        if (!error) {
                              cmd = new String[]{"/bin/sh", "-c", "avr-gcc -g -mmcu=" + mmcu + " -o " + elfPath + " " + oPath};
                              System.out.println(cmd[2]);
                              appendToPane(consolePane, cmd[2] + "\n", 4);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              p = new ProcessBuilder(cmd).start();
                              br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              checkForErrors(consolePane, br);
                        }
                        if (!error) {
                              cmd = new String[]{"/bin/sh", "-c", "avr-objcopy -j .text -j .data -O ihex " + elfPath + " " + hexPath};
                              System.out.println(cmd[2]);
                              appendToPane(consolePane, cmd[2] + "\n", 4);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              p = new ProcessBuilder(cmd).start();
                              br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              checkForErrors(consolePane, br);
                        }

                        if (!error) {
                              cmd = new String[]{"/bin/sh", "-c", "rm -f \"" + fileToOpen.getAbsolutePath().replace(".c", ".o") + "\" \""
                                    + fileToOpen.getAbsolutePath().replace(".c", ".elf") + "\""};
                              System.out.println(cmd[2]);
                              appendToPane(consolePane, cmd[2] + "\n", 4);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              p = new ProcessBuilder(cmd).start();
                              br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              checkForErrors(consolePane, br);
                        }

                        if (!error) {
                              File f = new File(hexPath.replace("\"", ""));
                              if (f.exists()) {
                                    if (warning_count == 1) {
                                          System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                          appendToPane(consolePane, String.format("\n%d errors, %d warning\n", 0, warning_count), 1);
                                    } else if (warning_count > 0) {
                                          System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                          appendToPane(consolePane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 1);
                                    } else {
                                          System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                          appendToPane(consolePane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 0);
                                    }
                                    System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                                    appendToPane(consolePane, "Compiled Successfully for device " + mmcu + " !!!\n", 0);
                                    consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                    verified = true;
                              } else {
                                    if (warning_count == 1) {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          appendToPane(consolePane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                    } else if (warning_count > 0) {
                                          System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                          appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    } else {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    }
                                    System.err.println("Compilation Terminated, could not generate hex file !!!");
                                    appendToPane(consolePane, "Compilation Terminated, could not generate hex file !!!\n", 2);
                                    consolePane.setCaretPosition(consolePane.getDocument().getLength());
                              }
                        } else {
                              if (warning_count == 1) {
                                    System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                    appendToPane(consolePane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                              } else if (warning_count > 0) {
                                    System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                    appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                              } else {
                                    System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                    appendToPane(consolePane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                              }
                              System.err.println("Errors Occured During Compilation !!!");
                              appendToPane(consolePane, "Errors Occured During Compilation !!!\n", 2);
                              consolePane.setCaretPosition(consolePane.getDocument().getLength());
                        }
                  } catch (IOException | BadLocationException ex) {
                        System.err.println(ex.toString());
                  }
            }

            editingPane.setEnabled(true);
      }

      private void uploadHex() {
            try {
                  if (verified) {
                        if (error) {
                              System.out.println("Fix compilation errors and then upload the sketch !!!");
                              appendToPane(consolePane, "Fix compilation errors and then upload the sketch !!!\n", 2);
                              error = false;
                        } else {
                              //upload...
                              new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                          try {
                                                System.out.println("Uploading...");
                                                appendToPane(consolePane, "Uploading...\n", 4);
                                                consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                editingPane.setCaretPosition(editingPane.getDocument().getLength());
                                                oPath = cPath.replace(".c", ".o");
                                                elfPath = cPath.replace(".c", ".elf");
                                                hexPath = cPath.replace(".c", ".hex");
                                                editingPane.setEnabled(false);

                                                LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                                                if (info.getName().toLowerCase().equals("windows")) {
                                                      try {
                                                            String[] cmd = {"cmd", "/c", "avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + hexPath.replace("\\", "/") + ":i"};
                                                            //String[] cmd = {"cmd", "/c", "ping 127.0.0.1"};     //for testing
                                                            System.out.println(cmd[2]);
                                                            appendToPane(consolePane, cmd[2] + "\n", 4);
                                                            consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            ProcessBuilder pb = new ProcessBuilder(cmd);
                                                            Process p = pb.start();
                                                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                            //checkForErrors(consolePane, br);  //no real-time output

                                                            int value = 0;
                                                            while (value != -1) {
                                                                  char ch = (char) value;
                                                                  System.out.print(ch);
                                                                  appendToPane(consolePane, ch + "", 4);
                                                                  consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                                  value = br.read();
                                                            }
                                                            consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            String output = consolePane.getText();
                                                            if (output.contains("avrdude: verifying ...")) {
                                                                  System.out.println("Uploaded Successfully !!!");
                                                                  appendToPane(consolePane, "Uploaded Successfully !!!\n", 0);
                                                                  consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            } else {
                                                                  System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                                                  appendToPane(consolePane, "Could not upload hex file !!!\nPlease check for errors...", 2);
                                                                  consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            }
                                                            editingPane.setEnabled(true);

                                                      } catch (IOException | BadLocationException ex) {
                                                            System.err.println(ex.toString());
                                                      }
                                                } else {
                                                      try {
                                                            String[] cmd = {"/bin/sh", "-c", "sudo avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + hexPath.replace("\\", "/") + ":i"};
                                                            //String[] cmd = {"/bin/sh", "-c", "ping 127.0.0.1"};     //for testing
                                                            System.out.println(cmd[2]);
                                                            appendToPane(consolePane, cmd[2] + "\n", 4);
                                                            consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            ProcessBuilder pb = new ProcessBuilder(cmd);
                                                            Process p = pb.start();
                                                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                            //checkForErrors(consolePane, br);  //no real-time output
                                                            int value = 0;
                                                            while (value != -1) {
                                                                  char ch = (char) value;
                                                                  System.out.print(ch);
                                                                  appendToPane(consolePane, ch + "", 4);
                                                                  consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                                  value = br.read();
                                                            }
                                                            consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            String output = consolePane.getText();
                                                            if (output.contains("avrdude: verifying ...")) {
                                                                  System.out.println("Uploaded Successfully !!!");
                                                                  appendToPane(consolePane, "Uploaded Successfully !!!\n", 0);
                                                                  consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            } else {
                                                                  System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                                                  appendToPane(consolePane, "Could not upload hex file !!!\nPlease check for errors...", 2);
                                                                  consolePane.setCaretPosition(consolePane.getDocument().getLength());
                                                            }
                                                            editingPane.setEnabled(true);

                                                      } catch (IOException | BadLocationException ex) {
                                                            System.err.println(ex.toString());
                                                      }
                                                }
                                          } catch (BadLocationException ex) {
                                                System.err.println(ex.toString());
                                          }
                                    }
                              }).start();

                        }
                  } else {
                        compileFile();
                        if (error) {
                              System.out.println("Fix compilation errors and then upload the sketch !!!");
                              appendToPane(consolePane, "Fix compilation errors and then upload the sketch !!!\n", 2);
                              error = false;
                        } else {
                              uploadHex();
                        }

                  }
            } catch (BadLocationException ex) {
                  System.err.println(ex.toString());
            }
      }

      public Studio(String[] arguments) {
            initComponents();

            int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
            int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;

            this.setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.setLocation(screen_width / 3, screen_height / 9);

            port_menu.setEnabled(false);
            prog_option = "usbasp";
            mcuCombo.setSelectedItem("atmega16");
            mmcu = mcuCombo.getSelectedItem().toString();

            DateFormat dateFormat = new SimpleDateFormat("MMMdd_YY");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            tabFileLabel.setText("sketch_" + date.toLowerCase() + ".c");

            listener = new DocumentListener() {
                  @Override
                  public void insertUpdate(DocumentEvent e) {
                        tabFileLabel.setText("*" + tabFileLabel.getText().toLowerCase().replace("*", ""));
                        tabFileLabel.setForeground(Color.ORANGE);
                        verified = false;
                  }

                  @Override
                  public void removeUpdate(DocumentEvent e) {
                        tabFileLabel.setText("*" + tabFileLabel.getText().toLowerCase().replace("*", ""));
                        tabFileLabel.setForeground(Color.ORANGE);
                        verified = false;
                  }

                  @Override
                  public void changedUpdate(DocumentEvent e) {
                        tabFileLabel.setText("*" + tabFileLabel.getText().toLowerCase().replace("*", ""));
                        tabFileLabel.setForeground(Color.ORANGE);
                        verified = false;
                  }
            };

            LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
            if (info.getName().toLowerCase().equals("windows")) {
                  default_font = "consolas ";
            } else {
                  default_font = "liberation mono ";
            }

            DefaultSyntaxKit.initKit();
            config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
            config.put("DefaultFont", default_font + default_font_size);

        //Style.KEYWORD     if, else, for, while, break...
            //Style.KEYWORD2    #define, #include, #if, #else...
            //Style.NUMBER      1, 2, 3, 0xFF, 0b111...
            //Style.STRING      "any string"
            //Style.TYPE        int, char, float, static...
            //Style.COMMENT     comments
            //Style.OPERATOR    +, -, *, /...
            //Style.IDENTIFIER  variable names, other text
            //color, number
            //number:
            //  0 = normal
            //  1 = bold
            //  2 = italic
            //  3 = bold italic
            kit = new CSyntaxKit();
            kit.setProperty("Style.KEYWORD", "0x1e06c2, 1");
            kit.setProperty("Style.KEYWORD2", "0x1e06c2, 1");
            kit.setProperty("Style.NUMBER", "0xbc2b13, 1");
            kit.setProperty("Style.STRING", "0xfaba12, 2");
            kit.setProperty("Style.TYPE", "0x1e06c2, 1");

            editingPane.setEditorKit(kit);
            editingPane.getDocument().addDocumentListener(listener);
            if (arguments.length > 0) {
                  try {
                        fileToOpen = new File(arguments[0]);
                        if (fileToOpen.getAbsolutePath().charAt(fileToOpen.getAbsolutePath().length() - 2) == '.'
                                && fileToOpen.getAbsolutePath().charAt(fileToOpen.getAbsolutePath().length() - 1) == 'c') {
                              cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
                              parentPath = "\"" + fileToOpen.getParent() + "\"";
                              editingPane.setText(null);
                              Scanner scan = new Scanner(fileToOpen);
                              String text = "";
                              while (scan.hasNext()) {
                                    text += scan.nextLine() + "\n";
                              }
                              
                              editingPane.setText(text);
                              if (editingPane.getText().length() > 0) {
                                    editingPane.setText(editingPane.getText().substring(0, editingPane.getText().length() - 1));
                              } else {
                                    editingPane.setText(null);
                              }
                              tabFileLabel.setText(fileToOpen.getName());
                              tabFileLabel.setForeground(Color.BLACK);
                              choseFile = true;
                        } else {
                              JOptionPane.showMessageDialog(this, "AVR-Studio can only open its own sketches and other files ending in .c",
                                      "Bad file selected", JOptionPane.WARNING_MESSAGE);
                              JOptionPane.showMessageDialog(this, "AVR-Studio will no exit",
                                      "Exiting...", JOptionPane.INFORMATION_MESSAGE);
                              System.exit(0);

                        }
                  } catch (FileNotFoundException ex) {
                        System.err.println(ex.getMessage());
                  }
            } else {
                  editingPane.setText("#define F_CPU 16000000UL\n"
                          + "#include <avr/io.h>\n\n"
                          + "int main() {\n"
                          + "\twhile(1) {\n"
                          + "\t}\n"
                          + "\treturn 0;\n"
                          + "}");
            }
      }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        programmer_options_button_group = new javax.swing.ButtonGroup();
        privacy_dialog = new javax.swing.JDialog();
        privacy_scroll_pane = new javax.swing.JScrollPane();
        privacy_text_pane = new javax.swing.JTextPane();
        toolBar = new javax.swing.JToolBar();
        verifyButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        mcuCombo = new javax.swing.JComboBox();
        tabFileLabel = new javax.swing.JLabel();
        splitPane = new javax.swing.JSplitPane();
        consoleScrollPane = new javax.swing.JScrollPane();
        consolePane = new javax.swing.JTextPane();
        editingScrollPane = new javax.swing.JScrollPane();
        editingPane = new javax.swing.JEditorPane();
        menuBar = new javax.swing.JMenuBar();
        file_menu = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        tools_menu = new javax.swing.JMenu();
        verifyMenuItem = new javax.swing.JMenuItem();
        uploadMenuItem = new javax.swing.JMenuItem();
        prog_options_menu = new javax.swing.JMenu();
        usbasp_item = new javax.swing.JCheckBoxMenuItem();
        arduino_item = new javax.swing.JCheckBoxMenuItem();
        stk500v1_item = new javax.swing.JCheckBoxMenuItem();
        port_menu = new javax.swing.JMenu();
        view_menu = new javax.swing.JMenu();
        font_menu = new javax.swing.JMenu();
        def_font_item = new javax.swing.JMenuItem();
        inc_font_item = new javax.swing.JMenuItem();
        dec_font_item = new javax.swing.JMenuItem();

        privacy_scroll_pane.setBackground(new java.awt.Color(214, 214, 214));

        privacy_text_pane.setEditable(false);
        privacy_text_pane.setBackground(new java.awt.Color(254, 254, 254));
        privacy_text_pane.setAutoscrolls(false);
        privacy_text_pane.setHighlighter(null);
        privacy_scroll_pane.setViewportView(privacy_text_pane);

        javax.swing.GroupLayout privacy_dialogLayout = new javax.swing.GroupLayout(privacy_dialog.getContentPane());
        privacy_dialog.getContentPane().setLayout(privacy_dialogLayout);
        privacy_dialogLayout.setHorizontalGroup(
            privacy_dialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(privacy_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        privacy_dialogLayout.setVerticalGroup(
            privacy_dialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(privacy_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

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

        mcuCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "avr2", "at90s2313", "at90s2323", "at90s2333", "at90s2343", "attiny22", "attiny26", "at90s4414", "at90s4433", "at90s4434", "at90s8515", "at90c8534", "at90s8535", "avr25", "ata6289", "attiny13", "attiny13a", "attiny2313", "attiny2313a", "attiny24", "attiny24a", "attiny4313", "attiny44", "attiny44a", "attiny84", "attiny25", "attiny45", "attiny85", "attiny261", "attiny261a", "attiny461", "attiny461a", "attiny861", "attiny861a", "attiny43u", "attiny87", "attiny48", "attiny88", "at86rf401", "avr3", "at43usb320", "at43usb355", "at76c711", "avr31", "atmega103", "avr35", "at90usb82", "at90usb162", "atmega8u2", "atmega16u2", "atmega32u2", "attiny167", "avr4", "atmega8", "atmega48", "atmega48a", "atmega48p", "atmega88", "atmega88a", "atmega88p", "atmega88pa", "atmega8515", "atmega8535", "atmega8hva", "atmega4hvd", "atmega8hvd", "at90pwm1", "at90pwm2", "at90pwm2b", "at90pwm3", "at90pwm3b", "at90pwm81", "avr5", "atmega16", "atmega16a", "atmega161", "atmega162", "atmega163", "atmega164a", "atmega164p", "atmega165", "atmega165a", "atmega165p", "atmega168", "atmega168a", "atmega168p", "atmega169", "atmega169a", "atmega169p", "atmega169pa", "atmega16c1", "atmega16hva", "atmega16hva2", "atmega16hvb", "atmega16m1", "atmega16u4", "atmega32", "atmega323", "atmega324a", "atmega324p", "atmega324pa", "atmega325", "atmega325p", "atmega3250", "atmega3250p", "atmega328", "atmega328p", "atmega329", "atmega329p", "atmega329pa", "atmega3290", "atmega3290p", "atmega32c1", "atmega32hvb", "atmega32m1", "atmega32u4", "atmega32u6", "atmega406", "atmega64", "atmega640", "atmega644", "atmega644a", "atmega644p", "atmega644pa", "atmega645", "atmega645a", "atmega645p", "atmega6450", "atmega6450a", "atmega6450p", "atmega649", "atmega649a", "atmega649p", "atmega6490", "atmega6490a", "atmega6490p", "atmega64c1", "atmega64m1", "atmega64hve", "at90can32", "at90can64", "at90pwm216", "at90pwm316", "at90scr100", "at90usb646", "at90usb647", "at94k", "avr51", "atmega128", "atmega1280", "atmega1281", "atmega1284p", "atmega128rfa1", "at90can128", "at90usb1286", "at90usb1287", "m3000f", "m3000s", "m3001b", "avr6", "atmega2560", "atmega2561", "avrxmega2", "atxmega16a4", "atxmega16d4", "atxmega32d4", "avrxmega3", "atxmega32a4", "avrxmega4", "atxmega64a3", "atxmega64d3", "avrxmega5", "atxmega64a1", "avrxmega6", "atxmega128a3", "atxmega128d3", "atxmega192a3", "atxmega192d3", "atxmega256a3", "atxmega256a3b", "atxmega256d3", "avrxmega7", "atxmega128a1", "avr1", "at90s1200", "attiny11", "attiny12", "attiny15", "attiny28" }));
        mcuCombo.setPreferredSize(new java.awt.Dimension(200, 27));
        mcuCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                mcuComboItemStateChanged(evt);
            }
        });
        toolBar.add(mcuCombo);

        tabFileLabel.setForeground(new java.awt.Color(1, 1, 1));
        tabFileLabel.setText("Title");

        splitPane.setBorder(null);
        splitPane.setDividerLocation(350);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setContinuousLayout(true);

        consolePane.setEditable(false);
        consolePane.setBackground(new java.awt.Color(1, 1, 1));
        consolePane.setBorder(null);
        consolePane.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        consolePane.setForeground(new java.awt.Color(254, 254, 254));
        consolePane.setCaretColor(new java.awt.Color(254, 254, 254));
        consolePane.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        consoleScrollPane.setViewportView(consolePane);

        splitPane.setRightComponent(consoleScrollPane);

        editingScrollPane.setViewportView(editingPane);

        splitPane.setLeftComponent(editingScrollPane);

        file_menu.setText("File");

        jMenu1.setText("New");

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("File");
        jMenu1.add(jMenuItem3);

        file_menu.add(jMenu1);

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setBackground(new java.awt.Color(235, 235, 235));
        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        file_menu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        saveMenuItem.setToolTipText("");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        file_menu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        file_menu.add(saveAsMenuItem);

        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        file_menu.add(aboutMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        exitMenuItem.setMnemonic('e');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        file_menu.add(exitMenuItem);

        menuBar.add(file_menu);

        tools_menu.setText("Tools");

        verifyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, java.awt.event.InputEvent.SHIFT_MASK));
        verifyMenuItem.setText("Verify");
        verifyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verifyMenuItemActionPerformed(evt);
            }
        });
        tools_menu.add(verifyMenuItem);

        uploadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, java.awt.event.InputEvent.SHIFT_MASK));
        uploadMenuItem.setText("Upload");
        uploadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadMenuItemActionPerformed(evt);
            }
        });
        tools_menu.add(uploadMenuItem);

        prog_options_menu.setText("Programmer");

        programmer_options_button_group.add(usbasp_item);
        usbasp_item.setSelected(true);
        usbasp_item.setText("USBasp");
        usbasp_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usbasp_itemActionPerformed(evt);
            }
        });
        prog_options_menu.add(usbasp_item);

        programmer_options_button_group.add(arduino_item);
        arduino_item.setText("Arduino");
        arduino_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arduino_itemActionPerformed(evt);
            }
        });
        prog_options_menu.add(arduino_item);

        programmer_options_button_group.add(stk500v1_item);
        stk500v1_item.setText("Atmel STK500 Version 1.x firmware (stk500v1)");
        stk500v1_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stk500v1_itemActionPerformed(evt);
            }
        });
        prog_options_menu.add(stk500v1_item);

        tools_menu.add(prog_options_menu);

        port_menu.setText("Port");
        tools_menu.add(port_menu);

        menuBar.add(tools_menu);

        view_menu.setText("View");

        font_menu.setText("Font");

        def_font_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.CTRL_MASK));
        def_font_item.setText("Default size");
        def_font_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                def_font_itemActionPerformed(evt);
            }
        });
        font_menu.add(def_font_item);

        inc_font_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_EQUALS, java.awt.event.InputEvent.CTRL_MASK));
        inc_font_item.setText("Increase font size");
        inc_font_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inc_font_itemActionPerformed(evt);
            }
        });
        font_menu.add(inc_font_item);

        dec_font_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_MASK));
        dec_font_item.setText("Decrease font size");
        dec_font_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dec_font_itemActionPerformed(evt);
            }
        });
        font_menu.add(dec_font_item);

        view_menu.add(font_menu);

        menuBar.add(view_menu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(splitPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

      private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
            saveAsFunction();
      }//GEN-LAST:event_saveAsMenuItemActionPerformed

      private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
            if (choseFile) {
                  saveFunction();
            } else {
                  saveAsFunction();
            }
      }//GEN-LAST:event_saveMenuItemActionPerformed

      private void verifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verifyButtonActionPerformed
            if (fileToOpen == null || !fileToOpen.exists()) {
                  PrintWriter writer = null;
                  try {
                        temporary = true;
                        String temp_folder = tabFileLabel.getText().replace("*", "").replace(".c", "");
                        String username = System.getProperty("user.name");

                        LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                        if (info.getName().toLowerCase().equals("windows")) {
                              File x = new File("C:\\Users\\" + username + "\\AppData\\Local\\Temp\\" + temp_folder);
                              x.mkdir();
                              fileToOpen = new File(x.getPath() + "\\" + tabFileLabel.getText().replace("*", ""));
                              temporaryFileToOpen = fileToOpen;
                              writer = new PrintWriter(fileToOpen);
                              writer.println(editingPane.getText());
                              compileFile();
                        } else {
                              File x = new File("/home/" + username + "/.avr_studio_temp");
                              x.mkdir();
                              x = new File("/home/" + username + "/.avr_studio_temp/" + temp_folder);
                              x.mkdir();
                              fileToOpen = new File(x.getPath() + "/" + tabFileLabel.getText().replace("*", ""));
                              fileToOpen.createNewFile();
                              temporaryFileToOpen = fileToOpen;
                              writer = new PrintWriter(fileToOpen);
                              writer.println(editingPane.getText());
                              compileFile();
                        }
                        choseFile = true;
                  } catch (FileNotFoundException ex) {
                        System.err.println(ex.toString());
                  } catch (IOException ex) {
                        System.err.println(ex.toString());
                  } finally {
                        writer.close();
                  }

            } else {
                  compileFile();
            }
      }//GEN-LAST:event_verifyButtonActionPerformed

      private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
            if (fileToOpen == null || !fileToOpen.exists()) {
                  PrintWriter writer = null;
                  try {
                        temporary = true;
                        String temp_folder = tabFileLabel.getText().replace("*", "").replace(".c", "");
                        String username = System.getProperty("user.name");

                        LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                        if (info.getName().toLowerCase().equals("windows")) {
                              File x = new File("C:\\Users\\" + username + "\\AppData\\Local\\Temp\\" + temp_folder);
                              x.mkdir();
                              fileToOpen = new File(x.getPath() + "\\" + tabFileLabel.getText().replace("*", ""));
                              temporaryFileToOpen = fileToOpen;
                              writer = new PrintWriter(fileToOpen);
                              writer.println(editingPane.getText());
                              compileFile();
                              uploadHex();
                        } else {
                              File x = new File("/home/" + username + "/.avr_studio_temp");
                              x.mkdir();
                              x = new File("/home/" + username + "/.avr_studio_temp/" + temp_folder);
                              x.mkdir();
                              fileToOpen = new File(x.getPath() + "/" + tabFileLabel.getText().replace("*", ""));
                              fileToOpen.createNewFile();
                              temporaryFileToOpen = fileToOpen;
                              writer = new PrintWriter(fileToOpen);
                              writer.println(editingPane.getText());
                              compileFile();
                              uploadHex();
                        }
                        choseFile = true;
                  } catch (FileNotFoundException ex) {
                        System.err.println(ex.toString());
                  } catch (IOException ex) {
                        System.err.println(ex.toString());
                  } finally {
                        writer.close();
                  }

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
            fd.setVisible(true);
            String selected = fd.getDirectory() + fd.getFile();
            File x = new File(selected);
            String parent = x.getParentFile().getName();
            if (!selected.contains("null")) {
                  if (!selected.substring(selected.length() - 2, selected.length()).toLowerCase().equals(".c")) {
                        JOptionPane.showMessageDialog(this, "AVR-Studio can only open its own sketches and other files ending in .c",
                                "Bad file selected", JOptionPane.WARNING_MESSAGE);
                  } else if (!parent.equals(x.getName().replace(".c", ""))) {
                        int query_result = JOptionPane.showConfirmDialog(this, "The file \"" + x.getName()
                                + "\" needs to be inside a folder named \"" + x.getName().replace(".c", "") + "\".\nCreate this folder, move the file, and continue?",
                                "Parent Folder not Found", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (query_result == JOptionPane.YES_OPTION) {
                              try {
                                    if (temporary) {
                                          try {
                                                temporary = false;
                                                LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                                                String[] cmd = (info.getName().toLowerCase().equals("windows"))
                                                        ? new String[]{"cmd", "/c", "rm -rf " + temporaryFileToOpen.getParentFile()}
                                                        : new String[]{"/bin/sh", "-c", "rm -rf " + temporaryFileToOpen.getParentFile()};
                                                System.out.println(cmd[2]);
                                                new ProcessBuilder(cmd).start();

                                          } catch (IOException ex) {
                                                System.err.println(ex.toString());
                                          }
                                    }
                                    x = new File(x.getParent() + "\\" + x.getName().replace(".c", ""));
                                    x.mkdir();
                                    LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                                    String[] cmd = (info.getName().toLowerCase().equals("windows"))
                                            ? new String[]{"cmd", "/c", "mv " + selected + " " + x.getPath() + "\\" + fd.getFile()}
                                            : new String[]{"/bin/sh", "-c", "mv " + selected + " " + x.getPath() + "\\" + fd.getFile()};
                                    System.out.println(cmd[2]);
                                    new ProcessBuilder(cmd).start();
                                    fileToOpen = new File(x.getPath() + "\\" + fd.getFile());
                                    cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
                                    parentPath = "\"" + fileToOpen.getParent() + "\"";
                                    editingPane.setText(null);
                                    Thread.sleep(50);
                                    Scanner scan = new Scanner(fileToOpen);
                                    String text = "";
                                    while (scan.hasNext()) {
                                          text += scan.nextLine() + "\n";
                                    }
                                    editingPane.setText(text);
                                    if (editingPane.getText().length() > 0) {
                                          editingPane.setText(editingPane.getText().substring(0, editingPane.getText().length() - 1));
                                    } else {
                                          editingPane.setText(null);
                                    }
                                    tabFileLabel.setText(fileToOpen.getName());
                                    tabFileLabel.setForeground(Color.BLACK);
                                    choseFile = true;
                              } catch (IOException | InterruptedException ex) {
                                    System.err.println(ex.toString());
                              }
                        } else {
                              JOptionPane.showMessageDialog(this, "Cannot open selected file !!!", "Bad Selection", JOptionPane.ERROR_MESSAGE);
                        }
                  } else {
                        try {
                              if (temporary) {
                                    try {
                                          temporary = false;
                                          LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                                          String[] cmd = (info.getName().toLowerCase().equals("windows"))
                                                  ? new String[]{"cmd", "/c", "rm -rf " + temporaryFileToOpen.getParentFile()}
                                                  : new String[]{"/bin/sh", "-c", "rm -rf " + temporaryFileToOpen.getParentFile()};
                                          System.out.println(cmd[2]);
                                          new ProcessBuilder(cmd).start();

                                    } catch (IOException ex) {
                                          System.err.println(ex.toString());
                                    }
                              }
                              fileToOpen = fd.getFiles()[0];
                              cPath = "\"" + fileToOpen.getAbsolutePath() + "\"";
                              parentPath = "\"" + fileToOpen.getParent() + "\"";
                              editingPane.setText(null);
                              Scanner scan = new Scanner(fileToOpen);
                              String text = "";
                              while (scan.hasNext()) {
                                    text += scan.nextLine() + "\n";
                              }
                              editingPane.setText(text);
                              if (editingPane.getText().length() > 0) {
                                    editingPane.setText(editingPane.getText().substring(0, editingPane.getText().length() - 1));
                              } else {
                                    editingPane.setText(null);
                              }
                              tabFileLabel.setText(fileToOpen.getName());
                              tabFileLabel.setForeground(Color.BLACK);
                              choseFile = true;

                        } catch (FileNotFoundException ex) {
                              System.err.println(ex.toString());
                        }
                  }
            }
      }//GEN-LAST:event_openMenuItemActionPerformed

      private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
            // maximized
            if ((evt.getNewState() & Studio.MAXIMIZED_BOTH) == Studio.MAXIMIZED_BOTH) {
                  splitPane.setResizeWeight(0.9);
            } // minimized
            else if ((evt.getNewState() & Studio.ICONIFIED) == Studio.ICONIFIED) {
                  System.out.println(evt.getID());
            }
      }//GEN-LAST:event_formWindowStateChanged

      private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
            String copy_left
                    = "**********************************************************************************\n"
                    + "NOTICE TO USERS\n"
                    + "\n"
                    + "\n"
                    + "This computer software is the private property of its owner, whether\n"
                    + "individual, corporate or government.  It is for authorized use only.\n"
                    + "Users (authorized or unauthorized) have no explicit or implicit\n"
                    + "expectation of privacy.\n"
                    + "\n"
                    + "Any or all uses of this software may be\n"
                    + "intercepted, monitored, recorded, copied, audited, inspected, and\n"
                    + "disclosed to your employer, to authorized site, government, and law\n"
                    + "enforcement personnel, as well as authorized officials of government\n"
                    + "agencies, both domestic and foreign.\n"
                    + "\n"
                    + "By using this software, the user consents to such interception, monitoring,\n"
                    + "recording, copying, auditing, inspection, and disclosure at the\n"
                    + "discretion of such personnel or officials.  Unauthorized or improper use\n"
                    + "of this software may result in civil and criminal penalties and\n"
                    + "administrative or disciplinary action, as appropriate. By continuing to\n"
                    + "use this software you indicate your awareness of and consent to these terms\n"
                    + "and conditions of use. CLOSE and REMOVE / UNINSTALL THIS SOFTWARE\n"
                    + "IMMEDIATELY if you do not agree to the\n"
                    + "conditions stated in this warning.\n"
                    + "\n"
                    + "This program is free software: you can redistribute it and/or modify\n"
                    + "it under the terms of the GNU General Public License as published by\n"
                    + "the Free Software Foundation, either version 3 of the License, or\n"
                    + "(at your option) any later version.\n"
                    + "\n"
                    + "This program is distributed in the hope that it will be useful,\n"
                    + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                    + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
                    + "GNU General Public License for more details.\n"
                    + "\n"
                    + "You should have received a copy of the GNU General Public License\n"
                    + "along with this program.  If not, see <http://www.gnu.org/licenses/>."
                    + "\n"
                    + "\n"
                    + "Compiled, Edited and Designed by AbdAlMoniem AlHifnawy"
                    + "\n"
                    + "**********************************************************************************";
            privacy_text_pane.setText(copy_left);
            privacy_text_pane.setCaretPosition(0);

            privacy_dialog.setLocation(getLocation().x - 40, getLocation().y + 10);
            privacy_dialog.setSize(700, 500);
            privacy_dialog.setVisible(true);
//            JOptionPane.showMessageDialog(this, copy_left, "About", JOptionPane.INFORMATION_MESSAGE);
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
                        if (choseFile) {
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
            } else if (temporary) {
                  try {
                        LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                        String[] cmd = (info.getName().toLowerCase().equals("windows"))
                                ? new String[]{"cmd", "/c", "rm -rf " + temporaryFileToOpen.getParentFile()}
                                : new String[]{"/bin/sh", "-c", "rm -rf " + temporaryFileToOpen.getParentFile()};
                        System.out.println(cmd[2]);
                        new ProcessBuilder(cmd).start();
                        int result = JOptionPane.showOptionDialog(this,
                                "This is a temporary file.\nDo you want to save the file in a permenant location ?",
                                "File \"" + tabFileLabel.getText().toLowerCase().replace("*", "") + "\" is Temporary.",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[]{"Save Changes", "Close Without Saving", "Cancel"},
                                "Save Changes");
                        if (result == JOptionPane.YES_OPTION) {
                              saveAsFunction();
                              if (!fileToOpen.getParent().toLowerCase().contains("temp")) {
                                    System.exit(0);
                              }
                        } else if (result == JOptionPane.NO_OPTION) {
                              System.exit(0);
                        } else if (result == JOptionPane.CANCEL_OPTION) {
                              System.out.println("no exit");
                        }
                  } catch (IOException ex) {
                        System.err.println(ex.toString());
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

    private void mcuComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_mcuComboItemStateChanged
          mmcu = mcuCombo.getSelectedItem().toString();
    }//GEN-LAST:event_mcuComboItemStateChanged

    private void usbasp_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usbasp_itemActionPerformed
          prog_option = "usbasp";
          port_menu.setEnabled(false);
    }//GEN-LAST:event_usbasp_itemActionPerformed

    private void stk500v1_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stk500v1_itemActionPerformed
          port_menu.removeAll();
          port_menu.setEnabled(true);

          Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
          ButtonGroup ports_button_group = new ButtonGroup();
          new Thread(new Runnable() {
                @Override
                public void run() {
                      int counter = 0;
                      while (portEnum.hasMoreElements()) {
                            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                            JCheckBoxMenuItem new_port = new JCheckBoxMenuItem(currPortId.getName());
                            new_port.addActionListener(new java.awt.event.ActionListener() {
                                  @Override
                                  public void actionPerformed(ActionEvent e) {
                                        prog_option = "stk500v1 -b19200 -P " + currPortId.getName();
                                        System.out.println(prog_option);
                                  }
                            });
                            ports_button_group.add(new_port);
                            new_port.setSelected(true);
                            port_menu.add(new_port);
                            counter++;
                      }
                      if (counter < 1) {
                            port_menu.setEnabled(false);
                      }
                }
          }).start();
    }//GEN-LAST:event_stk500v1_itemActionPerformed

    private void arduino_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arduino_itemActionPerformed
          port_menu.removeAll();
          port_menu.setEnabled(true);

          Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
          ButtonGroup ports_button_group = new ButtonGroup();
          new Thread(new Runnable() {
                @Override
                public void run() {
                      int counter = 0;
                      while (portEnum.hasMoreElements()) {
                            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                            JCheckBoxMenuItem new_port = new JCheckBoxMenuItem(currPortId.getName());
                            new_port.addActionListener(new java.awt.event.ActionListener() {
                                  @Override
                                  public void actionPerformed(ActionEvent e) {
                                        prog_option = "arduino -b57600 -P " + currPortId.getName();
                                        System.out.println(prog_option);
                                  }
                            });
                            ports_button_group.add(new_port);
                            port_menu.add(new_port);
                            new_port.setSelected(true);
                            System.out.println(new_port.getName());
                            counter++;
                      }
                      if (counter < 1) {
                            port_menu.setEnabled(false);
                      }
                }
          }).start();
    }//GEN-LAST:event_arduino_itemActionPerformed

    private void inc_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inc_font_itemActionPerformed
          font_size += 1;
          String text = editingPane.getText();
          int caret_position = editingPane.getCaretPosition();
          editingPane.getDocument().removeDocumentListener(listener);
          config.put("DefaultFont", default_font + font_size);
          editingPane.setEditorKit(kit);
          editingPane.getDocument().addDocumentListener(listener);
          editingPane.setText(text);
          editingPane.setCaretPosition(caret_position);
    }//GEN-LAST:event_inc_font_itemActionPerformed

    private void dec_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dec_font_itemActionPerformed
          font_size -= 1;
          String text = editingPane.getText();
          int caret_position = editingPane.getCaretPosition();
          editingPane.getDocument().removeDocumentListener(listener);
          config.put("DefaultFont", default_font + font_size);
          editingPane.setEditorKit(kit);
          editingPane.getDocument().addDocumentListener(listener);
          editingPane.setText(text);
          editingPane.setCaretPosition(caret_position);
    }//GEN-LAST:event_dec_font_itemActionPerformed

    private void def_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_def_font_itemActionPerformed
          font_size = default_font_size;
          String text = editingPane.getText();
          int caret_position = editingPane.getCaretPosition();
          editingPane.getDocument().removeDocumentListener(listener);
          config.put("DefaultFont", default_font + default_font_size);
          editingPane.setEditorKit(kit);
          editingPane.getDocument().addDocumentListener(listener);
          editingPane.setText(text);
          editingPane.setCaretPosition(caret_position);
    }//GEN-LAST:event_def_font_itemActionPerformed

      public static void main(String args[]) {
            try {
                  LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
                  UIManager.setLookAndFeel(info.getClassName());

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                  java.util.logging.Logger.getLogger(Studio.class
                          .getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
    private javax.swing.JCheckBoxMenuItem arduino_item;
    private javax.swing.JTextPane consolePane;
    private javax.swing.JScrollPane consoleScrollPane;
    private javax.swing.JMenuItem dec_font_item;
    private javax.swing.JMenuItem def_font_item;
    private javax.swing.JEditorPane editingPane;
    private javax.swing.JScrollPane editingScrollPane;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu file_menu;
    private javax.swing.JMenu font_menu;
    private javax.swing.JMenuItem inc_font_item;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JComboBox mcuCombo;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenu port_menu;
    private javax.swing.JDialog privacy_dialog;
    private javax.swing.JScrollPane privacy_scroll_pane;
    private javax.swing.JTextPane privacy_text_pane;
    private javax.swing.JMenu prog_options_menu;
    private javax.swing.ButtonGroup programmer_options_button_group;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JTextField searchField;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JCheckBoxMenuItem stk500v1_item;
    private javax.swing.JLabel tabFileLabel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenu tools_menu;
    private javax.swing.JButton uploadButton;
    private javax.swing.JMenuItem uploadMenuItem;
    private javax.swing.JCheckBoxMenuItem usbasp_item;
    private javax.swing.JButton verifyButton;
    private javax.swing.JMenuItem verifyMenuItem;
    private javax.swing.JMenu view_menu;
    // End of variables declaration//GEN-END:variables
}
