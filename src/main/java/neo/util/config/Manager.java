package neo.util.config;

import neo.Neo;
import neo.NeoCloud;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.Utils;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Manager extends Module {
    private final ButtonSetting loadConfigs;
    private final ButtonSetting openFolder;
    private final ButtonSetting createProfile;

    public Manager() {
        super("Manager", category.config);

        this.registerSetting(createProfile = new ButtonSetting("Create config", new Runnable() {
            @Override
            public void run() {
                if (Utils.isnull() && Neo.configManager != null) {
                    String baseName = "cfg-";
                    for (int i = 1; i <= 100; i++) {
                        String name = baseName + i;
                        if (Neo.configManager.getConfig(name) != null) {
                            continue;
                        }
                        Neo.configManager.saveConfig(new Config(name, 0));
                        Utils.sendMessage("&7Created config: &b" + name);
                        Neo.configManager.loadConfigs();
                        break;
                    }
                }
            }
        }));

        this.registerSetting(loadConfigs = new ButtonSetting("Load configs", new Runnable() {
            @Override
            public void run() {
                if (Utils.isnull() && Neo.configManager != null) {
                    List<String> onlineConfigs = fetchConfigList();
                    for (String cfg : onlineConfigs) {
                        downloadConfig(cfg);
                    }
                    Neo.configManager.loadConfigs();
                }
            }
        }));

        this.registerSetting(openFolder = new ButtonSetting("Open folder", new Runnable() {
            @Override
            public void run() {
                try {
                    Desktop.getDesktop().open(Neo.configManager.directory);
                } catch (IOException ex) {
                    Neo.configManager.directory.mkdirs();
                    Utils.sendMessage("&cError locating folder, recreated.");
                }
            }
        }));

        ignoreOnSave = true;
        canBeEnabled = false;
    }

    private List<String> fetchConfigList() {
        List<String> configNames = new ArrayList<>();
        try {
            String listUrl = NeoCloud.CONFIGS_URL + "list.json";
            String json = fetchUrl(listUrl);
            json = json.trim();
            if (json.startsWith("[") && json.endsWith("]")) {
                json = json.substring(1, json.length() - 1);
                String[] files = json.split(",");
                for (String file : files) {
                    file = file.trim().replace("\"", "");
                    if (file.endsWith(".json")) {
                        configNames.add(file.substring(0, file.length() - 5));
                    }
                }
            }
        } catch (Exception e) {
            Utils.sendMessage("&cFailed to fetch config list");
            e.printStackTrace();
        }
        return configNames;
    }

    private String fetchUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }
        reader.close();
        return result.toString().trim();
    }

    private void downloadConfig(String configName) {
        try {
            String url = "https://raw.githubusercontent.com/Panagiotis3149/NeoCloud/main/configs/" + configName + ".json";
            URL downloadUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
                File outFile = new File(Neo.configManager.directory, configName + ".json");
                FileOutputStream fos = new FileOutputStream(outFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                in.close();

                Utils.sendMessage("&aDownloaded config: &b" + configName);
            } else {
                Utils.sendMessage("&cFailed to download config: &b" + configName);
            }
        } catch (Exception e) {
            Utils.sendMessage("&cError downloading config: &b" + configName);
            e.printStackTrace();
        }
    }
}
