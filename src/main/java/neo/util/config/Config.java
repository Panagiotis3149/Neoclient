package neo.util.config;

import neo.module.Module;

public class Config {
    private final Module module;
    private int bind = 0;
    private final String configName;

    public Config(String configName, int bind) {
        this.configName = configName;
        this.bind = bind;
        this.module = new ConfigModule(this, configName, bind);
        this.module.ignoreOnSave = true;
    }

    public Module getModule() {
        return module;
    }

    public int getBind() {
        return bind;
    }

    public String getName() {
        return configName;
    }
}
