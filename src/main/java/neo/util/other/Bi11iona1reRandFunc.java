package neo.util.other;

import java.util.Random;

public class Bi11iona1reRandFunc {

    public static int pro(int min, int max) {
        if (min > max) {
            throw new IndexOutOfBoundsException("ewwor");
        }

        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

}