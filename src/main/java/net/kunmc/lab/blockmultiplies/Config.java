package net.kunmc.lab.blockmultiplies;

import org.bukkit.command.CommandSender;

public class Config {
    private static Integer rndRange;
    private static Integer possibility;

    public static void loadConfig() {
        rndRange = 4;
        possibility = 0;
    }

    public static Integer rndRange() {
        return rndRange;
    }

    public static Integer possibility() {
        return possibility;
    }

    public static void setRndRange(CommandSender sender, Integer n) {
        if (n >= 1) {
            rndRange = n;
            sender.sendMessage("生成範囲を" + n + "に設定しました");
        } else {
            sender.sendMessage("生成範囲は1以上の整数にしてください");
        }
    }

    public static void setPossibility(CommandSender sender, Integer n) {
        if ((n >= 0) && (n <= 100)) {
            possibility = n;
            sender.sendMessage("生成確率を" + n + "に設定しました");
        } else {
            sender.sendMessage("生成確率は0以上100以下の整数に限ります");
        }
    }
}
