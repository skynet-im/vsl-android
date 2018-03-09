package de.vectordata.jvsl.util.cscompat;

/**
 * Created by Daniel Lerch on 09.03.2018.
 * © 2018 Daniel Lerch
 */

public class Ref<T> {

    private T value;

    public Ref() {
    }

    public Ref(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
