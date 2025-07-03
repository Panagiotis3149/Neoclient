package neo.module.impl.render;

import neo.module.Module;

import neo.module.setting.impl.ButtonSetting;

    public class Interface extends Module {
        public  final ButtonSetting ns;

    public Interface() {
        super("Interface", Module.category.render, 0);
        this.registerSetting(ns = new ButtonSetting("No Scoreboard", false));
    }
}