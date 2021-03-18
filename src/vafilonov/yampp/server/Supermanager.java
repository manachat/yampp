package vafilonov.yampp.server;

import vafilonov.yampp.server.userData.DialogRegister;
import vafilonov.yampp.server.userData.UserRegister;


import java.nio.channels.SelectionKey;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class Supermanager {

    private volatile boolean shutdown = false;

    private final UserRegister userRegister = new UserRegister();
    private final DialogRegister dialogRegister = new DialogRegister();
    private final ConcurrentHashMap<Integer, Dialog> dialogRegister = new ConcurrentHashMap<>();


    // submits message to dialog register
    public void submitMessage(int sender, String dest, String message, ZonedDateTime time) {

    }

    // attempts notification of online thread
    private void tryNotification() {

    }

    public int createUser(String name) {
        return userRegister.createUser(name);
    }

    public int getUserId(String name) {
        return userRegister.getUserId(name);
    }

    /**
     * Registers user thread to get notifications about messages.
     * @param username
     * @param key
     */
    public boolean registerUserThread(String username, SelectionKey key) {

    }

    /**
     * Unregisters thread from notifications
     * @param username
     */
    public void unregisterUserThread(String username) {
        dialogRegister.get()
    }

    /**
     * Shutdowns manager. Uncancellable.
     * Used to notify handler threads about executor shutdown
     */
    public synchronized void shutdown() {
        shutdown = true;
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }



}
