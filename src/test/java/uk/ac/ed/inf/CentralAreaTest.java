package uk.ac.ed.inf;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.DisplayName;

public class CentralAreaTest
{
    @BeforeEach
    void display(TestInfo info)
    {
        System.out.println(info.getDisplayName());
    }
    @Test
    @DisplayName("Testing if correct number of fly zones is present")
    void testCentralArea()
    {
        NoFlyZones[] zones = new CentralArea().getNoFlyZones("https://ilp-rest.azurewebsites.net");
        assertEquals(4, zones.length);
    }

}
