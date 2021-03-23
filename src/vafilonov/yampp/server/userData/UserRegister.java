package vafilonov.yampp.server.userData;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basically, a wrapper for username->id (concurrent)mapping
 */
public class UserRegister {

    /**
     * mapping of username to User instance
     */
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    /**
     * array of users for Id access
     */
    private final ArrayList<User> userVector = new ArrayList<>();


    /**
     * Returns userId associated with name
     * @param username username
     * @return userId or -1 if user does not exist
     */
    int getUserIdByName(String username) {
        User u = users.getOrDefault(username, null);
        return u == null ? -1 : u.getId();
    }

    /**
     * Returns user by its id
     * @param id userId
     * @return  user
     */
    User getUserById(int id) {
        return userVector.get(id - 1);
    }

    /**
     * Returns user by its name or null if he does not exist.
     * @param name username
     * @return user or null in case of error
     */
    User getUserByName(String name) {
        return users.getOrDefault(name, null);
    }



    /**
     * Pool of userIds
     */
    private final AtomicInteger idPool = new AtomicInteger(0);

    /**
     * Attempts to create new user and put him into catalog.
     * Returns id of created user or error code if creation is impossible
     * @param name username
     * @return created user id; -1 if user already exists, -10 if user limit reached
     */
    public int createUser(String name) {
        if (users.containsKey(name)){
            return -1;
        }
        int newId =  idPool.addAndGet(1);

        /*
        Needed for userVector
         */
        if (newId < 0) {
            return -10;
        }

        User newUser = new User(name, newId);

        users.put(name, newUser);
        userVector.add(newUser);
        //userVector.set(newId, newUser);
        return newId;
    }

    static class User {
        private final int       id;
        private final String    name;

        private final Queue<Integer> incomingSenders = new ArrayDeque<>();

        synchronized void offerSender(int id) {
            incomingSenders.offer(id);
        }

        /**
         * polls sender from queue
         * if queue is empty, returns - 1
         * @return
         */
        synchronized int pollSender() {
            if (!incomingSenders.isEmpty()) {
                return incomingSenders.poll();
            } else {
                return -1;
            }
        }

        private User(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }


    }








}
