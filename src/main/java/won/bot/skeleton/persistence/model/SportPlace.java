package won.bot.skeleton.persistence.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SportPlace implements Serializable {

    private Long id;
    private Location location;
    private String address;
    private boolean outdoor;
    private List<String> category;
    private String weblink;

    public SportPlace() {
        this.category = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isOutdoor() {
        return outdoor;
    }

    public void setOutdoor(boolean outdoor) {
        this.outdoor = outdoor;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        getCategory().forEach(cat -> {
            sb.append(cat);
            sb.append(", ");
        });

        String output = sb.toString().substring(0, sb.toString().length() - 2);
        return address + " (Category: " + output + ")";
    }
}
