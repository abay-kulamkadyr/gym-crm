package com.epam.infrastructure.persistence.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class UsernameFinderTest {

    // Simple test DAO class
    private record TestUserDAO(String username) {}

    // ========================================================================
    // FIND LATEST USERNAME TESTS
    // ========================================================================

    @Test
    void findLatestUsername_shouldReturnEmptyWhenCollectionIsEmpty() {
        // Given
        Collection<TestUserDAO> emptyCollection = Collections.emptyList();
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(emptyCollection, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findLatestUsername_shouldReturnEmptyWhenPrefixIsNull() {
        // Given
        Collection<TestUserDAO> daos = List.of(new TestUserDAO("John.Doe"), new TestUserDAO("John.Doe1"));

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, null, TestUserDAO::username);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findLatestUsername_shouldReturnBaseUsernameWhenNoSerialNumbers() {
        // Given
        Collection<TestUserDAO> daos = List.of(new TestUserDAO("John.Doe"), new TestUserDAO("Jane.Smith"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe");
    }

    @Test
    void findLatestUsername_shouldReturnUsernameWithHighestSerialNumber() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("John.Doe1"),
                new TestUserDAO("John.Doe5"),
                new TestUserDAO("John.Doe3"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe5");
    }

    @Test
    void findLatestUsername_shouldHandleLargeSerialNumbers() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("John.Doe999"),
                new TestUserDAO("John.Doe1000"),
                new TestUserDAO("John.Doe500"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe1000");
    }

    @Test
    void findLatestUsername_shouldIgnoreNonMatchingPrefixes() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("Jane.Smith1"),
                new TestUserDAO("Jane.Smith5"),
                new TestUserDAO("John.Doe2"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe2");
    }

    @Test
    void findLatestUsername_shouldReturnEmptyWhenNoPrefixMatch() {
        // Given
        Collection<TestUserDAO> daos =
                List.of(new TestUserDAO("Jane.Smith"), new TestUserDAO("Jane.Smith1"), new TestUserDAO("Bob.Jones"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findLatestUsername_shouldHandleNullUsernames() {
        // Given
        Collection<TestUserDAO> daos =
                List.of(new TestUserDAO("John.Doe"), new TestUserDAO(null), new TestUserDAO("John.Doe1"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe1");
    }

    @Test
    void findLatestUsername_shouldTreatInvalidSerialAsZero() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("John.DoeABC"), // Invalid
                // serial
                new TestUserDAO("John.Doe1"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then - John.Doe1 should win because DoeABC's serial is treated as 0
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe1");
    }

    @Test
    void findLatestUsername_shouldTreatBaseUsernameAsSerialZero() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"), // Serial 0
                new TestUserDAO("John.DoeXYZ") // Invalid serial, treated as 0
                );
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then - Either could be returned (both have serial 0), but one will be selected
        assertThat(result).isPresent();
        assertThat(result.get()).startsWith(prefix);
    }

    @Test
    void findLatestUsername_shouldHandleMixedValidAndInvalidSerials() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("John.Doe2"),
                new TestUserDAO("John.DoeABC"),
                new TestUserDAO("John.Doe10"),
                new TestUserDAO("John.Doe!@#"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe10");
    }

    @Test
    void findLatestUsername_shouldHandleVeryLargeSerialNumbers() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("John.Doe999999999"),
                new TestUserDAO("John.Doe1000000000"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe1000000000");
    }

    @Test
    void findLatestUsername_shouldBeCaseSensitive() {
        // Given
        Collection<TestUserDAO> daos =
                List.of(new TestUserDAO("john.doe"), new TestUserDAO("John.Doe"), new TestUserDAO("John.Doe1"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then - Should only match exact prefix
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe1");
    }

    @Test
    void findLatestUsername_shouldHandleSingleElementCollection() {
        // Given
        Collection<TestUserDAO> daos = List.of(new TestUserDAO("John.Doe5"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe5");
    }

    @Test
    void findLatestUsername_shouldHandleEmptyPrefix() {
        // Given
        Collection<TestUserDAO> daos = List.of(new TestUserDAO("John.Doe"), new TestUserDAO("Jane.Smith"));
        String prefix = "";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then - All usernames start with empty string
        assertThat(result).isPresent();
    }

    @Test
    void findLatestUsername_shouldWorkWithDifferentExtractors() {
        // Given
        class CustomDAO {

            private final String userIdentifier;

            CustomDAO(String userIdentifier) {
                this.userIdentifier = userIdentifier;
            }

            String getUserIdentifier() {
                return userIdentifier;
            }
        }

        Collection<CustomDAO> daos =
                List.of(new CustomDAO("John.Doe"), new CustomDAO("John.Doe3"), new CustomDAO("John.Doe1"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, CustomDAO::getUserIdentifier);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe3");
    }

    @Test
    void findLatestUsername_shouldHandleLeadingZerosInSerial() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("John.Doe01"), // Leading
                // zero
                new TestUserDAO("John.Doe001"), // Leading zeros
                new TestUserDAO("John.Doe10"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then - 10 should be highest
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe10");
    }

    @Test
    void findLatestUsername_shouldHandleNegativeNumbersInSerial() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"), new TestUserDAO("John.Doe-5") // Negative
                // number
                );
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then - John.Doe-5 will be parsed, -5 < 0 (base username)
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe");
    }

    @Test
    void findLatestUsername_shouldHandleAllNullUsernames() {
        // Given
        Collection<TestUserDAO> daos = List.of(new TestUserDAO(null), new TestUserDAO(null), new TestUserDAO(null));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findLatestUsername_shouldHandlePartialPrefixMatches() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe"),
                new TestUserDAO("John.Doe1"),
                new TestUserDAO("John.Doenot") // Starts with prefix but not a serial
                );
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then - "John.Doenot" should be treated as serial "not" (invalid, so 0)
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe1");
    }

    @Test
    void findLatestUsername_shouldHandleSpecialCharactersInPrefix() {
        // Given
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.O'Brien"), new TestUserDAO("John.O'Brien1"), new TestUserDAO("John.O'Brien5"));
        String prefix = "John.O'Brien";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.O'Brien5");
    }

    @Test
    void findLatestUsername_shouldWorkWithUnsortedCollection() {
        // Given - Intentionally unsorted
        Collection<TestUserDAO> daos = List.of(
                new TestUserDAO("John.Doe7"),
                new TestUserDAO("John.Doe2"),
                new TestUserDAO("John.Doe100"),
                new TestUserDAO("John.Doe1"),
                new TestUserDAO("John.Doe50"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe100");
    }

    @Test
    void findLatestUsername_shouldHandleZeroSerial() {
        // Given
        Collection<TestUserDAO> daos =
                List.of(new TestUserDAO("John.Doe"), new TestUserDAO("John.Doe0"), new TestUserDAO("John.Doe1"));
        String prefix = "John.Doe";

        // When
        Optional<String> result = UsernameFinder.findLatestUsername(daos, prefix, TestUserDAO::username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("John.Doe1");
    }
}
