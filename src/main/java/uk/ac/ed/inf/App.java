package uk.ac.ed.inf;

import java.util.*;
import java.util.List;
import java.time.LocalDate;
import java.time.DateTimeException;
import com.mapbox.geojson.*;
import com.google.gson.JsonObject;

/**
 * Main class of the application
 */
public class App {
    /**
     * Main function of the application. It checks if console inputs are correct, executes drone behaviour
     * and writes generated data to 3 different files.
     *
     * @param args date and REST server address arguments
     * @throws InvalidPizzaCombinationException Exception to be thrown if invalid order combination is detected
     */
    public static void main( String[] args ) throws InvalidPizzaCombinationException {
        String date = args[0];
        String restUrl = args[1];
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")){
            throw new DateTimeException("Incorrect format or no date");
        }
        LocalDate parsedDate = LocalDate.parse(date);
        if (parsedDate.isBefore(LocalDate.of(2023, 1, 1))
                || parsedDate.isAfter(LocalDate.of(2023, 5, 31))) {
            throw new DateTimeException("Date out of range");
        }
//
        Order[] orders = Order.getOrdersFromServer(restUrl, date);
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(restUrl);

        assert orders != null;
        HashMap<Restaurant, ArrayList<Order>> restaurantOrders = Drone.getValidOrdersByDistance(restaurants, orders);

        LngLat startCoordinates = new LngLat(-3.186874, 55.944494);
        NoFlyZones[] noFlyZones = CentralArea.getNoFlyZones(restUrl);

        HashMap<Restaurant, ArrayList<DronePath>> restaurantPaths = Drone.GreedyAlgorithm(restaurants,
                startCoordinates, noFlyZones);
        ArrayList<Map.Entry<Restaurant, Integer>> restaurantsByDistance = Restaurant.getRestaurantsByDistance
                (restaurantPaths, restaurants);

        ArrayList<Point> jsonPoints = new ArrayList<>();
        ArrayList<JsonObject> movesAsJsonObjects = new ArrayList<>();

        ArrayList<Order> completedOrders = new Drone().performDelivery(movesAsJsonObjects, jsonPoints, restaurantPaths,
                                                                 restaurantOrders, restaurantsByDistance);
        List<JsonObject> jsonOrders = Drone.generateOrderOutcomes(completedOrders, restaurants, orders);

        Drone.writeDroneFlightPathToFile(movesAsJsonObjects, date);
        Drone.writeDroneFlightPathToFileGeoJson(jsonPoints, date);
        Order.writeOrderOutcomes(jsonOrders, date);
    }
}
