package vafilonov.yampp.server;

import vafilonov.yampp.server.userData.UserRegister;

import java.awt.*;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Supermanager {

    private volatile boolean shutdown = false;

    private final UserRegister userRegister = new UserRegister();
    private final ConcurrentHashMap<Integer, Dialog> dialogRegister = new ConcurrentHashMap<>();


    public void submitMessage() {

    }

    private void tryNotification() {

    }

    public int createUser(String name) {
        return userRegister.createUser(name);
    }

    /**
     * Registers user thread to get notifications about messages.
     * @param username
     * @param key
     */
    public boolean registerUserThread(String username, SelectionKey key) {

    }

    public void unregisterUserThread(String username) {
        dialogRegister.get()
    }

    /**
     * Shutdowns manager.
     * Used to notify handler threads about executor shutdown
     */
    public synchronized void shutdown() {
        shutdown = true;
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }



}
