package com.speechify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UserRepository {
    private static final String ALL_USERS_KEY = "__ALL_USERS__";
    private final JsonDb db;
    private final ObjectMapper mapper;
    private final LRUCache<User> userByEmailCache;
    private final LRUCache<List<User>> allUsersCache;

    public UserRepository(JsonDb db, ObjectMapper mapper, CacheLimits userCacheLimits) {
        this.db = db;
        this.mapper = mapper;
        this.userByEmailCache = LRUCacheProvider.createLRUCache(userCacheLimits);
        this.allUsersCache = LRUCacheProvider.createLRUCache(new CacheLimits(1));
    }

    public synchronized List<User> getAll() {
        List<User> cached = allUsersCache.get(ALL_USERS_KEY);
        if (cached != null) {
            return new ArrayList<>(cached);
        }
        List<User> loaded = loadAllFromDb();
        allUsersCache.set(ALL_USERS_KEY, new ArrayList<>(loaded));
        return loaded;
    }

    public synchronized User getByEmail(String email) {
        User cached = userByEmailCache.get(email);
        if (cached != null) {
            return cached;
        }
        User loaded = loadByEmailFromDb(email);
        if (loaded != null) {
            userByEmailCache.set(email, loaded);
        }
        return loaded;
    }

    public synchronized boolean add(User user) {
        if (loadByEmailFromDb(user.getEmail()) != null) {
            return false;
        }
        boolean saved = appendToDb(user);
        if (saved) {
            userByEmailCache.set(user.getEmail(), user);
            List<User> cachedList = allUsersCache.get(ALL_USERS_KEY);
            if (cachedList != null) {
                List<User> updated = new ArrayList<>(cachedList);
                updated.add(user);
                allUsersCache.set(ALL_USERS_KEY, updated);
            }
        }
        return saved;
    }

    public synchronized boolean update(User user) {
        boolean updated = replaceByIdInDb(user);
        if (updated) {
            userByEmailCache.set(user.getEmail(), user);
            List<User> cachedList = allUsersCache.get(ALL_USERS_KEY);
            if (cachedList != null) {
                List<User> updatedList = new ArrayList<>(cachedList);
                for (int i = 0; i < updatedList.size(); i++) {
                    if (updatedList.get(i).getId().equals(user.getId())) {
                        updatedList.set(i, user);
                        break;
                    }
                }
                allUsersCache.set(ALL_USERS_KEY, updatedList);
            }
        }
        return updated;
    }

    private List<User> loadAllFromDb() {
        try {
            ObjectNode root = db.readRoot();
            ArrayNode users = (ArrayNode) root.get("users");
            List<User> result = new ArrayList<>();
            for (int i = 0; i < users.size(); i++) {
                result.add(mapper.treeToValue(users.get(i), User.class));
            }
            return result;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private User loadByEmailFromDb(String email) {
        try {
            ObjectNode root = db.readRoot();
            ArrayNode users = (ArrayNode) root.get("users");
            for (int i = 0; i < users.size(); i++) {
                ObjectNode node = (ObjectNode) users.get(i);
                if (node.get("email").asText().equals(email)) {
                    return mapper.treeToValue(node, User.class);
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private boolean appendToDb(User user) {
        try {
            ObjectNode root = db.readRoot();
            ArrayNode users = (ArrayNode) root.get("users");
            users.add(mapper.valueToTree(user));
            db.writeRoot(root);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean replaceByIdInDb(User user) {
        try {
            ObjectNode root = db.readRoot();
            ArrayNode users = (ArrayNode) root.get("users");
            for (int i = 0; i < users.size(); i++) {
                ObjectNode node = (ObjectNode) users.get(i);
                if (node.get("id").asText().equals(user.getId())) {
                    users.set(i, mapper.valueToTree(user));
                    db.writeRoot(root);
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
