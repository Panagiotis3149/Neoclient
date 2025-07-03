package neo.mixins.impl.render;

import com.google.common.base.Predicate;
import net.minecraft.scoreboard.Score;

import java.util.Objects;

public class ScoreFilter implements Predicate<Score> {
    @Override
    public boolean apply(Score score) {
        return Objects.requireNonNull(score).getPlayerName() != null && !score.getPlayerName().startsWith("#");
    }
}