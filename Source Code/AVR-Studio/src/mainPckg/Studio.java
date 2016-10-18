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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Enumeration;
import javax.swing.UIManager.*;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.Lexer;
import jsyntaxpane.Token;
import jsyntaxpane.syntaxkits.BashSyntaxKit;
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

      private String os = null;
      private String username = null;

      private int warning_count = 0;
      private long chars_inserted = 0;

      private boolean chose_file = false;
      private boolean verified = false;
      private boolean error = false;
      private boolean temporary = false;
      private boolean make = false;

      private String prog_option = null;
      private String mmcu = null;

      private String parentPath = null;
      private String cPath = null;
      private String oPath = null;
      private String elfPath = null;
      private String hexPath = null;

      private int selected_tab = 0;
      private String sketch_name = null;

      private File makefile = null;
      private File file_to_open = null;
      private File temporary_file_to_open = null;

      private FocusListener f_listener = null;
      private CaretListener c_listener = null;
      private DocumentListener listener = null;

      private FocusListener mkfl_f_listener = null;
      private CaretListener mkfl_c_listener = null;
      private DocumentListener mkfl_listener = null;

      private Configuration config = null;
      private CSyntaxKit c_editor_kit = null;

      private String default_font = null;
      private int default_font_size = 15;
      private int font_size = default_font_size;

      Color color = null;
      int style = -1;

      String keyword_color = null;
      String keyword2_color = null;
      String number_color = null;
      String string_color = null;
      String type_color = null;
      String comment_color = null;
      String operator_color = null;
      String identifier_color = null;

      private void append_to_pane(JTextPane pane, String msg, int mode) throws BadLocationException {
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

      private String get_style(int style) {
            switch (style) {
                  case 0:
                        return "normal";
                  case 1:
                        return "bold";
                  case 2:
                        return "italic";
                  case 3:
                        return "bold italic";
                  default:
                        return null;
            }
      }

      private void check_for_errors(JTextPane pane, BufferedReader br) {
            try {
                  boolean warning = false;
                  String line = br.readLine();
                  while (line != null) {
                        if (line.toLowerCase().contains("in function")) {
                              String line_before_that_maybe_error = line;
                              line = br.readLine();
                              if (line.toLowerCase().contains("error")
                                      || line.toLowerCase().contains("stop")
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
                                    append_to_pane(pane, line_before_that_maybe_error + "\n", 2);
                                    append_to_pane(pane, line + "\n", 2);
                                    error = true;
                              } else if (line.toLowerCase().contains("avrdude done") && error) {
                                    append_to_pane(pane, line + "\n", 2);
                              } else if (line.toLowerCase().contains("warning") || line.toLowerCase().contains("disable")) {
                                    append_to_pane(pane, line_before_that_maybe_error + "\n", 1);
                                    append_to_pane(pane, line + "\n", 1);

                                    if (!os.contains("windows")) {
                                          warning = true;
                                    }
                                    warning_count++;
                              } else if (line.toLowerCase().contains("note")) {
                                    append_to_pane(pane, line + "\n", 1);
                              } else {
                                    append_to_pane(pane, line + "\n", 0);
                              }
                        } else if (line.toLowerCase().contains("error")
                                || line.toLowerCase().contains("stop")
                                || line.toLowerCase().contains("no such file")
                                || line.toLowerCase().contains("not found")
                                || line.toLowerCase().contains("undefined reference")
                                || line.toLowerCase().contains("double check chip")
                                || line.toLowerCase().contains("invalid")
                                || line.toLowerCase().contains("failed")) {
                              append_to_pane(pane, line + "\n", 2);
                              error = true;
                        } else if (line.toLowerCase().contains(";")
                                || line.toLowerCase().contains("{")
                                || line.toLowerCase().contains("}")) {
                              if (warning) {
                                    append_to_pane(pane, line + "\n", 1);

                                    if (!os.contains("windows")) {
                                          line = br.readLine();
                                          if (line != null && line.toLowerCase().contains("^")) {
                                                append_to_pane(pane, line + "\n", 1);
                                          }
                                    }
                                    warning_count++;
                                    warning = false;
                              } else {
                                    append_to_pane(pane, line + "\n", 2);
                                    error = true;
                              }
                        } else if (line.toLowerCase().contains("^") && error) {
                              append_to_pane(pane, line + "\n", 2);
                        } else if (line.toLowerCase().contains("avrdude done") && error) {
                              append_to_pane(pane, line + "\n", 2);
                        } else if (line.toLowerCase().contains("warning") || line.toLowerCase().contains("disable")) {
                              append_to_pane(pane, line + "\n", 1);
                              warning_count++;
                              warning = true;
                        } else if (line.toLowerCase().contains("note")) {
                              append_to_pane(pane, line + "\n", 1);
                        } else {
                              append_to_pane(pane, line + "\n", 0);
                        }
                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        line = br.readLine();
                  }
            } catch (IOException | BadLocationException ex) {
                  System.err.println(ex.toString());
            }
      }

      private void save_method() {
            try {
                  editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
                  if (mkfl_build_item.isSelected()) {
                        if (selected_tab == 0) {
                              try (PrintWriter writer = new PrintWriter(file_to_open, "UTF-8")) {
                                    writer.println(editing_pane.getText());
                              }
                              cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
                              parentPath = "\"" + file_to_open.getParent() + "\"";

                              tab_pane.setTitleAt(0, sketch_name);
                              tab_pane.setForegroundAt(0, Color.BLACK);

                              editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
                              editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_TEXT));
                        } else {
                              try (PrintWriter writer = new PrintWriter(makefile, "UTF-8")) {
                                    writer.println(mkfl_editing_pane.getText());
                              }
                              cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
                              parentPath = "\"" + file_to_open.getParent() + "\"";

                              tab_pane.setTitleAt(1, "makefile");
                              tab_pane.setForegroundAt(1, Color.BLACK);

                              editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
                              editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_TEXT));
                        }

                        if (!tab_pane.getTitleAt(0).contains("*") && !tab_pane.getTitleAt(1).contains("*")) {
                              sketch_name = file_to_open.getName();
                              status_label.setText("All Saved");
                              status_label.setForeground(Color.BLACK);
                        }
                  } else if (std_build_item.isSelected()) {
                        try (PrintWriter writer = new PrintWriter(file_to_open, "UTF-8")) {
                              writer.println(editing_pane.getText());
                        }
                        cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
                        parentPath = "\"" + file_to_open.getParent() + "\"";

                        tab_pane.setTitleAt(0, sketch_name);
                        tab_pane.setForegroundAt(0, Color.BLACK);

                        editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
                        editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_TEXT));

                        sketch_name = file_to_open.getName();
                        status_label.setText("All Saved");
                        status_label.setForeground(Color.BLACK);
                  }
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                  Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
      }

      private void save_as_method() {
            if (temporary) {
                  try {
                        String[] cmd = (os.equals("windows"))
                                ? new String[]{"cmd", "/c", "rm -rf " + temporary_file_to_open.getParentFile()}
                                : new String[]{"/bin/sh", "-c", "rm -rf " + temporary_file_to_open.getParentFile()};
                        System.out.println(cmd[2]);
                        new ProcessBuilder(cmd).start();
                  } catch (IOException ex) {
                        System.err.println(ex.toString());
                  }
            }
            FileDialog fd = new FileDialog(this, "Save As...", FileDialog.SAVE);
            fd.setTitle("Save As...");
            fd.setFile(tab_pane.getTitleAt(0).toLowerCase().replace("*", "").replace(".c", ""));
            fd.setVisible(true);
            String selected = fd.getDirectory() + fd.getFile();
            if (!selected.contains("null")) {
                  try {
                        temporary = false;
                        File x = new File(fd.getFiles()[0].getPath().replace(".c", ""));
                        x.mkdir();

                        if (os.equals("windows")) {
                              file_to_open = new File(x.getPath() + "\\" + fd.getFile() + ".c");
                              makefile = mkfl_build_item.isSelected() ? new File(x.getPath() + "\\makefile") : null;
                        } else {
                              file_to_open = new File(x.getPath() + "/" + fd.getFile() + ".c");
                              makefile = mkfl_build_item.isSelected() ? new File(x.getPath() + "/makefile") : null;
                              if (file_to_open.getParentFile().getName().equals(file_to_open.getParentFile().getParentFile().getName())) {
                                    file_to_open.getParentFile().delete();
                                    file_to_open = new File(file_to_open.getParentFile().getParentFile() + "/" + fd.getFile().replace(".c", "") + ".c");
                                    makefile = mkfl_build_item.isSelected() ? new File(file_to_open.getParentFile().getParentFile() + "/makefile") : null;
                              }
                        }
                        try (PrintWriter writer = new PrintWriter(file_to_open, "UTF-8")) {
                              writer.println(editing_pane.getText());
                        }

                        tab_pane.setTitleAt(0, sketch_name);
                        tab_pane.setForegroundAt(0, Color.BLACK);

                        if (makefile != null) {
                              try (PrintWriter writer = new PrintWriter(makefile, "UTF-8")) {
                                    writer.println(mkfl_editing_pane.getText());
                              }

                              tab_pane.setTitleAt(1, "makefile");
                              tab_pane.setForegroundAt(1, Color.BLACK);
                        }

                        cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
                        parentPath = "\"" + file_to_open.getParent() + "\"";

                        sketch_name = file_to_open.getName();
                        tab_pane.setTitleAt(0, sketch_name);
                        status_label.setText("All Saved");
                        status_label.setForeground(Color.BLACK);

                        chose_file = true;
                  } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        System.err.println(ex.toString());
                  }
            }
      }

      private void save_all_method() {
            try (PrintWriter writer = new PrintWriter(file_to_open, "UTF-8")) {
                  writer.println(editing_pane.getText());
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                  System.err.println(ex.getMessage());
            }
            try (PrintWriter writer = new PrintWriter(makefile, "UTF-8")) {
                  writer.println(mkfl_editing_pane.getText().replaceAll("    ", "\t"));
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                  System.err.println(ex.getMessage());
            }
            cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
            parentPath = "\"" + file_to_open.getParent() + "\"";

            tab_pane.setTitleAt(0, sketch_name);
            tab_pane.setForegroundAt(0, Color.BLACK);

            tab_pane.setTitleAt(1, "makefile");
            tab_pane.setForegroundAt(1, Color.BLACK);

            sketch_name = file_to_open.getName();
            status_label.setText("All Saved");
            status_label.setForeground(Color.BLACK);

            editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_CROSSHAIR));
            editing_pane.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.CURSOR_TEXT));
      }

      private void compile_file() {
            if (mkfl_build_item.isSelected()) {
                  save_all_method();

                  editing_pane.setEnabled(false);
                  mkfl_editing_pane.setEnabled(false);

                  oPath = cPath.replace(".c", ".o");
                  elfPath = cPath.replace(".c", ".elf");
                  hexPath = cPath.replace(".c", ".hex");
                  console_pane.setText(null);
                  warning_count = 0;
                  error = false;

                  if (os.equals("windows")) {
                        try {
                              String[] cmd = {"cmd", "/c", "cd /d " + file_to_open.getParent() + " && make compile"};
                              System.out.println(cmd[2]);
                              append_to_pane(console_pane, cmd[2] + "\n", 4);
                              console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              Process p = new ProcessBuilder(cmd).start();
                              BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              check_for_errors(console_pane, br);
                              if (!error) {
                                    File f = new File(hexPath.replace("\"", ""));
                                    if (f.exists()) {
                                          if (warning_count == 1) {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), 1);
                                          } else if (warning_count > 0) {
                                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 1);
                                          } else {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 0);
                                          }
                                          System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                                          append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", 0);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                          verified = true;
                                    } else {
                                          if (warning_count == 1) {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                          } else if (warning_count > 0) {
                                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          } else {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          }
                                          System.err.println("Compilation Terminated, could not generate hex file !!!");
                                          append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", 2);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    }
                              } else {
                                    if (warning_count == 1) {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                    } else if (warning_count > 0) {
                                          System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    } else {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    }
                                    System.err.println("Errors Occured During Compilation !!!");
                                    append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", 2);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              }
                        } catch (BadLocationException | IOException ex) {
                              System.err.println(ex.getMessage());
                        }
                  } else {
                        try {
                              String[] cmd = {"/bin/sh", "-c", "cd " + file_to_open.getParent() + " && make compile"};
                              System.out.println(cmd[2]);
                              append_to_pane(console_pane, cmd[2] + "\n", 4);
                              console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              Process p = new ProcessBuilder(cmd).start();
                              BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              check_for_errors(console_pane, br);

                              if (!error) {
                                    File f = new File(hexPath.replace("\"", ""));
                                    if (f.exists()) {
                                          if (warning_count == 1) {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), 1);
                                          } else if (warning_count > 0) {
                                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 1);
                                          } else {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 0);
                                          }
                                          System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                                          append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", 0);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                          verified = true;
                                    } else {
                                          if (warning_count == 1) {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                          } else if (warning_count > 0) {
                                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          } else {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          }
                                          System.err.println("Compilation Terminated, could not generate hex file !!!");
                                          append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", 2);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    }
                              } else {
                                    if (warning_count == 1) {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                    } else if (warning_count > 0) {
                                          System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    } else {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    }
                                    System.err.println("Errors Occured During Compilation !!!");
                                    append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", 2);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              }
                        } catch (BadLocationException | IOException ex) {
                              System.err.println(ex.getMessage());
                        }
                  }

                  editing_pane.setEnabled(true);
                  mkfl_editing_pane.setEnabled(true);
            } else if (std_build_item.isSelected()) {
                  save_method();
                  editing_pane.setEnabled(false);
                  mkfl_editing_pane.setEnabled(false);

                  oPath = cPath.replace(".c", ".o");
                  elfPath = cPath.replace(".c", ".elf");
                  hexPath = cPath.replace(".c", ".hex");
                  console_pane.setText(null);
                  warning_count = 0;
                  error = false;

                  if (os.equals("windows")) {       //Windows
                        try {
                              String[] cmd = {"cmd", "/c", "avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + cPath + " -o " + oPath};
                              System.out.println(cmd[2]);
                              append_to_pane(console_pane, cmd[2] + "\n", 4);
                              console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              Process p = new ProcessBuilder(cmd).start();
                              BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              check_for_errors(console_pane, br);

                              if (!error) {
                                    cmd = new String[]{"cmd", "/c", "avr-gcc -g -mmcu=" + mmcu + " -o " + elfPath + " " + oPath};
                                    System.out.println(cmd[2]);
                                    append_to_pane(console_pane, cmd[2] + "\n", 4);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    p = new ProcessBuilder(cmd).start();
                                    br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    check_for_errors(console_pane, br);
                              }
                              if (!error) {
                                    cmd = new String[]{"cmd", "/c", "avr-objcopy -j .text -j .data -O ihex " + elfPath + " " + hexPath};
                                    System.out.println(cmd[2]);
                                    append_to_pane(console_pane, cmd[2] + "\n", 4);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    p = new ProcessBuilder(cmd).start();
                                    br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    check_for_errors(console_pane, br);
                              }

                              if (!error) {
                                    cmd = new String[]{"cmd", "/c", "rm -f \"" + file_to_open.getAbsolutePath().replace(".c", ".o") + "\" \""
                                          + file_to_open.getAbsolutePath().replace(".c", ".elf") + "\""};
                                    System.out.println(cmd[2]);
                                    append_to_pane(console_pane, cmd[2] + "\n", 4);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    p = new ProcessBuilder(cmd).start();
                                    br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    check_for_errors(console_pane, br);
                              }

                              if (!error) {
                                    File f = new File(hexPath.replace("\"", ""));
                                    if (f.exists()) {
                                          if (warning_count == 1) {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), 1);
                                          } else if (warning_count > 0) {
                                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 1);
                                          } else {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 0);
                                          }
                                          System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                                          append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", 0);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                          verified = true;
                                    } else {
                                          if (warning_count == 1) {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                          } else if (warning_count > 0) {
                                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          } else {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          }
                                          System.err.println("Compilation Terminated, could not generate hex file !!!");
                                          append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", 2);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    }
                              } else {
                                    if (warning_count == 1) {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                    } else if (warning_count > 0) {
                                          System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    } else {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    }
                                    System.err.println("Errors Occured During Compilation !!!");
                                    append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", 2);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              }
                        } catch (IOException | BadLocationException ex) {
                              System.err.println(ex.toString());
                        }
                  } else {        //Linux
                        try {
                              String[] cmd = {"/bin/sh", "-c", "avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + cPath + " -o " + oPath};
                              System.out.println(cmd[2]);
                              append_to_pane(console_pane, cmd[2] + "\n", 4);
                              console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              Process p = new ProcessBuilder(cmd).start();
                              BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                              check_for_errors(console_pane, br);

                              if (!error) {
                                    cmd = new String[]{"/bin/sh", "-c", "avr-gcc -g -mmcu=" + mmcu + " -o " + elfPath + " " + oPath};
                                    System.out.println(cmd[2]);
                                    append_to_pane(console_pane, cmd[2] + "\n", 4);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    p = new ProcessBuilder(cmd).start();
                                    br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    check_for_errors(console_pane, br);
                              }
                              if (!error) {
                                    cmd = new String[]{"/bin/sh", "-c", "avr-objcopy -j .text -j .data -O ihex " + elfPath + " " + hexPath};
                                    System.out.println(cmd[2]);
                                    append_to_pane(console_pane, cmd[2] + "\n", 4);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    p = new ProcessBuilder(cmd).start();
                                    br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    check_for_errors(console_pane, br);
                              }

                              if (!error) {
                                    cmd = new String[]{"/bin/sh", "-c", "rm -f \"" + file_to_open.getAbsolutePath().replace(".c", ".o") + "\" \""
                                          + file_to_open.getAbsolutePath().replace(".c", ".elf") + "\""};
                                    System.out.println(cmd[2]);
                                    append_to_pane(console_pane, cmd[2] + "\n", 4);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    p = new ProcessBuilder(cmd).start();
                                    br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    check_for_errors(console_pane, br);
                              }

                              if (!error) {
                                    File f = new File(hexPath.replace("\"", ""));
                                    if (f.exists()) {
                                          if (warning_count == 1) {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), 1);
                                          } else if (warning_count > 0) {
                                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 1);
                                          } else {
                                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), 0);
                                          }
                                          System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                                          append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", 0);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                          verified = true;
                                    } else {
                                          if (warning_count == 1) {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                          } else if (warning_count > 0) {
                                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          } else {
                                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                          }
                                          System.err.println("Compilation Terminated, could not generate hex file !!!");
                                          append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", 2);
                                          console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    }
                              } else {
                                    if (warning_count == 1) {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), 2);
                                    } else if (warning_count > 0) {
                                          System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    } else {
                                          System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                          append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), 2);
                                    }
                                    System.err.println("Errors Occured During Compilation !!!");
                                    append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", 2);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                              }
                        } catch (IOException | BadLocationException ex) {
                              System.err.println(ex.toString());
                        }
                  }

                  editing_pane.setEnabled(true);
                  mkfl_editing_pane.setEnabled(true);
            }
      }

      private void upload_hex() {
            try {
                  if (verified) {
                        if (error) {
                              System.out.println("Fix compilation errors and then upload the sketch !!!");
                              append_to_pane(console_pane, "Fix compilation errors and then upload the sketch !!!\n", 2);
                              error = false;
                        } else //upload...
                        if (std_build_item.isSelected()) {
                              new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                          try {
                                                System.out.println("Uploading...");
                                                append_to_pane(console_pane, "Uploading...\n", 4);
                                                console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                editing_pane.setCaretPosition(editing_pane.getDocument().getLength());
                                                oPath = cPath.replace(".c", ".o");
                                                elfPath = cPath.replace(".c", ".elf");
                                                hexPath = cPath.replace(".c", ".hex");
                                                editing_pane.setEnabled(false);

                                                if (os.equals("windows")) {
                                                      try {
                                                            String[] cmd = {"cmd", "/c", "avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + hexPath.replace("\\", "/") + ":i"};
                                                            //String[] cmd = {"cmd", "/c", "ping 127.0.0.1"};     //for testing
                                                            System.out.println(cmd[2]);
                                                            append_to_pane(console_pane, cmd[2] + "\n", 4);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            ProcessBuilder pb = new ProcessBuilder(cmd);
                                                            Process p = pb.start();
                                                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                            //checkForErrors(console_pane, br);  //no real-time output

                                                            int value = 0;
                                                            while (value != -1) {
                                                                  char ch = (char) value;
                                                                  System.out.print(ch);
                                                                  append_to_pane(console_pane, ch + "", 4);
                                                                  console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                                  value = br.read();
                                                            }
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            String output = console_pane.getText();
                                                            if (output.contains("verified")) {
                                                                  System.out.println("Uploaded Successfully !!!");
                                                                  append_to_pane(console_pane, "Uploaded Successfully !!!\n", 0);
                                                                  console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            } else {
                                                                  System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                                                  append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", 2);
                                                                  console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            }
                                                            editing_pane.setEnabled(true);

                                                      } catch (IOException | BadLocationException ex) {
                                                            System.err.println(ex.toString());
                                                      }
                                                } else {
                                                      try {
                                                            String[] cmd = {"/bin/sh", "-c", "sudo avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + hexPath.replace("\\", "/") + ":i"};
                                                            //String[] cmd = {"/bin/sh", "-c", "ping 127.0.0.1"};     //for testing
                                                            System.out.println(cmd[2]);
                                                            append_to_pane(console_pane, cmd[2] + "\n", 4);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            ProcessBuilder pb = new ProcessBuilder(cmd);
                                                            Process p = pb.start();
                                                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                            //checkForErrors(console_pane, br);  //no real-time output
                                                            int value = 0;
                                                            while (value != -1) {
                                                                  char ch = (char) value;
                                                                  System.out.print(ch);
                                                                  append_to_pane(console_pane, ch + "", 4);
                                                                  console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                                  value = br.read();
                                                            }
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            String output = console_pane.getText();
                                                            if (output.contains("verified")) {
                                                                  System.out.println("Uploaded Successfully !!!");
                                                                  append_to_pane(console_pane, "Uploaded Successfully !!!\n", 0);
                                                                  console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            } else {
                                                                  System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                                                  append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", 2);
                                                                  console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            }
                                                            editing_pane.setEnabled(true);

                                                      } catch (IOException | BadLocationException ex) {
                                                            System.err.println(ex.toString());
                                                      }
                                                }
                                          } catch (BadLocationException ex) {
                                                System.err.println(ex.toString());
                                          }
                                    }
                              }).start();
                        } else if (mkfl_build_item.isSelected()) {
                              new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                          if (os.equals("windows")) {
                                                try {
                                                      String[] cmd = {"cmd", "/c", "cd /d " + file_to_open.getParent() + " && make upload"};
                                                      System.out.println(cmd[2]);
                                                      append_to_pane(console_pane, cmd[2] + "\n", 4);
                                                      console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      Process p = new ProcessBuilder(cmd).start();
                                                      BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                      //checkForErrors(console_pane, br);  //no real-time output
                                                      int value = 0;
                                                      while (value != -1) {
                                                            char ch = (char) value;
                                                            System.out.print(ch);
                                                            append_to_pane(console_pane, ch + "", 4);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            value = br.read();
                                                      }
                                                      console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      String output = console_pane.getText();
                                                      if (output.contains("verified")) {
                                                            System.out.println("Uploaded Successfully !!!");
                                                            append_to_pane(console_pane, "Uploaded Successfully !!!\n", 0);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      } else {
                                                            System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                                            append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", 2);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      }
                                                } catch (BadLocationException | IOException ex) {
                                                      System.err.println(ex.getMessage());
                                                }
                                          } else {
                                                try {
                                                      String[] cmd = {"/bin/sh", "-c", "cd " + file_to_open.getParent() + " && make upload"};
                                                      System.out.println(cmd[2]);
                                                      append_to_pane(console_pane, cmd[2] + "\n", 4);
                                                      console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      Process p = new ProcessBuilder(cmd).start();
                                                      BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                                      //checkForErrors(console_pane, br);  //no real-time output
                                                      int value = 0;
                                                      while (value != -1) {
                                                            char ch = (char) value;
                                                            System.out.print(ch);
                                                            append_to_pane(console_pane, ch + "", 4);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                            value = br.read();
                                                      }
                                                      console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      String output = console_pane.getText();
                                                      if (output.contains("verified")) {
                                                            System.out.println("Uploaded Successfully !!!");
                                                            append_to_pane(console_pane, "Uploaded Successfully !!!\n", 0);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      } else {
                                                            System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                                            append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", 2);
                                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                                      }
                                                } catch (BadLocationException | IOException ex) {
                                                      System.err.println(ex.getMessage());
                                                }
                                          }
                                    }
                              }).start();
                        }
                  } else {
                        compile_file();
                        if (error) {
                              System.out.println("Fix compilation errors and then upload the sketch !!!");
                              append_to_pane(console_pane, "Fix compilation errors and then upload the sketch !!!\n", 2);
                              error = false;
                        } else {
                              upload_hex();
                        }

                  }
            } catch (BadLocationException ex) {
                  System.err.println(ex.toString());
            }
      }

      private void search_ports() {
            new Thread(new Runnable() {
                  @Override
                  public void run() {
                        while (true) {
                              try {
                                    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
                                    ButtonGroup ports_button_group = new ButtonGroup();
                                    int counter = 0;

                                    if (!usbasp_item.isSelected()) {
                                          port_menu.removeAll();
                                          port_menu.setEnabled(true);
                                    } else {
                                          port_menu.setEnabled(false);
                                    }

                                    while (portEnum.hasMoreElements()) {
                                          CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                                          JCheckBoxMenuItem new_port = new JCheckBoxMenuItem(currPortId.getName());

                                          new_port.addActionListener((ActionEvent e) -> {
                                                prog_option = usbasp_item.isSelected() ? "usbasp" : arduino_item.isSelected() ? "arduino -b57600 -P " + currPortId.getName()
                                                        : "stk500v1 -b19200 -P " + currPortId.getName();
                                                System.out.println(prog_option);
                                          });
                                          ports_button_group.add(new_port);
                                          port_menu.add(new_port);
                                          new_port.setSelected(true);
                                          counter++;
                                    }

                                    if (counter < 1) {
                                          port_menu.setEnabled(false);
                                    }

                                    Thread.sleep(300);
                              } catch (InterruptedException ex) {
                                    System.err.println(ex.getMessage());
                              }
                        }
                  }
            }).start();
      }

      private String[] get_font() {
            JFontChooser fc = new JFontChooser();
            int result = fc.showDialog(null);

            String new_font = null;
            String new_font_size = null;
            String new_font_style = null;

            if (result == JFontChooser.OK_OPTION) {
                  new_font = fc.getSelectedFont().getFontName() + " ";
                  new_font_size = Integer.toString(fc.getSelectedFontSize());
                  new_font_style = Integer.toString(fc.getSelectedFontStyle());
            }

            return new String[]{new_font, new_font_size, new_font_style};
      }

      public Studio(String[] arguments) {
            initComponents();

//            search_ports();
            int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
            int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;

            os = UIManager.getInstalledLookAndFeels()[3].getName().toLowerCase();
            username = System.getProperty("user.name");

            this.setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.setLocation(screen_width / 3, screen_height / 15);

            port_menu.setEnabled(false);
            prog_option = "usbasp";
            mcuCombo.setSelectedItem("atmega16");
            mmcu = mcuCombo.getSelectedItem().toString();

            DateFormat dateFormat = new SimpleDateFormat("MMMdd_YY");
            String date = dateFormat.format(Calendar.getInstance().getTime());

            sketch_name = "sketch_" + date.toLowerCase() + ".c";
            tab_pane.setTitleAt(0, sketch_name);

            listener = new DocumentListener() {
                  @Override
                  public void insertUpdate(DocumentEvent e) {
                        status_label.setText("*editing...");
                        status_label.setForeground(Color.ORANGE);

                        tab_pane.setTitleAt(0, "*" + sketch_name);
                        tab_pane.setForegroundAt(0, Color.ORANGE);

                        verified = false;

                        chars_inserted += e.getLength();
                        char_ins_label.setText("Characters inserted: " + chars_inserted);
                  }

                  @Override
                  public void removeUpdate(DocumentEvent e) {
                        status_label.setText("*editing...");
                        status_label.setForeground(Color.ORANGE);

                        tab_pane.setTitleAt(0, "*" + sketch_name);
                        tab_pane.setForegroundAt(0, Color.ORANGE);

                        verified = false;

                        chars_inserted -= e.getLength();
                        char_ins_label.setText("Characters inserted: " + chars_inserted);
                  }

                  @Override
                  public void changedUpdate(DocumentEvent e) {
                        status_label.setText("*editing...");
                        status_label.setForeground(Color.ORANGE);

                        tab_pane.setTitleAt(0, "*" + sketch_name);
                        tab_pane.setForegroundAt(0, Color.ORANGE);

                        verified = false;
                  }
            };
            c_listener = new CaretListener() {
                  @Override
                  public void caretUpdate(CaretEvent e) {
                        try {
                              int caret_pos = editing_pane.getCaretPosition();
                              int row_num = (caret_pos == 0) ? 1 : 0;
                              for (int offset = caret_pos; offset > 0;) {
                                    offset = Utilities.getRowStart(editing_pane, offset) - 1;
                                    row_num++;
                              }
                              int offset = Utilities.getRowStart(editing_pane, caret_pos);
                              int col_num = caret_pos - offset + 1;

                              row_col_label.setText(row_num + ":" + col_num);
                        } catch (BadLocationException ex) {
                              System.err.println(ex.getMessage());
                        }
                  }
            };
            f_listener = new FocusListener() {

                  @Override
                  public void focusGained(FocusEvent e) {
                        chars_inserted = editing_pane.getText().length();
                        char_ins_label.setText("Characters inserted: " + chars_inserted);
                        info_label.setText(" Font Size: " + editing_pane.getFont().getSize());
                  }

                  @Override
                  public void focusLost(FocusEvent e) {
                  }
            };

            mkfl_listener = new DocumentListener() {
                  @Override
                  public void insertUpdate(DocumentEvent e) {
                        status_label.setText("*editing...");
                        status_label.setForeground(Color.ORANGE);

                        tab_pane.setTitleAt(1, "*makefile");
                        tab_pane.setForegroundAt(1, Color.ORANGE);

                        verified = false;

                        chars_inserted += e.getLength();
                        char_ins_label.setText("Characters inserted: " + chars_inserted);
                  }

                  @Override
                  public void removeUpdate(DocumentEvent e) {
                        status_label.setText("*editing...");
                        status_label.setForeground(Color.ORANGE);

                        tab_pane.setTitleAt(1, "*makefile");
                        tab_pane.setForegroundAt(1, Color.ORANGE);

                        verified = false;

                        chars_inserted -= e.getLength();
                        char_ins_label.setText("Characters inserted: " + chars_inserted);
                  }

                  @Override
                  public void changedUpdate(DocumentEvent e) {
                        status_label.setText("*editing...");
                        status_label.setForeground(Color.ORANGE);

                        tab_pane.setTitleAt(1, "*makefile");
                        tab_pane.setForegroundAt(1, Color.ORANGE);

                        verified = false;
                  }
            };
            mkfl_c_listener = new CaretListener() {
                  @Override
                  public void caretUpdate(CaretEvent e) {
                        try {
                              int caret_pos = mkfl_editing_pane.getCaretPosition();
                              int row_num = (caret_pos == 0) ? 1 : 0;
                              for (int offset = caret_pos; offset > 0;) {
                                    offset = Utilities.getRowStart(mkfl_editing_pane, offset) - 1;
                                    row_num++;
                              }
                              int offset = Utilities.getRowStart(mkfl_editing_pane, caret_pos);
                              int col_num = caret_pos - offset + 1;

                              row_col_label.setText(row_num + ":" + col_num);
                        } catch (BadLocationException ex) {
                              System.err.println(ex.getMessage());
                        }
                  }
            };
            mkfl_f_listener = new FocusListener() {
                  @Override
                  public void focusGained(FocusEvent e) {
                        chars_inserted = mkfl_editing_pane.getText().length();
                        char_ins_label.setText("Characters inserted: " + chars_inserted);
                        info_label.setText(" Font Size: " + mkfl_editing_pane.getFont().getSize());
                  }

                  @Override
                  public void focusLost(FocusEvent e) {
                  }
            };

            if (os.equals("windows")) {
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
            keyword_color = "0x1e06c2, ";
            keyword2_color = "0x1e06c2, ";
            number_color = "0xbc2b13, ";
            string_color = "0xfaba12, ";
            type_color = "0x1e06c2, ";

            c_editor_kit = new CSyntaxKit();
            c_editor_kit.setProperty("Style.KEYWORD", keyword_color + "1");
            c_editor_kit.setProperty("Style.KEYWORD2", keyword2_color + "1");
            c_editor_kit.setProperty("Style.NUMBER", number_color + "1");
            c_editor_kit.setProperty("Style.STRING", string_color + "2");
            c_editor_kit.setProperty("Style.TYPE", type_color + "1");

            editing_pane.setEditorKit(c_editor_kit);
            editing_pane.addFocusListener(f_listener);
            editing_pane.addCaretListener(c_listener);
            editing_pane.getDocument().addDocumentListener(listener);
            editing_pane.requestFocus();

            def_font_item.setText(def_font_item.getText() + " (" + default_font_size + ")");
            inc_font_item.setText(inc_font_item.getText() + " (" + (default_font_size + 1) + ")");
            dec_font_item.setText(dec_font_item.getText() + " (" + (default_font_size - 1) + ")");

            if (arguments.length > 0) {
                  try {
                        file_to_open = new File(arguments[0]);

                        String parent = file_to_open.getParentFile().getName();
                        File[] files = file_to_open.getParentFile().listFiles();

                        for (File f : files) {
                              if (f.getName().toLowerCase().equals("makefile")) {
                                    make = true;
                                    break;
                              }
                        }

                        if (file_to_open.getAbsolutePath().charAt(file_to_open.getAbsolutePath().length() - 2) == '.'
                                && file_to_open.getAbsolutePath().charAt(file_to_open.getAbsolutePath().length() - 1) == 'c') {
                              cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
                              parentPath = "\"" + file_to_open.getParent() + "\"";

                              editing_pane.setText(null);
                              Scanner scan = new Scanner(file_to_open);
                              String text = "";
                              while (scan.hasNext()) {
                                    text += scan.nextLine() + "\n";
                              }

                              editing_pane.setText(text);
                              if (editing_pane.getText().length() > 0) {
                                    editing_pane.setText(editing_pane.getText().substring(0, editing_pane.getText().length() - 1));
                              } else {
                                    editing_pane.setText(null);
                              }

                              sketch_name = file_to_open.getName();
                              tab_pane.setTitleAt(0, sketch_name);
                              tab_pane.setForegroundAt(0, Color.BLACK);

                              chose_file = true;
                        } else {
                              JOptionPane.showMessageDialog(this, "AVR-Studio can only open its own sketches and other files ending in .c",
                                      "Bad file selected", JOptionPane.WARNING_MESSAGE);
                              JOptionPane.showMessageDialog(this, "AVR-Studio will no exit",
                                      "Exiting...", JOptionPane.INFORMATION_MESSAGE);
                              System.exit(0);

                        }

                        save_method();

                        if (make) {
                              mkfl_build_item.setSelected(true);
                              gen_makefile.setEnabled(true);
                              tab_pane.add("makefile", mkfl_editing_scroll_pane);
                              tab_pane.setSelectedIndex(1);

                              mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
                              mkfl_editing_pane.addFocusListener(mkfl_f_listener);
                              mkfl_editing_pane.addCaretListener(mkfl_c_listener);
                              mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);

                              makefile = os.equals("windows") ? new File(file_to_open.getParent() + "\\makefile") : new File(file_to_open.getParent() + "/makefile");
                              Scanner scan = new Scanner(makefile);
                              String text = "";

                              boolean found = false;
                              while (scan.hasNext()) {
                                    String line = scan.nextLine();
                                    if (line.contains("-mmcu")) {
                                          mmcu = line.split("-mmcu=")[1].split(" ")[0];
                                          for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                                                if (mcuCombo.getItemAt(i).toString().toLowerCase().equals(mmcu.toLowerCase())) {
                                                      mcuCombo.setSelectedIndex(i);
                                                      found = true;
                                                }
                                          }
                                    }

                                    text += line + "\n";
                              }
                              mkfl_editing_pane.setText(text);
                              selected_tab = 1;

                              if (!found) {
                                    String variable_name = mkfl_editing_pane.getText().split("-mmcu=")[1].split(" ")[0].replace("$(", "").replace(")", "");
                                    String found_mmcu = mkfl_editing_pane.getText().split(variable_name + "=")[1].split("\n")[0];

                                    if (!found_mmcu.toLowerCase().equals(mmcu.toLowerCase())) {
                                          mmcu = found_mmcu;

                                          for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                                                if (mcuCombo.getItemAt(i).toString().toLowerCase().equals(mmcu.toLowerCase())) {
                                                      mcuCombo.setSelectedIndex(i);
                                                }
                                          }
                                    }
                              }

                              save_all_method();
                              mkfl_editing_pane.requestFocus();
                        }
                  } catch (FileNotFoundException ex) {
                        System.err.println(ex.getMessage());
                  }
            } else {
                  dateFormat = new SimpleDateFormat("dd-MMM-YYYY hh:mm:ss a");
                  date = dateFormat.format(Calendar.getInstance().getTime());

                  editing_pane.setText(
                          "/**\n"
                          + "* author: " + username + "\n"
                          + "* date: " + date + "\n"
                          + "* blinky: toggles PORTB pins on and off every 150ms\n"
                          + "*/\n\n"
                          + "#define F_CPU 16000000UL\n"
                          + "#include <avr/io.h>\n"
                          + "#include <avr/interrupt.h>\n"
                          + "#include <util/delay.h>\n\n"
                          + "int main() {\n"
                          + "\tDDRB = 0xff;\n"
                          + "\twhile(1) {\n"
                          + "\t\t//write your code here\n"
                          + "\t\tPORTB ^= 0xff;\n"
                          + "\t\t_delay_ms(150);\n"
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
            build_options_button_group = new javax.swing.ButtonGroup();
            mkfl_editing_scroll_pane = new javax.swing.JScrollPane();
            mkfl_editing_pane = new javax.swing.JEditorPane();
            pref_frame = new javax.swing.JFrame();
            pref_category_scroll_pane = new javax.swing.JScrollPane();
            pref_category_list = new javax.swing.JList();
            pref_cancel_btn = new javax.swing.JButton();
            pref_apply_btn = new javax.swing.JButton();
            pref_ok_btn = new javax.swing.JButton();
            pref_export_btn = new javax.swing.JButton();
            pref_import_btn = new javax.swing.JButton();
            pref_preview_scroll_pane = new javax.swing.JScrollPane();
            pref_preview_editing_pane = new javax.swing.JEditorPane();
            pref_preview_label = new javax.swing.JLabel();
            pref_font_label = new javax.swing.JLabel();
            pref_font_txt_fld = new javax.swing.JTextField();
            pref_font_btn = new javax.swing.JButton();
            pref_style_btn = new javax.swing.JButton();
            pref_style_label = new javax.swing.JLabel();
            pref_style_txt_fld = new javax.swing.JTextField();
            pref_color_btn = new javax.swing.JButton();
            pref_color_txt_fld = new javax.swing.JTextField();
            pref_color_label = new javax.swing.JLabel();
            toolbar = new javax.swing.JToolBar();
            verify_button = new javax.swing.JButton();
            upload_button = new javax.swing.JButton();
            search_field = new javax.swing.JTextField();
            mcuCombo = new javax.swing.JComboBox();
            info_label = new javax.swing.JLabel();
            status_label = new javax.swing.JLabel();
            iteration_label = new javax.swing.JLabel();
            tab_pane = new javax.swing.JTabbedPane();
            split_pane = new javax.swing.JSplitPane();
            console_scroll_pane = new javax.swing.JScrollPane();
            console_pane = new javax.swing.JTextPane();
            editing_scroll_pane = new javax.swing.JScrollPane();
            editing_pane = new javax.swing.JEditorPane();
            char_ins_label = new javax.swing.JLabel();
            row_col_label = new javax.swing.JLabel();
            menuBar = new javax.swing.JMenuBar();
            file_menu = new javax.swing.JMenu();
            new_menu = new javax.swing.JMenu();
            new_file_item = new javax.swing.JMenuItem();
            openMenuItem = new javax.swing.JMenuItem();
            save_menu_item = new javax.swing.JMenuItem();
            save_as_menu_item = new javax.swing.JMenuItem();
            pref_menu_item = new javax.swing.JMenuItem();
            about_menu_item = new javax.swing.JMenuItem();
            exit_menu_item = new javax.swing.JMenuItem();
            tools_menu = new javax.swing.JMenu();
            verify_menu_item = new javax.swing.JMenuItem();
            upload_menu_item = new javax.swing.JMenuItem();
            prog_options_menu = new javax.swing.JMenu();
            usbasp_item = new javax.swing.JCheckBoxMenuItem();
            arduino_item = new javax.swing.JCheckBoxMenuItem();
            stk500v1_item = new javax.swing.JCheckBoxMenuItem();
            port_menu = new javax.swing.JMenu();
            view_menu = new javax.swing.JMenu();
            font_menu = new javax.swing.JMenu();
            choose_font_item = new javax.swing.JMenuItem();
            font_menu_sep = new javax.swing.JPopupMenu.Separator();
            def_font_item = new javax.swing.JMenuItem();
            inc_font_item = new javax.swing.JMenuItem();
            dec_font_item = new javax.swing.JMenuItem();
            build_opts_menu = new javax.swing.JMenu();
            std_build_item = new javax.swing.JCheckBoxMenuItem();
            mkfl_build_item = new javax.swing.JCheckBoxMenuItem();
            build_menu_sep = new javax.swing.JPopupMenu.Separator();
            gen_makefile = new javax.swing.JMenuItem();

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
                  .addComponent(privacy_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
            );
            privacy_dialogLayout.setVerticalGroup(
                  privacy_dialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(privacy_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            );

            mkfl_editing_scroll_pane.setViewportView(mkfl_editing_pane);

            pref_frame.setTitle("Preferences");
            pref_frame.setResizable(false);

            pref_category_list.setBorder(javax.swing.BorderFactory.createTitledBorder("Category:"));
            pref_category_list.setModel(new javax.swing.AbstractListModel() {
                  String[] strings = { "Keywords", "Preprocessor Keywords", "Numbers", "Strings", "Comments", "Types", "Operators", "Identifiers" };
                  public int getSize() { return strings.length; }
                  public Object getElementAt(int i) { return strings[i]; }
            });
            pref_category_scroll_pane.setViewportView(pref_category_list);

            pref_cancel_btn.setText("Cancel");
            pref_cancel_btn.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        pref_cancel_btnActionPerformed(evt);
                  }
            });

            pref_apply_btn.setText("Apply");

            pref_ok_btn.setText("Ok");

            pref_export_btn.setText("Export");

            pref_import_btn.setText("Import");

            pref_preview_editing_pane.setEditable(false);
            pref_preview_scroll_pane.setViewportView(pref_preview_editing_pane);

            pref_preview_label.setText("Preview:");

            pref_font_label.setText("Font:");

            pref_font_txt_fld.setEditable(false);

            pref_font_btn.setText("...");
            pref_font_btn.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        pref_font_btnActionPerformed(evt);
                  }
            });

            pref_style_btn.setText("...");

            pref_style_label.setText("Style:");

            pref_style_txt_fld.setEditable(false);

            pref_color_btn.setText("...");
            pref_color_btn.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        pref_color_btnActionPerformed(evt);
                  }
            });

            pref_color_txt_fld.setEditable(false);

            pref_color_label.setText("Color:");

            javax.swing.GroupLayout pref_frameLayout = new javax.swing.GroupLayout(pref_frame.getContentPane());
            pref_frame.getContentPane().setLayout(pref_frameLayout);
            pref_frameLayout.setHorizontalGroup(
                  pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(pref_frameLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pref_frameLayout.createSequentialGroup()
                                    .addComponent(pref_export_btn)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pref_import_btn)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pref_ok_btn)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pref_apply_btn)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pref_cancel_btn))
                              .addComponent(pref_preview_scroll_pane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                              .addGroup(pref_frameLayout.createSequentialGroup()
                                    .addComponent(pref_font_label)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pref_font_txt_fld)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pref_font_btn))
                              .addGroup(pref_frameLayout.createSequentialGroup()
                                    .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                          .addComponent(pref_preview_label)
                                          .addGroup(pref_frameLayout.createSequentialGroup()
                                                .addComponent(pref_category_scroll_pane, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pref_frameLayout.createSequentialGroup()
                                                            .addComponent(pref_color_label)
                                                            .addGap(18, 18, 18))
                                                      .addGroup(pref_frameLayout.createSequentialGroup()
                                                            .addComponent(pref_style_label)
                                                            .addGap(22, 22, 22)))
                                                .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                      .addGroup(pref_frameLayout.createSequentialGroup()
                                                            .addComponent(pref_style_txt_fld)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(pref_style_btn))
                                                      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pref_frameLayout.createSequentialGroup()
                                                            .addComponent(pref_color_txt_fld, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(pref_color_btn)))))
                                    .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
            );
            pref_frameLayout.setVerticalGroup(
                  pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pref_frameLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                              .addGroup(pref_frameLayout.createSequentialGroup()
                                    .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                          .addComponent(pref_font_label)
                                          .addComponent(pref_font_txt_fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addComponent(pref_font_btn))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pref_category_scroll_pane, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                              .addGroup(pref_frameLayout.createSequentialGroup()
                                    .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                          .addComponent(pref_color_label)
                                          .addComponent(pref_color_txt_fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addComponent(pref_color_btn))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                          .addComponent(pref_style_label)
                                          .addComponent(pref_style_txt_fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addComponent(pref_style_btn))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pref_preview_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pref_preview_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                              .addComponent(pref_cancel_btn)
                              .addComponent(pref_apply_btn)
                              .addComponent(pref_ok_btn)
                              .addComponent(pref_export_btn)
                              .addComponent(pref_import_btn))
                        .addContainerGap())
            );

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            setTitle("AVR Studio");
            setMinimumSize(new java.awt.Dimension(525, 650));
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

            toolbar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            toolbar.setEnabled(false);

            verify_button.setText("verify");
            verify_button.setFocusable(false);
            verify_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            verify_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            verify_button.addMouseListener(new java.awt.event.MouseAdapter() {
                  public void mouseEntered(java.awt.event.MouseEvent evt) {
                        verify_buttonMouseEntered(evt);
                  }
                  public void mouseExited(java.awt.event.MouseEvent evt) {
                        verify_buttonMouseExited(evt);
                  }
            });
            verify_button.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        verify_buttonActionPerformed(evt);
                  }
            });
            toolbar.add(verify_button);

            upload_button.setText("upload");
            upload_button.setFocusable(false);
            upload_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            upload_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            upload_button.addMouseListener(new java.awt.event.MouseAdapter() {
                  public void mouseEntered(java.awt.event.MouseEvent evt) {
                        upload_buttonMouseEntered(evt);
                  }
                  public void mouseExited(java.awt.event.MouseEvent evt) {
                        upload_buttonMouseExited(evt);
                  }
            });
            upload_button.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        upload_buttonActionPerformed(evt);
                  }
            });
            toolbar.add(upload_button);

            search_field.setForeground(new java.awt.Color(180, 180, 180));
            search_field.setText("Find microcontrollers");
            search_field.setToolTipText("Search microcontrollers");
            search_field.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
            search_field.setPreferredSize(new java.awt.Dimension(200, 27));
            search_field.addFocusListener(new java.awt.event.FocusAdapter() {
                  public void focusGained(java.awt.event.FocusEvent evt) {
                        search_fieldFocusGained(evt);
                  }
                  public void focusLost(java.awt.event.FocusEvent evt) {
                        search_fieldFocusLost(evt);
                  }
            });
            search_field.addMouseListener(new java.awt.event.MouseAdapter() {
                  public void mouseEntered(java.awt.event.MouseEvent evt) {
                        search_fieldMouseEntered(evt);
                  }
                  public void mouseExited(java.awt.event.MouseEvent evt) {
                        search_fieldMouseExited(evt);
                  }
            });
            search_field.addKeyListener(new java.awt.event.KeyAdapter() {
                  public void keyTyped(java.awt.event.KeyEvent evt) {
                        search_fieldKeyTyped(evt);
                  }
            });
            toolbar.add(search_field);

            mcuCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "avr2", "at90s2313", "at90s2323", "at90s2333", "at90s2343", "attiny22", "attiny26", "at90s4414", "at90s4433", "at90s4434", "at90s8515", "at90c8534", "at90s8535", "avr25", "ata6289", "attiny13", "attiny13a", "attiny2313", "attiny2313a", "attiny24", "attiny24a", "attiny4313", "attiny44", "attiny44a", "attiny84", "attiny25", "attiny45", "attiny85", "attiny261", "attiny261a", "attiny461", "attiny461a", "attiny861", "attiny861a", "attiny43u", "attiny87", "attiny48", "attiny88", "at86rf401", "avr3", "at43usb320", "at43usb355", "at76c711", "avr31", "atmega103", "avr35", "at90usb82", "at90usb162", "atmega8u2", "atmega16u2", "atmega32u2", "attiny167", "avr4", "atmega8", "atmega48", "atmega48a", "atmega48p", "atmega88", "atmega88a", "atmega88p", "atmega88pa", "atmega8515", "atmega8535", "atmega8hva", "atmega4hvd", "atmega8hvd", "at90pwm1", "at90pwm2", "at90pwm2b", "at90pwm3", "at90pwm3b", "at90pwm81", "avr5", "atmega16", "atmega16a", "atmega161", "atmega162", "atmega163", "atmega164a", "atmega164p", "atmega165", "atmega165a", "atmega165p", "atmega168", "atmega168a", "atmega168p", "atmega169", "atmega169a", "atmega169p", "atmega169pa", "atmega16c1", "atmega16hva", "atmega16hva2", "atmega16hvb", "atmega16m1", "atmega16u4", "atmega32", "atmega323", "atmega324a", "atmega324p", "atmega324pa", "atmega325", "atmega325p", "atmega3250", "atmega3250p", "atmega328", "atmega328p", "atmega329", "atmega329p", "atmega329pa", "atmega3290", "atmega3290p", "atmega32c1", "atmega32hvb", "atmega32m1", "atmega32u4", "atmega32u6", "atmega406", "atmega64", "atmega640", "atmega644", "atmega644a", "atmega644p", "atmega644pa", "atmega645", "atmega645a", "atmega645p", "atmega6450", "atmega6450a", "atmega6450p", "atmega649", "atmega649a", "atmega649p", "atmega6490", "atmega6490a", "atmega6490p", "atmega64c1", "atmega64m1", "atmega64hve", "at90can32", "at90can64", "at90pwm216", "at90pwm316", "at90scr100", "at90usb646", "at90usb647", "at94k", "avr51", "atmega128", "atmega1280", "atmega1281", "atmega1284p", "atmega128rfa1", "at90can128", "at90usb1286", "at90usb1287", "m3000f", "m3000s", "m3001b", "avr6", "atmega2560", "atmega2561", "avrxmega2", "atxmega16a4", "atxmega16d4", "atxmega32d4", "avrxmega3", "atxmega32a4", "avrxmega4", "atxmega64a3", "atxmega64d3", "avrxmega5", "atxmega64a1", "avrxmega6", "atxmega128a3", "atxmega128d3", "atxmega192a3", "atxmega192d3", "atxmega256a3", "atxmega256a3b", "atxmega256d3", "avrxmega7", "atxmega128a1", "avr1", "at90s1200", "attiny11", "attiny12", "attiny15", "attiny28" }));
            mcuCombo.setPreferredSize(new java.awt.Dimension(200, 27));
            mcuCombo.addItemListener(new java.awt.event.ItemListener() {
                  public void itemStateChanged(java.awt.event.ItemEvent evt) {
                        mcuComboItemStateChanged(evt);
                  }
            });
            mcuCombo.addMouseListener(new java.awt.event.MouseAdapter() {
                  public void mouseExited(java.awt.event.MouseEvent evt) {
                        mcuComboMouseExited(evt);
                  }
                  public void mouseEntered(java.awt.event.MouseEvent evt) {
                        mcuComboMouseEntered(evt);
                  }
            });
            toolbar.add(mcuCombo);

            info_label.setText(" Font Size: 15");
            toolbar.add(info_label);

            status_label.setForeground(new java.awt.Color(1, 1, 1));
            status_label.setText("Status");

            iteration_label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            iteration_label.setText("Iteration: 3,513");

            tab_pane.addMouseListener(new java.awt.event.MouseAdapter() {
                  public void mouseClicked(java.awt.event.MouseEvent evt) {
                        tab_paneMouseClicked(evt);
                  }
            });

            split_pane.setDividerLocation(370);
            split_pane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            split_pane.setContinuousLayout(true);

            console_pane.setEditable(false);
            console_pane.setBackground(new java.awt.Color(1, 1, 1));
            console_pane.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
            console_pane.setForeground(new java.awt.Color(254, 254, 254));
            console_pane.setCaretColor(new java.awt.Color(254, 254, 254));
            console_pane.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
            console_scroll_pane.setViewportView(console_pane);

            split_pane.setRightComponent(console_scroll_pane);

            editing_scroll_pane.setViewportView(editing_pane);

            split_pane.setLeftComponent(editing_scroll_pane);

            tab_pane.addTab("TAB", split_pane);

            char_ins_label.setText("Characters Inserted: 0");

            row_col_label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            row_col_label.setText("0:0");

            file_menu.setText("File");

            new_menu.setText("New");

            new_file_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
            new_file_item.setText("File");
            new_file_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        new_file_itemActionPerformed(evt);
                  }
            });
            new_menu.add(new_file_item);

            file_menu.add(new_menu);

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

            save_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
            save_menu_item.setMnemonic('s');
            save_menu_item.setText("Save");
            save_menu_item.setToolTipText("");
            save_menu_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        save_menu_itemActionPerformed(evt);
                  }
            });
            file_menu.add(save_menu_item);

            save_as_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
            save_as_menu_item.setMnemonic('a');
            save_as_menu_item.setText("Save As");
            save_as_menu_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        save_as_menu_itemActionPerformed(evt);
                  }
            });
            file_menu.add(save_as_menu_item);

            pref_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
            pref_menu_item.setText("Preferences");
            pref_menu_item.setEnabled(false);
            pref_menu_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        pref_menu_itemActionPerformed(evt);
                  }
            });
            file_menu.add(pref_menu_item);

            about_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
            about_menu_item.setMnemonic('a');
            about_menu_item.setText("About");
            about_menu_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        about_menu_itemActionPerformed(evt);
                  }
            });
            file_menu.add(about_menu_item);

            exit_menu_item.setMnemonic('e');
            exit_menu_item.setText("Exit");
            exit_menu_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        exit_menu_itemActionPerformed(evt);
                  }
            });
            file_menu.add(exit_menu_item);

            menuBar.add(file_menu);

            tools_menu.setText("Tools");

            verify_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, java.awt.event.InputEvent.SHIFT_MASK));
            verify_menu_item.setText("Verify");
            verify_menu_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        verify_menu_itemActionPerformed(evt);
                  }
            });
            tools_menu.add(verify_menu_item);

            upload_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, java.awt.event.InputEvent.SHIFT_MASK));
            upload_menu_item.setText("Upload");
            upload_menu_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        upload_menu_itemActionPerformed(evt);
                  }
            });
            tools_menu.add(upload_menu_item);

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

            choose_font_item.setText("Choose font");
            choose_font_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        choose_font_itemActionPerformed(evt);
                  }
            });
            font_menu.add(choose_font_item);
            font_menu.add(font_menu_sep);

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

            build_opts_menu.setText("Build Options");

            build_options_button_group.add(std_build_item);
            std_build_item.setSelected(true);
            std_build_item.setText("Use standard build options");
            std_build_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        std_build_itemActionPerformed(evt);
                  }
            });
            build_opts_menu.add(std_build_item);

            build_options_button_group.add(mkfl_build_item);
            mkfl_build_item.setText("Use a makefile to build the project");
            mkfl_build_item.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        mkfl_build_itemActionPerformed(evt);
                  }
            });
            build_opts_menu.add(mkfl_build_item);
            build_opts_menu.add(build_menu_sep);

            gen_makefile.setText("Generate makefile contents");
            gen_makefile.setEnabled(false);
            gen_makefile.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                        gen_makefileActionPerformed(evt);
                  }
            });
            build_opts_menu.add(gen_makefile);

            menuBar.add(build_opts_menu);

            setJMenuBar(menuBar);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(tab_pane)
                  .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addGroup(layout.createSequentialGroup()
                                    .addComponent(status_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(18, 18, 18)
                                    .addComponent(iteration_label, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                              .addGroup(layout.createSequentialGroup()
                                    .addComponent(char_ins_label, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(row_col_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())
            );
            layout.setVerticalGroup(
                  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                              .addComponent(status_label, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addComponent(iteration_label))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tab_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                              .addComponent(row_col_label)
                              .addComponent(char_ins_label))
                        .addContainerGap())
            );

            pack();
      }// </editor-fold>//GEN-END:initComponents

      private void save_as_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_as_menu_itemActionPerformed
            save_as_method();
      }//GEN-LAST:event_save_as_menu_itemActionPerformed

      private void save_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_menu_itemActionPerformed
            if (chose_file) {
                  save_method();
            } else {
                  save_as_method();
            }
      }//GEN-LAST:event_save_menu_itemActionPerformed

      private void verify_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verify_buttonActionPerformed
            tab_pane.setSelectedIndex(0);
            if (file_to_open == null || !file_to_open.exists()) {
                  PrintWriter writer = null;
                  try {
                        temporary = true;
                        String temp_folder = tab_pane.getTitleAt(0).replace("*", "").replace(".c", "");

                        if (std_build_item.isSelected()) {
                              if (os.equals("windows")) {
                                    File x = new File("C:\\Users\\" + username + "\\AppData\\Local\\Temp\\" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "\\" + tab_pane.getTitleAt(0).replace("*", ""));
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();
                                    compile_file();
                              } else {
                                    File x = new File("/home/" + username + "/.avr_studio_temp");
                                    x.mkdir();
                                    x = new File("/home/" + username + "/.avr_studio_temp/" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "/" + tab_pane.getTitleAt(0).replace("*", ""));
                                    file_to_open.createNewFile();
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();
                                    compile_file();
                              }
                              chose_file = true;
                        } else if (mkfl_build_item.isSelected()) {
                              if (os.equals("windows")) {
                                    File x = new File("C:\\Users\\" + username + "\\AppData\\Local\\Temp\\" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "\\" + tab_pane.getTitleAt(0).replace("*", ""));
                                    makefile = new File(x.getPath() + "/makefile");
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();

                                    writer = new PrintWriter(makefile);
                                    writer.println(mkfl_editing_pane.getText().replaceAll("    ", "\t"));
                                    writer.close();
                                    compile_file();
                              } else {
                                    File x = new File("/home/" + username + "/.avr_studio_temp");
                                    x.mkdir();
                                    x = new File("/home/" + username + "/.avr_studio_temp/" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "/" + tab_pane.getTitleAt(0).replace("*", ""));
                                    file_to_open.createNewFile();
                                    makefile = new File(x.getPath() + "/makefile");
                                    makefile.createNewFile();
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();

                                    writer = new PrintWriter(makefile);
                                    writer.println(mkfl_editing_pane.getText().replaceAll("    ", "\t"));
                                    writer.close();
                                    compile_file();
                              }
                              chose_file = true;
                        }
                  } catch (FileNotFoundException ex) {
                        System.err.println(ex.toString());
                  } catch (IOException ex) {
                        System.err.println(ex.toString());
                  } finally {
                        writer.close();
                  }

            } else {
                  compile_file();
            }
      }//GEN-LAST:event_verify_buttonActionPerformed

      private void upload_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upload_buttonActionPerformed
            tab_pane.setSelectedIndex(0);
            if (file_to_open == null || !file_to_open.exists()) {
                  PrintWriter writer = null;
                  try {
                        temporary = true;
                        String temp_folder = tab_pane.getTitleAt(0).replace("*", "").replace(".c", "");

                        if (std_build_item.isSelected()) {
                              if (os.equals("windows")) {
                                    File x = new File("C:\\Users\\" + username + "\\AppData\\Local\\Temp\\" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "\\" + tab_pane.getTitleAt(0).replace("*", ""));
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();
                                    compile_file();
                                    upload_hex();
                              } else {
                                    File x = new File("/home/" + username + "/.avr_studio_temp");
                                    x.mkdir();
                                    x = new File("/home/" + username + "/.avr_studio_temp/" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "/" + tab_pane.getTitleAt(0).replace("*", ""));
                                    file_to_open.createNewFile();
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();
                                    compile_file();
                                    upload_hex();
                              }
                              chose_file = true;
                        } else if (mkfl_build_item.isSelected()) {
                              if (os.equals("windows")) {
                                    File x = new File("C:\\Users\\" + username + "\\AppData\\Local\\Temp\\" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "\\" + tab_pane.getTitleAt(0).replace("*", ""));
                                    makefile = new File(x.getPath() + "/makefile");
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();

                                    writer = new PrintWriter(makefile);
                                    writer.println(mkfl_editing_pane.getText().replaceAll("    ", "\t"));
                                    writer.close();
                                    compile_file();
                                    upload_hex();
                              } else {
                                    File x = new File("/home/" + username + "/.avr_studio_temp");
                                    x.mkdir();
                                    x = new File("/home/" + username + "/.avr_studio_temp/" + temp_folder);
                                    x.mkdir();
                                    file_to_open = new File(x.getPath() + "/" + tab_pane.getTitleAt(0).replace("*", ""));
                                    file_to_open.createNewFile();
                                    makefile = new File(x.getPath() + "/makefile");
                                    makefile.createNewFile();
                                    temporary_file_to_open = file_to_open;
                                    writer = new PrintWriter(file_to_open);
                                    writer.println(editing_pane.getText());
                                    writer.close();

                                    writer = new PrintWriter(makefile);
                                    writer.println(mkfl_editing_pane.getText().replaceAll("    ", "\t"));
                                    writer.close();
                                    compile_file();
                                    upload_hex();
                              }
                              chose_file = true;
                        }
                  } catch (FileNotFoundException ex) {
                        System.err.println(ex.toString());
                  } catch (IOException ex) {
                        System.err.println(ex.toString());
                  } finally {
                        writer.close();
                  }

            } else {
                  upload_hex();
            }
      }//GEN-LAST:event_upload_buttonActionPerformed

      private void search_fieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_search_fieldKeyTyped
            String s = "" + evt.getKeyChar();
            Pattern regex = Pattern.compile("[~`!@#$%^&*()_+=;'\"/|.,><-{}A-Za-z0-9]");
            Matcher matcher = regex.matcher(s);
            if (matcher.find()) {
                  for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                        if (mcuCombo.getItemAt(i).toString().toLowerCase().contains(search_field.getText().toLowerCase())) {
                              mcuCombo.setSelectedIndex(i);
                              mmcu = mcuCombo.getSelectedItem().toString();
                        }
                  }
            } else {
                  for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                        if (mcuCombo.getItemAt(i).toString().toLowerCase().equals(search_field.getText().toLowerCase())) {
                              mcuCombo.setSelectedIndex(i);
                              mmcu = mcuCombo.getSelectedItem().toString();
                        }
                  }
            }

            if (evt.getKeyChar() == 8) {   //backspace
                  if (search_field.getText().length() < 1) {
                        search_field.setForeground(new Color(180, 180, 180));
                        search_field.setText("Find microcontrollers");
                        mcuCombo.requestFocus();
                  }
            }
      }//GEN-LAST:event_search_fieldKeyTyped

      private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
            FileDialog fd = new FileDialog(this, "Open...", FileDialog.LOAD);
            fd.setTitle("Open...");
            fd.setVisible(true);
            String selected = fd.getDirectory() + fd.getFile();

            try {
                  File x = new File(selected);
                  String parent = x.getParentFile().getName();
                  File[] files = x.getParentFile().listFiles();

                  for (File f : files) {
                        if (f.getName().toLowerCase().equals("makefile")) {
                              make = true;
                              break;
                        }
                  }

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

                                                      String[] cmd = (os.equals("windows"))
                                                              ? new String[]{"cmd", "/c", "rm -rf " + temporary_file_to_open.getParentFile()}
                                                              : new String[]{"/bin/sh", "-c", "rm -rf " + temporary_file_to_open.getParentFile()};
                                                      System.out.println(cmd[2]);
                                                      new ProcessBuilder(cmd).start();

                                                } catch (IOException ex) {
                                                      System.err.println(ex.toString());
                                                }
                                          }
                                          x = os.equals("windows") ? new File(x.getParent() + "\\" + x.getName().replace(".c", "")) : new File(x.getParent() + "/" + x.getName().replace(".c", ""));
                                          x.mkdir();

                                          String[] cmd = (os.equals("windows"))
                                                  ? new String[]{"cmd", "/c", "mv " + selected + " " + x.getPath() + "\\" + fd.getFile()}
                                                  : new String[]{"/bin/sh", "-c", "mv " + selected + " " + x.getPath() + "/" + fd.getFile()};
                                          System.out.println(cmd[2]);

                                          new ProcessBuilder(cmd).start();
                                          file_to_open = os.equals("windows") ? new File(x.getPath() + "\\" + fd.getFile()) : new File(x.getPath() + "/" + fd.getFile());
                                          cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
                                          parentPath = "\"" + file_to_open.getParent() + "\"";

                                          editing_pane.setText(null);
                                          Thread.sleep(50);
                                          Scanner scan = new Scanner(file_to_open);
                                          String text = "";
                                          while (scan.hasNext()) {
                                                text += scan.nextLine() + "\n";
                                          }
                                          editing_pane.setText(text);
                                          if (editing_pane.getText().length() > 0) {
                                                editing_pane.setText(editing_pane.getText().substring(0, editing_pane.getText().length() - 1));
                                          } else {
                                                editing_pane.setText(null);
                                          }

                                          sketch_name = file_to_open.getName();
                                          tab_pane.setTitleAt(0, sketch_name);
                                          tab_pane.setForegroundAt(0, Color.BLACK);
                                          chose_file = true;
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

                                                String[] cmd = (os.equals("windows"))
                                                        ? new String[]{"cmd", "/c", "rm -rf " + temporary_file_to_open.getParentFile()}
                                                        : new String[]{"/bin/sh", "-c", "rm -rf " + temporary_file_to_open.getParentFile()};
                                                System.out.println(cmd[2]);
                                                new ProcessBuilder(cmd).start();

                                          } catch (IOException ex) {
                                                System.err.println(ex.toString());
                                          }
                                    }
                                    file_to_open = fd.getFiles()[0];
                                    cPath = "\"" + file_to_open.getAbsolutePath() + "\"";
                                    parentPath = "\"" + file_to_open.getParent() + "\"";

                                    editing_pane.setText(null);
                                    Scanner scan = new Scanner(file_to_open);
                                    String text = "";
                                    while (scan.hasNext()) {
                                          text += scan.nextLine() + "\n";
                                    }
                                    editing_pane.setText(text);
                                    if (editing_pane.getText().length() > 0) {
                                          editing_pane.setText(editing_pane.getText().substring(0, editing_pane.getText().length() - 1));
                                    } else {
                                          editing_pane.setText(null);
                                    }

                                    sketch_name = file_to_open.getName();
                                    tab_pane.setTitleAt(0, sketch_name);
                                    tab_pane.setForegroundAt(0, Color.BLACK);
                                    chose_file = true;

                              } catch (FileNotFoundException ex) {
                                    System.err.println(ex.toString());
                              }
                        }
                  }

                  if (make) {
                        mkfl_build_item.setSelected(true);
                        gen_makefile.setEnabled(true);
                        tab_pane.add("makefile", mkfl_editing_scroll_pane);
                        tab_pane.setSelectedIndex(1);

                        mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
                        mkfl_editing_pane.addFocusListener(mkfl_f_listener);
                        mkfl_editing_pane.addCaretListener(mkfl_c_listener);
                        mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);

                        makefile = os.equals("windows") ? new File(x.getParent() + "\\makefile") : new File(x.getParent() + "/makefile");
                        Scanner scan = new Scanner(makefile);
                        String text = "";
                        boolean found = false;
                        while (scan.hasNext()) {
                              String line = scan.nextLine();
                              if (line.contains("-mmcu")) {
                                    mmcu = line.split("-mmcu=")[1].split(" ")[0];
                                    for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                                          if (mcuCombo.getItemAt(i).toString().toLowerCase().equals(mmcu.toLowerCase())) {
                                                mcuCombo.setSelectedIndex(i);
                                                found = true;
                                          }
                                    }
                              }

                              text += line + "\n";
                        }

                        mkfl_editing_pane.setText(text);
                        selected_tab = 1;

                        if (!found) {
                              String variable_name = mkfl_editing_pane.getText().split("-mmcu=")[1].split(" ")[0].replace("$(", "").replace(")", "");
                              String found_mmcu = mkfl_editing_pane.getText().split(variable_name + "=")[1].split("\n")[0];

                              if (!found_mmcu.toLowerCase().equals(mmcu.toLowerCase())) {
                                    mmcu = found_mmcu;

                                    for (int i = 0; i < mcuCombo.getItemCount(); i++) {
                                          if (mcuCombo.getItemAt(i).toString().toLowerCase().equals(mmcu.toLowerCase())) {
                                                mcuCombo.setSelectedIndex(i);
                                          }
                                    }
                              }
                        }
                  }

                  save_all_method();
                  mkfl_editing_pane.requestFocus();

            } catch (NullPointerException | FileNotFoundException ex) {
                  System.err.println(ex.getMessage());
            }
      }//GEN-LAST:event_openMenuItemActionPerformed

      private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
            // maximized
            if ((evt.getNewState() & Studio.MAXIMIZED_BOTH) == Studio.MAXIMIZED_BOTH) {
                  split_pane.setResizeWeight(0.9);
            } // minimized
            else if ((evt.getNewState() & Studio.ICONIFIED) == Studio.ICONIFIED) {
                  System.out.println(evt.getID());
            }
      }//GEN-LAST:event_formWindowStateChanged

      private void about_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_about_menu_itemActionPerformed
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

            if (os.equals("windows")) {
                  privacy_text_pane.setFont(new Font("Consolas", Font.PLAIN, 15));
            }

            privacy_text_pane.setText(copy_left);
            privacy_text_pane.setCaretPosition(0);

            privacy_dialog.setLocation(getLocation().x - 40, getLocation().y + 10);
            privacy_dialog.setSize(700, 500);
            privacy_dialog.setVisible(true);
      }//GEN-LAST:event_about_menu_itemActionPerformed

      private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
            String modified_file = null;

            if (status_label.getText().contains("*")) {
                  if (tab_pane.getTabCount() > 1) {
                        if (tab_pane.getTitleAt(0).toLowerCase().contains("*") && !tab_pane.getTitleAt(1).toLowerCase().contains("*")) {
                              modified_file = tab_pane.getTitleAt(0).replace("*", "");
                        } else if (!tab_pane.getTitleAt(0).toLowerCase().contains("*") && tab_pane.getTitleAt(1).toLowerCase().contains("*")) {
                              modified_file = tab_pane.getTitleAt(1).replace("*", "");
                        } else {
                              modified_file = tab_pane.getTitleAt(0).replace("*", "") + ", " + tab_pane.getTitleAt(1).replace("*", "");
                        }
                  } else {
                        modified_file = tab_pane.getTitleAt(0).replace("*", "");
                  }
                  int result = JOptionPane.showOptionDialog(this,
                          "Changes occured in  \"" + modified_file + "\"\nDo you want to save these changes?",
                          "\"" + modified_file + "\" modified.",
                          JOptionPane.YES_NO_CANCEL_OPTION,
                          JOptionPane.QUESTION_MESSAGE,
                          null,
                          new String[]{"Save Changes", "Close Without Saving", "Cancel"},
                          "Save Changes");
                  if (result == JOptionPane.YES_OPTION) {
                        if (chose_file) {
                              save_method();
                              System.exit(0);
                        } else {
                              save_as_method();
                              if (file_to_open != null) {
                                    System.exit(0);
                              }
                        }
                  } else if (result == JOptionPane.NO_OPTION) {
                        System.exit(0);
                  }
            } else if (temporary) {
                  try {
                        String[] cmd = (os.equals("windows"))
                                ? new String[]{"cmd", "/c", "rm -rf " + temporary_file_to_open.getParentFile()}
                                : new String[]{"/bin/sh", "-c", "rm -rf " + temporary_file_to_open.getParentFile()};
                        System.out.println(cmd[2]);
                        new ProcessBuilder(cmd).start();
                        int result = JOptionPane.showOptionDialog(this,
                                "This is a temporary project (will be deleted on exit).\nDo you want to save the project in a permenant location ?",
                                "Project \"" + sketch_name.replace(".c", "") + "\" is Temporary.",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[]{"Save Project", "Close Without Saving", "Cancel"},
                                "Save Changes");
                        if (result == JOptionPane.YES_OPTION) {
                              save_as_method();
                              if (!file_to_open.getParent().toLowerCase().contains("temp")) {
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

      private void exit_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exit_menu_itemActionPerformed
            formWindowClosing(new WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING));
      }//GEN-LAST:event_exit_menu_itemActionPerformed

    private void verify_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verify_menu_itemActionPerformed
          verify_buttonActionPerformed(new ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "verify"));
    }//GEN-LAST:event_verify_menu_itemActionPerformed

    private void upload_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upload_menu_itemActionPerformed
          upload_buttonActionPerformed(new ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "upload"));
    }//GEN-LAST:event_upload_menu_itemActionPerformed

    private void mcuComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_mcuComboItemStateChanged
          mmcu = mcuCombo.getSelectedItem().toString();

          if (make) {
                try {
                      String old_mmcu = mkfl_editing_pane.getText().split("-mmcu=")[1].split(" ")[0];
                      String part = mkfl_editing_pane.getText().split("-p")[1].split(" ")[1];

                      if (!old_mmcu.contains("$")) {
                            if (!old_mmcu.toLowerCase().equals(mmcu.toLowerCase())) {
                                  mkfl_editing_pane.setText(mkfl_editing_pane.getText().replaceAll("-mmcu=" + old_mmcu, "-mmcu=" + mmcu));
                            }
                      } else {
                            String variable_name = mkfl_editing_pane.getText().split("-mmcu=")[1].split(" ")[0].replace("$(", "").replace(")", "");
                            old_mmcu = mkfl_editing_pane.getText().split(variable_name + "=")[1].split("\n")[0];
                            if (!old_mmcu.toLowerCase().equals(mmcu.toLowerCase())) {
                                  mkfl_editing_pane.setText(mkfl_editing_pane.getText().replaceAll(variable_name + "=" + old_mmcu, variable_name + "=" + mmcu));
                            }
                      }

                      if (!part.contains("$")) {
                            if (!part.toLowerCase().equals(mmcu.toLowerCase())) {
                                  mkfl_editing_pane.setText(mkfl_editing_pane.getText().replaceAll("-p " + part, "-p " + mmcu));
                            }
                      } else {
                            String variable_name = mkfl_editing_pane.getText().split("-p ")[1].split(" ")[0].replace("$(", "").replace(")", "");
                            part = mkfl_editing_pane.getText().split(variable_name + "=")[1].split("\n")[0];

                            if (!part.toLowerCase().equals(mmcu.toLowerCase())) {
                                  mkfl_editing_pane.setText(mkfl_editing_pane.getText().replaceAll(variable_name + "=" + part, variable_name + "=" + mmcu));
                            }
                      }
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                      System.err.println(aioobe.toString());
                }
          }
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
                            prog_option = "stk500v1 -b19200 -P " + currPortId.getName();
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
                            prog_option = "arduino -b57600 -P " + currPortId.getName();
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
          chars_inserted = 0;
          font_size += 1;

          if (tab_pane.getSelectedIndex() == 0) {
                String text = editing_pane.getText();
                int caret_position = editing_pane.getCaretPosition();
                config.put("DefaultFont", default_font + font_size);
                editing_pane.setEditorKit(c_editor_kit);
                editing_pane.addFocusListener(f_listener);
                editing_pane.addCaretListener(c_listener);
                editing_pane.getDocument().addDocumentListener(listener);
                editing_pane.setText(text);
                editing_pane.setCaretPosition(caret_position);
          } else if (tab_pane.getSelectedIndex() == 1) {
                String text = mkfl_editing_pane.getText();
                int caret_position = mkfl_editing_pane.getCaretPosition();
                config.put("DefaultFont", default_font + font_size);
                mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
                mkfl_editing_pane.addFocusListener(mkfl_f_listener);
                mkfl_editing_pane.addCaretListener(mkfl_c_listener);
                mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);
                mkfl_editing_pane.setText(text);
                mkfl_editing_pane.setCaretPosition(caret_position);
          }

          info_label.setText(" Font Size: " + font_size);
    }//GEN-LAST:event_inc_font_itemActionPerformed

    private void dec_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dec_font_itemActionPerformed
          chars_inserted = 0;
          font_size -= 1;

          if (tab_pane.getSelectedIndex() == 0) {
                String text = editing_pane.getText();
                int caret_position = editing_pane.getCaretPosition();
                config.put("DefaultFont", default_font + font_size);
                editing_pane.setEditorKit(c_editor_kit);
                editing_pane.addFocusListener(f_listener);
                editing_pane.addCaretListener(c_listener);
                editing_pane.getDocument().addDocumentListener(listener);
                editing_pane.setText(text);
                editing_pane.setCaretPosition(caret_position);
          } else if (tab_pane.getSelectedIndex() == 1) {
                String text = mkfl_editing_pane.getText();
                int caret_position = mkfl_editing_pane.getCaretPosition();
                config.put("DefaultFont", default_font + font_size);
                mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
                mkfl_editing_pane.addFocusListener(mkfl_f_listener);
                mkfl_editing_pane.addCaretListener(mkfl_c_listener);
                mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);
                mkfl_editing_pane.setText(text);
                mkfl_editing_pane.setCaretPosition(caret_position);
          }

          info_label.setText(" Font Size: " + font_size);
    }//GEN-LAST:event_dec_font_itemActionPerformed

    private void def_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_def_font_itemActionPerformed
          chars_inserted = 0;
          font_size = default_font_size;

          if (tab_pane.getSelectedIndex() == 0) {
                String text = editing_pane.getText();
                int caret_position = editing_pane.getCaretPosition();
                config.put("DefaultFont", default_font + font_size);
                editing_pane.setEditorKit(c_editor_kit);
                editing_pane.addFocusListener(f_listener);
                editing_pane.addCaretListener(c_listener);
                editing_pane.getDocument().addDocumentListener(listener);
                editing_pane.setText(text);
                editing_pane.setCaretPosition(caret_position);
          } else if (tab_pane.getSelectedIndex() == 1) {
                String text = mkfl_editing_pane.getText();
                int caret_position = mkfl_editing_pane.getCaretPosition();
                config.put("DefaultFont", default_font + font_size);
                mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
                mkfl_editing_pane.addFocusListener(mkfl_f_listener);
                mkfl_editing_pane.addCaretListener(mkfl_c_listener);
                mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);
                mkfl_editing_pane.setText(text);
                mkfl_editing_pane.setCaretPosition(caret_position);
          }

          info_label.setText(" Font Size: " + font_size);
    }//GEN-LAST:event_def_font_itemActionPerformed

      private void std_build_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_std_build_itemActionPerformed
            gen_makefile.setEnabled(false);
            if (tab_pane.getTabCount() > 1) {
                  tab_pane.remove(1);
            }
            make = false;
            makefile = null;
      }//GEN-LAST:event_std_build_itemActionPerformed

      private void mkfl_build_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mkfl_build_itemActionPerformed
            gen_makefile.setEnabled(true);
            if (!make) {
                  tab_pane.add("makefile", mkfl_editing_scroll_pane);
                  tab_pane.setSelectedIndex(1);

                  mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
                  mkfl_editing_pane.addFocusListener(mkfl_f_listener);
                  mkfl_editing_pane.addCaretListener(mkfl_c_listener);
                  mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);

                  if (makefile == null) {
                        sketch_name = sketch_name.replace(".c", "");
                        String upload_string = os.equals("windows") ? "    avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n"
                                : "    sudo avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n";
                        mkfl_editing_pane.setText(
                                "#when compiling you must name the compilation rule to compile\n"
                                + "#when uploading you must name the upload rule to upload\n\n"
                                + "target=" + sketch_name + "\n\n"
                                + "compile:\n"
                                + "    avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + "$(target).c" + " -o " + "$(target).o\n"
                                + "    avr-gcc -g -mmcu=" + mmcu + " -o " + "$(target).elf" + " $(target).o\n"
                                + "    avr-objcopy -j .text -j .data -O ihex " + "$(target).elf" + " $(target).hex\n"
                                + "    rm -f " + "$(target).o " + "$(target).elf\n\n"
                                + "upload:\n"
                                + upload_string);
                        sketch_name += ".c";
                        if (file_to_open != null) {
                              makefile = os.equals("windows") ? new File(file_to_open.getParent() + "\\makefile") : new File(file_to_open.getParent() + "/makefile");
                        }
                  } else {
                        try {
                              Scanner scan = new Scanner(makefile);

                              String line = "";
                              while (scan.hasNext()) {
                                    line += scan.nextLine() + "\n";
                              }

                              mkfl_editing_pane.setText(line);
                        } catch (FileNotFoundException ex) {
                              System.err.println(ex.getMessage());
                        }

                  }
                  selected_tab = 1;

                  if (temporary) {
                        PrintWriter writer;
                        String temp_folder = tab_pane.getTitleAt(0).replace("*", "").replace(".c", "");

                        try {
                              if (os.equals("windows")) {
                                    File x = new File("C:\\Users\\" + username + "\\AppData\\Local\\Temp\\" + temp_folder);
                                    makefile = new File(x.getPath() + "/makefile");

                                    writer = new PrintWriter(makefile);
                                    writer.println(mkfl_editing_pane.getText().replaceAll("    ", "\t"));
                                    writer.close();
                              } else {
                                    File x = new File("/home/" + username + "/.avr_studio_temp/" + temp_folder);
                                    makefile = new File(x.getPath() + "/makefile");
                                    makefile.createNewFile();

                                    writer = new PrintWriter(makefile);
                                    writer.println(mkfl_editing_pane.getText().replaceAll("    ", "\t"));
                                    writer.close();
                              }
                              make = true;
                        } catch (FileNotFoundException ex) {
                              System.err.println(ex.getMessage());
                        } catch (IOException ex) {
                              System.err.println(ex.getMessage());
                        }
                  }
            }

            mkfl_editing_pane.requestFocus();
      }//GEN-LAST:event_mkfl_build_itemActionPerformed

      private void tab_paneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tab_paneMouseClicked
            selected_tab = tab_pane.getSelectedIndex();

            if (selected_tab == 0) {
                  int caret_pos = editing_pane.getCaretPosition();

                  if (caret_pos != editing_pane.getText().length()) {
                        editing_pane.setCaretPosition(editing_pane.getText().length());
                        editing_pane.setCaretPosition(caret_pos);
                  } else {
                        editing_pane.setCaretPosition(0);
                        editing_pane.setCaretPosition(caret_pos);
                  }
            } else {
                  int caret_pos = mkfl_editing_pane.getCaretPosition();
                  
                  if (caret_pos != mkfl_editing_pane.getText().length()) {
                        mkfl_editing_pane.setCaretPosition(mkfl_editing_pane.getText().length());
                        mkfl_editing_pane.setCaretPosition(caret_pos);
                  } else {
                        mkfl_editing_pane.setCaretPosition(0);
                        mkfl_editing_pane.setCaretPosition(caret_pos);
                  }
            }
      }//GEN-LAST:event_tab_paneMouseClicked

      private void new_file_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_new_file_itemActionPerformed
            if (os.equals("windows")) {
                  new Thread(new Runnable() {
                        @Override
                        public void run() {
                              try {
                                    String[] cmd = {"cmd", "/c", "avr-studio"};
                                    Process p = new ProcessBuilder(cmd).start();
                              } catch (IOException ex) {
                                    System.err.println(ex.getMessage());
                              }
                        }
                  }).start();
            } else {
                  new Thread(new Runnable() {
                        @Override
                        public void run() {
                              try {
                                    String[] cmd = {"/bin/sh", "-c", "java -jar /usr/share/avr-studio/AVR-Studio.jar"};
                                    Process p = new ProcessBuilder(cmd).start();
                              } catch (IOException ex) {
                                    System.err.println(ex.getMessage());
                              }
                        }
                  }).start();
            }
      }//GEN-LAST:event_new_file_itemActionPerformed

      private void search_fieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_search_fieldFocusGained
            if (search_field.getText().toLowerCase().contains("find microcontrollers")) {
                  search_field.setForeground(new Color(76, 76, 76));
                  search_field.setText(null);
            }
      }//GEN-LAST:event_search_fieldFocusGained

      private void search_fieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_search_fieldFocusLost
            if (search_field.getText().length() < 1) {
                  search_field.setForeground(new Color(180, 180, 180));
                  search_field.setText("Find microcontrollers");
            }
      }//GEN-LAST:event_search_fieldFocusLost

      private void gen_makefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gen_makefileActionPerformed
            sketch_name = sketch_name.replace(".c", "");
            String upload_string = os.equals("windows") ? "    avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n"
                    : "    sudo avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n";
            mkfl_editing_pane.setText(
                    "#when compiling you must name the compilation rule to compile\n"
                    + "#when uploading you must name the upload rule to upload\n\n"
                    + "target=" + sketch_name + "\n\n"
                    + "compile:\n"
                    + "    avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + "$(target).c" + " -o " + "$(target).o\n"
                    + "    avr-gcc -g -mmcu=" + mmcu + " -o " + "$(target).elf" + " $(target).o\n"
                    + "    avr-objcopy -j .text -j .data -O ihex " + "$(target).elf" + " $(target).hex\n"
                    + "    rm -f " + "$(target).o " + "$(target).elf\n\n"
                    + "upload:\n"
                    + upload_string);
            sketch_name += ".c";

            mkfl_editing_pane.requestFocus();
      }//GEN-LAST:event_gen_makefileActionPerformed

    private void verify_buttonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_verify_buttonMouseEntered
          info_label.setText(verify_button.getText());
    }//GEN-LAST:event_verify_buttonMouseEntered

    private void verify_buttonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_verify_buttonMouseExited
          info_label.setText(" Font Size: " + font_size);
    }//GEN-LAST:event_verify_buttonMouseExited

    private void upload_buttonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_upload_buttonMouseEntered
          info_label.setText(upload_button.getText());
    }//GEN-LAST:event_upload_buttonMouseEntered

    private void upload_buttonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_upload_buttonMouseExited
          info_label.setText(" Font Size: " + font_size);
    }//GEN-LAST:event_upload_buttonMouseExited

    private void search_fieldMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_search_fieldMouseEntered
          info_label.setText(" search");
    }//GEN-LAST:event_search_fieldMouseEntered

    private void search_fieldMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_search_fieldMouseExited
          info_label.setText(" Font Size: " + font_size);
    }//GEN-LAST:event_search_fieldMouseExited

    private void mcuComboMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mcuComboMouseEntered
          info_label.setText(" select");
    }//GEN-LAST:event_mcuComboMouseEntered

    private void mcuComboMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mcuComboMouseExited
          info_label.setText(" Font Size: " + font_size);
    }//GEN-LAST:event_mcuComboMouseExited

    private void choose_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_choose_font_itemActionPerformed
          String[] reslut = get_font();
          default_font = reslut[0];
          default_font_size = font_size = Integer.parseInt(reslut[1]);
          int font_style = Integer.parseInt(reslut[2]);

          DefaultSyntaxKit.initKit();
          config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
          config.put("DefaultFont", default_font + default_font_size);

          c_editor_kit = new CSyntaxKit();
          c_editor_kit.setProperty("Style.KEYWORD", keyword_color + font_style);
          c_editor_kit.setProperty("Style.KEYWORD2", keyword2_color + font_style);
          c_editor_kit.setProperty("Style.NUMBER", number_color + font_style);
          c_editor_kit.setProperty("Style.STRING", string_color + font_style);
          c_editor_kit.setProperty("Style.TYPE", type_color + font_style);
          //c_editor_kit.setProperty("Style.IDENTIFIER", "0x000000, " + font_style);

          String text = editing_pane.getText();
          editing_pane.setEditorKit(c_editor_kit);
          editing_pane.addFocusListener(f_listener);
          editing_pane.addCaretListener(c_listener);
          editing_pane.getDocument().addDocumentListener(listener);
          editing_pane.requestFocus();
          editing_pane.setText(text);

          info_label.setText("Font Size: " + font_size);
    }//GEN-LAST:event_choose_font_itemActionPerformed

      private void pref_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_menu_itemActionPerformed
            pref_frame.setLocation(getLocation().x - 40, getLocation().y + 10);
            pref_frame.setSize(630, 600);
            pref_frame.setVisible(true);

            pref_font_txt_fld.setText(default_font);

            ListSelectionListener list_sel_listener = new ListSelectionListener() {
                  @Override
                  public void valueChanged(ListSelectionEvent e) {
                        try {
                              switch (pref_category_list.getSelectedIndex()) {
                                    case 0:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[1].trim());
                                          break;
                                    case 1:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[1].trim());
                                          break;
                                    case 2:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[1].trim());
                                          break;
                                    case 3:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[1].trim());
                                          break;
                                    case 4:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[1].trim());
                                          break;
                                    case 5:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[1].trim());
                                          break;
                                    case 6:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[1].trim());
                                          break;
                                    case 7:
                                          color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[0].replace("0x", ""), 16));
                                          style = Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[1].trim());
                                          break;
                              }
                              
                              pref_color_txt_fld.setText("#" + Integer.toHexString(color.getRGB()).toUpperCase().substring(2));
                              pref_color_txt_fld.setBackground(color);
                              pref_style_txt_fld.setText(get_style(style));
                        } catch (Exception ex) {
                              System.err.println(ex.toString());
                        }
                  }
            };

            pref_category_list.addListSelectionListener(list_sel_listener);
            pref_category_list.setSelectedIndex(0);

            pref_preview_editing_pane.setEditorKit(c_editor_kit);
            pref_preview_editing_pane.setText(
                    "/**\n"
                    + "* author: " + username + "\n"
                    + "* blinky: toggles PORTB pins on and off every 150ms\n"
                    + "*/\n\n"
                    + "#define F_CPU 16000000UL\n"
                    + "#include <avr/io.h>\n"
                    + "#include <avr/interrupt.h>\n"
                    + "#include <util/delay.h>\n\n"
                    + "int main() {\n"
                    + "\tchar* a_string = \"this is a string\";\n"
                    + "\tDDRB = 0xff;\n"
                    + "\twhile(1) {\n"
                    + "\t\t//write your code here\n"
                    + "\t\tPORTB ^= 0xff;\n"
                    + "\t\t_delay_ms(150);\n"
                    + "\t}\n"
                    + "\treturn 0;\n"
                    + "}");
      }//GEN-LAST:event_pref_menu_itemActionPerformed

      private void pref_color_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_color_btnActionPerformed
            Color c = JColorChooser.showDialog(null, "Choose Color", color);

            if (c != null) {
                  color = c;
                  try {
                        switch (pref_category_list.getSelectedIndex()) {
                              case 0:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[1].trim());
                                    String color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.KEYWORD", color_style);
                                    break;
                              case 1:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[1].trim());
                                    color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.KEYWORD2", color_style);
                                    break;
                              case 2:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[1].trim());
                                    color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.NUMBER", color_style);
                                    break;
                              case 3:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[1].trim());
                                    color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.STRING", color_style);
                                    break;
                              case 4:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[1].trim());
                                    color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.COMMENT", color_style);
                                    break;
                              case 5:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[1].trim());
                                    color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.TYPE", color_style);
                                    break;
                              case 6:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[1].trim());
                                    color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.OPERATOR", color_style);
                                    break;
                              case 7:
                                    style = Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[1].trim());
                                    color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + style;
                                    c_editor_kit.setProperty("Style.IDENTIFIER", color_style);
                                    break;
                        }
                        pref_color_txt_fld.setText("#" + Integer.toHexString(color.getRGB()).toUpperCase().substring(2));
                        pref_color_txt_fld.setBackground(color);
                        pref_style_txt_fld.setText(get_style(style));

                        String text = pref_preview_editing_pane.getText();
                        pref_preview_editing_pane.setEditorKit(c_editor_kit);
                        pref_preview_editing_pane.setText(text);
                  } catch (Exception ex) {
                        System.err.println(ex.toString());
                  }

            }

      }//GEN-LAST:event_pref_color_btnActionPerformed

      private void pref_cancel_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_cancel_btnActionPerformed
            pref_frame.setVisible(false);
      }//GEN-LAST:event_pref_cancel_btnActionPerformed

      private void pref_font_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_font_btnActionPerformed
            String[] reslut = get_font();
            String new_font = reslut[0];
            int new_font_size = Integer.parseInt(reslut[1]);
            int font_style = Integer.parseInt(reslut[2]);

            DefaultSyntaxKit.initKit();
            Configuration new_config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
            new_config.put("DefaultFont", new_font + new_font_size);

            CSyntaxKit pref_c_editor_kit = new CSyntaxKit();
            pref_c_editor_kit.setProperty("Style.KEYWORD", keyword_color + font_style);
            pref_c_editor_kit.setProperty("Style.KEYWORD2", keyword2_color + font_style);
            pref_c_editor_kit.setProperty("Style.NUMBER", number_color + font_style);
            pref_c_editor_kit.setProperty("Style.STRING", string_color + font_style);
            pref_c_editor_kit.setProperty("Style.TYPE", type_color + font_style);

            String text = pref_preview_editing_pane.getText();
            pref_preview_editing_pane.setEditorKit(pref_c_editor_kit);
            pref_preview_editing_pane.setText(text);
      }//GEN-LAST:event_pref_font_btnActionPerformed

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
      private javax.swing.JMenuItem about_menu_item;
      private javax.swing.JCheckBoxMenuItem arduino_item;
      private javax.swing.JPopupMenu.Separator build_menu_sep;
      private javax.swing.ButtonGroup build_options_button_group;
      private javax.swing.JMenu build_opts_menu;
      private javax.swing.JLabel char_ins_label;
      private javax.swing.JMenuItem choose_font_item;
      private javax.swing.JTextPane console_pane;
      private javax.swing.JScrollPane console_scroll_pane;
      private javax.swing.JMenuItem dec_font_item;
      private javax.swing.JMenuItem def_font_item;
      private javax.swing.JEditorPane editing_pane;
      public static javax.swing.JScrollPane editing_scroll_pane;
      private javax.swing.JMenuItem exit_menu_item;
      private javax.swing.JMenu file_menu;
      private javax.swing.JMenu font_menu;
      private javax.swing.JPopupMenu.Separator font_menu_sep;
      private javax.swing.JMenuItem gen_makefile;
      private javax.swing.JMenuItem inc_font_item;
      private javax.swing.JLabel info_label;
      private javax.swing.JLabel iteration_label;
      private javax.swing.JComboBox mcuCombo;
      private javax.swing.JMenuBar menuBar;
      private javax.swing.JCheckBoxMenuItem mkfl_build_item;
      private javax.swing.JEditorPane mkfl_editing_pane;
      public static javax.swing.JScrollPane mkfl_editing_scroll_pane;
      private javax.swing.JMenuItem new_file_item;
      private javax.swing.JMenu new_menu;
      private javax.swing.JMenuItem openMenuItem;
      private javax.swing.JMenu port_menu;
      private javax.swing.JButton pref_apply_btn;
      private javax.swing.JButton pref_cancel_btn;
      private javax.swing.JList pref_category_list;
      private javax.swing.JScrollPane pref_category_scroll_pane;
      private javax.swing.JButton pref_color_btn;
      private javax.swing.JLabel pref_color_label;
      private javax.swing.JTextField pref_color_txt_fld;
      private javax.swing.JButton pref_export_btn;
      private javax.swing.JButton pref_font_btn;
      private javax.swing.JLabel pref_font_label;
      private javax.swing.JTextField pref_font_txt_fld;
      private javax.swing.JFrame pref_frame;
      private javax.swing.JButton pref_import_btn;
      private javax.swing.JMenuItem pref_menu_item;
      private javax.swing.JButton pref_ok_btn;
      private javax.swing.JEditorPane pref_preview_editing_pane;
      private javax.swing.JLabel pref_preview_label;
      private javax.swing.JScrollPane pref_preview_scroll_pane;
      private javax.swing.JButton pref_style_btn;
      private javax.swing.JLabel pref_style_label;
      private javax.swing.JTextField pref_style_txt_fld;
      private javax.swing.JDialog privacy_dialog;
      private javax.swing.JScrollPane privacy_scroll_pane;
      private javax.swing.JTextPane privacy_text_pane;
      private javax.swing.JMenu prog_options_menu;
      private javax.swing.ButtonGroup programmer_options_button_group;
      private javax.swing.JLabel row_col_label;
      private javax.swing.JMenuItem save_as_menu_item;
      private javax.swing.JMenuItem save_menu_item;
      private javax.swing.JTextField search_field;
      private javax.swing.JSplitPane split_pane;
      private javax.swing.JLabel status_label;
      private javax.swing.JCheckBoxMenuItem std_build_item;
      private javax.swing.JCheckBoxMenuItem stk500v1_item;
      private javax.swing.JTabbedPane tab_pane;
      private javax.swing.JToolBar toolbar;
      private javax.swing.JMenu tools_menu;
      private javax.swing.JButton upload_button;
      private javax.swing.JMenuItem upload_menu_item;
      private javax.swing.JCheckBoxMenuItem usbasp_item;
      private javax.swing.JButton verify_button;
      private javax.swing.JMenuItem verify_menu_item;
      private javax.swing.JMenu view_menu;
      // End of variables declaration//GEN-END:variables
}