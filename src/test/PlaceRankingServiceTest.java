import com.peertopark.java.geocalc.*;
import org.junit.Test;
import won.bot.skeleton.persistence.model.Location;

public class PlaceRankingServiceTest {

    @Test
    public void TestDistanceCalculation() {
        Location location1 = new Location(47.411788, 9.733054);
        Location location2 = new Location(47.411164, 9.734459);

        System.out.println(EarthCalc.getDistance(location1.toPoint(), location2.toPoint()));

    }

}
