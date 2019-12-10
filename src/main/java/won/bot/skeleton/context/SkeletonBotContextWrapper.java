package won.bot.skeleton.context;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.extensions.serviceatom.ServiceAtomEnabledBotContextWrapper;
import won.bot.skeleton.model.GroupMember;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class SkeletonBotContextWrapper extends ServiceAtomEnabledBotContextWrapper {
    private final String connectedSocketsMap;
    private final String groupMemberMap = "groupmembermap";

    public SkeletonBotContextWrapper(BotContext botContext, String botName) {
        super(botContext, botName);
        this.connectedSocketsMap = botName + ":connectedSocketsMap";
    }

    public Map<URI, Set<URI>> getConnectedSockets() {
        Map<String, List<Object>> connectedSockets = getBotContext().loadListMap(connectedSocketsMap);
        Map<URI, Set<URI>> connectedSocketsMapSet = new HashMap<>(connectedSockets.size());

        for(Map.Entry<String, List<Object>> entry : connectedSockets.entrySet()) {
            URI senderSocket = URI.create(entry.getKey());
            Set<URI> targetSocketsSet = new HashSet<>(entry.getValue().size());
            for(Object o : entry.getValue()) {
                targetSocketsSet.add((URI) o);
            }
            connectedSocketsMapSet.put(senderSocket, targetSocketsSet);
        }

        return connectedSocketsMapSet;
    }

    public void addConnectedSocket(URI senderSocket, URI targetSocket) {
        getBotContext().addToListMap(connectedSocketsMap, senderSocket.toString(), targetSocket);
    }

    public void removeConnectedSocket(URI senderSocket, URI targetSocket) {
        getBotContext().removeFromListMap(connectedSocketsMap, senderSocket.toString(), targetSocket);
    }



    public void addGroupMember(URI atomUri, GroupMember member) {
        getBotContext().addToListMap(groupMemberMap, atomUri.toString(), member);
    }

    public List<GroupMember> getGroupMembers(URI atomUri) {
        return getBotContext().loadListMap(groupMemberMap).get(atomUri.toString()).stream()
                .map(m -> (GroupMember)m)
                .collect(Collectors.toList());
    }

    public void removeGroupMember(URI atomUri, URI connectionUri) {
        getBotContext().loadListMap(groupMemberMap).get(atomUri.toString()).stream()
                .filter(m -> ((GroupMember)m).getConnectionUri().equals(connectionUri))
                .forEach(m -> getBotContext().removeFromListMap(groupMemberMap, atomUri.toString(), (GroupMember)m));
    }
}
