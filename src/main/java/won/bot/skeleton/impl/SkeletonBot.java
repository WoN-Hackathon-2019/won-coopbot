package won.bot.skeleton.impl;

import java.lang.invoke.MethodHandles;
import java.net.URI;

import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.behaviour.ExecuteWonMessageCommandBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.AtomUriInNamedListFilter;
import won.bot.framework.eventbot.filter.impl.NotFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.matcher.MatcherBehaviour;
import won.bot.framework.extensions.matcher.MatcherExtension;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.bot.framework.extensions.serviceatom.ServiceAtomBehaviour;
import won.bot.framework.extensions.serviceatom.ServiceAtomExtension;
import won.bot.skeleton.action.CreateGroupChatAtomAction;
import won.bot.skeleton.action.CreateLocationApiAtomAction;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.bot.skeleton.event.CreateLocationApiAtomEvent;
import won.protocol.model.Coordinate;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.linkeddata.WonLinkedDataUtils;

public class SkeletonBot extends EventBot implements MatcherExtension, ServiceAtomExtension {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private int registrationMatcherRetryInterval;
    private MatcherBehaviour matcherBehaviour;
    private ServiceAtomBehaviour serviceAtomBehaviour;

    private AtomMessageEventHandler messageBroker;
    private URI receiverAtomUri;
    private URI receiverAtomSocketUri;

    // bean setter, used by spring
    public void setRegistrationMatcherRetryInterval(final int registrationMatcherRetryInterval) {
        this.registrationMatcherRetryInterval = registrationMatcherRetryInterval;
    }

    @Override
    public ServiceAtomBehaviour getServiceAtomBehaviour() {
        return serviceAtomBehaviour;
    }

    @Override
    public MatcherBehaviour getMatcherBehaviour() {
        return matcherBehaviour;
    }

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        if (!(getBotContextWrapper() instanceof SkeletonBotContextWrapper)) {
            logger.error(getBotContextWrapper().getBotName() + " does not work without a SkeletonBotContextWrapper");
            throw new IllegalStateException(
                            getBotContextWrapper().getBotName() + " does not work without a SkeletonBotContextWrapper");
        }
        EventBus bus = getEventBus();
        SkeletonBotContextWrapper botContextWrapper = (SkeletonBotContextWrapper) getBotContextWrapper();

        // register listeners for event.impl.command events used to tell the bot to send
        // messages
        ExecuteWonMessageCommandBehaviour wonMessageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
        wonMessageCommandBehaviour.activate();
        // activate ServiceAtomBehaviour

        serviceAtomBehaviour = new ServiceAtomBehaviour(ctx);
        serviceAtomBehaviour.activate();

        // Set receiverAtom URI
        bus.subscribe(AtomCreatedEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) throws Exception {
                if (messageBroker == null) {
                    receiverAtomUri = ((AtomCreatedEvent)event).getAtomURI();
                    DefaultAtomModelWrapper amw = new DefaultAtomModelWrapper(((AtomCreatedEvent) event).getAtomDataset());
                    receiverAtomSocketUri = URI.create(amw.getDefaultSocket().get());
                    messageBroker = new AtomMessageBroker(
                            ctx,
                            new ReceiverAtomEventHandler(botContextWrapper, ctx, bus),
                            new GroupAtomEventHandler(botContextWrapper, ctx, bus)
                    );
                }
            }
        });

        // set up matching extension
        // as this is an extension, it can be activated and deactivated as needed
        // if activated, a MatcherExtensionAtomCreatedEvent is sent every time a new
        // atom is created on a monitored node
        matcherBehaviour = new MatcherBehaviour(ctx, "BotSkeletonMatchingExtension", registrationMatcherRetryInterval);
        matcherBehaviour.activate();
        // create filters to determine which atoms the bot should react to
        NotFilter noOwnAtoms = new NotFilter(
                        new AtomUriInNamedListFilter(ctx, ctx.getBotContextWrapper().getAtomCreateListName()));
        // filter to prevent reacting to serviceAtom<->ownedAtom events;
        NotFilter noInternalServiceAtomEventFilter = getNoInternalServiceAtomEventFilter();
        bus.subscribe(ConnectFromOtherAtomEvent.class, noInternalServiceAtomEventFilter, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) {
                ConnectFromOtherAtomEvent connectFromOtherAtomEvent = (ConnectFromOtherAtomEvent) event;
                messageBroker.onConnect(connectFromOtherAtomEvent);
            }
        });


        // Disconnect
        bus.subscribe(CloseFromOtherAtomEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) {
                CloseFromOtherAtomEvent closeFromOtherAtomEvent = (CloseFromOtherAtomEvent) event;
                messageBroker.onClose(closeFromOtherAtomEvent);
            }
        });

        // Create GropuChat Atom
        bus.subscribe(CreateGroupChatEvent.class, new CreateGroupChatAtomAction(ctx));

        // LocationApi Atom Creation
        bus.subscribe(CreateLocationApiAtomEvent.class, new CreateLocationApiAtomAction(ctx));

        // Receiving Messages in connections
        bus.subscribe(MessageFromOtherAtomEvent.class, noInternalServiceAtomEventFilter, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) {
                MessageFromOtherAtomEvent recEvent = (MessageFromOtherAtomEvent) event;
                messageBroker.onMessage(recEvent);
            }
        });


        // Send new created atoms with a location an invitation
        bus.subscribe(MatcherExtensionAtomCreatedEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) throws Exception {
                if (!(event instanceof MatcherExtensionAtomCreatedEvent)) {
                    return;
                }

                MatcherExtensionAtomCreatedEvent e = (MatcherExtensionAtomCreatedEvent)event;
                Dataset atomData = WonLinkedDataUtils.getFullAtomDataset(e.getAtomURI(), getEventListenerContext().getLinkedDataSource());
                final DefaultAtomModelWrapper amw = new DefaultAtomModelWrapper(atomData);
                Coordinate latlang = amw.getLocationCoordinate();
                if (latlang != null /*&& amw.getAllTags().contains("groupactivity")*/) {
                    logger.info("Found a new atom with a location. Trying to establish a connection ...");
                    // Open Connection to atom
                    String targetUri = amw.getDefaultSocket().orElse(null);
                    bus.publish(new ConnectCommandEvent(
                            receiverAtomSocketUri,
                            URI.create(targetUri),
                            "You need to manage some group activity?"
                    ));
                }
            }
        });
    }

}
