package neo.util.packet.backtrack;

import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

public class Warm {

    private long lastMs;

    private long time;

    private boolean checkedFinish;

    public Warm(long lasts) {
        this.lastMs = lasts;
    }

    public Warm() {
        lastMs = System.currentTimeMillis();
    }

    public void start() {
        reset();
        checkedFinish = false;
    }

    public boolean firstFinish() {
        return checkAndSetFinish(() -> System.currentTimeMillis() >= (time + lastMs));
    }

    public void setCooldown(long time) {
        this.lastMs = time;
    }

    public boolean hasFinished() {
        return isElapsed(time + lastMs, System::currentTimeMillis);
    }


    public boolean finished(long delay) {
        return isElapsed(time, () -> System.currentTimeMillis() - delay);
    }


    public boolean isDelayComplete(long l) {
        return isElapsed(lastMs, () -> System.currentTimeMillis() - l);
    }


    public boolean reached(long currentTime) {
        return isElapsed(time, () -> Math.max(0L, System.currentTimeMillis() - currentTime));
    }

    public void reset() {
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return Math.max(0L, System.currentTimeMillis() - time);
    }

    public boolean getIdk(long noIdea) {
        return getTime() - lastMs >= noIdea;
    }


    public boolean hasTimeElapsed(long huh, boolean reset) {
        if (getTime() >= huh) {
            if (reset) {
                reset();
            }
            return true;
        }
        return false;
    }

    private boolean checkAndSetFinish(BooleanSupplier condition) {
        if (condition.getAsBoolean() && !checkedFinish) {
            checkedFinish = true;
            return true;
        }
        return false;
    }

    private boolean isElapsed(long targetTime, LongSupplier currentTimeSupplier) {
        return currentTimeSupplier.getAsLong() >= targetTime;
    }

}