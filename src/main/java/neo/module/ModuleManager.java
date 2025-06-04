package neo.module;

import neo.module.impl.client.*;
import neo.module.impl.combat.*;
import neo.module.impl.minigames.*;
import neo.module.impl.movement.*;
import neo.module.impl.other.*;
import neo.module.impl.player.*;
import neo.module.impl.render.*;
import neo.util.font.FontManager;
import neo.util.font.MinecraftFontRenderer;
import neo.util.font.impl.FontRenderer;
import neo.util.profile.Manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModuleManager {
    static List<Module> modules = new ArrayList<>();
    public static List<Module> organizedModules = new ArrayList<>();
    public static Module nameHider;
    public static Module fastPlace;
    public static MurderMystery murderMystery;
    public static AntiFireball antiFireball;
    public static BedAura bedAura;
    public static ItemPhysics itemPhysics;
    public static Interface interfacemod; // Can't use Interface for some reason, probably java's interface
    public static FastMine fastMine;
    public static Module antiBot;
    public static Module animations;
    public static Sprint sprint;
    public static Module noSlow;
    public static Notifications notifications;
    public static KillAura killAura;
    public static Module autoClicker;
    public static Module hitBox;
    public static Module reach;
    public static BedESP bedESP;
    public static HUD hud;
    public static Module timer;
    public static Fly fly;
    public static Disabler disabler;
    public static Module wTap;
    public static TargetHUD targetHUD;
    public static NoFall noFall;
    public static PlayerESP playerESP;
    public static SafeWalk safeWalk;
    public static Module keepSprint;
    public static Module antiKnockback;
    public static Velocity2 velocity2;
    public static Module bedwars;
    public static TargetStrafe targetStrafe;
    public static BHop bHop;
    public static Scaffold scaffold;
    public static Watermark watermark;
    public static TargetESP targetESP;

    public void register() {
        this.addModule(autoClicker = new AutoClicker());
        this.addModule(new LongJump());
        this.addModule(new AimAssist());
        this.addModule(new Blink());
        this.addModule(new ClickAssist());
        this.addModule(new DelayRemover());
        this.addModule(hitBox = new HitBox());
        this.addModule(new Settings());
        this.addModule(reach = new Reach());
        this.addModule(new Velocity());
        this.addModule(bHop = new BHop());
        this.addModule(disabler = new Disabler());
        this.addModule(new InvManager());
        this.addModule(scaffold = new Scaffold());
        this.addModule(new AutoTool());
        this.addModule(fly = new Fly());
        this.addModule(new InvMove());
        this.addModule(new Enabler());
        this.addModule(targetStrafe = new TargetStrafe());
        this.addModule(new Trajectories());
        this.addModule(new AutoSwap());
        this.addModule(keepSprint = new KeepSprint());
        this.addModule(bedAura = new BedAura());
        this.addModule(noSlow = new NoSlow());
        this.addModule(new Indicators());
        this.addModule(sprint = new Sprint());
        this.addModule(velocity2 = new Velocity2());
        this.addModule(timer = new Timer());
        this.addModule(new AutoPlace());
        this.addModule(fastPlace = new FastPlace());
        this.addModule(new Freecam());
        this.addModule(noFall = new NoFall());
        this.addModule(safeWalk = new SafeWalk());
        this.addModule(antiKnockback = new AntiKnockback());
        this.addModule(antiBot = new AntiBot());
        this.addModule(new Chams());
        this.addModule(new ChestESP());
        this.addModule(new Nametags());
        this.addModule(playerESP = new PlayerESP());
        this.addModule(hud = new HUD());
        this.addModule(new Anticheat());
        this.addModule(new BreakProgress());
        this.addModule(wTap = new WTap());
        this.addModule(targetHUD = new TargetHUD());
        this.addModule(antiFireball = new AntiFireball());
        this.addModule(bedESP = new BedESP());
        this.addModule(murderMystery = new MurderMystery());
        this.addModule(new neo.script.Manager());
        this.addModule(new ClientTheme());
        this.addModule(killAura = new KillAura());
        this.addModule(watermark = new Watermark());
        this.addModule(animations = new  Animations());
        this.addModule(itemPhysics = new ItemPhysics());
        this.addModule(new Criticals());
        this.addModule(targetESP = new TargetESP());
        this.addModule(new RotationHandler());
        this.addModule(new NoRotate());
        this.addModule(nameHider = new NameHider());
        this.addModule(new FakeLag());
        this.addModule(bedwars = new BedWars());
        this.addModule(fastMine = new FastMine());
        this.addModule(new JumpReset());
        this.addModule(new Manager());
        this.addModule(new ViewPackets());
        this.addModule(new AutoWho());
        this.addModule(new Gui());
        this.addModule(new ServerHelper());
        this.addModule(new Step());
        this.addModule(new Strafe());
        this.addModule(new AntiVoid());
        this.addModule(interfacemod = new Interface());
        this.addModule(notifications = new Notifications());
        Collections.sort(modules, Comparator.comparing(Module::getName));
    }

    public void addModule(Module m) {
        modules.add(m);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> inCategory(Module.category categ) {
        ArrayList<Module> categML = new ArrayList<>();

        for (Module mod : this.getModules()) {
            if (mod.moduleCategory().equals(categ)) {
                categML.add(mod);
            }
        }

        return categML;
    }

    public Module getModule(String moduleName) {
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public Module getModuleCI(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public static List<Module> getModulesStatic() {
        return modules;
    }


    public static void sort() {
        FontRenderer fontRenderer;



        switch ((int) HUD.fonts.getInput()) {
            case 0: fontRenderer = null; break;
            case 1: fontRenderer = FontManager.helveticaNeue; break;
            case 2: fontRenderer = FontManager.productSans20; break;
            case 3: fontRenderer = FontManager.google; break;
            case 4: fontRenderer = FontManager.sfRegular; break;
            case 5: fontRenderer = FontManager.greyCliffCF; break;
            case 6: fontRenderer = FontManager.productSansLight22; break;
            case 7: fontRenderer = FontManager.poppinsBold20; break;
            case 8: fontRenderer = FontManager.proximaNova; break;
            default:
                fontRenderer = FontManager.helveticaNeue;
                break;
        }




        if (HUD.alphabeticalSort.isToggled()) {
            Collections.sort(organizedModules, Comparator.comparing(Module::getName));
        } else {
            FontRenderer finalFontRenderer = fontRenderer;
            Collections.sort(organizedModules, (m1, m2) -> {
                String nameWithInfo1 = "";
                String nameWithInfo2 = "";
                if (HUD.lowercase.isToggled()) {
                    nameWithInfo1 = m1.getName().toLowerCase() + (HUD.showInfo.isToggled() && !m1.getInfo().isEmpty() ? " " + m1.getInfo().toLowerCase() : "");
                    nameWithInfo2 = m2.getName().toLowerCase() + (HUD.showInfo.isToggled() && !m2.getInfo().isEmpty() ? " " + m2.getInfo().toLowerCase() : "");
                } else if (!HUD.lowercase.isToggled()) {
                    nameWithInfo1 = m1.getName() + (HUD.showInfo.isToggled() && !m1.getInfo().isEmpty() ? " " + m1.getInfo() : "");
                    nameWithInfo2 = m2.getName() + (HUD.showInfo.isToggled() && !m2.getInfo().isEmpty() ? " " + m2.getInfo() : "");
                }

                double width1;
                double width2;

                if (finalFontRenderer != null) {
                    width1 = finalFontRenderer.getStringWidth(nameWithInfo1) + 10;
                    width2 = finalFontRenderer.getStringWidth(nameWithInfo2) + 10;
                } else {
                    width1 = MinecraftFontRenderer.INSTANCE.getAccurateWidthTest(nameWithInfo1) + 10;
                    width2 = MinecraftFontRenderer.INSTANCE.getAccurateWidthTest(nameWithInfo2) + 10;
                }

                return Double.compare(width2, width1);
            });
        }
    }

}
