package uk.ac.ed.inf;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.util.*;
import java.io.IOException;

/**
 * A class which gets restaurants from the REST server and manages them
 */
public class Restaurant {
    @JsonProperty("name")
    public String name;
    @JsonProperty("longitude")
    public double longitude;
    @JsonProperty("latitude")
    public double latitude;
    @JsonProperty("menu")
    public List<Menu> menu;

    /**
     * A method that gets an array of menus of the restaurants
     * @return array of menus
     */
    public Menu[] getMenu() {
        int size = this.menu.size();
        Menu[] menu = new Menu[size];
        for (int i = 0; i < size; i++) {
            menu[i] = this.menu.get(i);
        }
        return menu;
    }

    /**
     * A method that gets an array of participating restaurants from the REST server
     * @param serverBaseAddress base address of the REST server
     * @return array of restaurants
     */
    static Restaurant[] getRestaurantsFromRestServer(String serverBaseAddress) {
        try {
            String url;
            if (!serverBaseAddress.endsWith("/")) {
                url = serverBaseAddress + "/restaurants";
            }
            else {
                url = serverBaseAddress + "restaurants";
            }
            return new ObjectMapper().readValue(new URL(url), Restaurant[].class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A method that puts restaurants in a list in order of how close they are to the drone's starting point,
     * the first entry being the closest. Precomputed flight paths are used to determine the distance.
     *
     * @param restaurants     array of restaurants
     * @param restaurantPaths HashMap containing restaurants ant the path taken to them by the drone
     * @return list of restaurants and steps it takes to get to them,
     * sorted by increasing distance from starting point
     */
    public static ArrayList<Map.Entry<Restaurant, Integer>> getRestaurantsByDistance(
            HashMap<Restaurant, ArrayList<DronePath>> restaurantPaths, Restaurant[] restaurants) {
        ArrayList<Map.Entry<Restaurant, Integer>> restaurantsByDistance = new ArrayList<>();
        ArrayList<Restaurant> restaurantsAlreadyProcessed = new ArrayList<>();
        int minSteps;
        Restaurant closestRestaurant;
        for (int i = 0; i < restaurants.length; i++) {
            minSteps = 0;
            closestRestaurant = null;
            for (Map.Entry<Restaurant, ArrayList<DronePath>> path : restaurantPaths.entrySet()) {
                Restaurant currentRestaurant = path.getKey();
                if (restaurantsAlreadyProcessed.contains(currentRestaurant)) {
                    continue;
                }
                if (minSteps == 0 || path.getValue().size() < minSteps) {
                    minSteps = path.getValue().size();
                    closestRestaurant = path.getKey();
                }
            }
            restaurantsAlreadyProcessed.add(closestRestaurant);
            restaurantsByDistance.add(new AbstractMap.SimpleEntry<>(closestRestaurant, minSteps));
        }
        return restaurantsByDistance;
    }
}
