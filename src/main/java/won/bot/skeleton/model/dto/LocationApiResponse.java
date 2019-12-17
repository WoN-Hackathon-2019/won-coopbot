package won.bot.skeleton.model.dto;

public class LocationApiResponse {

    private String name;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String formattedAddress;
    private String mapsLink;
    private double longitude;
    private double latitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public String getMapsLink() {
        return mapsLink;
    }

    public void setMapsLink(String mapsLink) {
        this.mapsLink = mapsLink;
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


    /**
     * {
     *     "name": (String) <name of the venue>,
     *     "address": (String) <street and house number>,
     *     "city": (String) <city>,
     *     "country": (String) <country>,
     *     "postalCode": (String) <postal code>,
     *     "formattedAddress": (String) <the most important address data formatted in a readable way>
     *     "mapsLink": (String) <the url of the place in google maps>
     *     "longitude": (Double) <longitude>
     *     "latitude": (Double) <latitude>
     *
     * }
     */
}
