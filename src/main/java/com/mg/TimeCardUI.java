package com.mg;

import com.mg.config.ConfigManager;
import com.mg.enums.TimeCardStatus;
import com.mg.service.AutoTimeCardService;
import com.mg.service.TimeCardScheduler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;


public class TimeCardUI extends JFrame {

    private final TimeCardScheduler scheduler = new TimeCardScheduler();

    private JButton clockInButton;
    private JButton clockOutButton;
    private JButton clearButton;
    private JTextArea logArea;
    private JCheckBox debugCheckBox;

    // Schedule components
    private JCheckBox scheduleOnEnabled;
    private JSpinner onTimeSpinner;
    private JCheckBox scheduleOffEnabled;
    private JSpinner offTimeSpinner;
    private JButton saveScheduleButton;


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

        // --- Schedule Panel ---
        JPanel schedulePanel = new JPanel();
        schedulePanel.setBorder(BorderFactory.createTitledBorder("定時打卡設定"));
        schedulePanel.setLayout(new GridLayout(3, 3, 5, 5));

        scheduleOnEnabled = new JCheckBox("啟用定時上班打卡");
        onTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor onTimeEditor = new JSpinner.DateEditor(onTimeSpinner, "HH:mm");
        onTimeSpinner.setEditor(onTimeEditor);

        scheduleOffEnabled = new JCheckBox("啟用定時下班打卡");
        offTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor offTimeEditor = new JSpinner.DateEditor(offTimeSpinner, "HH:mm");
        offTimeSpinner.setEditor(offTimeEditor);

        saveScheduleButton = new JButton("儲存定時設定");

        schedulePanel.add(scheduleOnEnabled);
        schedulePanel.add(new JLabel("上班時間:"));
        schedulePanel.add(onTimeSpinner);
        schedulePanel.add(scheduleOffEnabled);
        schedulePanel.add(new JLabel("下班時間:"));
        schedulePanel.add(offTimeSpinner);
        schedulePanel.add(new JLabel()); // placeholder
        schedulePanel.add(saveScheduleButton);
        schedulePanel.add(new JLabel()); // placeholder


        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(schedulePanel, BorderLayout.SOUTH);

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

        saveScheduleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSchedule();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                scheduler.shutdown();
                super.windowClosing(e);
            }
        });

        // Load initial schedule
        loadSchedule();
        updateSchedules();
    }

    private void loadSchedule() {
        // Load ON_DUTY schedule
        scheduleOnEnabled.setSelected(ConfigManager.isScheduleEnabled(TimeCardStatus.ON));
        Calendar onCal = Calendar.getInstance();
        onCal.set(Calendar.HOUR_OF_DAY, ConfigManager.getScheduleHour(TimeCardStatus.ON));
        onCal.set(Calendar.MINUTE, ConfigManager.getScheduleMinute(TimeCardStatus.ON));
        onTimeSpinner.setValue(onCal.getTime());

        // Load OFF_DUTY schedule
        scheduleOffEnabled.setSelected(ConfigManager.isScheduleEnabled(TimeCardStatus.OFF));
        Calendar offCal = Calendar.getInstance();
        offCal.set(Calendar.HOUR_OF_DAY, ConfigManager.getScheduleHour(TimeCardStatus.OFF));
        offCal.set(Calendar.MINUTE, ConfigManager.getScheduleMinute(TimeCardStatus.OFF));
        offTimeSpinner.setValue(offCal.getTime());
    }

    private void saveSchedule() {
        // Save ON_DUTY schedule
        ConfigManager.setProperty("schedule.on.enabled", String.valueOf(scheduleOnEnabled.isSelected()));
        Date onDate = (Date) onTimeSpinner.getValue();
        Calendar onCal = Calendar.getInstance();
        onCal.setTime(onDate);
        ConfigManager.setProperty("schedule.on.hour", String.valueOf(onCal.get(Calendar.HOUR_OF_DAY)));
        ConfigManager.setProperty("schedule.on.minute", String.valueOf(onCal.get(Calendar.MINUTE)));

        // Save OFF_DUTY schedule
        ConfigManager.setProperty("schedule.off.enabled", String.valueOf(scheduleOffEnabled.isSelected()));
        Date offDate = (Date) offTimeSpinner.getValue();
        Calendar offCal = Calendar.getInstance();
        offCal.setTime(offDate);
        ConfigManager.setProperty("schedule.off.hour", String.valueOf(offCal.get(Calendar.HOUR_OF_DAY)));
        ConfigManager.setProperty("schedule.off.minute", String.valueOf(offCal.get(Calendar.MINUTE)));

        ConfigManager.save();
        System.out.println("定時設定已儲存！");

        // Reschedule tasks
        updateSchedules();
    }

    private void updateSchedules() {
        if (scheduleOnEnabled.isSelected()) {
            Date onDate = (Date) onTimeSpinner.getValue();
            Calendar onCal = Calendar.getInstance();
            onCal.setTime(onDate);
            scheduler.schedule(TimeCardStatus.ON, onCal.get(Calendar.HOUR_OF_DAY), onCal.get(Calendar.MINUTE));
        } else {
            scheduler.cancel(TimeCardStatus.ON);
        }

        if (scheduleOffEnabled.isSelected()) {
            Date offDate = (Date) offTimeSpinner.getValue();
            Calendar offCal = Calendar.getInstance();
            offCal.setTime(offDate);
            scheduler.schedule(TimeCardStatus.OFF, offCal.get(Calendar.HOUR_OF_DAY), offCal.get(Calendar.MINUTE));
        } else {
            scheduler.cancel(TimeCardStatus.OFF);
        }
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
