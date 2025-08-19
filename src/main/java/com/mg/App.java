package com.mg;

import com.mg.enums.TimeCardStatus;
import com.mg.service.AutoTimeCardService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.util.Objects;

/**
 * 自動登入打卡
 */
public class App {

    public static void main(String[] args) {
        // if no arguments are provided, launch the GUI.
        if (args.length == 0) {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            SwingUtilities.invokeLater(() -> {
                TimeCardUI ui = new TimeCardUI();
                ui.setVisible(true);
            });
        } else {
            // otherwise, run the command-line version
            try {
                Pair<TimeCardStatus, Boolean> params = getCheckinStatus(args);
                new AutoTimeCardService().checkin(params.getLeft(), params.getRight());
                System.exit(0);
            } catch (Exception ex) {
                System.out.println("自動打卡失敗:" + ex.getMessage());
                System.exit(2);
            }
        }
    }


    /**
     * 取得傳入參數
     * @param args
     * @return
     * @throws ParseException
     */
    private static Pair<TimeCardStatus, Boolean> getCheckinStatus(String[] args) throws ParseException {

        boolean dubug = false;
        Options options = new Options();
        options.addOption("s", "status", true, "上/下班打卡");
        options.addOption("d", "dubug", false, "除錯模式");

        DefaultParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);

        if(!cmdLine.hasOption("s")) {
            System.out.println("尚未輸入參數，ex : -s on/off");
            System.exit(2);
        }

        TimeCardStatus status = TimeCardStatus.findByCode(cmdLine.getOptionValue("s"));

        if(Objects.isNull(status)) {
            System.out.println("輸入參數錯誤，ex : -s on/off");
            System.exit(2);
        }

        if(cmdLine.hasOption("d")) {
            dubug = true;
        }

        return new ImmutablePair<>(status, dubug);
    }
}
