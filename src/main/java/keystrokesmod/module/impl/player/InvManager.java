package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class InvManager extends Module {
    private final ButtonSetting autoArmor;
    private final SliderSetting autoArmorDelay;
    private final ButtonSetting autoSort;
    private final SliderSetting sortDelay;
    private final ButtonSetting stealChests;
    private final ButtonSetting customChest;
    private final ButtonSetting autoClose;
    private final SliderSetting stealerDelay;
    private final ButtonSetting inventoryCleaner;
    private final SliderSetting cleanerDelay;
    private final SliderSetting swordSlot;
    private final SliderSetting blocksSlot;
    private final SliderSetting goldenAppleSlot;
    private final SliderSetting projectileSlot;
    private final SliderSetting speedPotionSlot;
    private final SliderSetting pearlSlot;
   // private final ButtonSetting noInv;
    private final String[] ignoreItems = {"stick", "book", "expbottle", "flesh",
            "string", "cake", "mushroom", "flint", "compass", "dyePowder", "feather",
            "shears", "anvil", "torch", "seeds", "leather", "skull", "record"};
    private int lastStole;
    private int lastSort;
    private int lastArmor;
    private int lastClean;
    private final Random random = new Random(); // for randomizing chest delay

    public InvManager() {
        super("InvManager", category.player);
        this.registerSetting(autoArmor = new ButtonSetting("Auto armor", true));
        this.registerSetting(autoArmorDelay = new SliderSetting("Auto armor delay", 7, 1, 20, 1));
        this.registerSetting(autoSort = new ButtonSetting("Auto sort", true));
        this.registerSetting(sortDelay = new SliderSetting("Sort delay", 7, 1, 20, 1));
        this.registerSetting(stealChests = new ButtonSetting("Steal chests", true));
        this.registerSetting(customChest = new ButtonSetting("Custom chest", false));
        this.registerSetting(autoClose = new ButtonSetting("Close after stealing", true));
        this.registerSetting(stealerDelay = new SliderSetting("Stealer delay", 7, 1, 20, 1));
        this.registerSetting(inventoryCleaner = new ButtonSetting("Inventory cleaner", true));
        this.registerSetting(cleanerDelay = new SliderSetting("Cleaner delay", 7, 1, 20, 1));
        this.registerSetting(swordSlot = new SliderSetting("Sword slot", 1, 0, 9, 1));
        this.registerSetting(blocksSlot = new SliderSetting("Blocks slot", 3, 0, 9, 1));
        this.registerSetting(goldenAppleSlot = new SliderSetting("Golden apple slot", 2, 0, 9, 1));
        this.registerSetting(projectileSlot = new SliderSetting("Projectile slot", 7, 0, 9, 1));
        this.registerSetting(speedPotionSlot = new SliderSetting("Speed potion slot", 8, 0, 9, 1));
        this.registerSetting(pearlSlot = new SliderSetting("Pearl slot", 9, 0, 9, 1));
    //    this.registerSetting(noInv = new ButtonSetting("NoInv", false));
    }

    public void onEnable() {
        resetDelay();
    }

    public void onUpdate() {
        if (Utils.inInventory()) {
            // Auto armor equip
            if (autoArmor.isToggled() && lastArmor++ >= autoArmorDelay.getInput()) {
                for (int i = 0; i < 4; i++) {
                    int bestSlot = getBestArmor(i, null);
                    if (bestSlot == i + 5) {
                        continue;
                    }
                    if (bestSlot != -1) {
                        if (getItemStack(i + 5) != null) {
                            drop(i + 5);
                        } else {
                            click(bestSlot, 0, true);
                            lastArmor = 0;
                        }
                        return;
                    }
                }
            }
            // Auto sorting (prioritize sword, blocks, etc.)
            if (autoSort.isToggled() && ++lastSort >= sortDelay.getInput()) {
                if (swordSlot.getInput() != 0) {
                    if (sort(getBestSword(null, (int) swordSlot.getInput()), (int) swordSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (blocksSlot.getInput() != 0) {
                    if (sort(getMostBlocks(), (int) blocksSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (goldenAppleSlot.getInput() != 0) {
                    if (sort(getBiggestStack(Items.golden_apple, (int) goldenAppleSlot.getInput()), (int) goldenAppleSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (projectileSlot.getInput() != 0) {
                    if (sort(getMostProjectiles((int) projectileSlot.getInput()), (int) projectileSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (speedPotionSlot.getInput() != 0) {
                    if (sort(getBestPotion((int) speedPotionSlot.getInput(), null), (int) speedPotionSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (pearlSlot.getInput() != 0) {
                    if (sort(getBiggestStack(Items.ender_pearl, (int) pearlSlot.getInput()), (int) pearlSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
            }
            // Inventory cleaning (drops unwanted items)
            if (inventoryCleaner.isToggled()) {

                if (++lastClean >= cleanerDelay.getInput()) {
                    for (int i = 5; i < 45; i++) {
                        ItemStack stack = getItemStack(i);
                        if (stack == null) {
                            continue;
                        }
                        if (!canDrop(stack, i)) {
                            continue;
                        }
                        drop(i);
                        lastClean = 0;
                        break;
                    }
                }
            }
        }
        else if (stealChests.isToggled() && mc.thePlayer.openContainer instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            if (chest == null || inventoryFull()) {
                autoClose();
                return;
            }
            String name = chest.getLowerChestInventory().getName();
            if (!customChest.isToggled() && !name.equals("Chest") && !name.equals("Ender Chest") && !name.equals("Large Chest")) {
                return;
            }
            boolean notEmpty = false;
            boolean stolen = false;
            int size = chest.getLowerChestInventory().getSizeInventory();
            for (int i = 0; i < size; i++) {
                ItemStack item = chest.getLowerChestInventory().getStackInSlot(i);
                if (item == null) {
                    continue;
                }
                // Check ignore list names
                if (Arrays.stream(ignoreItems).anyMatch(item.getUnlocalizedName().toLowerCase()::contains)) {
                    continue;
                }
                notEmpty = true;

                // Swords (best weapon) stealing
                if (item.getItem() instanceof ItemSword) {
                    if (getBestSword(chest.getLowerChestInventory(), (int) swordSlot.getInput()) != i) {
                        continue;
                    }
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1)); // random delay +/-1 tick
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (swordSlot.getInput() != 0) {
                            mc.playerController.windowClick(chest.windowId, i, (int) swordSlot.getInput() - 1, 2, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                // Axes: use as backup weapon if no sword present
                else if (item.getItem() instanceof ItemAxe) {
                    // if any sword in chest, skip axe
                    boolean swordFound = false;
                    for (int j = 0; j < size; j++) {
                        ItemStack it2 = chest.getLowerChestInventory().getStackInSlot(j);
                        if (it2 != null && it2.getItem() instanceof ItemSword) {
                            swordFound = true;
                            break;
                        }
                    }
                    if (swordFound) {
                        continue;
                    }
                    // check if this axe is the highest-damage axe
                    double thisDamage = Utils.getDamage(item);
                    double bestDamage = thisDamage;
                    for (int j = 0; j < size; j++) {
                        ItemStack it2 = chest.getLowerChestInventory().getStackInSlot(j);
                        if (it2 != null && it2.getItem() instanceof ItemAxe) {
                            double d = Utils.getDamage(it2);
                            if (d > bestDamage) {
                                bestDamage = d;
                                break;
                            }
                        }
                    }
                    if (bestDamage > thisDamage) {
                        continue;
                    }
                    // steal axe into sword slot as better weapon
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (swordSlot.getInput() != 0) {
                            mc.playerController.windowClick(chest.windowId, i, (int) swordSlot.getInput() - 1, 2, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                // Blocks
                else if (item.getItem() instanceof ItemBlock) {
                    if (!canBePlaced((ItemBlock) item.getItem())) {
                        continue;
                    }
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                // Golden apples
                else if (item.getItem() instanceof ItemAppleGold) {
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (goldenAppleSlot.getInput() == 0) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i,
                                    (int) (goldenAppleSlot.getInput() - 1), 2, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                // Skip eggs and snowballs (never pick up)
                else if (item.getItem() instanceof ItemSnowball || item.getItem() instanceof ItemEgg) {
                    continue;
                }
                // Ender pearls
                else if (item.getItem() instanceof ItemEnderPearl) {
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (pearlSlot.getInput() == 0) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i,
                                    (int) (pearlSlot.getInput() - 1), 2, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                // Armor
                else if (item.getItem() instanceof ItemArmor) {
                    if (getBestArmor(((ItemArmor) item.getItem()).armorType, chest.getLowerChestInventory()) != i) {
                        continue;
                    }
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                // Potions
                else if (item.getItem() instanceof ItemPotion) {
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (!isSpeedPot(item)) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        } else {
                            if (getBestPotion((int) speedPotionSlot.getInput(),
                                    chest.getLowerChestInventory()) != i || speedPotionSlot.getInput() == 0) {
                                mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            }
                            else {
                                mc.playerController.windowClick(chest.windowId, i,
                                        (int) (speedPotionSlot.getInput() - 1), 2, mc.thePlayer);
                            }
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                // Tools (axes/pickaxes etc.  backup pick logic)
                else if (item.getItem() instanceof ItemTool) {
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (getBestTool(item, chest.getLowerChestInventory()) != i) {
                            continue;
                        }
                        int delay2 = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                        if (delay2 < 1) delay2 = 1;
                        if (++lastStole >= delay2) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                // Bows
                else if (item.getItem() instanceof ItemBow) {
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (getBestBow(chest.getLowerChestInventory()) != i) {
                            continue;
                        }
                        int delay2 = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                        if (delay2 < 1) delay2 = 1;
                        if (++lastStole >= delay2) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                // Fishing rods
                else if (item.getItem() instanceof ItemFishingRod) {
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        if (getBestRod(chest.getLowerChestInventory()) != i) {
                            continue;
                        }
                        int delay2 = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                        if (delay2 < 1) delay2 = 1;
                        if (++lastStole >= delay2) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                // Default: pick up any other item
                else {
                    int delay = (int) (stealerDelay.getInput() + (random.nextInt(3) - 1));
                    if (delay < 1) delay = 1;
                    if (++lastStole >= delay) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
            }

            if (inventoryFull() || !notEmpty || !stolen) {
                autoClose();
            }
        }
        else {
            resetDelay();
        }
    }

    private int getProtection(final ItemStack itemStack) {
        return ((ItemArmor)itemStack.getItem()).damageReduceAmount +
                EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{ itemStack },
                        DamageSource.generic);
    }

    private void click(int slot, int mouseButton, boolean shiftClick) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, mouseButton, shiftClick ? 1 : 0, mc.thePlayer);
    }

    private boolean sort(int bestSlot, int desiredSlot) {
        if (bestSlot != -1 && bestSlot != desiredSlot + 35) {
            swap(bestSlot, desiredSlot - 1);
            return true;
        }
        return false;
    }

    private void drop(int slot) {
        mc.playerController.windowClick(0, slot, 1, 4, mc.thePlayer);
    }

    private void swap(int slot, int hSlot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hSlot, 2, mc.thePlayer);
    }

    private boolean isSpeedPot(ItemStack item) {
        List<PotionEffect> list = ((ItemPotion)
                item.getItem()).getEffects(item);
        if (list == null) {
            return false;
        }
        for (PotionEffect effect : list) {
            if (effect.getEffectName().equals("potion.moveSpeed")) {
                return true;
            }
        }
        return false;
    }

    private boolean inventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

    private void resetDelay() {
        lastStole = lastArmor = lastClean = lastSort = 0;
    }

    private void autoClose() {
        if (autoClose.isToggled()) {
            mc.thePlayer.closeScreen();
        }
    }

    private int getBestSword(IInventory inventory, int desiredSlot) {
        int bestSword = -1;
        double lastDamage = -1;
        double damageInSlot = -1;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && (itemStackInSlot.getItem() instanceof ItemSword)) {
                damageInSlot = Utils.getDamage(itemStackInSlot);
            }
        }
        // scan player inventory for best sword
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemSword)) {
                continue;
            }
            double damage = Utils.getDamage(item);
            if (damage > lastDamage && damage > damageInSlot) {
                lastDamage = damage;
                bestSword = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemSword)) {
                    continue;
                }
                double damage = Utils.getDamage(item);
                if (damage > lastDamage && damage > damageInSlot) {
                    lastDamage = damage;
                    bestSword = i;
                }
            }
        }
        // If no sword found, try axes as backup
        if (bestSword == -1) {
            double lastAxeDamage = -1;
            int bestAxeSlot = -1;
            for (int i = 9; i < 45; i++) {
                ItemStack item = getItemStack(i);
                if (item == null || !(item.getItem() instanceof ItemAxe)) {
                    continue;
                }
                double damage = Utils.getDamage(item);
                if (damage > lastAxeDamage && damage > damageInSlot) {
                    lastAxeDamage = damage;
                    bestAxeSlot = i;
                }
            }
            if (inventory != null) {
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack item = inventory.getStackInSlot(i);
                    if (item == null || !(item.getItem() instanceof ItemAxe)) {
                        continue;
                    }
                    double damage = Utils.getDamage(item);
                    if (damage > lastAxeDamage && damage > damageInSlot) {
                        lastAxeDamage = damage;
                        bestAxeSlot = i;
                    }
                }
            }
            if (bestAxeSlot != -1) {
                bestSword = bestAxeSlot;
            } else {
                bestSword = desiredSlot + 35;
            }
        }
        return bestSword;
    }

    private int getBestArmor(int armorType, IInventory inventory) {
        int bestArmor = -1;
        double lastProtection = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemArmor) ||
                    !(((ItemArmor) item.getItem()).armorType == armorType)) {
                continue;
            }
            double protection = getProtection(item);
            if (protection > lastProtection) {
                lastProtection = protection;
                bestArmor = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemArmor) ||
                        !(((ItemArmor) item.getItem()).armorType == armorType)) {
                    continue;
                }
                double protection = getProtection(item);
                if (protection > lastProtection) {
                    lastProtection = protection;
                    bestArmor = i;
                }
            }
        }
        return bestArmor;
    }

    private boolean dropPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion) stack.getItem();
            if (potion.getEffects(stack) == null) {
                return true;
            }
            for (PotionEffect effect : potion.getEffects(stack)) {
                if (effect.getPotionID() == Potion.moveSlowdown.getId() ||
                        effect.getPotionID() == Potion.weakness.getId() ||
                        effect.getPotionID() == Potion.poison.getId() ||
                        effect.getPotionID() == Potion.harm.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getBestBow(IInventory inventory) {
        int bestBow = -1;
        double lastPower = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemBow)) {
                continue;
            }
            double protection = getPower(item);
            if (protection > lastPower) {
                lastPower = protection;
                bestBow = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemBow)) {
                    continue;
                }
                double power = getPower(item);
                if (power > lastPower) {
                    lastPower = power;
                    bestBow = i;
                }
            }
        }
        return bestBow;
    }

    private float getPower(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemBow) {
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            score += (float) (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 0.5);
            score += (float) (EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1);
        }
        return score;
    }

    private int getBestRod(IInventory inventory) {
        int bestRod = -1;
        double lastKnocback = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemFishingRod)) {
                continue;
            }
            double knockback = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, item);
            if (knockback > lastKnocback) {
                lastKnocback = knockback;
                bestRod = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemFishingRod)) {
                    continue;
                }
                double knockback = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, item);
                if (knockback > lastKnocback) {
                    lastKnocback = knockback;
                    bestRod = i;
                }
            }
        }
        return bestRod;
    }

    private int getBestTool(ItemStack itemStack, IInventory inventory) {
        int bestTool = -1;
        double lastEfficiency = -1;
        Block blockType = Blocks.dirt;
        if (itemStack.getItem() instanceof ItemAxe) {
            blockType = Blocks.log;
        }
        else if (itemStack.getItem() instanceof ItemPickaxe) {
            blockType = Blocks.stone;
        }
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemTool) || item.getItem() != itemStack.getItem()) {
                continue;
            }
            double efficiency = Utils.getEfficiency(item, blockType);
            if (efficiency > lastEfficiency) {
                lastEfficiency = efficiency;
                bestTool = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemTool) || item.getItem() != itemStack.getItem()) {
                    continue;
                }
                double efficiency = Utils.getEfficiency(item, blockType);
                if (efficiency > lastEfficiency) {
                    lastEfficiency = efficiency;
                    bestTool = i;
                }
            }
        }
        return bestTool;
    }

    private int getBestPotion(int desiredSlot, IInventory inventory) {
        int amplifier = -1;
        int bestPotion = -1;
        double amplifierInSlot = -1;
        if (amplifierInSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && itemStackInSlot.getItem() instanceof ItemPotion) {
                amplifierInSlot = getPotionLevel(itemStackInSlot);
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() instanceof ItemPotion) {
                List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
                if (list == null) {
                    continue;
                }
                for (PotionEffect effect : list) {
                    if (effect.getEffectName().equals("potion.moveSpeed")) {
                        int level = effect.getAmplifier() + effect.getDuration();
                        if (level > amplifier) {
                            amplifier = level;
                            bestPotion = i;
                        }
                        break;
                    }
                }
            }
        }
        return bestPotion;
    }

    private int getPotionLevel(ItemStack item) {
        List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
        if (list == null) {
            return -1;
        }
        for (PotionEffect effect : list) {
            if (effect.getEffectName().equals("potion.moveSpeed")) {
                return effect.getAmplifier() + effect.getDuration();
            }
        }
        return -1;
    }

    private int getBiggestStack(Item targetItem, int desiredSlot) {
        int stack = 0;
        int biggestSlot = -1;
        int stackInSlot = -1;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null) {
                stackInSlot = itemStackInSlot.stackSize;
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() == targetItem && item.stackSize > stack && item.stackSize > stackInSlot) {
                stack = item.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private int getMostProjectiles(int desiredSlot) {
        int biggestSnowballSlot = getBiggestStack(Items.snowball, (int) projectileSlot.getInput());
        int biggestEggSlot = getBiggestStack(Items.egg, (int) projectileSlot.getInput());
        int biggestSlot = -1;
        int stackInSlot = 0;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && (itemStackInSlot.getItem() instanceof ItemEgg || itemStackInSlot.getItem() instanceof ItemSnowball)) {
                stackInSlot = itemStackInSlot.stackSize;
            }
        }
        if (stackInSlot >= biggestEggSlot && stackInSlot >= biggestSnowballSlot) {
            return -1;
        }
        if (biggestEggSlot > biggestSnowballSlot) {
            biggestSlot = biggestEggSlot;
        }
        else if (biggestSnowballSlot > biggestEggSlot) {
            biggestSlot = biggestSnowballSlot;
        }
        else if (biggestSnowballSlot != -1 && biggestEggSlot != -1 && biggestEggSlot == biggestSnowballSlot) {
            biggestSlot = biggestSnowballSlot;
        }
        return biggestSlot;
    }

    private int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;
        ItemStack itemStackInSlot = getItemStack((int) (blocksSlot.getInput() + 35));
        int stackInSlot = 0;
        if (itemStackInSlot != null) {
            stackInSlot = itemStackInSlot.stackSize;
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() instanceof ItemBlock && item.stackSize > stack && canBePlaced((ItemBlock) item.getItem()) && item.stackSize > stackInSlot) {
                stack = item.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private ItemStack getItemStack(int i) {
        Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
        if (slot == null) {
            return null;
        }
        ItemStack item = slot.getStack();
        return item;
    }

    public static boolean canBePlaced(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        if (block == null) {
            return false;
        }
        return !BlockUtils.isInteractable(block) && !(block instanceof BlockLever) && !(block instanceof BlockButton) && !(block instanceof BlockSkull) && !(block instanceof BlockLiquid) && !(block instanceof BlockCactus) && !(block instanceof BlockCarpet) && !(block instanceof BlockTripWire) && !(block instanceof BlockTripWireHook) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFlower) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockSign) && !(block instanceof BlockLadder) && !(block instanceof BlockTorch) && !(block instanceof BlockRedstoneTorch) && !(block instanceof BlockFence) && !(block instanceof BlockPane) && !(block instanceof BlockStainedGlassPane) && !(block instanceof BlockGravel) && !(block instanceof BlockClay) && !(block instanceof BlockSand) && !(block instanceof BlockSoulSand);
    }

    private boolean canDrop(ItemStack itemStack, int slot) {
        // Always drop eggs and snowballs during cleaning
        if (itemStack.getItem() instanceof ItemEgg || itemStack.getItem() instanceof ItemSnowball) {
            return true;
        }
        // Drop ignore-items and bad potions
        if (Arrays.stream(ignoreItems).anyMatch(itemStack.getUnlocalizedName().toLowerCase()::contains)) {
            return true;
        }
        if (dropPotion(itemStack)) {
            return true;
        }
        // Drop non-best sword
        if (itemStack.getItem() instanceof ItemSword && getBestSword(null, (int) swordSlot.getInput()) != slot) {
            return true;
        }
        // Drop non-best armor
        if (itemStack.getItem() instanceof ItemArmor &&
                getBestArmor(((ItemArmor) itemStack.getItem()).armorType, null) != slot) {
            return true;
        }
        // Drop non-best tool
        if (itemStack.getItem() instanceof ItemTool && getBestTool(itemStack, null) != slot) {
            return true;
        }
        // Drop non-best bow
        if (itemStack.getItem() instanceof ItemBow && getBestBow(null) != slot) {
            return true;
        }
        // Drop non-best fishing rod
        return itemStack.getItem() instanceof ItemFishingRod && getBestRod(null) != slot;
    }
}
