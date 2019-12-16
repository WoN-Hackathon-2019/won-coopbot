package won.bot.skeleton.impl;

import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;

public interface AtomMessageEventHandler {

    void onConnect(ConnectFromOtherAtomEvent event);

    void onMessage(MessageFromOtherAtomEvent event);

    void onClose(CloseFromOtherAtomEvent event);
}
