package com.github.sirmonkeyboy.loan.Utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;

public class Utils {

    static Audience console = Bukkit.getConsoleSender();

    public static void getErrorLogger(String message) {
        console.sendMessage(Component.text("[").color(NamedTextColor.WHITE)
                .append(Component.text("Loan").color(NamedTextColor.GOLD)
                        .append(Component.text("] ").color(NamedTextColor.WHITE))
                        .append(Component.text(message).color(NamedTextColor.DARK_RED))));
    }
}
