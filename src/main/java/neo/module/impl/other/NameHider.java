package neo.module.impl.other;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.util.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;

public class NameHider extends Module {
    public static DescriptionSetting a;
    public static String n = "raven";
    public static ButtonSetting hideAllNames;

    public NameHider() {
        super("NameHider", category.other);
        this.registerSetting(a = new DescriptionSetting(Utils.uf("command") + ": cname [name]"));
        this.registerSetting(hideAllNames = new ButtonSetting("Hide all names", false));
    }

    public static String getFakeName(String s) {
        if (mc.thePlayer != null) {
            if (hideAllNames.isToggled()) {
                s = s.replace(Utils.getServerName(), "You");
                NetworkPlayerInfo getPlayerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
                for (NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                    if (networkPlayerInfo.equals(getPlayerInfo)) {
                        continue;
                    }
                    s = s.replace(networkPlayerInfo.getGameProfile().getName(), n);
                }
            }
            else {
                s = s.replace(Utils.getServerName(), n);
            }
        }
        return s;
    }
}
