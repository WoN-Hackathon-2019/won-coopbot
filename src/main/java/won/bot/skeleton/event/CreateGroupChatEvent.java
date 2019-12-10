package won.bot.skeleton.event;

import won.bot.framework.eventbot.event.BaseEvent;

import java.net.URI;

public class CreateGroupChatEvent extends BaseEvent {

    private String name;
    private int max;
    private String category;
    private URI adminAtomUrl;

    public CreateGroupChatEvent(String name) {
        this.name = name;
    }

    public CreateGroupChatEvent(String category, URI adminAtomUrl) {
        this.category = category;
        this.name = category + " GropuChannel";
        this.adminAtomUrl = adminAtomUrl;
    }

    public CreateGroupChatEvent(String category, URI adminAtomUrl, int max) {
        this.max = max;
        this.category = category;
        this.name = category + " GropuChannel";
        this.adminAtomUrl = adminAtomUrl;
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

    public URI getAdminAtomUrl() {
        return adminAtomUrl;
    }
}
