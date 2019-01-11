package kyle.game.besiege.title;

import kyle.game.besiege.Random;

public class SelectOption<T> {
    Object[] objectsToGetRandomFrom;
    T object;
    String string;

    // Will use the given object
    public SelectOption(T object, String string) {
        if (object == null) throw new AssertionError(string + " is null");
        this.object = object;
        this.string = string;
    }

    // Will randomly select from the given array
    public SelectOption(T[] objects, String string) {
        if (objects == null) throw new AssertionError(string + " is null");
        this.objectsToGetRandomFrom = objects;
        this.string = string;
    }

    // Will return null
    public SelectOption(String string) {
        this.object = null;
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

    public T getObject() {
        if (object == null && objectsToGetRandomFrom == null) {
            System.out.println(string + " is returning null");
            return null;
        }

        if (object != null) return object;
        T obj = (T) Random.getRandomValue(objectsToGetRandomFrom);
        if (obj == null) {
            System.out.println(string + " got a null value for a random obj");
        }
        return obj;
    }
}
