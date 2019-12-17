package won.bot.skeleton.event;

import won.bot.framework.eventbot.event.BaseEvent;

import java.net.URI;

public class CreateLocationApiAtomEvent extends BaseEvent {

    private URI groupAtomUri;


    public CreateLocationApiAtomEvent(URI groupAtomUri) {
        this.groupAtomUri = groupAtomUri;
    }

    public URI getGroupAtomUri() {
        return groupAtomUri;
    }
}
