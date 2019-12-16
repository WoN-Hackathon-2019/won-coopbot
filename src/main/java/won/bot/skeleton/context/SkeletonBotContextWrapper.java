package won.bot.skeleton.context;

import com.github.jsonldjava.utils.Obj;
import net.minidev.json.JSONUtil;
import won.bot.framework.bot.context.BotContext;
import won.bot.framework.extensions.serviceatom.ServiceAtomEnabledBotContextWrapper;
import won.bot.skeleton.model.Group;
import won.bot.skeleton.model.GroupMember;
import won.bot.skeleton.persistence.model.SportPlace;

import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class SkeletonBotContextWrapper extends ServiceAtomEnabledBotContextWrapper {
    private final String connectedSocketsMap;
    private final String groupMemberMap = "groupmembermap";
    private final String groupMap = "groupmap";
    private final String sportPlace = "sportPlaces";

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


    public void addGroup(URI atomUri, Group group) {
        getBotContext().addToListMap(groupMap, atomUri.toString(), group);
    }

    public Group getGroup(URI atomUri) {
        return (Group) getBotContext().loadListMap(groupMap).get(atomUri.toString()).get(0);
    }

    public void removeGroup(URI atomUri) {
        getBotContext().removeFromListMap(groupMap, atomUri.toString());
    }


    public void addGroupMember(URI atomUri, GroupMember member) {
        getBotContext().addToListMap(groupMemberMap, atomUri.toString(), member);
    }

    public List<GroupMember> getGroupMembers(URI atomUri) {
        List<Object> members = getBotContext().loadListMap(groupMemberMap).get(atomUri.toString());
        if (members == null) {
            return Collections.EMPTY_LIST;
        }
        return members.stream()
                .map(m -> (GroupMember)m)
                .collect(Collectors.toList());
    }

    public void removeGroupMember(URI atomUri, URI connectionUri) {
        GroupMember member = (GroupMember) getBotContext().loadListMap(groupMemberMap).get(atomUri.toString()).stream()
                .filter(m -> ((GroupMember)m).getConnectionUri().equals(connectionUri))
                .findFirst().orElseGet(null);
        getBotContext().removeFromListMap(groupMemberMap, atomUri.toString(), member);
    }

    public void addSportplaces(Set<SportPlace> sportplaces) {
        getBotContext().addToListMap(sportPlace, "places", sportplaces.toArray());
    }

    public Set<SportPlace> loadSportplaces() {
        Map<String, List<Object>> stringListMap = getBotContext().loadListMap(sportPlace);
       LinkedList places =  (LinkedList) stringListMap.get("places");

       Object[] sportplaces = (Object []) places.getFirst();
       return       Arrays.stream(sportplaces).map(sp -> (SportPlace) sp).collect(Collectors.toSet());
   }
}
