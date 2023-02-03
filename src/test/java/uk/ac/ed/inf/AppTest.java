package uk.ac.ed.inf;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.DisplayName;

public class AppTest
{
    @BeforeEach
    void display(TestInfo info)
    {
        System.out.println(info.getDisplayName());
    }
    @Test
    @DisplayName("Testing general delivery performance (part 1)")
    void testSystem1() throws InvalidPizzaCombinationException {
        testSystemMethod2("2023-01-01");
        testSystemMethod2("2023-01-02");
        testSystemMethod2("2023-01-03");
        testSystemMethod2("2023-01-04");
        testSystemMethod2("2023-01-05");
    }
    @Test
    @DisplayName("Testing general delivery performance (part 2)")
    void testSystem2() throws InvalidPizzaCombinationException {
        testSystemMethod2("2023-01-06");
        testSystemMethod2("2023-01-07");
        testSystemMethod2("2023-01-08");
        testSystemMethod2("2023-01-09");
        testSystemMethod2("2023-01-10");
    }
    @Test
    @DisplayName("Testing if the correct amount of deliveries is done(part 1)")
    void testSystem3() throws InvalidPizzaCombinationException {
        testSystemMethod("2023-01-01", 29);
        testSystemMethod("2023-01-02", 29);
        testSystemMethod("2023-01-03", 29);
        testSystemMethod("2023-01-04", 29);
        testSystemMethod("2023-01-05", 29);
    }
    @Test
    @DisplayName("Testing if the correct amount of deliveries is done(part 2)")
    void testSystem4() throws InvalidPizzaCombinationException {
        testSystemMethod("2023-01-06", 29);
        testSystemMethod("2023-01-07", 29);
        testSystemMethod("2023-01-08", 29);
        testSystemMethod("2023-01-09", 30);
        testSystemMethod("2023-01-10", 29);
    }
    void testSystemMethod(String date, int expectedNumberOfDeliveries) throws InvalidPizzaCombinationException {
        Order[] orders = Order.getOrdersFromServer("https://ilp-rest.azurewebsites.net", date);
        ArrayList<Order> deliveredOrders = new Drone().appTest("https://ilp-rest.azurewebsites.net", date, orders);
        String path = System.getProperty("user.dir") + "/resultfiles";
        assertTrue(new File(path + "/" + "deliveries-" + date + ".json").exists());
        assertTrue(new File(path + "/" + "drone-" + date + ".geojson").exists());
        assertTrue(new File(path + "/" + "flightpath-" + date + ".json").exists());
        assertEquals(expectedNumberOfDeliveries, deliveredOrders.size());
    }
    void testSystemMethod2(String date) throws InvalidPizzaCombinationException {
        Order[] orders = Order.getOrdersFromServer("https://ilp-rest.azurewebsites.net", date);
        ArrayList<Order> deliveredOrders = new Drone().appTest("https://ilp-rest.azurewebsites.net", date, orders);
        String path = System.getProperty("user.dir") + "/resultfiles";
        assertTrue(new File(path + "/" + "deliveries-" + date + ".json").exists());
        assertTrue(new File(path + "/" + "drone-" + date + ".geojson").exists());
        assertTrue(new File(path + "/" + "flightpath-" + date + ".json").exists());
        assertTrue(deliveredOrders.size() > 25);
    }
}
