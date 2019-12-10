package won.bot.skeleton.impl;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;

import java.net.URI;

public class GroupAtomEventHandler {

    private EventListenerContext ctx;
    private EventBus bus;

    public GroupAtomEventHandler(EventListenerContext ctx, EventBus bus) {
        this.ctx = ctx;
        this.bus = bus;
    }

    public void receivedConnectMsg(ConnectFromOtherAtomEvent event) {

    }

    public void receivedMessage(MessageFromOtherAtomEvent event) {
        // Forward message to all participants
    }


    private void sendAll(String msg, URI atomUri) {
        // Get All Atoms of chat (buffer them local in the context)
        //
        //WonLinkedDataUtils.getConnectionURIForSocketAndTargetSocket()
    }

}
