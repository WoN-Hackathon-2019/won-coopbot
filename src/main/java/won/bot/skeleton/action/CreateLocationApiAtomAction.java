package won.bot.skeleton.action;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.EventFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.event.CreateGroupChatEvent;
import won.bot.skeleton.event.CreateLocationApiAtomEvent;
import won.bot.skeleton.model.Group;
import won.bot.skeleton.model.dto.LocationApiResponse;
import won.bot.skeleton.service.AtomLocationService;
import won.protocol.message.WonMessage;
import won.protocol.model.Coordinate;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.AtomModelWrapper;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.protocol.vocabulary.SCHEMA;
import won.protocol.vocabulary.WONCON;
import won.protocol.vocabulary.WONMATCH;
import won.protocol.vocabulary.WXCHAT;

import java.net.URI;
import java.util.Date;
import java.util.List;

public class CreateLocationApiAtomAction extends AbstractCreateAtomAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateLocationApiAtomAction.class);

    public CreateLocationApiAtomAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {

        EventListenerContext ctx = getEventListenerContext();
        SkeletonBotContextWrapper botContextWrapper = (SkeletonBotContextWrapper) ctx.getBotContextWrapper();

        final URI groupAtomUri = ((CreateLocationApiAtomEvent)event).getGroupAtomUri();
        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();
        final URI atomURI = wonNodeInformationService.generateAtomURI(wonNodeUri);
        final AtomModelWrapper amw = this.createAtomStructure(atomURI);
        Dataset dataset = amw.copyDataset();

        logger.debug("creating atom on won node {} with content {} ", wonNodeUri,
                StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
        WonMessage createAtomMessage = ctx.getWonMessageSender()
                .prepareMessage(createWonMessage(atomURI, wonNodeUri, dataset,
                        false, false));
        EventBotActionUtils.rememberInList(ctx, atomURI, uriListName);
        EventBus bus = ctx.getEventBus();
        EventListener successCallback = succEvent -> {

            // Register the connect from the api
            bus.subscribe(ConnectFromOtherAtomEvent.class, event1 -> {
                if (event1 instanceof ConnectFromOtherAtomEvent) {
                    ConnectFromOtherAtomEvent e = (ConnectFromOtherAtomEvent) event1;
                    return e.getAtomURI().equals(atomURI);
                }
                return false;
            }, new BaseEventBotAction(ctx) {

                private boolean sent = false;

                @Override
                protected void doRun(Event event, EventListener executingListener) throws Exception {

                    if (sent) {
                        return;
                    }
                    sent = true;

                    // Send connect before sending api request
                    ConnectFromOtherAtomEvent connectFromOtherAtomEvent = (ConnectFromOtherAtomEvent) event;
                    bus.publish(new ConnectCommandEvent(connectFromOtherAtomEvent.getRecipientSocket(), connectFromOtherAtomEvent.getSenderSocket(), ""));
                    //bus.publish(new ConnectionMessageCommandEvent(connectFromOtherAtomEvent.getCon(), ""));

                    Thread.sleep(1000);

                    // TODO: Send request and waint for response
                    List<Coordinate> locations = new AtomLocationService(ctx).getGroupLocations(groupAtomUri);
                    StringBuilder sb = new StringBuilder();
                    sb.append("/json \"");
                    sb.append("{\\\"locations\\\": [");
                    for (int i = 0; i < locations.size(); i++) {
                        sb.append("[" + locations.get(i).getLatitude() + ", " + locations.get(i).getLongitude() + "]");
                        if (i + 1 < locations.size()) {
                            sb.append(", ");
                        }
                    }
                    sb.append("],");
                    sb.append("\\\"categories\\\": [\\\"Socker Field\\\", \\\"Socker Stadium\\\"]}");
                    sb.append("\"");



                    bus.publish(new ConnectionMessageCommandEvent(connectFromOtherAtomEvent.getCon(), sb.toString()));
                }
            });


            // Register the response from the api
            bus.subscribe(MessageFromOtherAtomEvent.class, event1 -> {
                if (event1 instanceof MessageFromOtherAtomEvent) {
                    MessageFromOtherAtomEvent e = (MessageFromOtherAtomEvent) event1;
                    return e.getAtomURI().equals(atomURI);
                }
                return false;
            }, new BaseEventBotAction(ctx) {

                @Override
                protected void doRun(Event event, EventListener executingListener) throws Exception {
                    // TODO: Read resonse and forward it
                    MessageFromOtherAtomEvent messageFromOtherAtomEvent = (MessageFromOtherAtomEvent) event;
                    String responseText = WonRdfUtils.MessageUtils.getTextMessage(messageFromOtherAtomEvent.getWonMessage());
                    Gson gson = new Gson();
                    LocationApiResponse locationApiResponse = gson.fromJson(responseText, LocationApiResponse.class);

                    botContextWrapper.getGroupMembers(groupAtomUri).stream()
                            .map(m -> WonLinkedDataUtils.getConnectionForConnectionURI(m.getConnectionUri(), ctx.getLinkedDataSource()))
                            .filter(con -> con.isPresent())
                            .map(con -> con.get())
                            .forEach(con -> bus.publish(new ConnectionMessageCommandEvent(con, "Best location: " + locationApiResponse.getMapsLink())));

                }
            });


        };
        EventListener failureCallback = failEvent -> {

        };
        EventBotActionUtils.makeAndSubscribeResponseListener(createAtomMessage, successCallback, failureCallback,
                ctx);
        ctx.getWonMessageSender().sendMessage(createAtomMessage);

    }

    private AtomModelWrapper createAtomStructure(URI atomURI) {
        DefaultAtomModelWrapper atomModelWrapper = new DefaultAtomModelWrapper(atomURI);
        Resource atom = atomModelWrapper.getAtomModel().createResource(atomURI.toString());
        // @type
        // atom.addProperty(RDF.type, SCHEMA.JOBPOSTING);
        // s:url
        //atom.addProperty(SCHEMA.URL, chuckNorrisJoke.getUrl());
        // s:title
        atom.addProperty(SCHEMA.TITLE, "LocationApiRequest");
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
        atom.addProperty(SCHEMA.NAME, "LocationApiRequest");
        // won:tags
        String[] tags = { "meetingapi" };
        for (String tag : tags) {
            atom.addProperty(WONCON.tag, tag);
        }
        atomModelWrapper.addSocket("#ChatSocket", WXCHAT.ChatSocketString);
        atomModelWrapper.setDefaultSocket("#ChatSocket");
        //atomModelWrapper.addFlag(WONMATCH.NoHintForMe);

        return atomModelWrapper;
    }
}
