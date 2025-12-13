package com.epam.application.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.epam.application.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CredentialsUtilTest {

    // ========================================================================
    // GENERATE RANDOM PASSWORD TESTS
    // ========================================================================

    @Test
    void generate_shouldReturnPasswordOfCorrectLength() {
        // When
        String password = CredentialsUtil.generateRandomPassword(10);
        // Then
        assertThat(password).hasSize(10);
    }

    @Test
    void generate_shouldReturnPasswordWithOnlyValidCharacters() {
        // Given
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        // When
        String password = CredentialsUtil.generateRandomPassword(100);
        // Then
        for (char c : password.toCharArray()) {
            assertThat(validChars).contains(String.valueOf(c));
        }
    }

    @Test
    void generate_shouldReturnDifferentPasswordsOnMultipleCalls() {
        // When
        String password1 = CredentialsUtil.generateRandomPassword(10);
        String password2 = CredentialsUtil.generateRandomPassword(10);
        String password3 = CredentialsUtil.generateRandomPassword(10);
        // Then - at least one should be different (statistically almost certain)
        boolean allDifferent = !password1.equals(password3) || !password2.equals(password3);
        assertThat(allDifferent).isTrue();
    }

    @Test
    void generate_shouldHandleDifferentLengths() {
        // When
        String password5 = CredentialsUtil.generateRandomPassword(5);
        String password15 = CredentialsUtil.generateRandomPassword(15);
        String password20 = CredentialsUtil.generateRandomPassword(20);
        // Then
        assertThat(password5).hasSize(5);
        assertThat(password15).hasSize(15);
        assertThat(password20).hasSize(20);
    }

    @Test
    void generate_shouldHandleMinimumLength() {
        // When
        String password = CredentialsUtil.generateRandomPassword(1);
        // Then
        assertThat(password).hasSize(1);
    }

    @Test
    void generate_shouldReturnEmptyStringWithZeroLength() {
        // When
        String password = CredentialsUtil.generateRandomPassword(0);
        // Then
        assertThat(password).isEqualTo("");
    }

    @Test
    void generate_shouldThrowWhenLengthNegative() {
        // Then
        assertThrows(NegativeArraySizeException.class, () -> CredentialsUtil.generateRandomPassword(-20));
    }

    // ========================================================================
    // GENERATE UNIQUE USERNAME TESTS
    // ========================================================================

    @Test
    void generateUniqueUsername_shouldReturnBaseUsernameWhenNoExistingUser() {
        // Given
        String firstName = "John";
        String lastName = "Doe";

        // When
        String username = CredentialsUtil.generateUniqueUsername(firstName, lastName, baseUsername -> Optional.empty());

        // Then
        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void generateUniqueUsername_shouldAppend1WhenBaseUsernameExists() {
        // Given
        String firstName = "John";
        String lastName = "Doe";

        // When
        String username =
                CredentialsUtil.generateUniqueUsername(firstName, lastName, baseUsername -> Optional.of("John.Doe"));

        // Then
        assertThat(username).isEqualTo("John.Doe1");
    }

    @Test
    void generateUniqueUsername_shouldIncrementSerialNumber() {
        // Given
        String firstName = "John";
        String lastName = "Doe";

        // When
        String username =
                CredentialsUtil.generateUniqueUsername(firstName, lastName, baseUsername -> Optional.of("John.Doe5"));

        // Then
        assertThat(username).isEqualTo("John.Doe6");
    }

    @Test
    void generateUniqueUsername_shouldHandleLargeSerialNumbers() {
        // Given
        String firstName = "John";
        String lastName = "Doe";

        // When
        String username =
                CredentialsUtil.generateUniqueUsername(firstName, lastName, baseUsername -> Optional.of("John.Doe999"));

        // Then
        assertThat(username).isEqualTo("John.Doe1000");
    }

    @Test
    void generateUniqueUsername_shouldHandleInvalidSerialNumberFormat() {
        // Given
        String firstName = "John";
        String lastName = "Doe";

        // When - latest username has non-numeric suffix
        String username =
                CredentialsUtil.generateUniqueUsername(firstName, lastName, baseUsername -> Optional.of("John.DoeABC"));

        // Then - should append 1
        assertThat(username).isEqualTo("John.Doe1");
    }

    @Test
    void generateUniqueUsername_shouldHandleEmptySerialNumber() {
        // Given
        String firstName = "Jane";
        String lastName = "Smith";

        // When
        String username =
                CredentialsUtil.generateUniqueUsername(firstName, lastName, baseUsername -> Optional.of("Jane.Smith"));

        // Then
        assertThat(username).isEqualTo("Jane.Smith1");
    }

    // ========================================================================
    // VALIDATE PASSWORD TESTS
    // ========================================================================

    @Test
    void validatePassword_shouldAcceptValidPassword() {
        // Given
        String validPassword = "ValidPass123";

        // When & Then - should not throw
        CredentialsUtil.validatePassword(validPassword);
    }

    @Test
    void validatePassword_shouldThrowWhenPasswordTooShort() {
        // Given
        String shortPassword = "short";

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validatePassword(shortPassword))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be at least 10 characters long");
    }

    @Test
    void validatePassword_shouldThrowWhenPasswordExactly10Characters() {
        // Given
        String password = "1234567890";

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validatePassword(password))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be at least 10 characters long");
    }

    @Test
    void validatePassword_shouldAcceptPasswordWith11Characters() {
        // Given
        String password = "12345678901";

        // When & Then - should not throw
        CredentialsUtil.validatePassword(password);
    }

    @Test
    void validatePassword_shouldThrowWhenPasswordTooLong() {
        // Given
        String longPassword = "a".repeat(101);

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validatePassword(longPassword))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must not exceed 100 characters");
    }

    @Test
    void validatePassword_shouldAcceptPasswordWith100Characters() {
        // Given
        String password = "a".repeat(100);

        // When & Then - should not throw
        CredentialsUtil.validatePassword(password);
    }

    @Test
    void validatePassword_shouldThrowWhenPasswordIsOnlyWhitespace() {
        // Given
        String whitespacePassword = "           ";

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validatePassword(whitespacePassword))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cannot contain only whitespace");
    }

    @Test
    void validatePassword_shouldAcceptPasswordWithWhitespaceAndOtherCharacters() {
        // Given
        String password = "Valid Pass 123";

        // When & Then - should not throw
        CredentialsUtil.validatePassword(password);
    }

    // ========================================================================
    // VALIDATE USERNAME TESTS
    // ========================================================================

    @Test
    void validateUsername_shouldAcceptValidUsername() {
        // Given
        String validUsername = "John.Doe";

        // When & Then - should not throw
        CredentialsUtil.validateUsername(validUsername);
    }

    @Test
    void validateUsername_shouldAcceptUsernameWithSerial() {
        // Given
        String validUsername = "John.Doe1";

        // When & Then - should not throw
        CredentialsUtil.validateUsername(validUsername);
    }

    @Test
    void validateUsername_shouldAcceptUsernameWithLargeSerial() {
        // Given
        String validUsername = "Jane.Smith999";

        // When & Then - should not throw
        CredentialsUtil.validateUsername(validUsername);
    }

    @Test
    void validateUsername_shouldThrowWhenUsernameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername(""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Username cannot be null or empty");
    }

    @Test
    void validateUsername_shouldThrowWhenUsernameIsOnlyWhitespace() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername("   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Username cannot be null or empty");
    }

    @Test
    void validateUsername_shouldThrowWhenUsernameTooShort() {
        // Given
        String shortUsername = "J.D";

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername(shortUsername))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be at least 5 characters long");
    }

    @Test
    void validateUsername_shouldAcceptUsernameWith5Characters() {
        // Given
        String username = "Jo.Do";

        // When & Then - should not throw
        CredentialsUtil.validateUsername(username);
    }

    @Test
    void validateUsername_shouldThrowWhenUsernameTooLong() {
        // Given
        String longUsername = "a".repeat(51) + "." + "b".repeat(50);

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername(longUsername))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must not exceed 100 characters");
    }

    @ParameterizedTest
    @ValueSource(
            strings = { "JohnDoe", // No dot
                    "John_Doe", // Underscore instead of dot
                    "John Doe", // Space instead of dot
                    "john.doe.", // Trailing dot
                    ".john.doe", // Leading dot
                    "John.Doe.Smith", // Multiple dots
                    "123.456", // Numbers as names
                    "John.123", // Numbers in last name
                    "John.Doe-1", // Hyphen with serial
                    "John.Doe ABC" // Space in serial
            })
    void validateUsername_shouldThrowWhenUsernameHasInvalidFormat(String invalidUsername) {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername(invalidUsername))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must follow the format");
    }

    @Test
    void validateUsername_shouldThrowWhenFirstNameTooShort() {
        // Given
        String username = "J.Doe";

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername(username))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name in username must be at least 2 characters long");
    }

    @Test
    void validateUsername_shouldThrowWhenLastNameTooShort() {
        // Given
        String username = "John.D";

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername(username))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Last name in username must be at least 2 characters long");
    }

    @Test
    void validateUsername_shouldThrowWhenSerialNumberIsNegative() {
        // Given
        String username = "John.Doe-5";

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateUsername(username)).isInstanceOf(ValidationException.class);
    }

    @Test
    void validateUsername_shouldAcceptUsernameWithZeroSerial() {
        // Given
        String username = "John.Doe0";

        // When & Then - should not throw
        CredentialsUtil.validateUsername(username);
    }

    // ========================================================================
    // VALIDATE FULL NAME TESTS
    // ========================================================================

    @Test
    void validateFullName_shouldAcceptValidFullName() {
        // Given
        String firstName = "John";
        String lastName = "Doe";

        // When & Then - should not throw
        CredentialsUtil.validateFullName(firstName, lastName);
    }

    @Test
    void validateFullName_shouldThrowWhenFirstNameIsNull() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateFullName(null, "Doe")).isInstanceOf(ValidationException.class);
    }

    @Test
    void validateFullName_shouldThrowWhenLastNameIsNull() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateFullName("John", null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateFullName_shouldThrowWhenBothNamesAreNull() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateFullName(null, null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void validateFullName_shouldThrowWhenFirstNameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateFullName("", "Doe")).isInstanceOf(ValidationException.class);
    }

    @Test
    void validateFullName_shouldThrowWhenLastNameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateFullName("John", "")).isInstanceOf(ValidationException.class);
    }

    @Test
    void validateFullName_shouldThrowWhenFirstNameTooShort() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateFullName("J", "Doe"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name must be at least 2 characters long");
    }

    @Test
    void validateFullName_shouldThrowWhenLastNameTooShort() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateFullName("John", "D"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Last name must be at least 2 characters long");
    }

    @Test
    void validateFullName_shouldAcceptNamesWithHyphens() {
        // Given
        String firstName = "Mary-Jane";
        String lastName = "Watson-Parker";

        // When & Then - should not throw
        CredentialsUtil.validateFullName(firstName, lastName);
    }

    @Test
    void validateFullName_shouldAcceptNamesWithApostrophes() {
        // Given
        String firstName = "O'Brien";
        String lastName = "D'Angelo";

        // When & Then - should not throw
        CredentialsUtil.validateFullName(firstName, lastName);
    }

    @Test
    void validateFullName_shouldAcceptNamesWithSpaces() {
        // Given
        String firstName = "Mary Ann";
        String lastName = "Van Der Berg";

        // When & Then - should not throw
        CredentialsUtil.validateFullName(firstName, lastName);
    }

    // ========================================================================
    // VALIDATE NAME TESTS
    // ========================================================================

    @Test
    void validateName_shouldAcceptValidName() {
        // Given
        String name = "John";

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "First name");
    }

    @Test
    void validateName_shouldThrowWhenNameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateName("", "Last name")).isInstanceOf(ValidationException.class);
    }

    @Test
    void validateName_shouldThrowWhenNameIsOnlyWhitespace() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateName("   ", "First name"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateName_shouldThrowWhenNameTooShort() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateName("J", "First name"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be at least 2 characters long");
    }

    @Test
    void validateName_shouldAcceptNameWith2Characters() {
        // Given
        String name = "Jo";

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "First name");
    }

    @Test
    void validateName_shouldThrowWhenNameTooLong() {
        // Given
        String longName = "a".repeat(51);

        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateName(longName, "First name"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must not exceed 50 characters");
    }

    @Test
    void validateName_shouldAcceptNameWith50Characters() {
        // Given
        String name = "a".repeat(50);

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "First name");
    }

    @Test
    void validateName_shouldAcceptNameWithHyphen() {
        // Given
        String name = "Mary-Jane";

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "First name");
    }

    @Test
    void validateName_shouldAcceptNameWithApostrophe() {
        // Given
        String name = "O'Connor";

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "Last name");
    }

    @Test
    void validateName_shouldAcceptNameWithSpace() {
        // Given
        String name = "Van Der Berg";

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "Last name");
    }

    @ParameterizedTest
    @ValueSource(
            strings = { "John123", // Contains numbers
                    "John@Doe", // Contains special character
                    "John_Doe", // Contains underscore
                    "John.Doe", // Contains dot
                    "John!", // Contains exclamation
                    "123John", // Starts with number
                    "!John" // Starts with special character
            })
    void validateName_shouldThrowWhenNameContainsInvalidCharacters(String invalidName) {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateName(invalidName, "First name"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("can only contain letters, spaces, hyphens, and apostrophes");
    }

    @Test
    void validateName_shouldUseProvidedFieldNameInErrorMessage() {
        // When & Then
        assertThatThrownBy(() -> CredentialsUtil.validateName("", "Middle name"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateName_shouldAcceptMixedCase() {
        // Given
        String name = "McGregor";

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "Last name");
    }

    @Test
    void validateName_shouldAcceptComplexName() {
        // Given
        String name = "Mary-Anne O'Brien-Smith";

        // When & Then - should not throw
        CredentialsUtil.validateName(name, "First name");
    }

    @Test
    void validateName_shouldTrimWhitespaceBeforeValidation() {
        // Given
        String name = "  John  ";

        // When & Then - should not throw (trimmed length is valid)
        CredentialsUtil.validateName(name, "First name");
    }

}
