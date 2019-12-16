package won.bot.skeleton.impl;

import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;

import java.net.URI;

public class AtomMessageBroker implements AtomMessageEventHandler {

    private URI receiverAtomUri;
    private AtomMessageEventHandler receiverHandler;
    private AtomMessageEventHandler groupHandler;

    public AtomMessageBroker(URI receiverAtomUri, AtomMessageEventHandler receiverHandler, AtomMessageEventHandler groupHandler) {
        this.receiverAtomUri = receiverAtomUri;
        this.receiverHandler = receiverHandler;
        this.groupHandler = groupHandler;
    }

    @Override
    public void onConnect(ConnectFromOtherAtomEvent event) {
        if (isMessageForReceiverAtom(event.getAtomURI())) {
            receiverHandler.onConnect(event);
        } else {
            groupHandler.onConnect(event);
        }
    }

    @Override
    public void onMessage(MessageFromOtherAtomEvent event) {
        if (isMessageForReceiverAtom(event.getAtomURI())) {
            receiverHandler.onMessage(event);
        } else {
            groupHandler.onMessage(event);
        }
    }

    @Override
    public void onClose(CloseFromOtherAtomEvent event) {
        if (isMessageForReceiverAtom(event.getAtomURI())) {
            receiverHandler.onClose(event);
        } else {
            groupHandler.onClose(event);
        }
    }

    private boolean isMessageForReceiverAtom(URI atomUri) {
        return atomUri.equals(receiverAtomUri);
    }

}
