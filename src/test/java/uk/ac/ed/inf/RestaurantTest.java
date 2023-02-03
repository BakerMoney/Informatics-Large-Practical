package uk.ac.ed.inf;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.DisplayName;

public class RestaurantTest
{
    @BeforeEach
    void display(TestInfo info)
    {
        System.out.println(info.getDisplayName());
    }
    @Test
    @DisplayName("Testing if all restaurants were retrieved from server")
    void testRestaurantNumber()
    {
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer("https://ilp-rest.azurewebsites.net");
        assertEquals(4, restaurants.length);
    }
    @Test
    @DisplayName("Testing the menu item array")
    void testMenu()
    {
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer("https://ilp-rest.azurewebsites.net");
        for(int i = 0; i < 4; i++){
            assertEquals(2, restaurants[i].getMenu().length);
        }
    }
}
