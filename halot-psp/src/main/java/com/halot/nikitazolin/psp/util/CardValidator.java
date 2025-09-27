package com.halot.nikitazolin.psp.util;

import java.time.YearMonth;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CardValidator {

  /**
   * Checks the validity of the card number using the Luhn's algorithm
   * 
   * @param cardNumber card number as a string (may contain spaces and hyphens)
   * @return true if the number is valid, false if invalid or empty
   */
  public boolean isValidCardNumber(String cardNumber) {
    String cleanNumber = makeClearCardNumber(cardNumber);

    // Checking whether the number was successfully cleared
    if (cleanNumber == null) {
      return false;
    }

    // Checking the validity of a number using the Luhn algorithm
    return checkByLuhnAlgorithm(cleanNumber);
  }

  /**
   * Checks if the card is valid on the current date 
   * @param expiryDate card expiration date (year and month) 
   * @return true if the card is expired, false if valid
   */
  public boolean isCardExpired(YearMonth expiryDate) {
    return expiryDate.isBefore(YearMonth.now());
  }

  /**
   * Cleans the card number by removing all non-numeric characters and checks its
   * basic validity (length and content).
   *
   * @param cardNumber The card number string (may contain spaces and hyphens).
   * @return The cleaned card number as a string, or null if the input is invalid.
   */
  private String makeClearCardNumber(String cardNumber) {
    // Checking for null and blank string
    if (cardNumber == null) {
      log.warn("Null was passed instead of the card number.");
      return null;
    }

    if (cardNumber.isBlank()) {
      log.warn("An empty string was passed instead of a card number.");
      return null;
    }

    // Remove all non-numeric characters (spaces, hyphens, etc.)
    String cleanedNumber = cardNumber.replaceAll("\\D", "");

    // Check if any digits remain after cleaning
    if (cleanedNumber.isEmpty()) {
      log.warn("No digits found in the card number after cleaning. Original input: '{}'", cardNumber);
      return null;
    }

    // Check that the length is within acceptable range
    if (cleanedNumber.length() < 12 || cleanedNumber.length() > 19) {
      log.warn("The card number length is not valid. Number of digits after cleaning: {}", cleanedNumber.length());
      return null;
    }

    return cleanedNumber;
  }

  /**
   * Checks the card number using the Luhn's algorithm.
   *
   * @param cardNumber The clean, numeric-only card number string.
   * @return true if the number is valid according to the Luhn's algorithm, false
   *         otherwise.
   */
  private boolean checkByLuhnAlgorithm(String cardNumber) {
    int sum = 0;
    boolean shouldDouble = false;

    // Compute the numbers from right to left
    for (int i = cardNumber.length() - 1; i >= 0; i--) {
      int digit = Character.getNumericValue(cardNumber.charAt(i));
      int currentVal = digit;

      if (shouldDouble) {
        currentVal = digit * 2;

        if (currentVal > 9) {
          currentVal = (currentVal / 10) + (currentVal % 10);
        }
      }

      sum += currentVal;
      shouldDouble = !shouldDouble;
    }

    log.debug("The result of the algorithm calculation Luhn's: sum = {}, remainder after division by 10 = {}", sum,
        sum % 10);

    boolean isValid = sum % 10 == 0;

    if (isValid) {
      log.info("The card number has been validated by the Luhn's algorithm");
    } else {
      log.warn("The card number did not pass the Luhn's algorithm validation");
    }

    return isValid;
  }
}
