package mainPckg;

import com.sun.glass.ui.Cursor;
import gnu.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.*;
import javax.swing.text.*;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.syntaxkits.BashSyntaxKit;
import jsyntaxpane.syntaxkits.CSyntaxKit;
import jsyntaxpane.util.Configuration;
import org.json.JSONObject;

/**
 *
 * @author mn3m
 *
 * @e-mail hifnawy_moniem@hotmail.com
 *
 */
public class Main_Frame extends javax.swing.JFrame {

    private String os = null;
    private String username = null;

    private int warning_count = 0;
    private long chars_inserted = 0;

    private boolean chose_file = false;
    private boolean verified = false;
    private boolean uploaded = false;
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
    private String port = null;

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
    private String set_font = null;
    private int set_font_size = default_font_size;
    private int current_font_size = default_font_size;

    private Color color = null;
    private int current_font_style = -1;

    private String default_keyword_color_style = null;
    private String default_keyword2_color_style = null;
    private String default_number_color_style = null;
    private String default_string_color_style = null;
    private String default_type_color_style = null;
    private String default_comment_color_style = null;
    private String default_operator_color_style = null;
    private String default_identifier_color_style = null;

    private String backup_font = null;
    private int backup_font_size = -1;
    private String backup_keyword_color_style = null;
    private String backup_keyword2_color_style = null;
    private String backup_number_color_style = null;
    private String backup_string_color_style = null;
    private String backup_type_color_style = null;
    private String backup_comment_color_style = null;
    private String backup_operator_color_style = null;
    private String backup_identifier_color_style = null;

    private final int MODE_NO_ERROR = 0;
    private final int MODE_WARNING = 1;
    private final int MODE_ERROR = 2;
    private final int MODE_EDITING = 3;
    private final int MODE_CONSOLE = 4;

    private Preferences prefs = null;

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
                            || line.toLowerCase().contains("operable program or batch file")
                            || line.toLowerCase().contains("double check chip")
                            || line.toLowerCase().contains("is not recognized")
                            || line.toLowerCase().contains("invalid")
                            || line.toLowerCase().contains("cannot find")
                            || line.toLowerCase().contains("failed")
                            || line.toLowerCase().contains(";")
                            || line.toLowerCase().contains("^")
                            || line.toLowerCase().contains("{")
                            || line.toLowerCase().contains("}")) {
                        append_to_pane(pane, line_before_that_maybe_error + "\n", MODE_ERROR);
                        append_to_pane(pane, line + "\n", MODE_ERROR);
                        error = true;
                    } else if (line.toLowerCase().contains("avrdude done") && error) {
                        append_to_pane(pane, line + "\n", MODE_ERROR);
                    } else if (line.toLowerCase().contains("warning")
                            || line.toLowerCase().contains("disable")
                            || line.toLowerCase().contains("at top level")) {
                        append_to_pane(pane, line_before_that_maybe_error + "\n", MODE_WARNING);
                        append_to_pane(pane, line + "\n", MODE_WARNING);

                        if (!os.contains("windows")) {
                            warning = true;
                        }
                        warning_count++;
                    } else if (line.toLowerCase().contains("note")) {
                        append_to_pane(pane, line + "\n", MODE_WARNING);
                    } else {
                        append_to_pane(pane, line + "\n", MODE_NO_ERROR);
                    }
                } else if (line.toLowerCase().contains("error")
                        || line.toLowerCase().contains("cannot find")
                        || line.toLowerCase().contains("is not recognized")
                        || line.toLowerCase().contains("operable program or batch file")
                        || line.toLowerCase().contains("stop")
                        || line.toLowerCase().contains("note: previous")
                        || line.toLowerCase().contains("no such file")
                        || line.toLowerCase().contains("not found")
                        || line.toLowerCase().contains("undefined reference")
                        || line.toLowerCase().contains("double check chip")
                        || line.toLowerCase().contains("invalid")
                        || line.toLowerCase().contains("failed")) {
                    append_to_pane(pane, line + "\n", MODE_ERROR);
                    error = true;
                } else if (line.toLowerCase().contains(";")
                        || line.toLowerCase().contains("{")
                        || line.toLowerCase().contains("}")) {
                    if (warning) {
                        append_to_pane(pane, line + "\n", MODE_WARNING);

                        if (!os.contains("windows")) {
                            line = br.readLine();
                            if (line != null && line.toLowerCase().contains("^")) {
                                append_to_pane(pane, line + "\n", MODE_WARNING);
                            }
                        }
                        warning_count++;
                        warning = false;
                    } else {
                        append_to_pane(pane, line + "\n", MODE_ERROR);
                        error = true;
                    }
                } else if (line.toLowerCase().contains("^") && error) {
                    append_to_pane(pane, line + "\n", MODE_ERROR);
                } else if (line.toLowerCase().contains("avrdude done") && error) {
                    append_to_pane(pane, line + "\n", MODE_ERROR);
                } else if (line.toLowerCase().contains("warning")
                        || line.toLowerCase().contains("disable")
                        || line.toLowerCase().contains("at top level")) {
                    append_to_pane(pane, line + "\n", MODE_WARNING);
                    warning_count++;
                    warning = true;
                } else if (line.toLowerCase().contains("note")) {
                    append_to_pane(pane, line + "\n", MODE_WARNING);
                } else {
                    append_to_pane(pane, line + "\n", MODE_NO_ERROR);
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
            Logger.getLogger(Main_Frame.class.getName()).log(Level.SEVERE, null, ex);
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

                    tab_pane.setTitleAt(1, makefile.getName());
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
        console_pane.setText("");
        tab_pane.setSelectedIndex(0);

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
                    append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                    Process p = new ProcessBuilder(cmd).start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    check_for_errors(console_pane, br);
                    if (!error) {
                        File f = new File(hexPath.replace("\"", ""));
                        if (f.exists()) {
                            if (warning_count == 1) {
                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), MODE_WARNING);
                            } else if (warning_count > 0) {
                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_WARNING);
                            } else {
                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_NO_ERROR);
                            }
                            System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                            append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", MODE_NO_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                            verified = true;
                        } else {
                            if (warning_count == 1) {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                            } else if (warning_count > 0) {
                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            } else {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            }
                            System.err.println("Compilation Terminated, could not generate hex file !!!");
                            append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", MODE_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        }
                    } else {
                        if (warning_count == 1) {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                        } else if (warning_count > 0) {
                            System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        } else {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        }
                        System.err.println("Errors Occured During Compilation !!!");
                        append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", MODE_ERROR);
                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                    }
                } catch (BadLocationException | IOException ex) {
                    System.err.println(ex.getMessage());
                }
            } else {
                try {
                    String[] cmd = {"/bin/sh", "-c", "cd " + file_to_open.getParent() + " && make compile"};
                    System.out.println(cmd[2]);
                    append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                    Process p = new ProcessBuilder(cmd).start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    check_for_errors(console_pane, br);

                    if (!error) {
                        File f = new File(hexPath.replace("\"", ""));
                        if (f.exists()) {
                            if (warning_count == 1) {
                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), MODE_WARNING);
                            } else if (warning_count > 0) {
                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_WARNING);
                            } else {
                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_NO_ERROR);
                            }
                            System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                            append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", MODE_NO_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                            verified = true;
                        } else {
                            if (warning_count == 1) {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                            } else if (warning_count > 0) {
                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            } else {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            }
                            System.err.println("Compilation Terminated, could not generate hex file !!!");
                            append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", MODE_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        }
                    } else {
                        if (warning_count == 1) {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                        } else if (warning_count > 0) {
                            System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        } else {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        }
                        System.err.println("Errors Occured During Compilation !!!");
                        append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", MODE_ERROR);
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

            if (os.equals("windows")) {   //Windows
                try {
                    String[] cmd = {"cmd", "/c", "avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + cPath + " -o " + oPath};
                    System.out.println(cmd[2]);
                    append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                    Process p = new ProcessBuilder(cmd).start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    check_for_errors(console_pane, br);

                    if (!error) {
                        cmd = new String[]{"cmd", "/c", "avr-gcc -g -mmcu=" + mmcu + " -o " + elfPath + " " + oPath};
                        System.out.println(cmd[2]);
                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        p = new ProcessBuilder(cmd).start();
                        br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        check_for_errors(console_pane, br);
                    }
                    if (!error) {
                        cmd = new String[]{"cmd", "/c", "avr-objcopy -j .text -j .data -O ihex " + elfPath + " " + hexPath};
                        System.out.println(cmd[2]);
                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        p = new ProcessBuilder(cmd).start();
                        br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        check_for_errors(console_pane, br);
                    }

                    if (!error) {
                        cmd = new String[]{"cmd", "/c", "del \"" + file_to_open.getAbsolutePath().replace(".c", ".o") + "\""};
                        System.out.println(cmd[2]);
                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
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
                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), MODE_WARNING);
                            } else if (warning_count > 0) {
                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_WARNING);
                            } else {
                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_NO_ERROR);
                            }
                            System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                            append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", MODE_NO_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                            verified = true;
                        } else {
                            if (warning_count == 1) {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                            } else if (warning_count > 0) {
                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            } else {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            }
                            System.err.println("Compilation Terminated, could not generate hex file !!!");
                            append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", MODE_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        }
                    } else {
                        if (warning_count == 1) {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                        } else if (warning_count > 0) {
                            System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        } else {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        }
                        System.err.println("Errors Occured During Compilation !!!");
                        append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", MODE_ERROR);
                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                    }
                } catch (IOException | BadLocationException ex) {
                    System.err.println(ex.toString());
                }
            } else {    //Linux
                try {
                    String[] cmd = {"/bin/sh", "-c", "avr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + cPath + " -o " + oPath};
                    System.out.println(cmd[2]);
                    append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                    Process p = new ProcessBuilder(cmd).start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    check_for_errors(console_pane, br);

                    if (!error) {
                        cmd = new String[]{"/bin/sh", "-c", "avr-gcc -g -mmcu=" + mmcu + " -o " + elfPath + " " + oPath};
                        System.out.println(cmd[2]);
                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        p = new ProcessBuilder(cmd).start();
                        br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        check_for_errors(console_pane, br);
                    }
                    if (!error) {
                        cmd = new String[]{"/bin/sh", "-c", "avr-objcopy -j .text -j .data -O ihex " + elfPath + " " + hexPath};
                        System.out.println(cmd[2]);
                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        p = new ProcessBuilder(cmd).start();
                        br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        check_for_errors(console_pane, br);
                    }

                    if (!error) {
                        cmd = new String[]{"/bin/sh", "-c", "rm -f \"" + file_to_open.getAbsolutePath().replace(".c", ".o") + "\""};
                        System.out.println(cmd[2]);
                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
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
                                append_to_pane(console_pane, String.format("\n%d errors, %d warning\n", 0, warning_count), MODE_WARNING);
                            } else if (warning_count > 0) {
                                System.out.println(String.format("\n%d errors, %d warnings", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_WARNING);
                            } else {
                                System.out.println(String.format("\n%d errors, %d warning", 0, warning_count));
                                append_to_pane(console_pane, String.format("\n%d errors, %d warnings\n", 0, warning_count), MODE_NO_ERROR);
                            }
                            System.out.println("Compiled Successfully for device " + mmcu + " !!!");
                            append_to_pane(console_pane, "Compiled Successfully for device " + mmcu + " !!!\n", MODE_NO_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                            verified = true;
                        } else {
                            if (warning_count == 1) {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                            } else if (warning_count > 0) {
                                System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            } else {
                                System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                                append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                            }
                            System.err.println("Compilation Terminated, could not generate hex file !!!");
                            append_to_pane(console_pane, "Compilation Terminated, could not generate hex file !!!\n", MODE_ERROR);
                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                        }
                    } else {
                        if (warning_count == 1) {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warning\n", 1, warning_count), MODE_ERROR);
                        } else if (warning_count > 0) {
                            System.err.println(String.format("\n%d error, %d warnings", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        } else {
                            System.err.println(String.format("\n%d error, %d warning", 1, warning_count));
                            append_to_pane(console_pane, String.format("\n%d error, %d warnings\n", 1, warning_count), MODE_ERROR);
                        }
                        System.err.println("Errors Occured During Compilation !!!");
                        append_to_pane(console_pane, "Errors Occured During Compilation !!!\n", MODE_ERROR);
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
            if (uploaded) {
                console_pane.setText("");
                uploaded = false;
                upload_hex();
            } else if (verified) {
                if (error) {
                    System.out.println("Fix compilation errors and then upload the sketch !!!");
                    append_to_pane(console_pane, "Fix compilation errors and then upload the sketch !!!\n", MODE_ERROR);
                    error = false;
                } else //upload...
                if (std_build_item.isSelected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("Uploading...");
                                append_to_pane(console_pane, "\nUploading...\n", MODE_CONSOLE);
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
                                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        ProcessBuilder pb = new ProcessBuilder(cmd);
                                        Process p = pb.start();
                                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                        //checkForErrors(console_pane, br);  //no real-time output

                                        int value = 0;
                                        while (value != -1) {
                                            char ch = (char) value;
                                            System.out.print(ch);
                                            append_to_pane(console_pane, ch + "", MODE_CONSOLE);
                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                            value = br.read();
                                        }
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        String output = console_pane.getText();
                                        if (output.contains("verified")) {
                                            System.out.println("Uploaded Successfully !!!");
                                            append_to_pane(console_pane, "Uploaded Successfully !!!\n", MODE_NO_ERROR);
                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                            uploaded = true;
                                        } else {
                                            System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                            append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", MODE_ERROR);
                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                            uploaded = false;
                                        }
                                        editing_pane.setEnabled(true);

                                    } catch (IOException | BadLocationException ex) {
                                        System.err.println(ex.toString());
                                    }
                                } else {
                                    try {
                                        String[] cmd = {"/bin/sh", "-c", "avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + hexPath.replace("\\", "/") + ":i"};
                                        //String[] cmd = {"/bin/sh", "-c", "ping 127.0.0.1"};     //for testing
                                        System.out.println(cmd[2]);
                                        append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        ProcessBuilder pb = new ProcessBuilder(cmd);
                                        Process p = pb.start();
                                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                        //checkForErrors(console_pane, br);  //no real-time output
                                        int value = 0;
                                        while (value != -1) {
                                            char ch = (char) value;
                                            System.out.print(ch);
                                            append_to_pane(console_pane, ch + "", MODE_CONSOLE);
                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                            value = br.read();
                                        }
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        String output = console_pane.getText();
                                        if (output.contains("verified")) {
                                            System.out.println("Uploaded Successfully !!!");
                                            append_to_pane(console_pane, "Uploaded Successfully !!!\n", MODE_NO_ERROR);
                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                            uploaded = true;
                                        } else {
                                            System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                            append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", MODE_ERROR);
                                            console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                            uploaded = false;
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
                                    append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    Process p = new ProcessBuilder(cmd).start();
                                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    //checkForErrors(console_pane, br);  //no real-time output
                                    int value = 0;
                                    while (value != -1) {
                                        char ch = (char) value;
                                        System.out.print(ch);
                                        append_to_pane(console_pane, ch + "", MODE_CONSOLE);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        value = br.read();
                                    }
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    String output = console_pane.getText();
                                    if (output.contains("verified")) {
                                        System.out.println("Uploaded Successfully !!!");
                                        append_to_pane(console_pane, "Uploaded Successfully !!!\n", MODE_NO_ERROR);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        uploaded = true;
                                    } else {
                                        System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                        append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", MODE_ERROR);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        uploaded = false;
                                    }
                                } catch (BadLocationException | IOException ex) {
                                    System.err.println(ex.getMessage());
                                }
                            } else {
                                try {
                                    String[] cmd = {"/bin/sh", "-c", "cd " + file_to_open.getParent() + " && make upload"};
                                    System.out.println(cmd[2]);
                                    append_to_pane(console_pane, cmd[2] + "\n", MODE_CONSOLE);
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    Process p = new ProcessBuilder(cmd).start();
                                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                    //checkForErrors(console_pane, br);  //no real-time output
                                    int value = 0;
                                    while (value != -1) {
                                        char ch = (char) value;
                                        System.out.print(ch);
                                        append_to_pane(console_pane, ch + "", MODE_CONSOLE);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        value = br.read();
                                    }
                                    console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                    String output = console_pane.getText();
                                    if (output.contains("verified")) {
                                        System.out.println("Uploaded Successfully !!!");
                                        append_to_pane(console_pane, "Uploaded Successfully !!!\n", MODE_NO_ERROR);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        uploaded = true;
                                    } else {
                                        System.out.println("Could not upload hex file !!!\nPlease check for errors...");
                                        append_to_pane(console_pane, "Could not upload hex file !!!\nPlease check for errors...", MODE_ERROR);
                                        console_pane.setCaretPosition(console_pane.getDocument().getLength());
                                        uploaded = false;
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
                    append_to_pane(console_pane, "Fix compilation errors and then upload the sketch !!!\n", MODE_ERROR);
                    error = false;
                } else {
                    upload_hex();
                }

            }
        } catch (BadLocationException ex) {
            System.err.println(ex.toString());
        }
    }

    private boolean is_in_ports_menu(JMenu menu, String other) {
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) menu.getMenuComponent(i);

            if (item.getText().equals(other)) {
                return true;
            }
        }

        return false;
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

                        while (portEnum.hasMoreElements()) {
                            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                            JCheckBoxMenuItem new_port = new JCheckBoxMenuItem(currPortId.getName());

                            new_port.addActionListener((ActionEvent e) -> {
                                port = new_port.getText();
                            });

                            if (!is_in_ports_menu(port_menu, new_port.getText())) {
                                ports_button_group.add(new_port);
                                port_menu.add(new_port);
                                new_port.setSelected(true);
                            }

                            if (usbasp_item.isSelected()) {
                                port_menu.setText("Port: You are using USBASP");
                                port_menu.setEnabled(false);
                            } else if (avr_isp_item.isSelected()) {
                                port_menu.setText("Port");
                                port_menu.setEnabled(true);

                                if (port == null) {
                                    port = new_port.getText();
                                }

                                prog_option = "avrisp -b19200 -P " + port;
                            } else {
                                port_menu.setText("Port");
                                port_menu.setEnabled(true);

                                if (port == null) {
                                    port = new_port.getText();
                                }
                            }

                            counter++;
                        }

                        if (counter < 1) {
                            // System.out.println("no ports found");
                            serial_terminal_menu_item.setEnabled(false);
                            port_menu.setEnabled(false);

                            port = null;

                            mmcu_port_label.setText(mmcu + " on none");
                        } else {
                            serial_terminal_menu_item.setEnabled(true);

                            mmcu_port_label.setText(mmcu + " on " + port);
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
        Font font = new Font(set_font.trim(), Font.PLAIN, set_font_size);

        JFontChooser fc = new JFontChooser(font);
        int result = fc.showDialog(this);

        if (result == JFontChooser.OK_OPTION) {
            String new_font = fc.getSelectedFont().getFontName() + " ";
            String new_font_size = Integer.toString(fc.getSelectedFontSize());
            String new_font_style = Integer.toString(fc.getSelectedFontStyle());
            return new String[]{new_font.toLowerCase(), new_font_size, new_font_style};
        } else {
            return new String[]{null};
        }
    }

    public void set_chooser_panel(JColorChooser chooser, String name) {
        AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
        for (AbstractColorChooserPanel panel : panels) {
            String panel_name = panel.getDisplayName().toLowerCase();
            System.out.println(panel_name);
            String item_name = name.toLowerCase();
            if (!panel_name.equals(item_name)) {
                chooser.removeChooserPanel(panel);
            }
        }
    }

    public Main_Frame(String[] arguments) {
        initComponents();

        port_menu.setEnabled(true);
        search_ports();

        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;

        os = UIManager.getInstalledLookAndFeels()[3].getName().toLowerCase();
        username = System.getProperty("user.name");

        setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocation(screen_width / 2 - getWidth() / 2, screen_height / 2 - getHeight() / 2);

        prog_option = "avrisp -b19200 -P " + port;
        mcuCombo.setSelectedItem("atmega328p");
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
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        };

        if (os.equals("windows")) {
            set_font = default_font = "consolas ";
        } else {
            set_font = default_font = "liberation mono ";
        }

        prefs = Preferences.userRoot();

//        try {
//            prefs.clear();
//        } catch (BackingStoreException ex) {
//            System.err.println(ex.getMessage());
//        }
        set_font = prefs.get("default_font", default_font);
        set_font_size = prefs.getInt("default_font_size", default_font_size);

        DefaultSyntaxKit.initKit();
        config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
        config.put("DefaultFont", set_font + set_font_size);

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
        //  0 = plain
        //  1 = bold
        //  2 = italic
        //  3 = bold and italic
        default_keyword_color_style = "0x1e06c2, 1";
        default_keyword2_color_style = "0x1e06c2, 1";
        default_number_color_style = "0xbc2b13, 1";
        default_string_color_style = "0xfaba12, 2";
        default_type_color_style = "0x1e06c2, 1";
        default_comment_color_style = "0x339933, 2";
        default_operator_color_style = "0x000000, 0";
        default_identifier_color_style = "0x000000, 0";

        c_editor_kit = new CSyntaxKit();

        String saved_keyword_color_style = prefs.get("keyword", default_keyword_color_style);
        String saved_keyword2_color_style = prefs.get("keyword2", default_keyword2_color_style);
        String saved_number_color_style = prefs.get("number", default_number_color_style);
        String saved_string_color_style = prefs.get("string", default_string_color_style);
        String saved_type_color_style = prefs.get("type", default_type_color_style);
        String saved_comment_color_style = prefs.get("comment", default_comment_color_style);
        String saved_operator_color_style = prefs.get("operator", default_operator_color_style);
        String saved_identifier_color_style = prefs.get("identifier", default_identifier_color_style);

//        System.out.println("Saved Settings:");
//        System.out.println(saved_keyword_color_style);
//        System.out.println(saved_keyword2_color_style);
//        System.out.println(saved_number_color_style);
//        System.out.println(saved_string_color_style);
//        System.out.println(saved_type_color_style);
//        System.out.println(saved_comment_color_style);
//        System.out.println(saved_operator_color_style);
//        System.out.println(saved_identifier_color_style);
        c_editor_kit.setProperty("Style.KEYWORD", saved_keyword_color_style);
        c_editor_kit.setProperty("Style.KEYWORD2", saved_keyword2_color_style);
        c_editor_kit.setProperty("Style.NUMBER", saved_number_color_style);
        c_editor_kit.setProperty("Style.STRING", saved_string_color_style);
        c_editor_kit.setProperty("Style.TYPE", saved_type_color_style);
        c_editor_kit.setProperty("Style.COMMENT", saved_comment_color_style);
        c_editor_kit.setProperty("Style.OPERATOR", saved_operator_color_style);
        c_editor_kit.setProperty("Style.IDENTIFIER", saved_identifier_color_style);

//        System.out.println("\nSet Settings:");
//        System.out.println(c_editor_kit.getProperty("Style.KEYWORD"));
//        System.out.println(c_editor_kit.getProperty("Style.KEYWORD2"));
//        System.out.println(c_editor_kit.getProperty("Style.NUMBER"));
//        System.out.println(c_editor_kit.getProperty("Style.STRING"));
//        System.out.println(c_editor_kit.getProperty("Style.TYPE"));
//        System.out.println(c_editor_kit.getProperty("Style.COMMENT"));
//        System.out.println(c_editor_kit.getProperty("Style.OPERATOR"));
//        System.out.println(c_editor_kit.getProperty("Style.IDENTIFIER"));
        editing_pane.setEditorKit(c_editor_kit);
        editing_pane.addFocusListener(f_listener);
        editing_pane.addCaretListener(c_listener);
        editing_pane.getDocument().addDocumentListener(listener);
        editing_pane.requestFocus();

        def_font_item.setText(def_font_item.getText() + " (" + set_font_size + ")");
        inc_font_item.setText(inc_font_item.getText() + " (" + (set_font_size + 1) + ")");
        dec_font_item.setText(dec_font_item.getText() + " (" + (set_font_size - 1) + ")");

        if (arguments.length > 0) {
            try {
                file_to_open = new File(arguments[0]);

                String parent = file_to_open.getParentFile().getName();
                File[] files = file_to_open.getParentFile().listFiles();

                for (File f : files) {
                    if (f.getName().equals("makefile")) {
                        make = true;
                        makefile = os.equals("windows") ? new File(file_to_open.getParent() + "\\makefile") : new File(file_to_open.getParent() + "/makefile");
                        break;
                    } else if (f.getName().equals("Makefile")) {
                        make = true;
                        makefile = os.equals("windows") ? new File(file_to_open.getParent() + "\\Makefile") : new File(file_to_open.getParent() + "/Makefile");
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
                    JOptionPane.showMessageDialog(this, "AVR4L can only open its own sketches and other files ending in .c",
                            "Bad file selected", JOptionPane.WARNING_MESSAGE);
                    JOptionPane.showMessageDialog(this, "AVR4L will no exit",
                            "Exiting...", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);

                }

                save_method();

                if (make) {
                    mkfl_build_item.setSelected(true);
                    gen_makefile.setEnabled(true);
                    tab_pane.add(makefile.getName(), mkfl_editing_scroll_pane);
                    tab_pane.setSelectedIndex(1);

                    mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
                    mkfl_editing_pane.addFocusListener(mkfl_f_listener);
                    mkfl_editing_pane.addCaretListener(mkfl_c_listener);
                    mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);

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
        pref_font_label = new javax.swing.JLabel();
        pref_font_txt_fld = new javax.swing.JTextField();
        pref_font_btn = new javax.swing.JButton();
        pref_style_label = new javax.swing.JLabel();
        pref_color_btn = new javax.swing.JButton();
        pref_color_txt_fld = new javax.swing.JTextField();
        pref_color_label = new javax.swing.JLabel();
        pref_style_combo_bx = new javax.swing.JComboBox<>();
        pref_defaults_btn = new javax.swing.JButton();
        serial_frame = new javax.swing.JFrame();
        serial_send_txt_fld = new javax.swing.JTextField();
        serial_send_btn = new javax.swing.JButton();
        serial_baud_rate_combo_bx = new javax.swing.JComboBox<>();
        serial_baud_rate_label = new javax.swing.JLabel();
        serial_autoscroll_chk_bx = new javax.swing.JCheckBox();
        serial_rcv_scroll_pane = new javax.swing.JScrollPane();
        serial_rcv_text_pane = new javax.swing.JTextPane();
        serial_port_label = new javax.swing.JLabel();
        serial_clear_btn = new javax.swing.JButton();
        toolbar = new javax.swing.JToolBar();
        verify_button = new javax.swing.JButton();
        upload_button = new javax.swing.JButton();
        search_field = new javax.swing.JTextField();
        mcuCombo = new javax.swing.JComboBox();
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
        mmcu_port_label = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        file_menu = new javax.swing.JMenu();
        new_menu = new javax.swing.JMenu();
        new_file_item = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        save_menu_item = new javax.swing.JMenuItem();
        save_as_menu_item = new javax.swing.JMenuItem();
        separator = new javax.swing.JPopupMenu.Separator();
        pref_menu_item = new javax.swing.JMenuItem();
        about_menu_item = new javax.swing.JMenuItem();
        exit_menu_item = new javax.swing.JMenuItem();
        tools_menu = new javax.swing.JMenu();
        verify_menu_item = new javax.swing.JMenuItem();
        upload_menu_item = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        serial_terminal_menu_item = new javax.swing.JMenuItem();
        prog_options_menu = new javax.swing.JMenu();
        avr_isp_item = new javax.swing.JCheckBoxMenuItem();
        arduino_item = new javax.swing.JCheckBoxMenuItem();
        stk500v1_item = new javax.swing.JCheckBoxMenuItem();
        usbasp_item = new javax.swing.JCheckBoxMenuItem();
        port_menu = new javax.swing.JMenu();
        view_menu = new javax.swing.JMenu();
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
        pref_apply_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pref_apply_btnActionPerformed(evt);
            }
        });

        pref_ok_btn.setText("Ok");
        pref_ok_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pref_ok_btnActionPerformed(evt);
            }
        });

        pref_export_btn.setText("Export");
        pref_export_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pref_export_btnActionPerformed(evt);
            }
        });

        pref_import_btn.setText("Import");
        pref_import_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pref_import_btnActionPerformed(evt);
            }
        });

        pref_font_label.setText("Font:");

        pref_font_txt_fld.setEditable(false);

        pref_font_btn.setText("...");
        pref_font_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pref_font_btnActionPerformed(evt);
            }
        });

        pref_style_label.setText("Style:");

        pref_color_btn.setText("...");
        pref_color_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pref_color_btnActionPerformed(evt);
            }
        });

        pref_color_txt_fld.setEditable(false);
        pref_color_txt_fld.setText("#000000");
        pref_color_txt_fld.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pref_color_txt_fldMouseClicked(evt);
            }
        });

        pref_color_label.setText("Color:");

        pref_style_combo_bx.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Plain", "Bold", "Italic", "Bold and Italic" }));
        pref_style_combo_bx.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                pref_style_combo_bxPopupMenuWillBecomeVisible(evt);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                pref_style_combo_bxPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        pref_defaults_btn.setText("Defaults");
        pref_defaults_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pref_defaults_btnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pref_frameLayout = new javax.swing.GroupLayout(pref_frame.getContentPane());
        pref_frame.getContentPane().setLayout(pref_frameLayout);
        pref_frameLayout.setHorizontalGroup(
            pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pref_frameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pref_frameLayout.createSequentialGroup()
                        .addComponent(pref_font_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pref_font_txt_fld)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pref_font_btn))
                    .addGroup(pref_frameLayout.createSequentialGroup()
                        .addComponent(pref_category_scroll_pane, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pref_frameLayout.createSequentialGroup()
                                .addComponent(pref_style_label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pref_style_combo_bx, 0, 231, Short.MAX_VALUE))
                            .addGroup(pref_frameLayout.createSequentialGroup()
                                .addComponent(pref_color_label)
                                .addGap(8, 8, 8)
                                .addComponent(pref_color_txt_fld)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pref_color_btn))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pref_frameLayout.createSequentialGroup()
                        .addComponent(pref_export_btn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pref_import_btn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pref_ok_btn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pref_apply_btn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pref_defaults_btn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pref_cancel_btn)))
                .addContainerGap())
        );
        pref_frameLayout.setVerticalGroup(
            pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pref_frameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pref_font_label)
                    .addComponent(pref_font_txt_fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pref_font_btn))
                .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pref_frameLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pref_color_txt_fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pref_color_label)
                            .addComponent(pref_color_btn))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pref_style_combo_bx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pref_style_label))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(pref_frameLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pref_category_scroll_pane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(pref_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pref_cancel_btn)
                    .addComponent(pref_defaults_btn)
                    .addComponent(pref_apply_btn)
                    .addComponent(pref_ok_btn)
                    .addComponent(pref_export_btn)
                    .addComponent(pref_import_btn))
                .addContainerGap())
        );

        serial_frame.setTitle("Serial Terminal");
        serial_frame.setResizable(false);

        serial_send_btn.setText("send");

        serial_baud_rate_combo_bx.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "9600", "14400", "19200", "28800", "38400", "56000", "57600", "115200" }));

        serial_baud_rate_label.setText("Baud Rate:");

        serial_autoscroll_chk_bx.setText("Autoscroll");

        serial_rcv_text_pane.setEditable(false);
        serial_rcv_text_pane.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        serial_rcv_scroll_pane.setViewportView(serial_rcv_text_pane);

        serial_port_label.setText("Port:");

        serial_clear_btn.setText("Clear");
        serial_clear_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serial_clear_btnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout serial_frameLayout = new javax.swing.GroupLayout(serial_frame.getContentPane());
        serial_frame.getContentPane().setLayout(serial_frameLayout);
        serial_frameLayout.setHorizontalGroup(
            serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serial_frameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serial_rcv_scroll_pane)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, serial_frameLayout.createSequentialGroup()
                        .addComponent(serial_send_txt_fld)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serial_send_btn))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, serial_frameLayout.createSequentialGroup()
                        .addGroup(serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(serial_autoscroll_chk_bx, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(serial_clear_btn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 289, Short.MAX_VALUE)
                        .addGroup(serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, serial_frameLayout.createSequentialGroup()
                                .addComponent(serial_baud_rate_label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serial_baud_rate_combo_bx, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(serial_port_label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        serial_frameLayout.setVerticalGroup(
            serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serial_frameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serial_send_txt_fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serial_send_btn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serial_rcv_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serial_clear_btn)
                    .addComponent(serial_baud_rate_combo_bx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serial_baud_rate_label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(serial_frameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serial_autoscroll_chk_bx)
                    .addComponent(serial_port_label))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AVR4L");
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
        toolbar.add(mcuCombo);

        status_label.setForeground(new java.awt.Color(1, 1, 1));
        status_label.setText("Status");

        iteration_label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        iteration_label.setText("Iteration: 53,007");

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

        mmcu_port_label.setText("mmcu on port");

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
        file_menu.add(separator);

        pref_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        pref_menu_item.setText("Preferences");
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
        tools_menu.add(jSeparator1);

        serial_terminal_menu_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        serial_terminal_menu_item.setText("Serial terminal");
        serial_terminal_menu_item.setEnabled(false);
        serial_terminal_menu_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serial_terminal_menu_itemActionPerformed(evt);
            }
        });
        tools_menu.add(serial_terminal_menu_item);

        prog_options_menu.setText("Programmer");

        programmer_options_button_group.add(avr_isp_item);
        avr_isp_item.setSelected(true);
        avr_isp_item.setText("AVR ISP");
        avr_isp_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avr_isp_itemActionPerformed(evt);
            }
        });
        prog_options_menu.add(avr_isp_item);

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

        programmer_options_button_group.add(usbasp_item);
        usbasp_item.setText("USBasp");
        usbasp_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usbasp_itemActionPerformed(evt);
            }
        });
        prog_options_menu.add(usbasp_item);

        tools_menu.add(prog_options_menu);

        port_menu.setText("Port");
        tools_menu.add(port_menu);

        menuBar.add(tools_menu);

        view_menu.setText("View");

        def_font_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.CTRL_MASK));
        def_font_item.setText("Default font size");
        def_font_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                def_font_itemActionPerformed(evt);
            }
        });
        view_menu.add(def_font_item);

        inc_font_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_EQUALS, java.awt.event.InputEvent.CTRL_MASK));
        inc_font_item.setText("Increase font size");
        inc_font_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inc_font_itemActionPerformed(evt);
            }
        });
        view_menu.add(inc_font_item);

        dec_font_item.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_MASK));
        dec_font_item.setText("Decrease font size");
        dec_font_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dec_font_itemActionPerformed(evt);
            }
        });
        view_menu.add(dec_font_item);

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
            .addComponent(tab_pane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(status_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(iteration_label, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(row_col_label)
                        .addGap(18, 18, 18)
                        .addComponent(char_ins_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mmcu_port_label)))
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
                    .addComponent(char_ins_label)
                    .addComponent(mmcu_port_label))
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
                    File x = new File("/home/" + username + "/.avr4l_temp");
                    x.mkdir();
                    x = new File("/home/" + username + "/.avr4l_temp/" + temp_folder);
                    x.mkdir();
                    file_to_open = new File(x.getPath() + "/" + tab_pane.getTitleAt(0).replace("*", ""));
                    file_to_open.createNewFile();
                    temporary_file_to_open = file_to_open;
                    writer = new PrintWriter(file_to_open);
                    writer.println(editing_pane.getText());
                    writer.close();
                    compile_file();
                }
//                      chose_file = true;
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
                    File x = new File("/home/" + username + "/.avr4l_temp");
                    x.mkdir();
                    x = new File("/home/" + username + "/.avr4l_temp/" + temp_folder);
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
//                      chose_file = true;
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
                    File x = new File("/home/" + username + "/.avr4l_temp");
                    x.mkdir();
                    x = new File("/home/" + username + "/.avr4l_temp/" + temp_folder);
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
                    File x = new File("/home/" + username + "/.avr4l_temp");
                    x.mkdir();
                    x = new File("/home/" + username + "/.avr4l_temp/" + temp_folder);
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

    if (evt.getKeyChar() == 8) {     //backspace
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
            if (f.getName().equals("makefile")) {
                make = true;
                makefile = os.equals("windows") ? new File(x.getParent() + "\\makefile") : new File(x.getParent() + "/makefile");
                break;
            } else if (f.getName().equals("Makefile")) {
                make = true;
                makefile = os.equals("windows") ? new File(x.getParent() + "\\Makefile") : new File(x.getParent() + "/Makefile");
                break;
            }
        }

        if (!selected.contains("null")) {
            if (!selected.substring(selected.length() - 2, selected.length()).toLowerCase().equals(".c")) {
                JOptionPane.showMessageDialog(this, "AVR4L can only open its own sketches and other files ending in .c",
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
            tab_pane.add(makefile.getName(), mkfl_editing_scroll_pane);
            tab_pane.setSelectedIndex(1);

            mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
            mkfl_editing_pane.addFocusListener(mkfl_f_listener);
            mkfl_editing_pane.addCaretListener(mkfl_c_listener);
            mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);

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
    if ((evt.getNewState() & Main_Frame.MAXIMIZED_BOTH) == Main_Frame.MAXIMIZED_BOTH) {
        split_pane.setResizeWeight(0.9);
    } // minimized
    else if ((evt.getNewState() & Main_Frame.ICONIFIED) == Main_Frame.ICONIFIED) {
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

    System.out.println(copy_left);

    if (os.equals("windows")) {
        privacy_text_pane.setFont(new Font("Consolas", Font.PLAIN, 15));
    }

    privacy_text_pane.setText(copy_left);
    privacy_text_pane.setCaretPosition(0);

    int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
    int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;

    privacy_dialog.setSize(700, 500);
    privacy_dialog.setLocation(screen_width / 2 - privacy_dialog.getWidth() / 2, screen_height / 2 - privacy_dialog.getHeight() / 2);
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
    port_menu.setText("Port: You are using USBASP");
    gen_makefileActionPerformed(evt);
}//GEN-LAST:event_usbasp_itemActionPerformed

private void stk500v1_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stk500v1_itemActionPerformed
    port_menu.removeAll();
    port_menu.setEnabled(true);
    port_menu.setText("Port");
    serial_terminal_menu_item.setEnabled(true);

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
                port_menu.add(new_port);
                port = currPortId.getName();
                new_port.setSelected(true);
                prog_option = "stk500v1 -b19200 -P " + currPortId.getName();
                gen_makefileActionPerformed(evt);
                counter++;
            }
            if (counter < 1) {
                port_menu.setEnabled(false);
                serial_terminal_menu_item.setEnabled(false);
            }
        }
    }).start();
}//GEN-LAST:event_stk500v1_itemActionPerformed

private void arduino_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arduino_itemActionPerformed
    port_menu.removeAll();
    port_menu.setEnabled(true);
    port_menu.setText("Port");
    serial_terminal_menu_item.setEnabled(true);

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
                port = currPortId.getName();
                new_port.setSelected(true);
                prog_option = "arduino -b57600 -P " + currPortId.getName();
                gen_makefileActionPerformed(evt);
                counter++;
            }
            if (counter < 1) {
                port_menu.setEnabled(false);
                serial_terminal_menu_item.setEnabled(false);
            }
        }
    }).start();
}//GEN-LAST:event_arduino_itemActionPerformed

private void inc_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inc_font_itemActionPerformed
    chars_inserted = 0;
    current_font_size += 1;

    if (tab_pane.getSelectedIndex() == 0) {
        String text = editing_pane.getText();
        int caret_position = editing_pane.getCaretPosition();
        config.put("DefaultFont", set_font + current_font_size);
        editing_pane.setEditorKit(c_editor_kit);
        editing_pane.addFocusListener(f_listener);
        editing_pane.addCaretListener(c_listener);
        editing_pane.getDocument().addDocumentListener(listener);
        editing_pane.setText(text);
        editing_pane.setCaretPosition(caret_position);
    } else if (tab_pane.getSelectedIndex() == 1) {
        String text = mkfl_editing_pane.getText();
        int caret_position = mkfl_editing_pane.getCaretPosition();
        config.put("DefaultFont", set_font + current_font_size);
        mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
        mkfl_editing_pane.addFocusListener(mkfl_f_listener);
        mkfl_editing_pane.addCaretListener(mkfl_c_listener);
        mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);
        mkfl_editing_pane.setText(text);
        mkfl_editing_pane.setCaretPosition(caret_position);
    }

    inc_font_item.setText("Increase font size" + " (" + (current_font_size + 1) + ")");
    dec_font_item.setText("Decrease font size" + " (" + (current_font_size - 1) + ")");
}//GEN-LAST:event_inc_font_itemActionPerformed

private void dec_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dec_font_itemActionPerformed
    chars_inserted = 0;
    current_font_size -= 1;

    if (tab_pane.getSelectedIndex() == 0) {
        String text = editing_pane.getText();
        int caret_position = editing_pane.getCaretPosition();
        config.put("DefaultFont", set_font + current_font_size);
        editing_pane.setEditorKit(c_editor_kit);
        editing_pane.addFocusListener(f_listener);
        editing_pane.addCaretListener(c_listener);
        editing_pane.getDocument().addDocumentListener(listener);
        editing_pane.setText(text);
        editing_pane.setCaretPosition(caret_position);
    } else if (tab_pane.getSelectedIndex() == 1) {
        String text = mkfl_editing_pane.getText();
        int caret_position = mkfl_editing_pane.getCaretPosition();
        config.put("DefaultFont", set_font + current_font_size);
        mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
        mkfl_editing_pane.addFocusListener(mkfl_f_listener);
        mkfl_editing_pane.addCaretListener(mkfl_c_listener);
        mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);
        mkfl_editing_pane.setText(text);
        mkfl_editing_pane.setCaretPosition(caret_position);
    }

    inc_font_item.setText("Increase font size" + " (" + (current_font_size + 1) + ")");
    dec_font_item.setText("Decrease font size" + " (" + (current_font_size - 1) + ")");
}//GEN-LAST:event_dec_font_itemActionPerformed

private void def_font_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_def_font_itemActionPerformed
    chars_inserted = 0;
    current_font_size = set_font_size;

    if (tab_pane.getSelectedIndex() == 0) {
        String text = editing_pane.getText();
        int caret_position = editing_pane.getCaretPosition();
        config.put("DefaultFont", set_font + current_font_size);
        editing_pane.setEditorKit(c_editor_kit);
        editing_pane.addFocusListener(f_listener);
        editing_pane.addCaretListener(c_listener);
        editing_pane.getDocument().addDocumentListener(listener);
        editing_pane.setText(text);
        editing_pane.setCaretPosition(caret_position);
    } else if (tab_pane.getSelectedIndex() == 1) {
        String text = mkfl_editing_pane.getText();
        int caret_position = mkfl_editing_pane.getCaretPosition();
        config.put("DefaultFont", set_font + current_font_size);
        mkfl_editing_pane.setEditorKit(new BashSyntaxKit());
        mkfl_editing_pane.addFocusListener(mkfl_f_listener);
        mkfl_editing_pane.addCaretListener(mkfl_c_listener);
        mkfl_editing_pane.getDocument().addDocumentListener(mkfl_listener);
        mkfl_editing_pane.setText(text);
        mkfl_editing_pane.setCaretPosition(caret_position);
    }
}//GEN-LAST:event_def_font_itemActionPerformed

private void std_build_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_std_build_itemActionPerformed
    gen_makefile.setEnabled(false);
    if (tab_pane.getTabCount() > 1) {
        tab_pane.remove(1);
    }
    make = false;

    if (makefile != null) {
        makefile = os.equals("windows") ? new File(file_to_open.getParent() + "\\makefile") : new File(file_to_open.getParent() + "/makefile");
        makefile.delete();
    }

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
            String upload_string = os.equals("windows") ? "\tavrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n"
                    : "\tavrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n";
            String deletion_string = os.equals("windows") ? "\tdel " : "\trm -f ";

            mkfl_editing_pane.setText(
                    "#when compiling you must name the compilation rule to compile\n"
                    + "#when uploading you must name the upload rule to upload\n\n"
                    + "target=" + sketch_name + "\n\n"
                    + "compile:\n"
                    + "\tavr-gcc -std=c99 -g -Os -mmcu=" + mmcu + " -c " + "$(target).c" + " -o " + "$(target).o\n"
                    + "\tavr-gcc -g -mmcu=" + mmcu + " -o " + "$(target).elf" + " $(target).o\n"
                    + "\tavr-objcopy -j .text -j .data -O ihex " + "$(target).elf" + " $(target).hex\n"
                    + deletion_string + "$(target).o " + "\n\n"
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
                    File x = new File("/home/" + username + "/.avr4l_temp/" + temp_folder);
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
                    String[] cmd = {"cmd", "/c", "avr4l"};
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
                    String[] cmd = {"/bin/sh", "-c", "java -jar /usr/share/avr4l/AVR4L.jar"};
                    Process p = new ProcessBuilder(cmd).start();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }).start();
    }
}//GEN-LAST:event_new_file_itemActionPerformed

private void gen_makefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gen_makefileActionPerformed
    sketch_name = sketch_name.replace(".c", "");
    String upload_string = os.equals("windows") ? "avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n"
            : "avrdude -v -c " + prog_option + " -p " + mmcu + " -u -U flash:w:" + "$(target).hex:i\n";
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

private void pref_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_menu_itemActionPerformed
    int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
    int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;

    if (os.equals("windows")) {
        pref_frame.setSize(570, 270);
    } else {
        pref_frame.setSize(570, 357);
    }
    pref_frame.setLocation(screen_width / 2 - pref_frame.getWidth() / 2, screen_height / 2 - pref_frame.getHeight() / 2);
    pref_frame.setVisible(true);

    pref_font_txt_fld.setText(set_font + " " + set_font_size);

    ListSelectionListener list_sel_listener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            try {
                switch (pref_category_list.getSelectedIndex()) {
                    case 0:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[1].trim());
                        break;
                    case 1:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[1].trim());
                        break;
                    case 2:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[1].trim());
                        break;
                    case 3:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[1].trim());
                        break;
                    case 4:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[1].trim());
                        break;
                    case 5:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[1].trim());
                        break;
                    case 6:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[1].trim());
                        break;
                    case 7:
                        color = new Color(Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[0].replace("0x", ""), 16));
                        current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[1].trim());
                        break;
                }

                pref_color_txt_fld.setText("#" + Integer.toHexString(color.getRGB()).toUpperCase().substring(2));
                pref_color_txt_fld.setBackground(color);
                pref_style_combo_bx.setSelectedIndex(current_font_style);
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
    };

    pref_category_list.addListSelectionListener(list_sel_listener);
    pref_category_list.setSelectedIndex(0);
    pref_apply_btn.setEnabled(false);
}//GEN-LAST:event_pref_menu_itemActionPerformed

private void pref_color_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_color_btnActionPerformed
    JColorChooser color_chooser = new JColorChooser(color);
    color_chooser.setPreviewPanel(new JPanel());
    if (os.equals("windows")) {
        set_chooser_panel(color_chooser, "hsv");
    }

    String keyword_style = c_editor_kit.getProperty("Style.KEYWORD");
    String keyword2_style = c_editor_kit.getProperty("Style.KEYWORD2");
    String number_style = c_editor_kit.getProperty("Style.NUMBER");
    String string_style = c_editor_kit.getProperty("Style.STRING");
    String type_style = c_editor_kit.getProperty("Style.TYPE");
    String comment_style = c_editor_kit.getProperty("Style.COMMENT");
    String operator_style = c_editor_kit.getProperty("Style.OPERATOR");
    String identifier_style = c_editor_kit.getProperty("Style.IDENTIFIER");

    backup_keyword_color_style = new String(keyword_style);
    backup_keyword2_color_style = new String(keyword2_style);
    backup_number_color_style = new String(number_style);
    backup_string_color_style = new String(string_style);
    backup_type_color_style = new String(type_style);
    backup_comment_color_style = new String(comment_style);
    backup_operator_color_style = new String(operator_style);
    backup_identifier_color_style = new String(identifier_style);

    ActionListener ok_action_listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Color c = color_chooser.getColor();

            if (c != null) {
                color = c;
                try {
                    switch (pref_category_list.getSelectedIndex()) {
                        case 0:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[1].trim());
                            String color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.KEYWORD", color_style);
                            break;
                        case 1:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[1].trim());
                            color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.KEYWORD2", color_style);
                            break;
                        case 2:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[1].trim());
                            color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.NUMBER", color_style);
                            break;
                        case 3:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[1].trim());
                            color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.STRING", color_style);
                            break;
                        case 4:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[1].trim());
                            color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.COMMENT", color_style);
                            break;
                        case 5:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[1].trim());
                            color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.TYPE", color_style);
                            break;
                        case 6:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[1].trim());
                            color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.OPERATOR", color_style);
                            break;
                        case 7:
                            current_font_style = Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[1].trim());
                            color_style = "0x" + Integer.toHexString(color.getRGB()).substring(2) + ", " + current_font_style;
                            c_editor_kit.setProperty("Style.IDENTIFIER", color_style);
                            break;
                    }

                    pref_color_txt_fld.setText("#" + Integer.toHexString(color.getRGB()).toUpperCase().substring(2));
                    pref_color_txt_fld.setBackground(color);
                    pref_style_combo_bx.setSelectedIndex(current_font_style);
                } catch (Exception ex) {
                    System.err.println(ex.getCause());
                }
            }
        }
    };

    ActionListener cancel_action_listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("No color selected.");
        }
    };

    JDialog color_chooser_dialog = JColorChooser.createDialog(new JPanel(), "Choose a Color", true, color_chooser, ok_action_listener, cancel_action_listener);
    color_chooser_dialog.setResizable(false);
    color_chooser_dialog.setVisible(true);
    pref_apply_btn.setEnabled(true);
}//GEN-LAST:event_pref_color_btnActionPerformed

private void pref_cancel_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_cancel_btnActionPerformed
    if (backup_font != null && backup_font_size != -1) {
        set_font = backup_font;
        set_font_size = backup_font_size;
        DefaultSyntaxKit.initKit();
        Configuration new_config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
        new_config.put("DefaultFont", set_font + set_font_size);
        pref_font_txt_fld.setText(set_font + " " + set_font_size);
    }

    String ed_text = editing_pane.getText();

    if (backup_keyword_color_style != null) {
        System.out.println(backup_keyword_color_style);
        c_editor_kit.setProperty("Style.KEYWORD", backup_keyword_color_style);
    }
    if (backup_keyword2_color_style != null) {
        c_editor_kit.setProperty("Style.KEYWORD2", backup_keyword2_color_style);
    }
    if (backup_number_color_style != null) {
        c_editor_kit.setProperty("Style.NUMBER", backup_number_color_style);
    }
    if (backup_string_color_style != null) {
        c_editor_kit.setProperty("Style.STRING", backup_string_color_style);
    }
    if (backup_type_color_style != null) {
        c_editor_kit.setProperty("Style.TYPE", backup_type_color_style);
    }
    if (backup_comment_color_style != null) {
        c_editor_kit.setProperty("Style.COMMENT", backup_comment_color_style);
    }
    if (backup_operator_color_style != null) {
        c_editor_kit.setProperty("Style.OPERATOR", backup_operator_color_style);
    }
    if (backup_identifier_color_style != null) {
        c_editor_kit.setProperty("Style.IDENTIFIER", backup_identifier_color_style);
    }

    editing_pane.setEditorKit(c_editor_kit);

    editing_pane.setText(ed_text);

    String imported_color = "";
    int imported_style = -1;

    switch (pref_category_list.getSelectedIndex()) {
        case 0:
            imported_color = c_editor_kit.getProperty("Style.KEYWORD").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[1].trim());
            break;
        case 1:
            imported_color = c_editor_kit.getProperty("Style.KEYWORD2").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[1].trim());
            break;
        case 2:
            imported_color = c_editor_kit.getProperty("Style.NUMBER").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[1].trim());
            break;
        case 3:
            imported_color = c_editor_kit.getProperty("Style.STRING").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[1].trim());
            break;
        case 4:
            imported_color = c_editor_kit.getProperty("Style.COMMENT").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[1].trim());
            break;
        case 5:
            imported_color = c_editor_kit.getProperty("Style.TYPE").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[1].trim());
            break;
        case 6:
            imported_color = c_editor_kit.getProperty("Style.OPERATOR").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[1].trim());
            break;
        case 7:
            imported_color = c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[1].trim());
            break;
    }
    imported_color = imported_color.toLowerCase().replace("0x", "");
    int int_imported_color = Integer.parseInt(imported_color, 16);
    color = new Color(int_imported_color);
    pref_color_txt_fld.setText("#" + imported_color.toUpperCase());
    pref_color_txt_fld.setBackground(color);
    pref_style_combo_bx.setSelectedIndex(imported_style);

    backup_font = null;
    backup_font_size = -1;
    backup_keyword_color_style = null;
    backup_keyword2_color_style = null;
    backup_number_color_style = null;
    backup_string_color_style = null;
    backup_type_color_style = null;
    backup_comment_color_style = null;
    backup_operator_color_style = null;
    backup_identifier_color_style = null;
    pref_frame.setVisible(false);
}//GEN-LAST:event_pref_cancel_btnActionPerformed

private void pref_font_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_font_btnActionPerformed
    String[] result = get_font();

    for (String s : result) {
        if (s == null) {
            return;
        }
    }

    backup_font = new String(set_font);
    backup_font_size = new Integer(set_font_size);

    set_font = result[0];
    current_font_size = set_font_size = Integer.parseInt(result[1]);
    int font_style = Integer.parseInt(result[2]);

    DefaultSyntaxKit.initKit();
    Configuration new_config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
    new_config.put("DefaultFont", set_font + set_font_size);

    pref_font_txt_fld.setText(set_font + " " + set_font_size);
    pref_apply_btn.setEnabled(true);
}//GEN-LAST:event_pref_font_btnActionPerformed

private void serial_terminal_menu_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serial_terminal_menu_itemActionPerformed
    JSerialPanel serial_panel = new JSerialPanel();

    serial_panel.set_sending_text_field(serial_send_txt_fld);
    serial_panel.set_send_btn(serial_send_btn);
    serial_panel.set_receiving_pane(serial_rcv_text_pane);
    serial_panel.set_autoscroll_check_box(serial_autoscroll_chk_bx);
    serial_panel.set_port_label(serial_port_label);
    serial_panel.set_baud_rate_como_box(serial_baud_rate_combo_bx);
    serial_panel.set_serial_frame(serial_frame);
    serial_panel.set_port(port);

    serial_panel.showPanel();
}//GEN-LAST:event_serial_terminal_menu_itemActionPerformed

private void avr_isp_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avr_isp_itemActionPerformed
    port_menu.removeAll();
    port_menu.setEnabled(true);
    port_menu.setText("Port");
    serial_terminal_menu_item.setEnabled(true);

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
                        prog_option = "avrisp -b19200 -P " + currPortId.getName();
                        System.out.println(prog_option);
                    }
                });
                ports_button_group.add(new_port);
                port_menu.add(new_port);
                port = currPortId.getName();
                new_port.setSelected(true);
                prog_option = "avrisp -b19200 -P " + currPortId.getName();
                gen_makefileActionPerformed(evt);
                counter++;
            }
            if (counter < 1) {
                port_menu.setEnabled(false);
                serial_terminal_menu_item.setEnabled(false);
            }
        }
    }).start();
}//GEN-LAST:event_avr_isp_itemActionPerformed

private void pref_apply_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_apply_btnActionPerformed
    String ed_text = editing_pane.getText();

    String keyword_style = c_editor_kit.getProperty("Style.KEYWORD");
    String keyword2_style = c_editor_kit.getProperty("Style.KEYWORD2");
    String number_style = c_editor_kit.getProperty("Style.NUMBER");
    String string_style = c_editor_kit.getProperty("Style.STRING");
    String type_style = c_editor_kit.getProperty("Style.TYPE");
    String comment_style = c_editor_kit.getProperty("Style.COMMENT");
    String operator_style = c_editor_kit.getProperty("Style.OPERATOR");
    String identifier_style = c_editor_kit.getProperty("Style.IDENTIFIER");

    editing_pane.setEditorKit(c_editor_kit);

    c_editor_kit.setProperty("Style.KEYWORD", keyword_style);
    c_editor_kit.setProperty("Style.KEYWORD2", keyword2_style);
    c_editor_kit.setProperty("Style.NUMBER", number_style);
    c_editor_kit.setProperty("Style.STRING", string_style);
    c_editor_kit.setProperty("Style.TYPE", type_style);
    c_editor_kit.setProperty("Style.COMMENT", comment_style);
    c_editor_kit.setProperty("Style.OPERATOR", operator_style);
    c_editor_kit.setProperty("Style.IDENTIFIER", identifier_style);

    editing_pane.setText(ed_text);

    String imported_color = "";
    int imported_style = -1;

    switch (pref_category_list.getSelectedIndex()) {
        case 0:
            imported_color = c_editor_kit.getProperty("Style.KEYWORD").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[1].trim());
            break;
        case 1:
            imported_color = c_editor_kit.getProperty("Style.KEYWORD2").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[1].trim());
            break;
        case 2:
            imported_color = c_editor_kit.getProperty("Style.NUMBER").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[1].trim());
            break;
        case 3:
            imported_color = c_editor_kit.getProperty("Style.STRING").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[1].trim());
            break;
        case 4:
            imported_color = c_editor_kit.getProperty("Style.COMMENT").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[1].trim());
            break;
        case 5:
            imported_color = c_editor_kit.getProperty("Style.TYPE").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[1].trim());
            break;
        case 6:
            imported_color = c_editor_kit.getProperty("Style.OPERATOR").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[1].trim());
            break;
        case 7:
            imported_color = c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[0].trim();
            imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[1].trim());
            break;
    }
    imported_color = imported_color.toLowerCase().replace("0x", "");
    int int_imported_color = Integer.parseInt(imported_color, 16);
    color = new Color(int_imported_color);
    pref_font_txt_fld.setText(set_font + " " + set_font_size);
    pref_color_txt_fld.setText("#" + imported_color.toUpperCase());
    pref_color_txt_fld.setBackground(color);
    pref_style_combo_bx.setSelectedIndex(imported_style);
    pref_apply_btn.setEnabled(false);
}//GEN-LAST:event_pref_apply_btnActionPerformed

private void pref_ok_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_ok_btnActionPerformed
    String ed_text = editing_pane.getText();
    String keyword_style = c_editor_kit.getProperty("Style.KEYWORD");
    String keyword2_style = c_editor_kit.getProperty("Style.KEYWORD2");
    String number_style = c_editor_kit.getProperty("Style.NUMBER");
    String string_style = c_editor_kit.getProperty("Style.STRING");
    String type_style = c_editor_kit.getProperty("Style.TYPE");
    String comment_style = c_editor_kit.getProperty("Style.COMMENT");
    String operator_style = c_editor_kit.getProperty("Style.OPERATOR");
    String identifier_style = c_editor_kit.getProperty("Style.IDENTIFIER");

//        System.out.println(c_editor_kit.getProperty("Style.KEYWORD"));
//        System.out.println(c_editor_kit.getProperty("Style.KEYWORD2"));
//        System.out.println(c_editor_kit.getProperty("Style.NUMBER"));
//        System.out.println(c_editor_kit.getProperty("Style.STRING"));
//        System.out.println(c_editor_kit.getProperty("Style.TYPE"));
//        System.out.println(c_editor_kit.getProperty("Style.COMMENT"));
//        System.out.println(c_editor_kit.getProperty("Style.OPERATOR"));
//        System.out.println(c_editor_kit.getProperty("Style.IDENTIFIER"));
    editing_pane.setEditorKit(c_editor_kit);

    c_editor_kit.setProperty("Style.KEYWORD", keyword_style);
    c_editor_kit.setProperty("Style.KEYWORD2", keyword2_style);
    c_editor_kit.setProperty("Style.NUMBER", number_style);
    c_editor_kit.setProperty("Style.STRING", string_style);
    c_editor_kit.setProperty("Style.TYPE", type_style);
    c_editor_kit.setProperty("Style.COMMENT", comment_style);
    c_editor_kit.setProperty("Style.OPERATOR", operator_style);
    c_editor_kit.setProperty("Style.IDENTIFIER", identifier_style);

    editing_pane.setText(ed_text);

    prefs.put("keyword", c_editor_kit.getProperty("Style.KEYWORD"));
    prefs.put("keyword2", c_editor_kit.getProperty("Style.KEYWORD2"));
    prefs.put("number", c_editor_kit.getProperty("Style.NUMBER"));
    prefs.put("string", c_editor_kit.getProperty("Style.STRING"));
    prefs.put("type", c_editor_kit.getProperty("Style.TYPE"));
    prefs.put("comment", c_editor_kit.getProperty("Style.COMMENT"));
    prefs.put("operator", c_editor_kit.getProperty("Style.OPERATOR"));
    prefs.put("identifier", c_editor_kit.getProperty("Style.IDENTIFIER"));
    prefs.put("default_font", set_font);
    prefs.putInt("default_font_size", set_font_size);

//        System.out.println(c_editor_kit.getProperty("Style.KEYWORD"));
//        System.out.println(c_editor_kit.getProperty("Style.KEYWORD2"));
//        System.out.println(c_editor_kit.getProperty("Style.NUMBER"));
//        System.out.println(c_editor_kit.getProperty("Style.STRING"));
//        System.out.println(c_editor_kit.getProperty("Style.TYPE"));
//        System.out.println(c_editor_kit.getProperty("Style.COMMENT"));
//        System.out.println(c_editor_kit.getProperty("Style.OPERATOR"));
//        System.out.println(c_editor_kit.getProperty("Style.IDENTIFIER"));
    backup_font = null;
    backup_font_size = -1;
    backup_keyword_color_style = null;
    backup_keyword2_color_style = null;
    backup_number_color_style = null;
    backup_string_color_style = null;
    backup_type_color_style = null;
    backup_comment_color_style = null;
    backup_operator_color_style = null;
    backup_identifier_color_style = null;
    pref_frame.setVisible(false);
}//GEN-LAST:event_pref_ok_btnActionPerformed

private void search_fieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_search_fieldFocusLost
    if (search_field.getText().length() < 1) {
        search_field.setForeground(new Color(180, 180, 180));
        search_field.setText("Find microcontrollers");
    }
}//GEN-LAST:event_search_fieldFocusLost

private void search_fieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_search_fieldFocusGained
    if (search_field.getText().toLowerCase().contains("find microcontrollers")) {
        search_field.setForeground(new Color(76, 76, 76));
        search_field.setText(null);
    }
}//GEN-LAST:event_search_fieldFocusGained

private void pref_export_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_export_btnActionPerformed
    try {
        JSONObject json_data = new JSONObject();

        String keyword_style = c_editor_kit.getProperty("Style.KEYWORD");
        String keyword2_style = c_editor_kit.getProperty("Style.KEYWORD2");
        String number_style = c_editor_kit.getProperty("Style.NUMBER");
        String string_style = c_editor_kit.getProperty("Style.STRING");
        String comment_style = c_editor_kit.getProperty("Style.COMMENT");
        String type_style = c_editor_kit.getProperty("Style.TYPE");
        String operator_style = c_editor_kit.getProperty("Style.OPERATOR");
        String identifier_style = c_editor_kit.getProperty("Style.IDENTIFIER");

        json_data.put("font", set_font.trim() + ", " + set_font_size);
        json_data.put("keyword", keyword_style);
        json_data.put("keyword2", keyword2_style);
        json_data.put("number", number_style);
        json_data.put("string", string_style);
        json_data.put("comment", comment_style);
        json_data.put("type", type_style);
        json_data.put("operator", operator_style);
        json_data.put("identifier", identifier_style);

        String json_data_string = json_data.toString(3);

        DateFormat dateFormat = new SimpleDateFormat("dd_MMM_YY_hh_mm_ss_a");
        String date = dateFormat.format(Calendar.getInstance().getTime());

        FileDialog fd = new FileDialog(this, "Export preferences", FileDialog.SAVE);
        fd.setTitle("Export preferences");
        fd.setFile("preferences_" + date + ".json");
        fd.setVisible(true);
        String selected = fd.getDirectory() + fd.getFile();

        if (!selected.contains("null")) {
            File json_data_file = new File(selected);

            try (PrintWriter writer = new PrintWriter(json_data_file, "UTF-8")) {
                writer.println(json_data_string);
            }
        }

        JOptionPane.showMessageDialog(this, "preferences exported successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
        System.err.println(ex.getMessage());
    }
}//GEN-LAST:event_pref_export_btnActionPerformed

private void pref_import_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_import_btnActionPerformed
    try {
        FileDialog fd = new FileDialog(this, "Export preferences", FileDialog.LOAD);
        fd.setTitle("Export preferences");
        fd.setVisible(true);
        String selected = fd.getDirectory() + fd.getFile();

        if (!selected.contains("null")) {
            File json_data_file = new File(selected);
            Scanner scan = new Scanner(json_data_file);
            String text = "";
            while (scan.hasNext()) {
                text += scan.nextLine();
            }
            scan.close();

//            System.out.println(text);
            JSONObject json_data = new JSONObject(text);

            System.out.println(json_data.toString(3));

            String[] imported_font_prefs = json_data.getString("font").split(",");
            String imported_font = imported_font_prefs[0].trim() + " ";
            int imported_font_size = Integer.parseInt(imported_font_prefs[1].trim());

            DefaultSyntaxKit.initKit();
            Configuration new_config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
            new_config.put("DefaultFont", imported_font + imported_font_size);

            set_font = imported_font;
            current_font_size = set_font_size = imported_font_size;

            c_editor_kit.setProperty("Style.KEYWORD", json_data.getString("keyword"));
            c_editor_kit.setProperty("Style.KEYWORD2", json_data.getString("keyword2"));
            c_editor_kit.setProperty("Style.NUMBER", json_data.getString("number"));
            c_editor_kit.setProperty("Style.STRING", json_data.getString("string"));
            c_editor_kit.setProperty("Style.COMMENT", json_data.getString("comment"));
            c_editor_kit.setProperty("Style.TYPE", json_data.getString("type"));
            c_editor_kit.setProperty("Style.OPERATOR", json_data.getString("operator"));
            c_editor_kit.setProperty("Style.IDENTIFIER", json_data.getString("identifier"));

            String imported_color = "";
            int imported_style = -1;

            switch (pref_category_list.getSelectedIndex()) {
                case 0:
                    imported_color = c_editor_kit.getProperty("Style.KEYWORD").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD").split(",")[1].trim());
                    break;
                case 1:
                    imported_color = c_editor_kit.getProperty("Style.KEYWORD2").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.KEYWORD2").split(",")[1].trim());
                    break;
                case 2:
                    imported_color = c_editor_kit.getProperty("Style.NUMBER").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.NUMBER").split(",")[1].trim());
                    break;
                case 3:
                    imported_color = c_editor_kit.getProperty("Style.STRING").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.STRING").split(",")[1].trim());
                    break;
                case 4:
                    imported_color = c_editor_kit.getProperty("Style.COMMENT").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.COMMENT").split(",")[1].trim());
                    break;
                case 5:
                    imported_color = c_editor_kit.getProperty("Style.TYPE").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.TYPE").split(",")[1].trim());
                    break;
                case 6:
                    imported_color = c_editor_kit.getProperty("Style.OPERATOR").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.OPERATOR").split(",")[1].trim());
                    break;
                case 7:
                    imported_color = c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[0].trim();
                    imported_style = Integer.parseInt(c_editor_kit.getProperty("Style.IDENTIFIER").split(",")[1].trim());
                    break;
            }
            imported_color = imported_color.toLowerCase().replace("0x", "");
            int int_imported_color = Integer.parseInt(imported_color, 16);
            color = new Color(int_imported_color);
            pref_font_txt_fld.setText(imported_font + imported_font_size);
            pref_color_txt_fld.setText("#" + imported_color.toUpperCase());
            pref_color_txt_fld.setBackground(color);
            pref_style_combo_bx.setSelectedIndex(imported_style);

            pref_apply_btnActionPerformed(new ActionEvent(this, 0, "apply defaults"));

            JOptionPane.showMessageDialog(this, "preferences imported successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (FileNotFoundException ex) {
        System.err.println(ex.getMessage());
    }
}//GEN-LAST:event_pref_import_btnActionPerformed

private void pref_defaults_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pref_defaults_btnActionPerformed
    String ed_text = editing_pane.getText();

    pref_font_txt_fld.setText(default_font);

    DefaultSyntaxKit.initKit();
    Configuration new_config = DefaultSyntaxKit.getConfig(DefaultSyntaxKit.class);
    new_config.put("DefaultFont", default_font + default_font_size);

    set_font = default_font;
    current_font_size = set_font_size = default_font_size;

    c_editor_kit.setProperty("Style.KEYWORD", default_keyword_color_style);
    c_editor_kit.setProperty("Style.KEYWORD2", default_keyword2_color_style);
    c_editor_kit.setProperty("Style.NUMBER", default_number_color_style);
    c_editor_kit.setProperty("Style.STRING", default_string_color_style);
    c_editor_kit.setProperty("Style.TYPE", default_type_color_style);
    c_editor_kit.setProperty("Style.COMMENT", default_comment_color_style);
    c_editor_kit.setProperty("Style.OPERATOR", default_operator_color_style);
    c_editor_kit.setProperty("Style.IDENTIFIER", default_identifier_color_style);

    editing_pane.setText(ed_text);

    pref_apply_btnActionPerformed(new ActionEvent(this, 0, "apply defaults"));
}//GEN-LAST:event_pref_defaults_btnActionPerformed

    private void serial_clear_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serial_clear_btnActionPerformed
        serial_rcv_text_pane.setText(null);
    }//GEN-LAST:event_serial_clear_btnActionPerformed

    private void pref_color_txt_fldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pref_color_txt_fldMouseClicked
        pref_color_btnActionPerformed(new ActionEvent(this, 0, "change color"));
    }//GEN-LAST:event_pref_color_txt_fldMouseClicked

    private void pref_style_combo_bxPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_pref_style_combo_bxPopupMenuWillBecomeVisible
        String keyword_style = c_editor_kit.getProperty("Style.KEYWORD");
        String keyword2_style = c_editor_kit.getProperty("Style.KEYWORD2");
        String number_style = c_editor_kit.getProperty("Style.NUMBER");
        String string_style = c_editor_kit.getProperty("Style.STRING");
        String type_style = c_editor_kit.getProperty("Style.TYPE");
        String comment_style = c_editor_kit.getProperty("Style.COMMENT");
        String operator_style = c_editor_kit.getProperty("Style.OPERATOR");
        String identifier_style = c_editor_kit.getProperty("Style.IDENTIFIER");

        backup_keyword_color_style = new String(keyword_style);
        backup_keyword2_color_style = new String(keyword2_style);
        backup_number_color_style = new String(number_style);
        backup_string_color_style = new String(string_style);
        backup_type_color_style = new String(type_style);
        backup_comment_color_style = new String(comment_style);
        backup_operator_color_style = new String(operator_style);
        backup_identifier_color_style = new String(identifier_style);
    }//GEN-LAST:event_pref_style_combo_bxPopupMenuWillBecomeVisible

    private void pref_style_combo_bxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_pref_style_combo_bxPopupMenuWillBecomeInvisible
        String hex_color = pref_color_txt_fld.getText().replace("#", "").toLowerCase();
        int int_color = Integer.parseInt(hex_color, 16);
        int style = pref_style_combo_bx.getSelectedIndex();

        switch (pref_category_list.getSelectedIndex()) {
            case 0:
                String color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.KEYWORD", color_style);
                break;
            case 1:
                color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.KEYWORD2", color_style);
                break;
            case 2:
                color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.NUMBER", color_style);
                break;
            case 3:
                color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.STRING", color_style);
                break;
            case 4:
                color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.COMMENT", color_style);
                break;
            case 5:
                color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.TYPE", color_style);
                break;
            case 6:
                color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.OPERATOR", color_style);
                break;
            case 7:
                color_style = "0x" + hex_color + ", " + style;
                c_editor_kit.setProperty("Style.IDENTIFIER", color_style);
                break;
        }

        pref_apply_btn.setEnabled(true);
    }//GEN-LAST:event_pref_style_combo_bxPopupMenuWillBecomeInvisible

    public static void main(String args[]) {
        try {
            LookAndFeelInfo info = UIManager.getInstalledLookAndFeels()[3];
            UIManager.setLookAndFeel(info.getClassName());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main_Frame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main_Frame(args).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem about_menu_item;
    private javax.swing.JCheckBoxMenuItem arduino_item;
    private javax.swing.JCheckBoxMenuItem avr_isp_item;
    private javax.swing.JPopupMenu.Separator build_menu_sep;
    private javax.swing.ButtonGroup build_options_button_group;
    private javax.swing.JMenu build_opts_menu;
    private javax.swing.JLabel char_ins_label;
    private javax.swing.JTextPane console_pane;
    private javax.swing.JScrollPane console_scroll_pane;
    private javax.swing.JMenuItem dec_font_item;
    private javax.swing.JMenuItem def_font_item;
    private javax.swing.JEditorPane editing_pane;
    public static javax.swing.JScrollPane editing_scroll_pane;
    private javax.swing.JMenuItem exit_menu_item;
    private javax.swing.JMenu file_menu;
    private javax.swing.JMenuItem gen_makefile;
    private javax.swing.JMenuItem inc_font_item;
    private javax.swing.JLabel iteration_label;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JComboBox mcuCombo;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JCheckBoxMenuItem mkfl_build_item;
    private javax.swing.JEditorPane mkfl_editing_pane;
    public static javax.swing.JScrollPane mkfl_editing_scroll_pane;
    private javax.swing.JLabel mmcu_port_label;
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
    private javax.swing.JButton pref_defaults_btn;
    private javax.swing.JButton pref_export_btn;
    private javax.swing.JButton pref_font_btn;
    private javax.swing.JLabel pref_font_label;
    private javax.swing.JTextField pref_font_txt_fld;
    private javax.swing.JFrame pref_frame;
    private javax.swing.JButton pref_import_btn;
    private javax.swing.JMenuItem pref_menu_item;
    private javax.swing.JButton pref_ok_btn;
    private javax.swing.JComboBox<String> pref_style_combo_bx;
    private javax.swing.JLabel pref_style_label;
    private javax.swing.JDialog privacy_dialog;
    private javax.swing.JScrollPane privacy_scroll_pane;
    private javax.swing.JTextPane privacy_text_pane;
    private javax.swing.JMenu prog_options_menu;
    private javax.swing.ButtonGroup programmer_options_button_group;
    private javax.swing.JLabel row_col_label;
    private javax.swing.JMenuItem save_as_menu_item;
    private javax.swing.JMenuItem save_menu_item;
    private javax.swing.JTextField search_field;
    private javax.swing.JPopupMenu.Separator separator;
    private javax.swing.JCheckBox serial_autoscroll_chk_bx;
    private javax.swing.JComboBox<String> serial_baud_rate_combo_bx;
    private javax.swing.JLabel serial_baud_rate_label;
    private javax.swing.JButton serial_clear_btn;
    private javax.swing.JFrame serial_frame;
    private javax.swing.JLabel serial_port_label;
    private javax.swing.JScrollPane serial_rcv_scroll_pane;
    private javax.swing.JTextPane serial_rcv_text_pane;
    private javax.swing.JButton serial_send_btn;
    private javax.swing.JTextField serial_send_txt_fld;
    private javax.swing.JMenuItem serial_terminal_menu_item;
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