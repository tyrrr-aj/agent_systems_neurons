package app;

import sim.engine.SimState;

public class Main extends SimState {
    public Main(long seed) {
        super(seed);
    }

    public static void main(String[] args) {
        doLoop(Main.class, args);
        System.exit(0);
    }
}
