package uk.ac.ed.inf;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.DisplayName;

public class ServerTest
{
    @BeforeEach
    void display(TestInfo info)
    {
        System.out.println(info.getDisplayName());
    }
    @Test
    @DisplayName("Testing order retrieval")
    void testOrderRetrieval()
    {
        Order[] orders = Order.getOrdersFromServer("https://ilp-rest.azurewebsites.net", null);
        assertEquals(7050, orders.length);
    }
    @Test
    @DisplayName("Testing order retrieval for a given day")
    void testOrderRetrievalCertainDay()
    {
        Order[] orders = Order.getOrdersFromServer("https://ilp-rest.azurewebsites.net", "2023-04-20");
        assertEquals(47, orders.length);
    }
    @Test
    @DisplayName("Testing restaurant retrieval")
    void testRestaurantRetrieval()
    {
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer("https://ilp-rest.azurewebsites.net");
        assertEquals(4, restaurants.length);
    }
}
