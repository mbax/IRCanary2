package org.kitteh.ircanary;
public class IRCAdmin {
    private final int accessLevel;
    private String hostname;
    private final String password;
    private final String username;

    public IRCAdmin(String name, String pass, int level) {
        this.username = name;
        this.password = pass;
        this.hostname = "";
        this.accessLevel = level;
    }

    public boolean auth(String pass, String host) {
        if (this.password.equals(pass)) {
            this.hostname = host;
            return true;
        }
        return false;
    }

    public int getAccessLevel() {
        return this.accessLevel;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getUsername() {
        return this.username;
    }
}