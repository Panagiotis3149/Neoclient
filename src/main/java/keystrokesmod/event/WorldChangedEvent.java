package keystrokesmod.event;

import net.minecraft.world.World;

public class WorldChangedEvent extends Event {
    private World oldWorld;
    private World newWorld;

    // Constructor
    public WorldChangedEvent(World oldWorld, World newWorld) {
        this.oldWorld = oldWorld;
        this.newWorld = newWorld;
    }

    // Getters
    public World getOldWorld() {
        return oldWorld;
    }

    public World getNewWorld() {
        return newWorld;
    }

    // Setters
    public void setOldWorld(World oldWorld) {
        this.oldWorld = oldWorld;
    }

    public void setNewWorld(World newWorld) {
        this.newWorld = newWorld;
    }
}
