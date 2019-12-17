package won.bot.skeleton.model;

import java.io.Serializable;
import java.net.URI;

public class Group implements Serializable {

    private String name;
    private int capacity;
    private URI adminConnectionUri;
    private URI groupAtomUri;


    public Group(String name, int capacity, URI groupAtomUri) {
        this.name = name;
        this.capacity = capacity;
        this.groupAtomUri = groupAtomUri;
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

    public URI getGroupAtomUri() {
        return groupAtomUri;
    }

    public void setGroupAtomUri(URI groupAtomUri) {
        this.groupAtomUri = groupAtomUri;
    }
}
