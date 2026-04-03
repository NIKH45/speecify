package com.speechify;

public class CreditLimitPolicy {
    private static final int BASE_LIMIT = 10000;

    public void apply(User user, Client client) {
        if ("VeryImportantClient".equals(client.getName())) {
            user.setHasCreditLimit(false);
        } else if ("ImportantClient".equals(client.getName())) {
            user.setHasCreditLimit(true);
            user.setCreditLimit(BASE_LIMIT * 2);
        } else {
            user.setHasCreditLimit(true);
            user.setCreditLimit(BASE_LIMIT);
        }
    }
}
