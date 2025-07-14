package neo.util.player.move;

public enum RotationAt {
    HEAD("Head"),
    CHEST("Chest"),
    LEGS("Legs"),
    FEET("Feet");

    private final String label;

    RotationAt(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
