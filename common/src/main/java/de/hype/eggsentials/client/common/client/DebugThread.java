package de.hype.eggsentials.client.common.client;


import de.hype.eggsentials.client.common.communication.BBsentialConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DebugThread implements Runnable {
    public static List<Object> store = new ArrayList<>();
    BBsentialConnection connection;

    @Override
    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {

        }
        init();
        while (true) {
        loop();
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
            }
        }
        //place a breakpoint for only this thread here.
    }

    public void loop() {
    }

    public List<String> test() {
        return Collections.singletonList("");
    }

    public void init() {
        connection = Eggsentials.connection;
    }

    public abstract void onServerJoin();

    public abstract void onServerLeave();
}
