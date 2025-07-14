package neo.util.other;

import neo.event.PreMotionEvent;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.EntityList;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static neo.util.Utils.mc;

public class GuiDetectionHandler {

    private static boolean inGUI;

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        inGUI = false;

        if (mc.currentScreen instanceof GuiChest) {
            final Container container = mc.thePlayer.openContainer;

            int confidence = 0;
            int totalSlots = 0;
            int amount = 0;

            for (final Slot slot : container.inventorySlots) {
                if (slot.getHasStack() && amount++ <= 26) {
                    final ItemStack stack = slot.getStack();
                    if (stack == null) continue;

                    final String actual = stack.getDisplayName().toLowerCase().replace(" ", "");
                    final String expected = expectedName(stack).toLowerCase().replace(" ", "");

                    if (!actual.contains(expected)) confidence++;
                    else confidence -= 0.1;

                    totalSlots++;
                }
            }

            inGUI = (float) confidence / totalSlots > 0.5f;
        }
    }

    public static boolean isInGUI() {
        return inGUI;
    }

    private String expectedName(final ItemStack stack) {
        String name = StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name").trim();
        final String entity = EntityList.getStringFromID(stack.getMetadata());

        if (entity != null) {
            name += " " + StatCollector.translateToLocal("entity." + entity + ".name");
        }

        return name;
    }
}
