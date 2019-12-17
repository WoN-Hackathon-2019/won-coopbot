package won.bot.skeleton.impl;

import at.apf.easycli.CliEngine;
import at.apf.easycli.exception.CommandNotFoundException;
import at.apf.easycli.exception.MalformedCommandException;
import at.apf.easycli.impl.EasyEngine;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.wonmessage.execCommand.ExecuteDeactivateAtomCommandAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.command.close.CloseCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.command.deactivate.DeactivateAtomCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.skeleton.cli.GroupCliExecuter;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.model.Group;
import won.bot.skeleton.model.GroupMember;
import won.bot.skeleton.service.AtomLocationService;
import won.protocol.model.Connection;
import won.protocol.model.Coordinate;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;

import java.net.URI;
import java.util.Optional;

public class GroupAtomEventHandler implements AtomMessageEventHandler {

    private SkeletonBotContextWrapper botContextWrapper;
    private EventListenerContext ctx;
    private EventBus bus;
    private CliEngine cliEngine;
    private AtomLocationService als;

    public GroupAtomEventHandler(SkeletonBotContextWrapper botContextWrapper, EventListenerContext ctx, EventBus bus) {
        this.botContextWrapper = botContextWrapper;
        this.ctx = ctx;
        this.bus = bus;
        this.cliEngine = new EasyEngine();
        this.cliEngine.register(new GroupCliExecuter(ctx, bus));
        this.als = new AtomLocationService(ctx);
    }

    @Override
    public void onConnect(ConnectFromOtherAtomEvent event) {
        if (botContextWrapper.getGroupMembers(event.getAtomURI()).size() >=
                botContextWrapper.getGroup(event.getAtomURI()).getCapacity()) {
            bus.publish(new CloseCommandEvent(event.getCon(), "The group is currently full."));
            return;
        }

        if (botContextWrapper.getGroupMembers(event.getAtomURI()).isEmpty()) {
            botContextWrapper.getGroup(event.getAtomURI()).setAdminConnectionUri(event.getConnectionURI());
        }

        String name = WonRdfUtils.MessageUtils.getTextMessage(event.getWonMessage());
        botContextWrapper.addGroupMember(event.getAtomURI(), new GroupMember(name, event.getConnectionURI()));
        String message = "Hello " + name + ". You joined the groupchat.";
        final ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(
                event.getRecipientSocket(),
                event.getSenderSocket(), message);
        bus.publish(connectCommandEvent);

        sendAll(name + " joined the group", event.getAtomURI(), event.getConnectionURI());

        AtomLocationService atomLocationService = new AtomLocationService(ctx);
        Coordinate loc = atomLocationService.getAtomLocation(event.getCon().getTargetAtomURI());
        if (loc == null) {
            /* ask for location */
            bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "Your location has not been found. Using default location! To specify your location use \"/setLocation\" with your location attached"));
        }
    }

    @Override
    public void onMessage(MessageFromOtherAtomEvent event) {
        // Forward message to all participants
        String msg = WonRdfUtils.MessageUtils.getTextMessage(event.getWonMessage());

        if(msg.startsWith("/")) {
            try {
                cliEngine.parse(msg, event);
                return;
            } catch (MalformedCommandException e) {
                bus.publish(new ConnectionMessageCommandEvent(event.getCon(), e.getMessage()));
                bus.publish(new ConnectionMessageCommandEvent(event.getCon(),cliEngine.usage(msg.split(" ")[0])));
            } catch (CommandNotFoundException e) {
                bus.publish(new ConnectionMessageCommandEvent(event.getCon(), "Command : " + msg + " not known!\nUse one of the following commands:\n" + cliEngine.listCommands()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        GroupMember member = botContextWrapper.getGroupMembers(event.getAtomURI()).stream()
                .filter(m -> event.getConnectionURI().equals(m.getConnectionUri()))
                .findFirst().orElseGet(null);
        if (member != null) {
            sendAll(member.getName() + ": " + msg, event.getAtomURI(), event.getConnectionURI());
        }
    }

    @Override
    public void onClose(CloseFromOtherAtomEvent event) {
        GroupMember member = botContextWrapper.getGroupMembers(event.getAtomURI()).stream()
                .filter(m -> event.getConnectionURI().equals(m.getConnectionUri()))
                .findFirst().orElseGet(null);
        if (member != null) {
            botContextWrapper.removeGroupMember(event.getAtomURI(), event.getConnectionURI());
            sendAll(member.getName() + " left the group", event.getAtomURI(), event.getConnectionURI());
        }

        /* handle last member leaves group */
        if (botContextWrapper.getGroupMembers(event.getAtomURI()).isEmpty()) {
            DeactivateAtomCommandEvent deactivateCommand = new DeactivateAtomCommandEvent(event.getAtomURI());
            bus.publish(deactivateCommand);
            botContextWrapper.removeGroup(event.getAtomURI());
        } else if (member.getConnectionUri().equals(botContextWrapper.getGroup(event.getAtomURI()).getAdminConnectionUri())) {
            GroupMember newAdmin = botContextWrapper.getGroupMembers(event.getAtomURI()).get(0);
            botContextWrapper.getGroup(event.getAtomURI()).setAdminConnectionUri(newAdmin.getConnectionUri());
            sendAll(newAdmin.getName() + " is the new admin of this group", event.getAtomURI(), event.getConnectionURI());
        }
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
