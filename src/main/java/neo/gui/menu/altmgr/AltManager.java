package neo.gui.menu.altmgr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import neo.Neo;

import java.io.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AltManager {

    private static final File ALT_FILE = new File(Neo.NeoDirectory, "alts.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AltFile altFile;

    public AltManager() {
        loadOrCreate();
    }

    public void loadOrCreate() {
        if (!ALT_FILE.exists()) {
            createNewFile();
        } else {
            try (Reader reader = new FileReader(ALT_FILE)) {
                altFile = GSON.fromJson(reader, AltFile.class);
                if (altFile == null || altFile.data == null) {
                    createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                createNewFile();
            }
        }
    }


    private void createNewFile() {
        altFile = new AltFile();
        altFile.Metadata.creationDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        altFile.data = new ArrayList<>();
        save();
    }


    public void save() {
        try (Writer writer = new FileWriter(ALT_FILE)) {
            GSON.toJson(altFile, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<AltFile.Account> getAccounts() {
        return new ArrayList<>(altFile.data);
    }

    public void addAccount(AltFile.Account account) {
        altFile.data.add(account);
        save();
    }

    public String getRefreshTokenByUUID(String uuid) {
        for (AltFile.Account acc : altFile.data) {
            if (acc.uuid.equals(uuid)) {
                return acc.refreshToken;
            }
        }
        return null;
    }





    public boolean deleteAccountByUUID(String uuid) {
        boolean removed = altFile.data.removeIf(acc -> acc.uuid.equals(uuid));
        if (removed) save();
        return removed;
    }

    public void setList(List<AltFile.Account> accounts) {
        altFile.data = new ArrayList<>(accounts);
        save();
    }


    public boolean setAccount(AltFile.Account newAccount) {
        for (int i = 0; i < altFile.data.size(); i++) {
            if (altFile.data.get(i).uuid.equals(newAccount.uuid)) {
                altFile.data.set(i, newAccount);
                save();
                return true;
            }
        }
        return false;
    }


    public boolean setPart(String uuid, AltFile.Account partial) {
        for (AltFile.Account acc : altFile.data) {
            if (acc.uuid.equals(uuid)) {
                if (partial.name != null) acc.name = partial.name;
                if (partial.type != null) acc.type = partial.type;
                if (partial.accessToken != null) acc.accessToken = partial.accessToken;
                if (partial.refreshToken != null) acc.refreshToken = partial.refreshToken;
                if (partial.lastUsed != 0) acc.lastUsed = partial.lastUsed;
                save();
                return true;
            }
        }
        return false;
    }
}
