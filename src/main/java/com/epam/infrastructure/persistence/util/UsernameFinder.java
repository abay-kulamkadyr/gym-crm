package com.epam.infrastructure.persistence.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public final class UsernameFinder {

    private UsernameFinder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> Optional<String> findLatestUsername(
            Collection<T> daos, String prefix, Function<T, String> usernameExtractor) {
        if (prefix == null) {
            return Optional.empty();
        }

        return daos.stream()
                .map(usernameExtractor)
                .filter(u -> u != null && u.startsWith(prefix))
                .max(Comparator.comparingLong(u -> getSerialNumberForComparison(u, prefix)));
    }

    private static long getSerialNumberForComparison(String username, String prefix) {
        String serialPart = username.substring(prefix.length());
        if (serialPart.isEmpty()) return 0L;
        try {
            return Long.parseLong(serialPart);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
