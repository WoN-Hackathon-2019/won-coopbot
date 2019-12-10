package won.bot.skeleton.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.bot.skeleton.model.GroupMember;

import java.net.URI;

public class CreateGroupChatEvent extends BaseEvent {

    private String name;
    private int max;
    private String category;
    private GroupMember admin;
    private URI adminSocketUri;

    public CreateGroupChatEvent(String category, GroupMember admin, URI adminSocketUri, int max) {
        this.max = max;
        this.category = category;
        this.name = category + " GropuChannel";
        this.admin = admin;
        this.adminSocketUri = adminSocketUri;
    }

    public String getName() {
        return name;
    }

    public int getMax() {
        return max;
    }

    public String getCategory() {
        return category;
    }

    public GroupMember getAdmin() {
        return admin;
    }

    public URI getAdminSocketUri() {
        return adminSocketUri;
    }
}
