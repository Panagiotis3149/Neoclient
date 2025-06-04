package neo.module.setting.impl;

import neo.module.Module;
import org.jetbrains.annotations.NotNull;

public class LiteralSubMode extends SubMode<Module> {
    public LiteralSubMode(String name, @NotNull Module parent) {
        super(name, parent);
    }
}