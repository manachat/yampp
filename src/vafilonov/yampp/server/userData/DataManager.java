package vafilonov.yampp.server.userData;


import vafilonov.yampp.util.TimedMessage;

import java.nio.channels.SelectionKey;
import java.sql.Time;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;


public class DataManager {

    private volatile boolean shutdown = false;

    private final UserRegister userRegister = new UserRegister();
    private final DialogRegister dialogRegister = new DialogRegister();

    private final ConcurrentHashMap<String, SelectionKey> online = new ConcurrentHashMap<>();


    // submits message to dialog register
    public boolean submitMessage(int sender, String dest, String message, ZonedDateTime time) {
        var senderUsr = userRegister.getUserById(sender);
        var destUsr = userRegister.getUserByName(dest);
        if (destUsr == null) {
            return false;
        }
        DialogRegister.Dialog dialog = dialogRegister.getDialog(senderUsr.getId(), destUsr.getId());
        TimedMessage timedMessage = new TimedMessage(message, time);
        dialog.putMessage(sender, timedMessage);
        destUsr.offerSender(sender);
        tryNotification(dest);
        return true;
    }

    // attempts notification of online thread
    private void tryNotification(String receiver) {
        SelectionKey selectionKey = online.getOrDefault(receiver, null);
        if (selectionKey != null) {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            selectionKey.selector().wakeup();
        }
    }

    public TimedMessage retrieveMessage(int receiver) {
        UserRegister.User receiverUser = userRegister.getUserById(receiver);
        int sender = receiverUser.pollSender();
        if (sender == -1) {
            return null;
        }

        var dialog = dialogRegister.getDialog(sender, receiver);
        return dialog.getMessage(receiver);
    }

    public int createUser(String name) {
        return userRegister.createUser(name);
    }

    public int getUserId(String name) {
        return userRegister.getUserIdByName(name);
    }

    /**
     * Registers user thread to get notifications about messages.
     * @param username
     * @param key
     */
    public boolean registerUserThread(String username, SelectionKey key) {
        var k = online.putIfAbsent(username, key);
        return k == null;
    }

    /**
     * Unregisters thread from notifications
     * @param username
     */
    public void unregisterUserThread(String username) {
        if (username != null) {
            online.remove(username);
        }
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
