package kyle.game.besiege;

/** Random utils */
public class Random {

    public static int getRandomIndex(Object[] array) {
        return (int) (Math.random() * array.length);
    }

    public static Object getRandomValue(Object[] array) {
        return array[getRandomIndex(array)];
    }

    // gets a random value between 0 and max
    public static double getRandom(double max) {
        return Math.random() * max;
    }

    public static int getRandom(int max) {
        return (int) (Math.random() * max);
    }

    // gets a random value in the specified range (uniform distribution)
    public static double getRandomInRange(double min, double max) {
        return getRandom(max - min) + min;
    }
    public static int getRandomInRange(int min, int max) {
        return getRandom(max - min) + min;
    }
}
