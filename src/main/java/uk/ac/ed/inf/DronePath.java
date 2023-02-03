package uk.ac.ed.inf;

import com.google.gson.JsonObject;

/**
 * A class that contains methods for drone pathing
 */
public class DronePath {
    public static String orderNumber;
    private final Double angle;
    public static Integer ticksSinceStartOfCalculation;
    public final double startLongitude;
    public final double startLatitude;
    public final double endLongitude;
    public final double endLatitude;


    /**
     * Constructs a drone path segment
     *
     * @param orderNumber Order number
     * @param angle angle that the drone will fly at
     * @param startPosition starting position of path segment
     * @param endPosition ending position of path segment
     * @param ticksSinceStartOfCalculation Integer that keeps track of the number of ticks that have
     *                                     passed since start of calculation
     */
    public DronePath(String orderNumber, Double angle, LngLat startPosition, LngLat endPosition,
                     Integer ticksSinceStartOfCalculation) {
        DronePath.orderNumber = orderNumber;
        DronePath.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
        this.angle = angle;
        this.startLongitude = startPosition.lng();
        this.startLatitude = startPosition.lat();
        this.endLongitude = endPosition.lng();
        this.endLatitude = endPosition.lat();
    }

    /**
     * Creates a reversed drone path for coming back from the restaurant
     *
     * @return reversed drone path
     */
    public DronePath getReversedDronePath() {
        return new DronePath(orderNumber, (180.0 + this.angle) % 360.0,
                new LngLat(this.endLongitude, this.endLatitude), new LngLat(this.startLongitude, this.startLatitude),
                ticksSinceStartOfCalculation);
    }

    /**
     * Method for creating a JsonObject for an output file
     *
     * @return Drone path JsonObject
     */
    public JsonObject convertToJson() {
        JsonObject json = new JsonObject();
        json.addProperty("orderNo", orderNumber);
        json.addProperty("fromLongitude", this.startLongitude);
        json.addProperty("fromLatitude", this.startLatitude);
        json.addProperty("angle", this.angle);
        json.addProperty("toLongitude", this.endLongitude);
        json.addProperty("toLatitude", this.endLatitude);
        json.addProperty("ticksSinceStartOfCalculation", ticksSinceStartOfCalculation);
        return json;
    }
}
