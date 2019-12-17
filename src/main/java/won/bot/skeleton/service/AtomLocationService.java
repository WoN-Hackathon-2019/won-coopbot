package won.bot.skeleton.service;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.persistence.model.Location;
import won.protocol.model.Coordinate;
import won.protocol.util.AtomModelWrapper;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.protocol.vocabulary.SCHEMA;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AtomLocationService {

    private EventListenerContext ctx;

    public AtomLocationService(EventListenerContext ctx) {
        this.ctx = ctx;
    }

    public List<Coordinate> getGroupLocations(URI atomUri) {

        return ((SkeletonBotContextWrapper)ctx.getBotContextWrapper()).getGroupMembers(atomUri).stream()
                .map(m -> WonLinkedDataUtils.getConnectionForConnectionURI(m.getConnectionUri(), ctx.getLinkedDataSource()))
                .map(c -> c.get().getTargetAtomURI())
                .map(this::getAtomLocation)
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    public Coordinate getAtomLocation(URI atomUri) {
        //Dataset atomData = WonLinkedDataUtils.getFullAtomDataset(atomUri, ctx.getLinkedDataSource());
        Dataset atomData = ctx.getLinkedDataSource().getDataForResource(atomUri);
        DefaultAtomModelWrapper amw = new DefaultAtomModelWrapper(atomData);
        return amw.getLocationCoordinate();
    }
}
