package uk.ac.ed.inf;

import java.time.Clock;
import java.time.Duration;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.FeatureCollection;
import com.google.gson.JsonObject;

/**
 * A class that controls drone behaviour.
 */
public class Drone {
    private final long startTime;
    private final Clock clock;
    private int batteryCharge;

    /**
     * Constructor of the drone class which initialises variables for battery charge and measuring time.
     */
    public Drone() {
        this.clock = Clock.tick(Clock.systemDefaultZone(), Duration.ofMillis(1));
        this.startTime = clock.millis();
        this.batteryCharge = 2000;
    }


    /**
     * Enum that holds compass directions and their angles in degrees
     */
    public enum Compass {
        E(0),
        ENE(22.5),
        NE(45),
        NNE(67.5),
        N(90),
        NNW(112.5),
        NW(135),
        WNW(157.5),
        W(180),
        WSW(202.5),
        SW(225),
        SSW(247.5),
        S(270),
        SSE(292.5),
        SE(315),
        ESE(337.5);
        public final double angle;

        Compass(double angle) {
            this.angle = angle;
        }
    }

    /**
     * Performs delivery of orders. Orders are delivered based on how close they are to the starting point: the
     * closest ones get delivered first. Method also takes into account battery charge, which is 2000 at the start.
     *
     * @param movesAsJsonObjects          array of drone moves as json objects
     * @param jsonPoints          array of points depicting drone position
     * @param restaurantPaths        paths to and from each restaurant
     * @param orders       Hashmap of restaurants which can fulfill certain orders
     * @param restaurantsByDistance sorted array of restaurants, where the first entry is closest to starting point
     * @return list of completed orders
     */
    public ArrayList<Order> performDelivery(ArrayList<JsonObject> movesAsJsonObjects, ArrayList<Point> jsonPoints,
    HashMap<Restaurant, ArrayList<DronePath>> restaurantPaths, HashMap<Restaurant, ArrayList<Order>> orders,
    ArrayList<Map.Entry<Restaurant, Integer>> restaurantsByDistance) {
        ArrayList<Order> completedOrders = new ArrayList<>();
        Integer previousTick = null;
        for (Map.Entry<Restaurant, Integer> restaurant : restaurantsByDistance) {
            ArrayList<Order> currentOrders = orders.get(restaurant.getKey());
            ArrayList<DronePath> currentPath = restaurantPaths.get(restaurant.getKey());
            while (currentOrders.size() > 0 && this.batteryCharge >= restaurant.getValue()) {
                Order order = currentOrders.get(0);
                currentOrders.remove(0);
                for (DronePath droneMovement : currentPath) {
                    DronePath.orderNumber = order.orderNo;
                    DronePath.ticksSinceStartOfCalculation = this.computeTicks(previousTick);
                    previousTick = this.computeTicks(previousTick);
                    movesAsJsonObjects.add(droneMovement.convertToJson());
                    jsonPoints.add(Point.fromLngLat(droneMovement.startLongitude,
                            droneMovement.startLatitude));
                }
                this.batteryCharge = this.batteryCharge - currentPath.size();
                if (this.batteryCharge < restaurant.getValue()) {
                    int pathValue = currentPath.size() - 1;
                    jsonPoints.add(Point.fromLngLat(currentPath.get(pathValue).startLongitude,
                            currentPath.get(pathValue).startLatitude));
                }
                completedOrders.add(order);
            }
        }
        return completedOrders;
    }

    /**
     * Method for keeping track of time
     *
     * @return current or previous tick
     */
    private int computeTicks(Integer previousTick) {
        int currentTick = (int) (this.clock.millis() - this.startTime);
        if (previousTick != null) {
            return Math.max(currentTick, previousTick+1);
        } else {
            return currentTick;
        }
    }

    /**
     * Computes outcome of orders and stores them in a JsonObject list
     *
     * @param deliveredOrders array of delivered orders
     * @param restaurants     array of restaurants
     * @param orders          array containing the orders
     * @return JsonObject list of order outcomes
     */
    public static List<JsonObject> generateOrderOutcomes(ArrayList<Order> deliveredOrders,
                                                         Restaurant[] restaurants, Order[] orders) {
        List<JsonObject> jsonOrders = new ArrayList<>();
        for (Order order : orders) {
            if (!deliveredOrders.contains(order)) {
                jsonOrders.add(Order.getOrderJson(0, order.orderNo, order.isOrderValid(restaurants)));
            } else {
                jsonOrders.add(Order.getOrderJson(order.priceTotalInPence, order.orderNo, OrderOutcome.Delivered));
            }
        }
        return jsonOrders;
    }

    /**
     * Produces hashmap of valid orders and a restaurant that can fulfill the order
     *
     * @param restaurants array of restaurants
     * @param orders order array
     * @return Hashmap of a valid order and a restaurant
     */
    public static HashMap<Restaurant, ArrayList<Order>> getValidOrdersByDistance(Restaurant[] restaurants,
                                                                                 Order[] orders) throws InvalidPizzaCombinationException {
        HashMap<Restaurant, ArrayList<Order>> sortedOrders = new HashMap<>();
        for (Order order : orders) {
            if (order.isOrderValid(restaurants) == OrderOutcome.ValidButNotDelivered) {
                try {
                    Restaurant deliveryRestaurant = order.findRestaurant(restaurants, order.orderItems);
                    sortedOrders.computeIfAbsent(deliveryRestaurant, k -> new ArrayList<>()).add(order);
                } catch (InvalidPizzaCombinationException e) {
                    throw new InvalidPizzaCombinationException("Invalid pizza combination");
                }
            }
        }
        return sortedOrders;
    }

    /**
     * Method for computing flight paths to restaurants from starting position based on a greedy pathfinding approach.
     * The drone flies in the direction that gets it closest to a restaurant, while avoiding ending up in a no-fly
     * zone or crossing one.
     *
     * @param restaurants     array of restaurants
     * @param beginningPosition The drone's starting position
     * @param noFlyZones      array of no-fly zones
     * @return HashMap of restaurant and the computed greedy path to it
     */
    public static HashMap<Restaurant, ArrayList<DronePath>> GreedyAlgorithm(Restaurant[] restaurants,
                                                            LngLat beginningPosition, NoFlyZones[] noFlyZones) {
        HashMap<Restaurant, ArrayList<DronePath>> restaurantPaths = new HashMap<>();
        Compass[] directions = Compass.values();
        for (Restaurant restaurant : restaurants) {
            LngLat position = beginningPosition;
            LngLat lastPosition;
            ArrayList<DronePath> pathsToRestaurant = new ArrayList<>();
            ArrayList<DronePath> pathReversed = new ArrayList<>();
            while (!position.closeTo(new LngLat(restaurant.longitude, restaurant.latitude))) {
                double minDistanceToRestaurant = 0.0;
                Compass bestDirection = null;
                for (Compass direction : directions) {
                    var nextPosition = position.nextPosition(direction.angle);
                    double nextDistanceToRestaurant = nextPosition.distanceTo(new LngLat(restaurant.longitude, restaurant.latitude));
                    if ((minDistanceToRestaurant == 0.0 || nextDistanceToRestaurant < minDistanceToRestaurant)
                            && !nextPosition.isDroneInNoFlyZone(noFlyZones)
                            && !position.isDronePassingNoFlyZone(noFlyZones, nextPosition)
                            ) {
                        minDistanceToRestaurant = nextDistanceToRestaurant;
                        bestDirection = direction;
                    }
                }
                assert bestDirection != null;
                DronePath path = new DronePath(null, bestDirection.angle,
                        position, position.nextPosition(bestDirection.angle), null);
                position = position.nextPosition(bestDirection.angle);
                pathsToRestaurant.add(path);
            }
            lastPosition = new LngLat(pathsToRestaurant.get(pathsToRestaurant.size() - 1).endLongitude,
                    pathsToRestaurant.get(pathsToRestaurant.size() - 1).endLatitude);
            beginningPosition = new LngLat(pathsToRestaurant.get(0).startLongitude,
                    pathsToRestaurant.get(0).startLatitude);
            ArrayList<DronePath> fullPath = new ArrayList<>(pathsToRestaurant);
            fullPath.add(new DronePath(null, null, lastPosition, lastPosition,
                    null));
            int i;
            for (i = pathsToRestaurant.size() - 1; i > -1; i--) {
                pathReversed.add(pathsToRestaurant.get(i).getReversedDronePath());
            }
            fullPath.addAll(pathReversed);
            fullPath.add(new DronePath(null, null,
                    beginningPosition, beginningPosition, null));
            restaurantPaths.put(restaurant, fullPath);
        }
        return restaurantPaths;
    }

    /**
     * Method for writing drone flight paths to a json file
     *
     * @param pathSegments array of drone moves
     * @param date order date
     */
    public static void writeDroneFlightPathToFile(ArrayList<JsonObject> pathSegments, String date) {
        try (PrintWriter out = new PrintWriter(new FileWriter("flightpath-" + date + ".json"))) {
            out.write(pathSegments.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for writing drone positions during flight to a geojson file "
     *
     * @param coordinates array of drone positions
     * @param date order date
     */
    public static void writeDroneFlightPathToFileGeoJson(ArrayList<Point> coordinates, String date) {
        try (PrintWriter out = new PrintWriter(new FileWriter("drone-" + date + ".geojson"))) {
            Feature feature = Feature.fromGeometry(LineString.fromLngLats(coordinates));
            out.write(FeatureCollection.fromFeature(feature).toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Order> appTest(String url, String date, Order[] orders) throws InvalidPizzaCombinationException {
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(url);
        assert orders != null;
        HashMap<Restaurant, ArrayList<Order>> restaurantOrders = Drone.getValidOrdersByDistance(restaurants, orders);
        LngLat startCoordinates = new LngLat(-3.186874, 55.944494);
        NoFlyZones[] noFlyZones = CentralArea.getNoFlyZones(url);
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
        return completedOrders;
    }
}
