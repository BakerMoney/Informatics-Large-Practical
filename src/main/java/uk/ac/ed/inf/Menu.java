package uk.ac.ed.inf;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data class which receives menu JSON data
 */
public class Menu {
    @JsonProperty("name")
    public String name;
    @JsonProperty("priceInPence")
    public int priceInPence;
}
