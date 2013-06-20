package org.neo4j.example.spatial;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.util.AssertionFailedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.gis.spatial.SimplePointLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpatialExampleTest {

    private static final String LAYER_NAME = "neo-text";
    private GraphDatabaseService gdb;
    private SpatialDatabaseService spatial;

    @Before
    public void setUp() throws Exception {
        gdb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        spatial = new SpatialDatabaseService(gdb);
    }

    @After
    public void tearDown() throws Exception {
        if (gdb!=null)
            gdb.shutdown();
    }

    @Test
    public void testAddSimpleCoordinate() throws Exception {
        SimplePointLayer layer = getSimplePointLayer();

        // add a point to the layer
        layer.add(13.77, 55.55);

        // Search for nearby locations
        Coordinate myPosition = new Coordinate(13.76, 55.56);

        List<GeoPipeFlow> results = layer.findClosestPointsTo(myPosition, 2.0);

        assertEquals(1,results.size());
        assertEquals(new Coordinate(13.77, 55.55), coords(results.get(0)));
    }

    @Test
    public void testAddManyCoordinates() throws Exception {
        SimplePointLayer layer = getSimplePointLayer();

        // add coordinates to the layer
        final Coordinate[] coords = makeCoordinateDataFromTextFile("neo4j-spatial.txt", 13.0, 55.6);
        for (Coordinate coordinate : coords) {
            layer.add(coordinate);
        }

        // Search for nearby locations
        Coordinate myPosition = new Coordinate(13.76, 55.56);

        List<GeoPipeFlow> results = layer.findClosestPointsTo(myPosition, 1.0);

        assertEquals(3,results.size());
        assertEquals(new Coordinate(13.76, 55.56), coords(results.get(0)));
        assertEquals(new Coordinate(13.75, 55.56), coords(results.get(1)));
        assertEquals(new Coordinate(13.77, 55.56), coords(results.get(2)));

        for (GeoPipeFlow result : results) {
            System.out.println("coord = " + coords(result));
        }
    }

    private SimplePointLayer getSimplePointLayer() {
        SimplePointLayer layer = (SimplePointLayer)spatial.getLayer(LAYER_NAME);
        if (layer==null) layer = spatial.createSimplePointLayer(LAYER_NAME);
        return layer;
    }

    private Coordinate coords(GeoPipeFlow result) {
        return result.getGeometry().getCoordinate();
    }

    private static Coordinate[] makeCoordinateDataFromTextFile(String textFile, double x, double y) {
   		CoordinateList data = new CoordinateList();
   		try {
   			BufferedReader reader = new BufferedReader(new FileReader(textFile));
   			Coordinate origin = new Coordinate(x, y);
   			String line;
   			int row = 0;
   			while ((line = reader.readLine()) != null) {
   				int col = 0;
   				for (String character : line.split("")) {
   					if (col > 0 && !character.matches("\\s")) {
   						Coordinate coordinate = new Coordinate(origin.x + (double) col / 100.0, origin.y - (double) row / 100.0);
   						data.add(coordinate);
   					}
   					col++;
   				}
   				row++;
   			}
   		} catch (IOException e) {
   			throw new AssertionFailedException("Input data for string test invalid: " + e.getMessage());
   		}
   		return data.toCoordinateArray();
   	}

}
