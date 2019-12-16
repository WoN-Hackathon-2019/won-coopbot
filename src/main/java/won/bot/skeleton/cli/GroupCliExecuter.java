package won.bot.skeleton.cli;

import at.apf.easycli.annotation.Command;
import at.apf.easycli.annotation.Meta;
import at.apf.easycli.annotation.Usage;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.close.CloseCommandEvent;
import won.bot.framework.eventbot.event.impl.command.close.CloseCommandResultEvent;
import won.bot.framework.eventbot.event.impl.command.close.CloseCommandSuccessEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.model.GroupMember;
import won.protocol.model.Connection;
import won.protocol.util.linkeddata.WonLinkedDataUtils;

import java.net.URI;
import java.util.List;

public class GroupCliExecuter {

    private EventListenerContext ctx;
    private EventBus bus;
    private SkeletonBotContextWrapper wrapper;

    public GroupCliExecuter(EventListenerContext ctx, EventBus bus) {
        this.ctx = ctx;
        this.bus = bus;
        this.wrapper = (SkeletonBotContextWrapper) this.ctx.getBotContextWrapper();
    }

    @Command("/setName")
    @Usage("name")
    public void createNewGroup(String name, @Meta MessageFromOtherAtomEvent event) {
        /* search the connected Group member */
        GroupMember member = wrapper.getGroupMembers(event.getAtomURI()).stream()
                .filter(m -> m.getConnectionUri().equals(event.getConnectionURI()))
                .findFirst().orElseGet(null);

        /* update the name */
        String oldName = member.getName();
        member.setName(name);

        /* notify all members about the name change */
        String nameChangeNotification = oldName + " changed name to " + name;
        sendBroadcastMessage(nameChangeNotification, event.getAtomURI(), event.getConnectionURI());
        bus.publish(new ConnectionMessageCommandEvent(event.getCon(), nameChangeNotification));
    }

    @Command("/list")
    public void listGroupMembers(@Meta MessageFromOtherAtomEvent event) {
        List<GroupMember> groupMembers = wrapper.getGroupMembers(event.getAtomURI());

        URI adminConnectionUri = wrapper.getGroup(event.getAtomURI()).getAdminConnectionUri();

        StringBuilder builder = new StringBuilder("Groupmembers:");
        for (GroupMember member: groupMembers) {
            builder.append("\n");
            builder.append(member.getName());
            if (member.getConnectionUri().equals(adminConnectionUri)) {
                builder.append(" (Admin)");
            }
        }
        bus.publish(new ConnectionMessageCommandEvent(event.getCon(), builder.toString()));
    }

    @Command("/remove")
    @Usage("name")
    public void removeMember(String name, @Meta MessageFromOtherAtomEvent event) {
        URI adminConnectionUri = wrapper.getGroup(event.getAtomURI()).getAdminConnectionUri();
        if (!adminConnectionUri.equals(event.getConnectionURI())) {
            bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "Only the admin can remove users"));
            return;
        }
        URI toRemoveConnection = wrapper.getGroupMembers(event.getAtomURI()).stream()
                .filter(groupMember -> groupMember.getName().equals(name))
                .map(GroupMember::getConnectionUri)
                .findFirst()
                .orElseGet(null);

        if (toRemoveConnection == null) {
            bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "User with name " + name + "not found."));
            return;
        }

        wrapper.removeGroupMember(event.getAtomURI(), toRemoveConnection);
        Connection toCloseConnection = WonLinkedDataUtils.getConnectionForConnectionURI(toRemoveConnection, ctx.getLinkedDataSource()).orElseGet(null);
        CloseCommandEvent closeCommandEvent = new CloseCommandEvent(toCloseConnection);
        bus.subscribe(CloseCommandSuccessEvent.class, new ActionOnFirstEventListener(ctx, new CommandResultFilter(closeCommandEvent), new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event1, EventListener eventListener) throws Exception {
                CloseCommandResultEvent closeCommandResultEvent = (CloseCommandSuccessEvent) event1;
                if (!closeCommandResultEvent.isSuccess()) {
                    bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "Could nor remove user"));
                } else {
                    sendBroadcastMessage("Admin removed  " + name, event.getAtomURI(), null);
                }
            }
        }));
        bus.publish(closeCommandEvent);

     }

    private void sendBroadcastMessage(String msg, URI atomUri, URI senderConUri) {
        wrapper.getGroupMembers(atomUri).stream()
                .filter(m -> !m.getConnectionUri().equals(senderConUri))
                .map(m -> WonLinkedDataUtils.getConnectionForConnectionURI(m.getConnectionUri(), ctx.getLinkedDataSource()))
                .filter(con -> con.isPresent())
                .map(con -> con.get())
                .forEach(con -> bus.publish(new ConnectionMessageCommandEvent(con, msg)));
    }

}
