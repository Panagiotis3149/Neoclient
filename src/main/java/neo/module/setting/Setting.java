package neo.module.setting;

import com.google.gson.JsonObject;
import java.util.function.Supplier;

public abstract class Setting {

    private Supplier<Boolean> visiblePredicate = null;

    public String n;

    public Setting(String n) {
        this.n = n;
    }

    public String getName() {
        return this.n;
    }

    public abstract void loadConfig(JsonObject data);

    public Setting setVisibleWhen(Supplier<Boolean> condition) {
        this.visiblePredicate = condition;
        return this;
    }

    public boolean isVisible() {
        return visiblePredicate == null || visiblePredicate.get();
    }

    public Setting setVisible(boolean vis) {
        this.visiblePredicate = () -> vis;
        return this;
    }
}
