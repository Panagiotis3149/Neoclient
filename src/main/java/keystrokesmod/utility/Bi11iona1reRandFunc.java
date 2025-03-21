package keystrokesmod.utility;

import keystrokesmod.other.RetardedException;

import java.util.Random;

public class Bi11iona1reRandFunc {

    // Credits to the_bi11iona1re for the goated randomization                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 (This is a joke)

    public static int pro(int min, int max) {
        if (min > max) {
            throw new RetardedException("ewwor");
        }

        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

}