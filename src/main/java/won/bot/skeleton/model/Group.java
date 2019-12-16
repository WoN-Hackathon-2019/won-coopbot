package won.bot.skeleton.model;

import java.io.Serializable;
import java.net.URI;

public class Group implements Serializable {

    private String name;
    private int capacity;
    private URI adminConnectionUri;


    public Group(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public URI getAdminConnectionUri() {
        return adminConnectionUri;
    }

    public void setAdminConnectionUri(URI adminConnectionUri) {
        this.adminConnectionUri = adminConnectionUri;
    }
}
