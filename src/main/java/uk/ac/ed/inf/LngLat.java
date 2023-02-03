package uk.ac.ed.inf;
import java.util.Objects;
import java.awt.geom.Line2D;
import java.util.List;

/**
 * A record for representing a point
 * @param lng longitude value of point
 * @param lat latitude value of point
 */
public record LngLat(double lng, double lat){

    /**
     * A method for calculating distance between two LngLat objects
     * @param lnglat object that we are measuring the distance to
     * @return distance between the two LngLat objects
     */
    public double distanceTo(LngLat lnglat){
        return Math.sqrt(((lnglat.lat-lat)*(lnglat.lat-lat))+((lnglat.lng-lng)*(lnglat.lng-lng)));
    }

    /**
     * A method for checking if two LngLat objects are close to each other
     * @param lnglat LngLat object to check the distance to
     * @return boolean value that shows if the LngLat objects are close
     */
    public boolean closeTo(LngLat lnglat){
        return distanceTo(lnglat) < 0.00015;
    }

    /** A method that checks if the drone is in a no-fly zone
     * @param noFlyZones array of no-fly zones
     * @return boolean indicating if drone is in no-fly zone or not
     */
    public boolean isDroneInNoFlyZone(NoFlyZones[] noFlyZones) {
        for (NoFlyZones zone: noFlyZones) {
            int i, j, isInZone = 0;
            List<List<Double>> coords = zone.coordinates;
            for (i = 0, j = coords.size() - 1; i < coords.size(); j = i++) {
                if ((coords.get(i).get(1) > this.lat) != (coords.get(j).get(1) > this.lat) &&
                        (this.lng < (coords.get(j).get(0) - coords.get(i).get(0)) * (this.lat - coords.get(i).get(1))
                                / (coords.get(j).get(1) - coords.get(i).get(1)) + coords.get(i).get(0))) {
                    if (isInZone == 0){
                        isInZone = 1;
                    }
                    else{
                        isInZone = 0;
                    }
                }
            }
            if (isInZone == 1) {
                return true;
            }
        }
        return false;
    }

    /** A method that checks if the drone is crossing a no-fly zone
     * @param noFlyZones array of no-fly zones
     * @param nextPosition point representing where the drone will fly next
     * @return boolean indicating if drone is crossing a no-fly zone or not
     */
    public boolean isDronePassingNoFlyZone(NoFlyZones[] noFlyZones, LngLat nextPosition) {
        for (NoFlyZones zone: noFlyZones) {
            List<List<Double>> coords = zone.coordinates;
            Line2D dronePath = new Line2D.Double(this.lng, this.lat, nextPosition.lng, nextPosition.lat);
            boolean isIntersecting;
            for (int i = 0; i < coords.size(); i++) {
                isIntersecting = dronePath.intersectsLine(coords.get(i).get(0), coords.get(i).get(1),
                        coords.get((i + 1)%coords.size()).get(0), coords.get((i + 1)%coords.size()).get(1));
                if (isIntersecting) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A method for calculating the next position of a LngLat object given a direction
     * @param degree direction parameter defined in degrees
     * @return LngLat object with new location
     */
    public LngLat nextPosition(Double degree){
        if (Objects.equals(degree,null)) {
            return new LngLat(lng,lat);
        }
        double length = 0.00015;
        double d1 = Math.cos(degree)*length;
        double d2 = Math.sin(degree)*length;
        return new LngLat(lng+d1, lat+d2);
    }

}


