package uk.ac.ed.inf;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Class representing no-fly zones
 */
public class NoFlyZones {
    @JsonProperty("name")
    public String name;
    @JsonProperty("coordinates")
    public List<List<Double>> coordinates;
}
