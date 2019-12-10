import com.peertopark.java.geocalc.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import won.bot.skeleton.persistence.model.Location;
import won.bot.skeleton.persistence.model.SportPlace;
import won.bot.skeleton.service.PlaceRankingService;

import java.util.ArrayList;
import java.util.List;

public class PlaceRankingServiceTest {

    private Location dornbirn;
    private Location hoerbranz;
    private Location wolfurt;
    private Location lustenau;
    private Location stadthalle;
    private Location rudolfscrime;

    private SportPlace birkenwiese;
    private SportPlace salzburg;
    private SportPlace admiraPlatz;
    private SportPlace casinoStadion;
    private SportPlace happel;
    private SportPlace west;

    private List<SportPlace> candidates;

    private PlaceRankingService placeRankingService;

    @Before
    public void setup() {
        dornbirn = new Location(47.414601, 9.729089);
        hoerbranz = new Location(47.552521, 9.751902);
        wolfurt = new Location(47.466603, 9.758670);
        lustenau = new Location(47.419463, 9.663820);
        stadthalle = new Location(48.202232, 16.332563);
        rudolfscrime = new Location(48.189397, 16.332365);

        birkenwiese = new SportPlace();
        birkenwiese.setLocation(new Location(47.416520, 9.723821));
        birkenwiese.setAddress("Birkawies");

        salzburg = new SportPlace();
        salzburg.setLocation(new Location(47.816583, 12.996729));
        salzburg.setAddress("Salzburg");

        admiraPlatz = new SportPlace();
        admiraPlatz.setLocation(new Location(47.434135, 9.728495));
        admiraPlatz.setAddress("Forach");

        casinoStadion = new SportPlace();
        casinoStadion.setLocation(new Location(47.503673, 9.733237));
        casinoStadion.setAddress("Breagaz");

        happel = new SportPlace();
        happel.setLocation(new Location(48.208209, 16.420974));
        happel.setAddress("Prater");

        west = new SportPlace();
        west.setLocation(new Location(48.197679, 16.265756));
        west.setAddress("Hütteldorf");

        candidates = new ArrayList<>();
        candidates.add(birkenwiese);
        candidates.add(admiraPlatz);
        candidates.add(casinoStadion);
        candidates.add(salzburg);
        candidates.add(happel);
        candidates.add(west);

        placeRankingService = new PlaceRankingService();
    }


    @Test
    public void testRankingService_shouldReturnPlacesInVorarlbergWithDornbirnFirst() {
        List<Location> personLocations = new ArrayList<>();
        personLocations.add(dornbirn);
        personLocations.add(wolfurt);
        personLocations.add(lustenau);

        List<SportPlace> rankedPlaces = placeRankingService.getSuggestedPlaces(personLocations, candidates, 3);
        Assert.assertEquals(rankedPlaces.size(), 3);
        Assert.assertEquals(birkenwiese.getAddress(), rankedPlaces.get(0).getAddress());
        Assert.assertEquals(admiraPlatz.getAddress(), rankedPlaces.get(1).getAddress());
        Assert.assertEquals(casinoStadion.getAddress(), rankedPlaces.get(2).getAddress());
    }

    @Test
    public void testRankingService_shouldReturnPlacesInVorarlbergWithBregenzFirst() {
        List<Location> personLocations = new ArrayList<>();
        personLocations.add(hoerbranz);
        personLocations.add(wolfurt);
        personLocations.add(lustenau);

        List<SportPlace> rankedPlaces = placeRankingService.getSuggestedPlaces(personLocations, candidates, 3);
        Assert.assertEquals(rankedPlaces.size(), 3);
        Assert.assertEquals(casinoStadion.getAddress(), rankedPlaces.get(0).getAddress());
        Assert.assertEquals(admiraPlatz.getAddress(), rankedPlaces.get(1).getAddress());
        Assert.assertEquals(birkenwiese.getAddress(), rankedPlaces.get(2).getAddress());
    }

    @Test
    public void testRankingService_shouldReturnPlacesSalzburgFirst() {
        List<Location> personLocations = new ArrayList<>();
        personLocations.add(dornbirn);
        personLocations.add(lustenau);
        personLocations.add(rudolfscrime);
        personLocations.add(stadthalle);

        List<SportPlace> rankedPlaces = placeRankingService.getSuggestedPlaces(personLocations, candidates, 3);
        Assert.assertEquals(3, rankedPlaces.size());
        Assert.assertEquals(salzburg.getAddress(), rankedPlaces.get(0).getAddress());
        Assert.assertEquals(west.getAddress(), rankedPlaces.get(1).getAddress());
        Assert.assertEquals(birkenwiese.getAddress(), rankedPlaces.get(2).getAddress());
    }

    @Test
    public void testRankingService_2ShouldOverrule1() {
        List<Location> personLocations = new ArrayList<>();
        personLocations.add(dornbirn);
        personLocations.add(rudolfscrime);
        personLocations.add(stadthalle);

        List<SportPlace> rankedPlaces = placeRankingService.getSuggestedPlaces(personLocations, candidates, 3);
        Assert.assertEquals(3, rankedPlaces.size());
        Assert.assertEquals(west.getAddress(), rankedPlaces.get(0).getAddress());
        Assert.assertEquals(happel.getAddress(), rankedPlaces.get(1).getAddress());
        Assert.assertEquals(salzburg.getAddress(), rankedPlaces.get(2).getAddress());
    }

    @Test
    public void testRankingService_Limit5_ShouldReturn5Places() {
        List<Location> personLocations = new ArrayList<>();
        personLocations.add(dornbirn);
        personLocations.add(rudolfscrime);
        personLocations.add(stadthalle);

        List<SportPlace> rankedPlaces = placeRankingService.getSuggestedPlaces(personLocations, candidates, 5);
        Assert.assertEquals(5, rankedPlaces.size());
    }

    @Test
    public void testRankingService_LimitGreaterNumberOfCandidates_ShouldReturnAllCandidates() {
        List<Location> personLocations = new ArrayList<>();
        personLocations.add(dornbirn);

        List<SportPlace> rankedPlaces = placeRankingService.getSuggestedPlaces(personLocations, candidates, 15);
        Assert.assertEquals(candidates.size(), rankedPlaces.size());

    }



}
