package uk.ac.ed.inf;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.DisplayName;

public class CreditCardTest
{
    @BeforeEach
    void display(TestInfo info)
    {
        System.out.println(info.getDisplayName());
    }
    Order validOrder = new Order("1", "2023-02-01", "Bill", "2720992980684610",
            "01/32", "453", 2000, new String[0]);
    Order invalidOrder1 = new Order("2", "2023-02-01", "Bill", "0000000000000000",
            "01/32", "453", 2000, new String[0]);
    Order invalidOrder2 = new Order("3", "2023-02-01", "Bill", "2720992980684610",
            "01/32", "1337", 2000, new String[0]);
    Order invalidOrder3 = new Order("4", "2023-02-01", "Bill", "2720992980684610",
            "04/20", "453", 2000, new String[0]);

    @Test
    @DisplayName("Testing if card number is valid")
    void testCardNumberValidation()
    {
        assertFalse(invalidOrder1.isCreditCardNumberValid());
        assertTrue(validOrder.isCreditCardNumberValid());
    }
    @Test
    @DisplayName("Testing if card cvv is correct")
    void testCardCvvValidation()
    {
        assertFalse(invalidOrder2.isCVVNumberValid());
        assertTrue(validOrder.isCVVNumberValid());
    }
    @Test
    @DisplayName("Testing if card is expired")
    void testCardExpiryDateValidation()
    {
        assertFalse(invalidOrder3.isCreditCardExpired());
        assertTrue(validOrder.isCreditCardExpired());
    }
}
