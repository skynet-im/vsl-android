package de.vectordata.libjvsl.util.cscompat;

/**
 * Created by Twometer on 07.03.2018.
 * (c) 2018 Twometer
 */

public class Nullable<T> {

    private T value;

    public Nullable(T value) {
        this.value = value;
    }

    public boolean hasValue() {
        return value != null;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
