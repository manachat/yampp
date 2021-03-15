package vafilonov.yampp.server.userData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basically, a wrapper for username->id (concurrent)mapping
 */
public class UserRegister {

    /**
     * mapping
     */
    private final ConcurrentHashMap<String, Integer> users = new ConcurrentHashMap<>();

    /**
     * Pool of userIds
     */
    private final AtomicInteger idPool = new AtomicInteger(0);

    /**
     * Attempts to create new user and put him into catalog.
     * If user already exists, returns null.
     * @param name username
     * @return created user id; -1 if user already exists, -10 if user limit reached
     */
    public int createUser(String name) {
        if (users.containsKey(name)){
            return -1;
        }
        int newId =  idPool.addAndGet(1);

        /*
        Needed for dialog_id format
         */
        if (newId < 0) {
            return -10;
        }

        users.put(name, newId);
        return newId;
    }

    /**
     * Returns userId associated with
     * @param username username
     * @return userId or -1 if user does not exist
     */
    public int getUserId(String username) {
        return users.getOrDefault(username, -1);
    }





}
