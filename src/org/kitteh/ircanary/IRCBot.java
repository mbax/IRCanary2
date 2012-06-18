package org.kitteh.ircanary;
import java.util.logging.Logger;

import net.canarymod.Canary;
import net.canarymod.api.entity.Player;

import org.jibble.pircbot.PircBot;

public class IRCBot extends PircBot {
    private final char commandPrefix;
    private final int ircCharLim;
    private final boolean ircEcho;
    private final String[] ircSeparator;
    private final String ircUserColor;
    private final Logger log;
    private final boolean msgCommandRequired;
    private final IRCanary plugin;

    public IRCBot(String mah_name, boolean msgenabled, int charlim, String usercolor, boolean echo, String[] sep, IRCanary ip, char startCommand) {
        this.setName(mah_name);
        this.setAutoNickChange(true);
        this.msgCommandRequired = msgenabled;
        this.ircCharLim = charlim;
        this.ircUserColor = usercolor;
        this.ircEcho = echo;
        this.ircSeparator = sep;
        this.log = Logger.getLogger("Minecraft");
        this.plugin = ip;
        this.commandPrefix = startCommand;
    }

    private boolean addMsg(String thenewmsg, String theuser) {
        final String combined = this.ircSeparator[0] + "§" + this.ircUserColor + theuser + "§f" + this.ircSeparator[1] + " " + thenewmsg;
        if (combined.length() > this.ircCharLim) {
            return false;
        }

        this.log.info("IRC <" + theuser + "> " + thenewmsg);
        for (final Player p : Canary.getServer().getPlayerList()) {
            if (p != null) {
                p.sendMessage(combined);
            }
        }
        return true;
    }

    private void messageToGame(String channel, String sender, String message) {
        if (this.addMsg(message, sender)) {
            if (this.ircEcho) {
                this.sendMessage(channel, "[IRC] <" + sender + "> " + message);
            }
        } else {
            this.sendMessage(channel, sender + ": Your message was too long. The limit's " + this.ircCharLim + " characters");
        }
    }

    @Override
    protected void onDisconnect() {
        this.plugin.resetBot();
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        if (message.charAt(0) == '!') {
            final String[] split = message.split(" ");
            if (message.equalsIgnoreCase("!help")) {
                this.sendMessage(channel, sender + ": I am here to set you free.");
            } else if (message.equalsIgnoreCase("!players")) {
                int currentPlayersCount = 0;
                final StringBuilder currentPlayers = new StringBuilder();
                for (final Player player : Canary.getServer().getPlayerList()) {
                    if (player != null) {
                        if (currentPlayers.length() != 0) {
                            currentPlayers.append(", ");
                        }
                        currentPlayersCount++;
                    }
                }
                if (currentPlayers.length() == 0) {
                    this.sendMessage(channel, "No players online.");
                } else {
                    this.sendMessage(channel, "Players (" + currentPlayersCount + "/" + Canary.getServer().getMaxPlayers() + "):" + currentPlayers.toString());
                }

            } else if ((this.msgCommandRequired) && (split[0].equalsIgnoreCase("!msg"))) {
                final String messageToSend = Canary.glueString(split, 1, " ");
                this.messageToGame(channel, sender, messageToSend);
            }
            return;
        }
        if (message.charAt(0) == this.commandPrefix) {
            if (this.plugin.ircCommandAttempt(hostname, message.split(" "))) {
                this.sendMessage(sender, "Done :)");
            } else {
                this.sendMessage(sender, "You don't have access to that command :(");
            }
            return;
        }
        if (!this.msgCommandRequired) {
            this.messageToGame(channel, sender, message);
        }
    }

    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        final String[] split = message.split(" ");
        if (split[0].equalsIgnoreCase("auth")) {
            if (this.plugin.authenticate(sender, split[1], split[2], hostname)) {
                this.sendMessage(sender, "Authenticated :)");
            } else {
                this.sendMessage(sender, "Authentication failed. Bad username or password");
            }
        } else if (this.plugin.ircCommandAttempt(hostname, message.split(" "))) {
            this.sendMessage(sender, "Done :)");
        } else {
            this.sendMessage(sender, "You don't have access to that command :(");
        }
    }

}