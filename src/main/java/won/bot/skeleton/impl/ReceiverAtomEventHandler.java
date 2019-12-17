package won.bot.skeleton.impl;

import at.apf.easycli.CliEngine;
import at.apf.easycli.impl.EasyEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandResultEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandSuccessEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.bot.skeleton.cli.ReceiverCliExecuter;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.protocol.util.WonRdfUtils;

public class ReceiverAtomEventHandler implements AtomMessageEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReceiverAtomEventHandler.class);

    private SkeletonBotContextWrapper botContextWrapper;
    private EventListenerContext ctx;
    private EventBus bus;
    private CliEngine cliEngine = new EasyEngine();

    public ReceiverAtomEventHandler(SkeletonBotContextWrapper botContextWrapper, EventListenerContext ctx, EventBus bus) {
        this.botContextWrapper = botContextWrapper;
        this.ctx = ctx;
        this.bus = bus;
        cliEngine.register(new ReceiverCliExecuter(ctx, bus));
    }

    @Override
    public void onConnect(ConnectFromOtherAtomEvent event) {
        String message = "Hello i am the CoopBot. You can create use the following commands:\n" + cliEngine.listCommands();
        final ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(
                event.getRecipientSocket(),
                event.getSenderSocket(), message);
        ctx.getEventBus().subscribe(ConnectCommandSuccessEvent.class, new ActionOnFirstEventListener(ctx,
                new CommandResultFilter(connectCommandEvent), new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) {
                ConnectCommandResultEvent connectionMessageCommandResultEvent = (ConnectCommandResultEvent) event;
                if (!connectionMessageCommandResultEvent.isSuccess()) {
                    logger.error("Failure when trying to open a received Request: "
                            + connectionMessageCommandResultEvent.getMessage());
                } else {
                    logger.info(
                            "Add an established connection " +
                                    connectCommandEvent.getLocalSocket()
                                    + " -> "
                                    + connectCommandEvent.getTargetSocket()
                                    +
                                    " to the botcontext ");
                    botContextWrapper.addConnectedSocket(
                            connectCommandEvent.getLocalSocket(),
                            connectCommandEvent.getTargetSocket());
                }
            }
        }));
        ctx.getEventBus().publish(connectCommandEvent);
    }

    @Override
    public void onMessage(MessageFromOtherAtomEvent event) {
        String recMsg = WonRdfUtils.MessageUtils.getTextMessage(event.getWonMessage());
        if (recMsg.startsWith("/")) {
            try {
                cliEngine.parse(recMsg, event);
            } catch (Exception e) {
                // TODO: Send usage
            }
            return;
        }

        String respMsg = "Blabla " + WonRdfUtils.MessageUtils.getTextMessage(event.getWonMessage());
        ConnectionMessageCommandEvent responseCmd = new ConnectionMessageCommandEvent(event.getCon(), respMsg);
        ctx.getEventBus().publish(responseCmd);
    }

    @Override
    public void onClose(CloseFromOtherAtomEvent event) {

    }
}
