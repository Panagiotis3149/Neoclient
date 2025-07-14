package neo.gui.menu.altmgr;

import java.util.List;

public class AltFile {
    public Metadata Metadata = new Metadata();
    public List<Account> data;

    public static class Metadata {
        public String version = "2";
        public String creationDate = "";
    }

    public static class Account {
        public String type;
        public String name;
        public String uuid;
        public String accessToken;
        public long lastUsed;
        public String refreshToken;

        public Account(String type, String name, String uuid, String accessToken, long lastUsed, String refreshToken) {
            this.type = type;
            this.name = name;
            this.uuid = uuid;
            this.accessToken = accessToken;
            this.lastUsed = lastUsed;
            this.refreshToken = refreshToken;
        }

        public Account(String type, String name, String uuid, String accessToken, long lastUsed) {
            this.type = type;
            this.name = name;
            this.uuid = uuid;
            this.accessToken = accessToken;
            this.lastUsed = lastUsed;
        }

        public Account() {
        }
    }
}
