/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainPckg;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author mn3m
 */
public class JSerialPanel {

    private DataInputStream input;
    private DataOutputStream output;
    private SerialPort serialPort = null;
    private CommPortIdentifier portId = null;

    private JTextField send_txt_field;
    private JButton send_btn;
    private JTextPane rcv_editor_pane;
    private JCheckBox autoscroll_chkbx;
    private JComboBox baud_rate_combo;
    private JFrame serial_frame;
    private String port;

    private static final int TIME_OUT = 2000;

    private int errors = 0;

    private void append_to_pane(JTextPane pane, String msg, int mode) {
        try {
            StyledDocument doc = pane.getStyledDocument();
            SimpleAttributeSet attr = new SimpleAttributeSet();
            switch (mode) {
                case 0:
                    //warning
                    StyleConstants.setForeground(attr, Color.YELLOW);
                    StyleConstants.setBold(attr, true);
                    doc.insertString(doc.getLength(), msg, attr);
                    break;
                case 1:
                    //error
                    StyleConstants.setForeground(attr, Color.RED);
                    StyleConstants.setBold(attr, true);
                    doc.insertString(doc.getLength(), msg, attr);
                    break;
                default:
                    //normal text in console area
                    StyleConstants.setForeground(attr, Color.BLACK);
                    doc.insertString(doc.getLength(), msg, attr);
                    break;
            }
        } catch (BadLocationException ex) {
            System.err.println(ex.toString());
        }
    }

    public void showPanel() {
        try {
            serial_frame.setSize(700, 500);
            serial_frame.setVisible(true);
            rcv_editor_pane.setText(null);

            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
            while (portEnum.hasMoreElements()) {
                CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

                if (currPortId.getName().toLowerCase().equals(port.toLowerCase())) {
                    portId = currPortId;
                    break;
                }
            }

            if (portId != null) {
                serialPort = (SerialPort) portId.open(JSerialPanel.class.getName(), TIME_OUT);
                serialPort.setSerialPortParams(9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                input = new DataInputStream(serialPort.getInputStream());
                output = new DataOutputStream(serialPort.getOutputStream());
                serialPort.addEventListener(spel);
                serialPort.notifyOnDataAvailable(true);
            }
        } catch (PortInUseException | IOException | TooManyListenersException | UnsupportedCommOperationException ex) {
            System.err.println(ex.toString());
        }
    }

    public void set_serial_frame(JFrame frame) {
        this.serial_frame = frame;
        this.serial_frame.addWindowListener(wl);
    }

    public void set_port(String port) {
        this.port = port;
    }

    public void set_receiving_pane(JTextPane pane) {
        this.rcv_editor_pane = pane;
    }

    public void set_sending_text_field(JTextField field) {
        this.send_txt_field = field;
        this.send_txt_field.addKeyListener(kl);
    }

    public void set_send_btn(JButton btn) {
        this.send_btn = btn;
        this.send_btn.addActionListener(al);
    }

    public void set_autoscroll_check_box(JCheckBox chkbx) {
        this.autoscroll_chkbx = chkbx;
        this.autoscroll_chkbx.setSelected(true);
    }

    public void set_baud_rate_como_box(JComboBox cmbx) {
        this.baud_rate_combo = cmbx;
        this.baud_rate_combo.addItemListener(il);
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    SerialPortEventListener spel = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    String data;
                    
                    if ((input.available() - 2) > 1) {
                        System.out.println("here");
                        data = input.readLine().trim() + "\n";
                    }
                    else {
                        System.out.println("there");
                        char recv = (char) input.read();
                        data = recv + "";
                        data = data.trim();
                    }
                    
                    append_to_pane(rcv_editor_pane, data + "", 2);

                    if (autoscroll_chkbx.isSelected()) {
                        rcv_editor_pane.setCaretPosition(rcv_editor_pane.getDocument().getLength());
                    }
                } catch (IOException ex) {
                    System.err.println("ERROR: " + ex.getMessage());
                    append_to_pane(rcv_editor_pane, "ERROR: " + ex.getMessage() + "\n", 1);

                    if (autoscroll_chkbx.isSelected()) {
                        rcv_editor_pane.setCaretPosition(rcv_editor_pane.getDocument().getLength());
                    }

                    errors++;

                    if (errors >= 5) {
                        append_to_pane(rcv_editor_pane, "Cannot communicate with serial device, "
                                + "please check if device is connected and restart the serial terminal." + "\n", 1);
                        close();
                    }
                }
            }
        }
    };

    WindowAdapter wl = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            close();
        }
    };

    ItemListener il = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            try {
                int baud_rate = Integer.parseInt(baud_rate_combo.getSelectedItem().toString());
                close();
                rcv_editor_pane.setText(null);

                serialPort = (SerialPort) portId.open(JSerialPanel.class.getName(), TIME_OUT);
                serialPort.setSerialPortParams(baud_rate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                input = new DataInputStream(serialPort.getInputStream());
                output = new DataOutputStream(serialPort.getOutputStream());
                serialPort.addEventListener(spel);
                serialPort.notifyOnDataAvailable(true);
            } catch (UnsupportedCommOperationException ex) {
                System.err.println(ex.toString());
            } catch (IOException | TooManyListenersException | PortInUseException ex) {
                System.err.println(ex.toString());
            }
        }
    };

    ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String text = send_txt_field.getText();
                output.writeUTF(text);
                output.flush();
                send_txt_field.setText(null);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    };

    KeyListener kl = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            if ((int) e.getKeyChar() == 10) { //enter
                al.actionPerformed(new ActionEvent(this, 0, "send data"));
            }
        }
    };
}