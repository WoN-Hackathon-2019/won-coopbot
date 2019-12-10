package won.bot.skeleton.service;

import won.bot.skeleton.persistence.model.SportPlace;

public class LocationDistanceTuple{

    private SportPlace sportPlace;
    private double combinedDistance;

    public LocationDistanceTuple(SportPlace sportPlace, double combinedDistance) {
        this.sportPlace = sportPlace;
        this.combinedDistance = combinedDistance;
    }

    public SportPlace getSportPlace() {
        return sportPlace;
    }

    public void setSportPlace(SportPlace sportPlace) {
        this.sportPlace = sportPlace;
    }

    public double getCombinedDistance() {
        return combinedDistance;
    }

    public void setCombinedDistance(double combinedDistance) {
        this.combinedDistance = combinedDistance;
    }
}
