package won.bot.skeleton.service;

import won.bot.skeleton.persistence.model.Location;
import won.bot.skeleton.persistence.model.SportPlace;

import java.util.List;

public class PlaceRankingService {

    /**
     * Method that tries to find the most suitable places for the persons to meet, based on their location.
     * The smaller the combined distance from a place to each person is, the higher it is ranked.
     *
     * @param personLocation The location the persons specified in their needs.
     * @param candidatePlaces All locations where the planned group activity can be executed
     * @param number Number of suggested places that should be returned.
     * @return List of most suitable places
     */
    public List<SportPlace> getSuggestedPlaces(List<Location> personLocation,
                                               List<SportPlace> candidatePlaces,
                                               int number) {

        return candidatePlaces;
    }


}
