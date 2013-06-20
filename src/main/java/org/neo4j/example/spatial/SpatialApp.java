package org.neo4j.example.spatial;

import com.vividsolutions.jts.geom.Coordinate;
import org.neo4j.collections.graphdb.impl.EmbeddedGraphDatabase;
import org.neo4j.gis.spatial.SimplePointLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;

public class SpatialApp {
    private static final String LAYER_NAME = "point-layer";

    private GraphDatabaseService gdb;
    private SpatialDatabaseService spatial;

    public static void main(String[] args) {
        final SpatialApp app = new SpatialApp();
        try {
            app.run();
        } finally {
            app.shutdown();
        }
    }

    public SpatialApp() {
        gdb = new EmbeddedGraphDatabase("target/test-db");
        spatial = new SpatialDatabaseService(gdb);
    }

    private void run() {
        SimplePointLayer layer = getSimplePointLayer();

        // add a point to the layer
        layer.add(13.77, 55.55);

        // Search for nearby locations
        Coordinate pos = new Coordinate(13.76, 55.56);

        List<GeoPipeFlow> results = layer.findClosestPointsTo(pos, 2.0);

        for (GeoPipeFlow result : results) {
            final Coordinate coord = result.getGeometry().getCoordinate();
            System.out.println("coord = " + coord);
        }
    }

    private SimplePointLayer getSimplePointLayer() {
        SimplePointLayer layer = (SimplePointLayer)spatial.getLayer(LAYER_NAME);
        if (layer==null) layer = spatial.createSimplePointLayer(LAYER_NAME);
        return layer;
    }

    private void shutdown() {
        gdb.shutdown();
    }
}
