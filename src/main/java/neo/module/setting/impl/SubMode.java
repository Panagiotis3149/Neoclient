package neo.module.setting.impl;

import neo.Neo;
import neo.module.Module;
import neo.module.setting.Setting;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a sub-mode within a module.
 * This class extends the Module class and provides additional functionality specific to sub-modes.
 *
 * @param <T> the type of the parent module
 */
public abstract class SubMode<T extends Module> extends Module {
    protected final String name;
    public final T parent;

    public SubMode(String name, @NotNull T parent) {
        super(parent.getName() + "$" + name, parent.moduleCategory());
        this.name = name;
        this.parent = parent;
    }

    public void register() {
        Neo.getModuleManager().addModule(this);
    }


    @Override
    public void registerSetting(Setting setting) {
        super.registerSetting(setting);
    }
}