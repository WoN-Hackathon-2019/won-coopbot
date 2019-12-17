package won.bot.skeleton.model;

import won.protocol.model.Coordinate;

import java.io.Serializable;
import java.net.URI;

public class GroupMember implements Serializable {

    private String name;
    private URI connectionUri;
    private Coordinate location;

    public GroupMember(String name, URI connectionUri) {
        this.name = name;
        this.connectionUri = connectionUri;
    }

    public String getName() {
        return name;
    }

    public URI getConnectionUri() {
        return connectionUri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConnectionUri(URI connectionUri) {
        this.connectionUri = connectionUri;
    }

    public Coordinate getLocation() {
        return location;
    }

    public void setLocation(Coordinate location) {
        this.location = location;
    }
}
