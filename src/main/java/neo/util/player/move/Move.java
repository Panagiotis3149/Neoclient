package neo.util.player.move;

import org.jetbrains.annotations.Contract;

public enum Move {
    FORWARD(0, 0.98f, 0f, Direction.POSITIVE_Z),
    FORWARD_RIGHT(45, 0.98f, -0.98f, Direction.POSITIVE_Z),
    RIGHT(90, 0f, -0.98f, Direction.NEGATIVE_X),
    BACKWARD_RIGHT(135, -0.98f, -0.98f, Direction.NEGATIVE_X),
    BACKWARD(180, -0.98f, 0f, Direction.NEGATIVE_Z),
    BACKWARD_LEFT(225, -0.98f, 0.98f, Direction.NEGATIVE_Z),
    LEFT(270, 0f, 0.98f, Direction.POSITIVE_X),
    FORWARD_LEFT(315, 0.98f, 0.98f, Direction.POSITIVE_X);

    private final float deltaYaw;
    private final float forward;
    private final float strafing;
    private final Direction direction;

    // Constructor
    Move(float deltaYaw, float forward, float strafing, Direction direction) {
        this.deltaYaw = deltaYaw;
        this.forward = forward;
        this.strafing = strafing;
        this.direction = direction;
    }

    // Getter for deltaYaw
    public float getDeltaYaw() {
        return deltaYaw;
    }

    // Getter for forward
    public float getForward() {
        return forward;
    }

    // Getter for strafing
    public float getStrafing() {
        return strafing;
    }

    // Getter for direction
    public Direction getDirection() {
        return direction;
    }

    @Contract(pure = true)
    public static Move fromMovement(float forward, float strafing) {
        if (forward > 0)
            if (strafing > 0)
                return FORWARD_LEFT;
            else if (strafing < 0)
                return FORWARD_RIGHT;
            else
                return FORWARD;
        else if (forward < 0)
            if (strafing > 0)
                return BACKWARD_LEFT;
            else if (strafing < 0)
                return BACKWARD_RIGHT;
            else
                return BACKWARD;
        else
        if (strafing > 0)
            return LEFT;
        else if (strafing < 0)
            return RIGHT;
        else
            return FORWARD;
    }

    @Contract(pure = true)
    public static Move fromDeltaYaw(float yaw) {
        yaw = RotationUtils.normalize(yaw, 0, 360);

        Move bestMove = FORWARD;
        float bestDeltaYaw = Math.abs(yaw - bestMove.getDeltaYaw());

        for (Move move : values()) {
            if (move != bestMove) {
                float deltaYaw = Math.abs(yaw - move.getDeltaYaw());
                if (deltaYaw < bestDeltaYaw) {
                    bestMove = move;
                    bestDeltaYaw = deltaYaw;
                }
            }
        }

        return bestMove;
    }

    public Move reverse() {
        switch (this) {
            case FORWARD:
                return BACKWARD;
            case FORWARD_RIGHT:
                return BACKWARD_LEFT;
            case RIGHT:
                return LEFT;
            case BACKWARD_RIGHT:
                return FORWARD_LEFT;
            default:
            case BACKWARD:
                return FORWARD;
            case BACKWARD_LEFT:
                return FORWARD_RIGHT;
            case LEFT:
                return RIGHT;
            case FORWARD_LEFT:
                return BACKWARD_RIGHT;
        }
    }
}
