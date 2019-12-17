package won.bot.skeleton.cli;

import at.apf.easycli.annotation.Command;
import at.apf.easycli.annotation.DefaultValue;
import at.apf.easycli.annotation.Meta;
import at.apf.easycli.annotation.Usage;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.bot.skeleton.event.CreateLocationApiAtomEvent;
import won.bot.skeleton.service.AtomLocationService;
import won.protocol.model.Coordinate;

public class ReceiverCliExecuter {

    private EventListenerContext ctx;
    private EventBus bus;
    private AtomLocationService als;

    public ReceiverCliExecuter(EventListenerContext ctx, EventBus bus) {
        this.ctx = ctx;
        this.bus = bus;
        als = new AtomLocationService(ctx);
    }

    @Command("/new")
    @Usage("name \\[capacity\\]")
    public void createNewGroup(String name, @DefaultValue("100") int capacity, @Meta MessageFromOtherAtomEvent event) {
        bus.publish(new CreateGroupChatEvent(name, event.getTargetSocketURI(), capacity));
    }

    @Command("/list")
    public void listAllGroups(@Meta MessageFromOtherAtomEvent event) {
        StringBuilder sb = new StringBuilder();
        ((SkeletonBotContextWrapper) this.ctx.getBotContextWrapper()).getAllGroups().stream()
                .forEach(g -> sb.append(g.getName() + "\n"));
        bus.publish(new ConnectionMessageCommandEvent(event.getCon(), sb.toString()));
    }


    @Command("/myloc")
    public void getLocationOfAtom(@Meta MessageFromOtherAtomEvent event) {
        Coordinate location = als.getAtomLocation(event.getTargetAtomURI());
        if (location != null) {
            bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "Lon: " + location.getLongitude() + " / Lan: " + location.getLatitude()));
        } else {
            bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "You don't have a location defined"));
        }
    }




}
