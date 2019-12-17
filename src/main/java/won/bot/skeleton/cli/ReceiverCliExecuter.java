package won.bot.skeleton.cli;

import at.apf.easycli.annotation.Command;
import at.apf.easycli.annotation.DefaultValue;
import at.apf.easycli.annotation.Meta;
import at.apf.easycli.annotation.Usage;
import org.apache.jena.query.Dataset;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.bot.skeleton.model.Group;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import won.bot.skeleton.service.AtomLocationService;
import won.protocol.model.Coordinate;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.linkeddata.LinkedDataSource;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.protocol.vocabulary.WXCHAT;

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
    @Usage("groupName \\[capacity\\]")
    public void createNewGroup(String name, @DefaultValue("100") int capacity, @Meta MessageFromOtherAtomEvent event) {
        bus.publish(new CreateGroupChatEvent(name, event.getTargetSocketURI(), capacity));
    }

    @Command("/plays")
    public void loadSportplaces() {
        System.out.println( ((SkeletonBotContextWrapper) this.ctx.getBotContextWrapper()).loadSportplaces());
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


    @Command("/joinGroup")
    @Usage("groupname")
    public void joinGroup(String groupName, @Meta MessageFromOtherAtomEvent event) {
        SkeletonBotContextWrapper wrapper = (SkeletonBotContextWrapper) ctx.getBotContextWrapper();

        List<Group> groups = wrapper.getAllGroups();
        Optional<Group> group = groups.stream()
                .filter(g -> g.getName().equals(groupName +" GroupChannel"))
                .findFirst();

        if (!group.isPresent()) {
            bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "Group " + groupName + " not found!"));
            return;
        }

        if (wrapper.getGroupMembers(group.get().getGroupAtomUri()).size() >= group.get().getCapacity()) {
            bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "Unfortunately the group is already full"));
            return;
        }

        /* get chat atom socket uri */
        Dataset atomData = WonLinkedDataUtils.getFullAtomDataset(group.get().getGroupAtomUri(), ctx.getLinkedDataSource());
        final DefaultAtomModelWrapper amw = new DefaultAtomModelWrapper(atomData);
        String targetUri = amw.getDefaultSocket().orElse(null);

        URI chatSocket = URI.create(targetUri);
        URI userSocket = event.getTargetSocketURI();

        bus.publish(new ConnectCommandEvent(chatSocket, userSocket, "Welcome to the group, pls type in a Username!"));
    }


}
