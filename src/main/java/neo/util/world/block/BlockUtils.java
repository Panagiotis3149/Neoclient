package neo.util.world.block;

import neo.util.Utils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class BlockUtils {
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isSamePos(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || (blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ());
    }

    public static boolean notFull(Block block) {
        return block instanceof BlockFenceGate || block instanceof BlockLadder || block instanceof BlockFlowerPot || block instanceof BlockBasePressurePlate || isFluid(block) || block instanceof BlockFence || block instanceof BlockAnvil || block instanceof BlockEnchantmentTable || block instanceof BlockChest;
    }

    public static boolean isFluid(Block block) {
        return block.getMaterial() == Material.lava || block.getMaterial() == Material.water;
    }

    public static boolean isInteractable(Block block) {
        return block instanceof BlockFurnace || block instanceof BlockFenceGate || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockEnchantmentTable || block instanceof BlockBrewingStand || block instanceof BlockBed || block instanceof BlockDropper || block instanceof BlockDispenser || block instanceof BlockHopper || block instanceof BlockAnvil || block == Blocks.crafting_table;
    }

    public static float getBlockHardness(final Block block, final ItemStack itemStack, boolean ignoreSlow, boolean ignoreGround) {
        final float getBlockHardness = block.getBlockHardness(mc.theWorld, null);
        if (getBlockHardness < 0.0f) {
            return 0.0f;
        }
        return (block.getMaterial().isToolNotRequired() || (itemStack != null && itemStack.canHarvestBlock(block))) ? (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 30.0f) : (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 100.0f);
    }

    public static float getToolDigEfficiency(ItemStack itemStack, Block block, boolean ignoreSlow, boolean ignoreGround) {
        float n = (itemStack == null) ? 1.0f : itemStack.getItem().getStrVsBlock(itemStack, block);
        if (n > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0 && itemStack != null) {
                n += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            n *= 1.0f + (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (!ignoreSlow) {
            if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
                float n2;
                switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                    case 0: {
                        n2 = 0.3f;
                        break;
                    }
                    case 1: {
                        n2 = 0.09f;
                        break;
                    }
                    case 2: {
                        n2 = 0.0027f;
                        break;
                    }
                    default: {
                        n2 = 8.1E-4f;
                        break;
                    }
                }
                n *= n2;
            }
            if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
                n /= 5.0f;
            }
            if (!mc.thePlayer.onGround && !ignoreGround) {
                n /= 5.0f;
            }
        }
        return n;
    }

    public static Block getBlock(BlockPos blockPos) {
        return getBlockState(blockPos).getBlock();
    }

    public static Block getBlock(double x, double y, double z) {
        return getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static IBlockState getBlockState(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos);
    }

    public static boolean isBlockUnderNoCollisions() {
        for (int offset = 0; offset < mc.thePlayer.posY + mc.thePlayer.getEyeHeight(); offset += 2) {
            BlockPos blockPos = new BlockPos(mc.thePlayer.posX, offset, mc.thePlayer.posZ);

            if (mc.theWorld.getBlockState(blockPos).getBlock() != Blocks.air) {
                return true;
            }
        }
        return false;
    }


    public static boolean isBlockUnder(int distance) {
        for(int y = (int)mc.thePlayer.posY; y >= (int)mc.thePlayer.posY - distance; --y) {
            if (!(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ)).getBlock() instanceof BlockAir)) {
                return true;
            }
        }

        return false;
    }


    public static boolean check(final BlockPos blockPos, final Block block) {
        return getBlock(blockPos) == block;
    }

    public static boolean replaceable(BlockPos blockPos) {
        if (!Utils.isnull()) {
            return true;
        }
        return getBlock(blockPos).isReplaceable(mc.theWorld, blockPos);
    }

    public static Block block(final double x, final double y, final double z) {
        return Utils.mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return block(Utils.mc.thePlayer.posX + offsetX, Utils.mc.thePlayer.posY + offsetY, Utils.mc.thePlayer.posZ + offsetZ);
    }

    public static boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public static boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (boundingBox) {
            final AxisAlignedBB bb = Utils.mc.thePlayer.getEntityBoundingBox().offset(0, -height, 0);

            return !Utils.mc.theWorld.getCollidingBoundingBoxes(Utils.mc.thePlayer, bb).isEmpty();
        } else {
            for (int offset = 0; offset < height; offset++) {
                if (blockRelativeToPlayer(0, -offset, 0).isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBlockUnder() {
        return isBlockUnder(10);
    }

    public static boolean overAir() {
        return Utils.mc.theWorld.isAirBlock(new BlockPos(Utils.mc.thePlayer.posX, Utils.mc.thePlayer.posY - 1.0, Utils.mc.thePlayer.posZ));
    }

    public static boolean overPlaceable(double yOffset) {
        BlockPos playerPos = new BlockPos(Utils.mc.thePlayer.posX, Utils.mc.thePlayer.posY + yOffset, Utils.mc.thePlayer.posZ);
        return replaceable(playerPos) || isFluid(getBlock(playerPos));
    }

    public static boolean blockNear(final int range) {
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = blockRelativeToPlayer(x, y, z);

                    if (!(block instanceof BlockAir)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
