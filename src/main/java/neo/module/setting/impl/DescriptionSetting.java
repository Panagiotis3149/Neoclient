package neo.module.setting.impl;

import com.google.gson.JsonObject;
import neo.module.setting.Setting;

public class DescriptionSetting extends Setting {
    private String desc;

    public DescriptionSetting(String t) {
        super(t);
        this.desc = t;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String t) {
        this.desc = t;
    }

    @Override
    public void loadConfig(JsonObject data) {
    }
}
