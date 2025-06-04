package neo.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class SprintEvent extends Event {
    private boolean sprint;
    private boolean omni;

    // Constructor
    public SprintEvent(boolean sprint, boolean omni) {
        this.sprint = sprint;
        this.omni = omni;
    }

    // Getter for sprint
    public boolean isSprint() {
        return sprint;
    }

    // Setter for sprint
    public void setSprint(boolean sprint) {
        this.sprint = sprint;
    }

    // Getter for omni
    public boolean isOmni() {
        return omni;
    }

    // Setter for omni
    public void setOmni(boolean omni) {
        this.omni = omni;
    }
}
