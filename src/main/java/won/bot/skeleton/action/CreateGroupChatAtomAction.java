package won.bot.skeleton.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.bot.skeleton.model.Group;
import won.bot.skeleton.model.GroupMember;
import won.protocol.message.WonMessage;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.*;
import won.protocol.vocabulary.SCHEMA;
import won.protocol.vocabulary.WONCON;
import won.protocol.vocabulary.WXCHAT;

import java.net.URI;
import java.util.Date;

public class CreateGroupChatAtomAction extends AbstractCreateAtomAction {

    private static Logger logger = LoggerFactory.getLogger(CreateGroupChatAtomAction.class);

    public CreateGroupChatAtomAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof CreateGroupChatEvent
                && ctx.getBotContextWrapper() instanceof SkeletonBotContextWrapper) {
            SkeletonBotContextWrapper botContextWrapper = (SkeletonBotContextWrapper) ctx.getBotContextWrapper();
            // JokeBotsApi jokeBotsApi = ((CreateAtomFromJokeEvent) event).getJokeBotsApi();
            CreateGroupChatEvent e = (CreateGroupChatEvent) event;
            try {
                // Only one single random joke
                logger.info("Create 1 random Chuck Norris joke atom");
                this.createAtomFromEvent(ctx, botContextWrapper, e);
            } catch (Exception me) {
                logger.error("messaging exception occurred:", me);
            }
        }
    }

    protected boolean createAtomFromEvent(EventListenerContext ctx, SkeletonBotContextWrapper botContextWrapper,
                                         CreateGroupChatEvent e) {
        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();
        final URI atomURI = wonNodeInformationService.generateAtomURI(wonNodeUri);
        final AtomModelWrapper amw = this.createAtomStructure(atomURI, e);
        Dataset dataset = amw.copyDataset();

        logger.debug("creating atom on won node {} with content {} ", wonNodeUri,
                StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
        WonMessage createAtomMessage = ctx.getWonMessageSender()
                .prepareMessage(createWonMessage(atomURI, wonNodeUri, dataset,
                        false, false));
        EventBotActionUtils.rememberInList(ctx, atomURI, uriListName);
        EventBus bus = ctx.getEventBus();
        EventListener successCallback = event -> {
            logger.debug("atom creation successful, new atom URI is {}", atomURI);
            botContextWrapper.addGroup(atomURI, new Group(e.getName(), e.getMax(), atomURI));
            bus.publish(new AtomCreatedEvent(atomURI, wonNodeUri, dataset, null));
            ConnectCommandEvent connectToAdminEvent = new ConnectCommandEvent(
                    URI.create(amw.getDefaultSocket().get()),
                    e.getAdminSocketUri(),
                    "Group Atom created. Whats your name?"
            );
            bus.publish(connectToAdminEvent);
        };
        EventListener failureCallback = event -> {
            String textMessage = WonRdfUtils.MessageUtils
                    .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
            logger.error("atom creation failed for atom URI {}, original message URI {}: {}", new Object[] {
                    atomURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage });
            EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(createAtomMessage, successCallback, failureCallback,
                ctx);
        logger.debug("registered listeners for response to message URI {}", createAtomMessage.getMessageURI());
        ctx.getWonMessageSender().sendMessage(createAtomMessage);
        logger.debug("atom creation message sent with message URI {}", createAtomMessage.getMessageURI());
        return true;
    }

    private AtomModelWrapper createAtomStructure(URI atomURI, CreateGroupChatEvent event) {
        DefaultAtomModelWrapper atomModelWrapper = new DefaultAtomModelWrapper(atomURI);
        Resource atom = atomModelWrapper.getAtomModel().createResource(atomURI.toString());
        // @type
        // atom.addProperty(RDF.type, SCHEMA.JOBPOSTING);
        // s:url
        //atom.addProperty(SCHEMA.URL, chuckNorrisJoke.getUrl());
        // s:title
        atom.addProperty(SCHEMA.TITLE, event.getName());
        // s:datePosted
        // TODO:convert to s:Date (ISO 8601)
        atom.addProperty(SCHEMA.DATEPOSTED, new Date().toString());
        // s:image
        Resource image = atom.getModel().createResource();
        image.addProperty(RDF.type, SCHEMA.URL);
        //image.addProperty(SCHEMA.VALUE, chuckNorrisJoke.getIcon_url());
        // s:description
        atom.addProperty(SCHEMA.DESCRIPTION, "Coop Group chat for place finding. Join the group with your nickname as opening message");
        // s:name
        atom.addProperty(SCHEMA.NAME, event.getName());
        if (event.getMax() > 1) {
            atom.addProperty(OWL.maxCardinality, event.getMax() + "");
        }
        // won:tags
        String[] tags = { "groupchat", "groupactivity" };
        for (String tag : tags) {
            atom.addProperty(WONCON.tag, tag);
        }
        atomModelWrapper.addSocket("#ChatSocket", WXCHAT.ChatSocketString);
        atomModelWrapper.setDefaultSocket("#ChatSocket");
        //atomModelWrapper.addFlag(WONMATCH.NoHintForMe);
        return atomModelWrapper;
    }
}
