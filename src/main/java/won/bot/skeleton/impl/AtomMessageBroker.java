package won.bot.skeleton.impl;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;

import java.net.URI;

public class AtomMessageBroker implements AtomMessageEventHandler {

    private EventListenerContext ctx;
    private SkeletonBotContextWrapper botContextWrapper;
    private AtomMessageEventHandler receiverHandler;
    private AtomMessageEventHandler groupHandler;

    public AtomMessageBroker(EventListenerContext ctx, AtomMessageEventHandler receiverHandler, AtomMessageEventHandler groupHandler) {
        this.ctx = ctx;
        this.botContextWrapper = (SkeletonBotContextWrapper) ctx.getBotContextWrapper();
        this.receiverHandler = receiverHandler;
        this.groupHandler = groupHandler;
    }

    @Override
    public void onConnect(ConnectFromOtherAtomEvent event) {
        if (isMessageForReceiverAtom(event.getAtomURI())) {
            receiverHandler.onConnect(event);
        } else if(isMessageForGroupAtom(event.getAtomURI())){
            groupHandler.onConnect(event);
        }
    }

    @Override
    public void onMessage(MessageFromOtherAtomEvent event) {
        if (isMessageForReceiverAtom(event.getAtomURI())) {
            receiverHandler.onMessage(event);
        } else if(isMessageForGroupAtom(event.getAtomURI())) {
            groupHandler.onMessage(event);
        }
    }

    @Override
    public void onClose(CloseFromOtherAtomEvent event) {
        if (isMessageForReceiverAtom(event.getAtomURI())) {
            receiverHandler.onClose(event);
        } else if(isMessageForGroupAtom(event.getAtomURI())) {
            groupHandler.onClose(event);
        }
    }

    private boolean isMessageForReceiverAtom(URI atomUri) {
        return atomUri.equals(botContextWrapper.getServiceAtomUri());
    }

    private boolean isMessageForGroupAtom(URI atomUri) {
        // TODO: Change g.getAdminConnectionUri() to g.getAtomUri()
        return botContextWrapper.getAllGroups().stream()
                .filter(g -> g.getGroupAtomUri().equals(atomUri))
                .findAny().isPresent();
        //return true;
    }

}
