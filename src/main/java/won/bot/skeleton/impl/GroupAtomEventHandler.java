package won.bot.skeleton.impl;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.model.GroupMember;
import won.protocol.model.Connection;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;

import java.net.URI;
import java.util.Optional;

public class GroupAtomEventHandler implements AtomMessageEventHandler {

    private SkeletonBotContextWrapper botContextWrapper;
    private EventListenerContext ctx;
    private EventBus bus;

    public GroupAtomEventHandler(SkeletonBotContextWrapper botContextWrapper, EventListenerContext ctx, EventBus bus) {
        this.botContextWrapper = botContextWrapper;
        this.ctx = ctx;
        this.bus = bus;
    }

    @Override
    public void onConnect(ConnectFromOtherAtomEvent event) {
        String openMsg = WonRdfUtils.MessageUtils.getTextMessage(event.getWonMessage());
        botContextWrapper.addGroupMember(event.getAtomURI(), new GroupMember(openMsg, event.getConnectionURI()));
        String message = "Hello " + openMsg + ". You joined the groupchat.";
        final ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(
                event.getRecipientSocket(),
                event.getSenderSocket(), message);
        bus.publish(connectCommandEvent);

        sendAll(openMsg + " joined the chat", event.getAtomURI(), event.getConnectionURI());
    }

    @Override
    public void onMessage(MessageFromOtherAtomEvent event) {
        // Forward message to all participants
        String msg = WonRdfUtils.MessageUtils.getTextMessage(event.getWonMessage());
        sendAll(msg, event.getAtomURI(), event.getConnectionURI());
    }

    @Override
    public void onClose(CloseFromOtherAtomEvent event) {
        botContextWrapper.removeGroupMember(event.getAtomURI(), event.getConnectionURI());
        sendAll("Someone left the chat", event.getAtomURI(), event.getConnectionURI());
    }


    private void sendAll(String msg, URI atomUri, URI senderConUri) {
        botContextWrapper.getGroupMembers(atomUri).stream()
                .filter(m -> !m.getConnectionUri().equals(senderConUri))
                .map(m -> WonLinkedDataUtils.getConnectionForConnectionURI(m.getConnectionUri(), ctx.getLinkedDataSource()))
                .filter(con -> con.isPresent())
                .map(con -> con.get())
                .forEach(con -> bus.publish(new ConnectionMessageCommandEvent(con, msg)));
    }
}