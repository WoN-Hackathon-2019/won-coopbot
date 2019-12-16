package won.bot.skeleton.cli;

import at.apf.easycli.annotation.Command;
import at.apf.easycli.annotation.DefaultValue;
import at.apf.easycli.annotation.Meta;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.bot.skeleton.impl.SkeletonBot;
import won.bot.skeleton.persistence.model.SportPlace;

import java.util.Set;

public class ReceiverCliExecuter {

    private EventListenerContext ctx;
    private EventBus bus;

    public ReceiverCliExecuter(EventListenerContext ctx, EventBus bus) {
        this.ctx = ctx;
        this.bus = bus;
    }

    @Command("/new")
    public void createNewGroup(String name, @DefaultValue("100") int capacity, @Meta MessageFromOtherAtomEvent event) {
        bus.publish(new CreateGroupChatEvent(name, event.getTargetSocketURI(), capacity));
    }

    @Command("/plays")
    public void loadSportplaces() {
        System.out.println( ((SkeletonBotContextWrapper) this.ctx.getBotContextWrapper()).loadSportplaces());
    }

}
