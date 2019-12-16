package won.bot.skeleton.service;

import at.apf.easycli.util.Tuple;
import com.peertopark.java.geocalc.EarthCalc;
import org.springframework.stereotype.Service;
import won.bot.skeleton.persistence.model.Location;
import won.bot.skeleton.persistence.model.SportPlace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceRankingService {

    /**
     * Method that tries to find the most suitable places for the persons to meet, based on their location.
     * The smaller the combined distance from a place to each person is, the higher it is ranked.
     *
     * @param personLocations The location the persons specified in their needs.
     * @param candidatePlaces All locations where the planned group activity can be executed
     * @param number Number of suggested places that should be returned.
     * @return List of most suitable places
     */
    public List<SportPlace> getSuggestedPlaces(List<Location> personLocations,
                                               List<SportPlace> candidatePlaces,
                                               int number) {

        List<Tuple<SportPlace, Double>> distanceTuples = new ArrayList<>();

        /* calculate combined distance from every person to the candidate location for every place */
        for (SportPlace place: candidatePlaces) {
            double distance = 0;
            for (Location personLocation: personLocations) {
                distance += EarthCalc.getDistance(personLocation.toPoint(), place.getLocation().toPoint());
            }
            distanceTuples.add(new Tuple<>(place, distance));
        }

        return distanceTuples.stream()
                .sorted(Comparator.comparingDouble(Tuple::getValue))
                .map(Tuple::getKey)
                .limit(number)
                .collect(Collectors.toList());
    }


}
