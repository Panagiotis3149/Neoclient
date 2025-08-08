package neo.module.impl.movement;

import neo.event.PreMotionEvent;
import neo.event.ReceivePacketEvent;
import neo.event.SendPacketEvent;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongJump extends Module {
    private final SliderSetting mode;
    private final SliderSetting horizontalBoost;
    private final SliderSetting verticalMotion;
    private final SliderSetting motionTicks;
    private final ButtonSetting addMotion;
    private final ButtonSetting invertYaw;
    private final ButtonSetting jump;
    private final ButtonSetting stopMotion;
    private int lastSlot = -1;
    private int ticks = -1;
    private boolean setSpeed;
    public static boolean stopModules;
    private boolean sentPlace;
    private int initTicks;
    private boolean threw;
    private final String[] modes = new String[]{"Fireball", "Fireball Auto"};
    public static int offGroundTicks;

    public LongJump() {
        super("LongJump", category.movement);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(horizontalBoost = new SliderSetting("Horizontal boost", 1.7, 0.0, 8.0, 0.1));
        this.registerSetting(verticalMotion = new SliderSetting("Vertical motion", 0, 0.0, 1.0, 0.01));
        this.registerSetting(motionTicks = new SliderSetting("Motion ticks", 10, 1, 40, 1));
        this.registerSetting(addMotion = new ButtonSetting("Add motion", false));
        this.registerSetting(invertYaw = new ButtonSetting("Invert yaw", true));
        this.registerSetting(jump = new ButtonSetting("Jump", false));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion on disable", false));
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (SendPacketEvent.getPacket() instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement) SendPacketEvent.getPacket()).getStack() != null && ((C08PacketPlayerBlockPlacement) SendPacketEvent.getPacket()).getStack().getItem() instanceof ItemFireball) {
            threw = true;
            if (mc.thePlayer.onGround && jump.isToggled()) {
                mc.thePlayer.jump();
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (ReceivePacketEvent.getPacket() instanceof S12PacketEntityVelocity && Utils.isnull()) {
            if (((S12PacketEntityVelocity) ReceivePacketEvent.getPacket()).getEntityID() == mc.thePlayer.getEntityId() && threw) {
                ticks = 0;
                setSpeed = true;
                threw = false;
                stopModules = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent e) {
        if ( !Utils.isnull() ) {
            return;
        }
        if ( mode.getInput() == 1 ) {
            if ( initTicks == 0 ) {
                if ( invertYaw.isToggled() ) {
                    e.setYaw(mc.thePlayer.rotationYaw - 180);
                    e.setPitch(89);
                } else {
                    e.setPitch(90);
                }
                int fireballSlot = getFireball();
                if ( fireballSlot != -1 && fireballSlot != mc.thePlayer.inventory.currentItem ) {
                    lastSlot = mc.thePlayer.inventory.currentItem;
                    mc.thePlayer.inventory.currentItem = fireballSlot;
                }
            } else if ( initTicks == 1 ) {
                if ( !sentPlace ) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    sentPlace = true;
                }
            } else if ( initTicks == 2 ) {
                if ( lastSlot != -1 ) {
                    mc.thePlayer.inventory.currentItem = lastSlot;
                    lastSlot = -1;
                }
            }
            if ( ticks > motionTicks.getInput() ) {
                if ( stopMotion.isToggled() ) {
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.motionZ = 0;
                }
                this.disable();
                return;
            }
            if ( setSpeed ) {
                stopModules = true;
                setSpeed();
                ticks++;
            }
            if ( initTicks < 3 ) {
                initTicks++;
            }
        } else if (mode.getInput() == 0) {
            if ( setSpeed ) {
                if ( ticks > motionTicks.getInput() ) {
                    stopModules = setSpeed = false;
                    ticks = 0;
                    if ( stopMotion.isToggled() ) {
                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionY = 0;
                        mc.thePlayer.motionZ = 0;
                    }
                    return;
                }
                stopModules = true;
                ticks++;
                setSpeed();
            }
         }
    }



    public void onDisable() {
        if (lastSlot != -1 && mode.getInput() == 1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
        }
        ticks = lastSlot = -1;
        setSpeed = stopModules = sentPlace = false;
        initTicks = 0;
    }

    public void onEnable() {
        if (getFireball() == -1 && mode.getInput() == 1) {
            Utils.sendMessage("§cNo fireball found.");
            this.disable();
            return;
        }
        if (mode.getInput() == 1) {
            stopModules = true;
        }
    }

    private void setSpeed() {
        if (verticalMotion.getInput() != 0.0 && addMotion.isToggled()) {
            mc.thePlayer.motionY = verticalMotion.getInput();
        }
        if (horizontalBoost.getInput() != 0.0) {
            Utils.setSpeed(horizontalBoost.getInput());
        }
    }

    private int getFireball() {
        int n = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.fire_charge) {
                n = i;
                break;
            }
        }
        return n;
    }
    @Override
    public void onUpdate() {
        if (mc.thePlayer.isAirBorne) {
            offGroundTicks++;
        } else {
            offGroundTicks = 0;
        }
    }
}