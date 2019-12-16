package won.bot.skeleton.cli;

import at.apf.easycli.annotation.Command;
import at.apf.easycli.annotation.DefaultValue;
import at.apf.easycli.annotation.Meta;
import won.bot.framework.bot.context.BotContextWrapper;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.bot.skeleton.impl.SkeletonBot;
import won.bot.skeleton.model.GroupMember;
import won.protocol.util.linkeddata.WonLinkedDataUtils;

import java.net.URI;

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

    private void sendBroadcastMessage(String msg, URI atomUri, URI senderConUri) {
        wrapper.getGroupMembers(atomUri).stream()
                .filter(m -> !m.getConnectionUri().equals(senderConUri))
                .map(m -> WonLinkedDataUtils.getConnectionForConnectionURI(m.getConnectionUri(), ctx.getLinkedDataSource()))
                .filter(con -> con.isPresent())
                .map(con -> con.get())
                .forEach(con -> bus.publish(new ConnectionMessageCommandEvent(con, msg)));
    }

}
