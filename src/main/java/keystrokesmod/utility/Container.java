package keystrokesmod.utility;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Container<T> {
    private final List<T> items = new CopyOnWriteArrayList<>();

    public List<T> getItems() {
        return items;
    }
}
