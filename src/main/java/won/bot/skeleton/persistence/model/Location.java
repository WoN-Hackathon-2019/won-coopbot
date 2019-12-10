package won.bot.skeleton.persistence.model;

import com.peertopark.java.geocalc.DegreeCoordinate;
import com.peertopark.java.geocalc.Point;

import javax.persistence.Entity;

@Entity
public class Location {

    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Point toPoint() {
        return new Point(new DegreeCoordinate(latitude), new DegreeCoordinate(longitude));
    }
}
