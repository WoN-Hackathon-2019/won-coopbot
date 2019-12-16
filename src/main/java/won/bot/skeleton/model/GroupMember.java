package won.bot.skeleton.model;

import java.io.Serializable;
import java.net.URI;

public class GroupMember implements Serializable {

    private String name;
    private URI connectionUri;

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
}
