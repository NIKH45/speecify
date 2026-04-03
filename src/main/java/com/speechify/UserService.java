package com.speechify;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserService {
    private static final String DB_FILE = "db.json";
    private static final int USER_CACHE_SIZE = 100;
    private final ObjectMapper objectMapper;
    private final JsonDb db;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final CreditLimitPolicy creditLimitPolicy;

    public UserService() {
        this.objectMapper = new ObjectMapper();
        this.db = new JsonDb(DB_FILE, objectMapper);
        this.clientRepository = new ClientRepository(db, objectMapper);
        this.userRepository = new UserRepository(db, objectMapper, new CacheLimits(USER_CACHE_SIZE));
        this.creditLimitPolicy = new CreditLimitPolicy();
    }

    public CompletableFuture<Boolean> addUser(
            String firstname,
            String surname,
            String email,
            LocalDate dateOfBirth,
            String clientId) {
        return CompletableFuture.supplyAsync(() -> {
            if (firstname == null || surname == null || email == null) {
                return false;
            }

            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            if (age < 21) {
                return false;
            }

            Client client = clientRepository.getById(clientId).join();
            if (client == null) {
                return false;
            }

            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setClient(client);
            user.setDateOfBirth(dateOfBirth);
            user.setEmail(email);
            user.setFirstname(firstname);
            user.setSurname(surname);

            creditLimitPolicy.apply(user, client);
            return userRepository.add(user);
        });
    }

    public CompletableFuture<Boolean> updateUser(User user) {
        return CompletableFuture.supplyAsync(() -> {
            if (user == null) {
                return false;
            }
            return userRepository.update(user);
        });
    }

    public CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(userRepository::getAll);
    }

    public CompletableFuture<User> getUserByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> userRepository.getByEmail(email));
    }
}
