package keystrokesmod.module;

import keystrokesmod.module.impl.client.CommandLine;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.client.Settings;
import keystrokesmod.module.impl.combat.*;
import keystrokesmod.module.impl.fun.Fun;
import keystrokesmod.module.impl.minigames.*;
import keystrokesmod.module.impl.movement.*;
import keystrokesmod.module.impl.other.*;
import keystrokesmod.module.impl.player.*;
import keystrokesmod.module.impl.render.*;
import keystrokesmod.module.impl.world.*;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.profile.Manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModuleManager {
    public static AutoSwap autoSwap;
    static List<Module> modules = new ArrayList<>();
    public static List<Module> organizedModules = new ArrayList<>();
    public static Module nameHider;
    public static Module fastPlace;
    public static MurderMystery murderMystery;
    public static AntiFireball antiFireball;
    public static BedAura bedAura;
    public static FastMine fastMine;
    public static Module antiShuffle;
    public static Module commandLine;
    public static AntiAim antiAim;
    public static Module antiBot;
    public static Sprint sprint;
    public static Module noSlow;
    public static Notifications notifications;
    public static KillAura killAura;
    public static Module autoClicker;
    public static Module hitBox;
    public static NewHUD newHUD;
    public static Module reach;
    public static BedESP bedESP;
    public static HUD hud;
    public static Module timer;
    public static Module fly;
    public static Disabler disabler;
    public static Module wTap;
    public static Potions potions;
    public static TargetHUD targetHUD;
    public static NoFall noFall;
    public static PlayerESP playerESP;
    public static Module reduce;
    public static SafeWalk safeWalk;
    public static Module keepSprint;
    public static Module antiKnockback;
    public static Velocity2 velocity2;
    public static Tower tower;
    public static NoCameraClip noCameraClip;
    public static Module bedwars;
    public static TargetStrafe targetStrafe;
    public static BHop bHop;
    public static NoHurtCam noHurtCam;
    public static Scaffold scaffold;

    public void register() {
        this.addModule(autoClicker = new AutoClicker());
        this.addModule(new LongJump());
        this.addModule(new AimAssist());
        this.addModule(new Blink());
        this.addModule(new BurstClicker());
        this.addModule(new ClickAssist());
        this.addModule(tower = new Tower());
        this.addModule(new DelayRemover());
        this.addModule(hitBox = new HitBox());
        this.addModule(new Radar());
        this.addModule(new Settings());
        this.addModule(reach = new Reach());
        this.addModule(new RodAimbot());
        this.addModule(new Velocity());
        this.addModule(bHop = new BHop());
        this.addModule(disabler = new Disabler());
        this.addModule(new InvManager());
        this.addModule(scaffold = new Scaffold());
        this.addModule(new AntiAFK());
        this.addModule(new Boost());
        this.addModule(new AutoTool());
        this.addModule(noHurtCam = new NoHurtCam());
        this.addModule(fly = new Fly());
        this.addModule(new InvMove());
        this.addModule(new AntiAim());
        this.addModule(new Enabler());
        this.addModule(newHUD = new NewHUD());
        this.addModule(targetStrafe = new TargetStrafe());
        this.addModule(new Trajectories());
        this.addModule(potions = new Potions());
        this.addModule(new AutoSwap());
        this.addModule(keepSprint = new KeepSprint());
        this.addModule(bedAura = new BedAura());
        this.addModule(noSlow = new NoSlow());
        this.addModule(new Indicators());
        this.addModule(new LatencyAlerts());
        this.addModule(noCameraClip = new NoCameraClip());
        this.addModule(sprint = new Sprint());
        this.addModule(velocity2 = new Velocity2());
        this.addModule(new StopMotion());
        this.addModule(timer = new Timer());
        this.addModule(new VClip());
        this.addModule(new AutoJump());
        this.addModule(new AutoPlace());
        this.addModule(fastPlace = new FastPlace());
        this.addModule(new Freecam());
        this.addModule(noFall = new NoFall());
        this.addModule(safeWalk = new SafeWalk());
        this.addModule(reduce = new Reduce());
        this.addModule(antiKnockback = new AntiKnockback());
        this.addModule(antiBot = new AntiBot());
        this.addModule(antiShuffle = new AntiShuffle());
        this.addModule(new Chams());
        this.addModule(new ChestESP());
        this.addModule(new Nametags());
        this.addModule(playerESP = new PlayerESP());
        this.addModule(new Tracers());
        this.addModule(hud = new HUD());
        this.addModule(new Anticheat());
        this.addModule(new BreakProgress());
        this.addModule(wTap = new WTap());
        this.addModule(new Xray());
        this.addModule(new BridgeInfo());
        this.addModule(targetHUD = new TargetHUD());
        this.addModule(new DuelsStats());
        this.addModule(antiFireball = new AntiFireball());
        this.addModule(bedESP = new BedESP());
        this.addModule(murderMystery = new MurderMystery());
        this.addModule(new keystrokesmod.script.Manager());
        this.addModule(new SumoFences());
        this.addModule(new Fun.ExtraBobbing());
        this.addModule(killAura = new KillAura());
        this.addModule(new Watermark());
        this.addModule(new Animations());
        this.addModule(new Fun.FlameTrail());
        this.addModule(new Fun.SlyPort());
        this.addModule(new Criticals());
        this.addModule(new TargetESP());
        this.addModule(new RotationHandler());
        this.addModule(new Fun.Spin());
        this.addModule(new NoRotate());
        this.addModule(new FakeChat());
        this.addModule(nameHider = new NameHider());
        this.addModule(new FakeLag());
        this.addModule(new WaterBucket());
        this.addModule(commandLine = new CommandLine());
        this.addModule(bedwars = new BedWars());
        this.addModule(fastMine = new FastMine());
        this.addModule(new JumpReset());
        this.addModule(new Manager());
        this.addModule(new ViewPackets());
        this.addModule(new AutoWho());
        this.addModule(new Gui());
        this.addModule(notifications = new Notifications());
        Collections.sort(this.modules, Comparator.comparing(Module::getName));
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



    public static void sort() {
        FontRenderer fontRenderer;



        switch ((int) HUD.Mode.getInput()) {
            case 2:
                fontRenderer = null;
                break;
            case 3:
                fontRenderer = null;
                break;
            case 4:
                fontRenderer = null;
                break;
            case 5:
                fontRenderer = null;
                break;
            default:
                fontRenderer = FontManager.helveticaNeue;
                break;
        }

        if (newHUD.isEnabled()) {
          fontRenderer = FontManager.googleRegular20;
        }


        if (HUD.alphabeticalSort.isToggled()) {
            Collections.sort(organizedModules, Comparator.comparing(Module::getName));
        } else {
            FontRenderer finalFontRenderer = fontRenderer;
            Collections.sort(organizedModules, (m1, m2) -> {
                String nameWithInfo1 = m1.getName() + (HUD.showInfo.isToggled() && !m1.getInfo().isEmpty() ? " " + m1.getInfo() : "");
                String nameWithInfo2 = m2.getName() + (HUD.showInfo.isToggled() && !m2.getInfo().isEmpty() ? " " + m2.getInfo() : "");

                double width1;
                double width2;

                if (finalFontRenderer != null) {
                    width1 = finalFontRenderer.getStringWidth(nameWithInfo1) + 10;
                    width2 = finalFontRenderer.getStringWidth(nameWithInfo2) + 10;
                } else {
                    width1 = MinecraftFontRenderer.INSTANCE.width(nameWithInfo1) + 10;
                    width2 = MinecraftFontRenderer.INSTANCE.width(nameWithInfo2) + 10;
                }

                return Double.compare(width2, width1);
            });
        }
    }

}
