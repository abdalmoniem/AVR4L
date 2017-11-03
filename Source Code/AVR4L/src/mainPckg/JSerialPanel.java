/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainPckg;

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
import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import com.fazecast.jSerialComm.*;
import java.awt.Toolkit;
import javax.swing.JLabel;

/**
 *
 * @author mn3m
 *
 * @e-mail hifnawy_moniem@hotmail.com
 *
 */
public class JSerialPanel {

    private DataOutputStream output;
    private SerialPort serial_port = null;

    private JTextField send_txt_field;
    private JButton send_btn;
    private JTextPane rcv_editor_pane;
    private JCheckBox autoscroll_chkbx;
    private JLabel serial_port_label;
    private JComboBox baud_rate_combo;
    private JFrame serial_frame;
    private String port;

    private static final int TIME_OUT = 2000;

    private final int MODE_WARNING = 0;
    private final int MODE_ERROR = 1;
    private final int MODE_CONSOLE = 2;

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
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        
        serial_frame.setSize(700, 500);
        serial_frame.setLocation(screen_width / 2 - serial_frame.getWidth() / 2, screen_height / 2 - serial_frame.getHeight() / 2);
        serial_frame.setVisible(true);
        rcv_editor_pane.setText(null);
        serial_port_label.setText("Port: " + port);

        serial_port = SerialPort.getCommPort(port);
        serial_port.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serial_port.setComPortTimeouts(TIME_OUT, TIME_OUT, TIME_OUT);
        serial_port.openPort();
        output = new DataOutputStream(serial_port.getOutputStream());

        serial_port.addDataListener(spdl);

        append_to_pane(rcv_editor_pane, "Serial Terminal Ready.\n", MODE_CONSOLE);
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

    public void set_port_label(JLabel lbl) {
        this.serial_port_label = lbl;
    }

    public void set_baud_rate_como_box(JComboBox cmbx) {
        this.baud_rate_combo = cmbx;
        this.baud_rate_combo.addItemListener(il);
    }

    SerialPortDataListener spdl = new SerialPortDataListener() {
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent spe) {
            if (spe.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                return;
            }
            byte[] newData = new byte[serial_port.bytesAvailable()];
            int numRead = serial_port.readBytes(newData, newData.length);

            String data = new String(newData);
            append_to_pane(rcv_editor_pane, data, MODE_CONSOLE);

            if (autoscroll_chkbx.isSelected()) {
                rcv_editor_pane.setCaretPosition(rcv_editor_pane.getDocument().getLength());
            }
        }
    };

    WindowAdapter wl = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
//            close();
            serial_port.closePort();
        }
    };

    ItemListener il = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            int baud_rate = Integer.parseInt(baud_rate_combo.getSelectedItem().toString());
            serial_port.closePort();
            rcv_editor_pane.setText(null);

            serial_port = SerialPort.getCommPort(port);
            serial_port.setComPortParameters(baud_rate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            serial_port.openPort();

            serial_port.addDataListener(spdl);
        }
    };

    ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String text = send_txt_field.getText();
                System.out.println(text);
                for (int i = 0; i < text.length(); i++) {
                    output.write(text.charAt(i));
                }
                output.flush();
                send_txt_field.setText(null);
            } catch (IOException ex) {
                System.err.println(ex.toString());
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