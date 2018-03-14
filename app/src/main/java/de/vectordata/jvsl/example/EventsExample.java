package de.vectordata.jvsl.example;

/**
 * Created by Twometer on 09.03.2018.
 * (c) 2018 Twometer
 */

class EventsExample {

    private TestListener listener;

    public void setListener(TestListener listener) {
        this.listener = listener;
    }

    public void someMethod() {
        // Some code
        if(listener != null) listener.onEvent(); // Raise the event
        // Some more code
    }

    interface TestListener {
        void onEvent();
    }

}
