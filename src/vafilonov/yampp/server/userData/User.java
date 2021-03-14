package vafilonov.yampp.server.userData;

import java.util.*;

public class User {

    private short id;
    private String name;
    private HashSet<Dialog> dialogs = new HashSet<>();

    private static HashMap<String, Short> users = new HashMap<>();

    private static short idPool = 1;

    /**
     * Attempts to create new user and put him into catalog.
     * If user already exists, returns null.
     * @param name username
     * @return  created user profile
     */
    public static User createUser(String name) {
        if (users.containsKey(name)){
            return null;
        }
        short newId = idPool++;

        User newUser = new User(name, newId);
        users.put(name, newId);
        return newUser;
    }



    private User(String name, short id) {
        this.name = name;
        this.id = id;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
