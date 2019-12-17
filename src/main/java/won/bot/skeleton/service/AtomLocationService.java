package won.bot.skeleton.service;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.skeleton.persistence.model.Location;
import won.protocol.model.Coordinate;
import won.protocol.util.AtomModelWrapper;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.protocol.vocabulary.SCHEMA;

import java.net.URI;

public class AtomLocationService {

    private EventListenerContext ctx;

    public AtomLocationService(EventListenerContext ctx) {
        this.ctx = ctx;
    }

    public Coordinate getAtomLocation(URI atomUri) {
        //Dataset atomData = WonLinkedDataUtils.getFullAtomDataset(atomUri, ctx.getLinkedDataSource());
        Dataset atomData = ctx.getLinkedDataSource().getDataForResource(atomUri);
        DefaultAtomModelWrapper amw = new DefaultAtomModelWrapper(atomData);
        return amw.getLocationCoordinate();
    }
}
