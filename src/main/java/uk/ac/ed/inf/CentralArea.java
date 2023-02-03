package uk.ac.ed.inf;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.io.IOException;

/**
 * A class that handles no-fly zones
 */
public class CentralArea {

    /**
     * Gets an array of no-fly zones from the server
     *
     * @param serverAddress base URL address of server
     * @return no-fly zone array
     */
    public static NoFlyZones[] getNoFlyZones(String serverAddress) {
        try {
            if (!serverAddress.endsWith("/")) {
                serverAddress = serverAddress + "/";
            }
            String url = serverAddress + "noFlyZones";
            return new ObjectMapper().readValue(new URL(url), NoFlyZones[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
