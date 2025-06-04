package neo.script.classes;

import neo.util.world.block.BlockUtils;

public class Block {
    public String type;
    public String name;
    public boolean interactable;

    public Block(net.minecraft.block.Block block) {
        this.type = block.getClass().getSimpleName();
        this.name = block.getUnlocalizedName().substring(5).replace(".name", "");
        this.interactable = BlockUtils.isInteractable(block);
    }
}
