package com.mg;

import com.mg.enums.TimeCardStatus;
import com.mg.service.AutoTimeCardService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public class TimeCardUI extends JFrame {

    private JButton clockInButton;
    private JButton clockOutButton;
    private JButton clearButton;
    private JTextArea logArea;
    private JCheckBox debugCheckBox;

    public TimeCardUI() {
        setTitle("自動打卡程式");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // --- Components ---
        clockInButton = new JButton("上班打卡 (on)");
        clockOutButton = new JButton("下班打卡 (off)");
        clearButton = new JButton("清除");
        debugCheckBox = new JCheckBox("除錯模式 (debug)");
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setMargin(new Insets(5,5,5,5));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);

        // --- Layout ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(clockInButton);
        topPanel.add(clockOutButton);
        topPanel.add(clearButton);
        topPanel.add(debugCheckBox);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- Redirect System.out to JTextArea ---
        try {
            PrintStream printStream = new PrintStream(new CustomOutputStream(logArea), true, "UTF-8");
            System.setOut(printStream);
            System.setErr(printStream);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // --- Event Listeners ---
        clockInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runTimeCard(TimeCardStatus.ON);
            }
        });

        clockOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runTimeCard(TimeCardStatus.OFF);
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logArea.setText("");
            }
        });
    }

    private void runTimeCard(TimeCardStatus status) {
        // Disable buttons to prevent multiple clicks
        clockInButton.setEnabled(false);
        clockOutButton.setEnabled(false);
        debugCheckBox.setEnabled(false);

        logArea.setText(""); // Clear log area
        System.out.println("準備執行 " + status.name() + "...");

        // Run the service in a separate thread to avoid freezing the UI
        new Thread(() -> {
            try {
                boolean isDebug = debugCheckBox.isSelected();
                new AutoTimeCardService().checkin(status, isDebug);
                System.out.println("執行完成！");
            } catch (Exception ex) {
                System.out.println("發生錯誤: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                // Re-enable buttons on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    clockInButton.setEnabled(true);
                    clockOutButton.setEnabled(true);
                    debugCheckBox.setEnabled(true);
                });
            }
        }).start();
    }

    // Custom OutputStream to redirect console output to JTextArea
    public static class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            // redirects data to the text area
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}
