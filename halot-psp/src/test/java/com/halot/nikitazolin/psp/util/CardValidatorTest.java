package com.halot.nikitazolin.psp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.YearMonth;

@DisplayName("CardValidator Unit Tests")
class CardValidatorTest {

  @Test
  void isValidCardNumber_Valid12DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "075086588854";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_Valid13DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "4222222222222";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_Valid14DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "44222222222224";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_Valid15DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "371234567892126";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_Valid16DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "4111111111111111";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_Valid17DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "37123456789212345";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_Valid18DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "741111111111111514";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_Valid19DigitsNumber_ReturnsTrue() {
    // Arrange
    String validCardNumber = "4241111111111111115";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number valid");
  }

  @Test
  void isValidCardNumber_ValidNumberWithSpacesAndHyphens_ReturnsTrue() {
    // Arrange
    String validCardNumber = "4111-1111 1111-1111  ";

    // Act
    boolean result = CardValidator.isValidCardNumber(validCardNumber);

    // Assert
    assertTrue(result, "The card number with spaces and hyphens must be valid");
  }

  @Test
  void isValidCardNumber_InvalidChecksum_ReturnsFalse() {
    // Arrange
    String invalidCardNumber = "4111111111111115";

    // Act
    boolean result = CardValidator.isValidCardNumber(invalidCardNumber);

    // Assert
    assertFalse(result, "Card number with an incorrect checksum must be not valid");
  }

  @Test
  void isValidCardNumber_NullInput_ReturnsFalse() {
    // Arrange
    String nullCardNumber = null;

    // Act
    boolean result = CardValidator.isValidCardNumber(nullCardNumber);

    // Assert
    assertFalse(result, "Null-value must return false");
  }

  @Test
  void isValidCardNumber_EmptyString_ReturnsFalse() {
    // Arrange
    String emptyCardNumber = "";

    // Act
    boolean result = CardValidator.isValidCardNumber(emptyCardNumber);

    // Assert
    assertFalse(result, "Empty string should return false");
  }

  @Test
  void isValidCardNumber_BlankString_ReturnsFalse() {
    // Arrange
    String blankCardNumber = "   ";

    // Act
    boolean result = CardValidator.isValidCardNumber(blankCardNumber);

    // Assert
    assertFalse(result, "String consisting only of spaces should return false");
  }

  @Test
  void isValidCardNumber_TooShortNumber_ReturnsFalse() {
    // Arrange
    String shortCardNumber = "12345678901";

    // Act
    boolean result = CardValidator.isValidCardNumber(shortCardNumber);

    // Assert
    assertFalse(result, "Too short card number should return false");
  }

  @Test
  void isValidCardNumber_TooLongNumber_ReturnsFalse() {
    // Arrange
    String longCardNumber = "12345678901234567890";

    // Act
    boolean result = CardValidator.isValidCardNumber(longCardNumber);

    // Assert
    assertFalse(result, "Too long card number should return false");
  }

  @Test
  void isValidCardNumber_NumberWithNonNumericCharactersButValidLuhn_ReturnsTrue() {
    // Arrange
    String mixedCardNumber = "4111fa111$11111!1111  ";

    // Act
    boolean result = CardValidator.isValidCardNumber(mixedCardNumber);

    // Assert
    assertTrue(result, "Card number with extra characters but a valid core must be valid");
  }
  
  @Test
  void isCardExpired_ExpiryDateInFuture_ReturnsFalse() {
    // Arrange
    YearMonth expiryDate = YearMonth.now().plusYears(1);

    // Act
    boolean isExpired = CardValidator.isCardExpired(expiryDate);

    // Assert
    assertFalse(isExpired, "A card with an expiration date next year should not be considered expired");
  }

  @Test
  void isCardExpired_ExpiryDateInPast_ReturnsTrue() {
    // Arrange
    YearMonth expiryDate = YearMonth.now().minusYears(1);

    // Act
    boolean isExpired = CardValidator.isCardExpired(expiryDate);

    // Assert
    assertTrue(isExpired, "A card with an expiration date last year should be considered expired");
  }

  @Test
  void isCardExpired_ExpiryDateIsCurrentMonth_ReturnsFalse() {
    // Arrange
    YearMonth expiryDate = YearMonth.now();

    // Act
    boolean isExpired = CardValidator.isCardExpired(expiryDate);

    // Assert
    assertFalse(isExpired, "The card with an expiration date in the current month must be valid");
  }
}