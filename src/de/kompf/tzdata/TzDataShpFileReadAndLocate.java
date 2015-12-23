package de.kompf.tzdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Get the timezone of a given coordinate by using the timezone shapefile
 * from {@link http://efele.net/maps/tz/world/tz_world.zip}
 * 
 */
public class TzDataShpFileReadAndLocate {

  private SimpleFeatureSource featureSource;
  private FilterFactory2 filterFactory;
  private GeometryFactory geometryFactory;

  /**
   * 
   * Reads lat lon pairs from stdin and prints lat lon timzone to stdout.
   * 
   * @param args
   *          If a filename is given, then read the input from it instead of
   *          stdin.
   * 
   * @throws IOException
   * @throws CQLException
   */
  public static void main(String[] args) throws IOException {
    // Download from http://efele.net/maps/tz/world/tz_world.zip
    final String shpfile = System.getProperty("user.home") + "/projects/tz/world/tz_world.shp";
    TzDataShpFileReadAndLocate readAndLocate = new TzDataShpFileReadAndLocate();

    readAndLocate.openInputShapefile(shpfile);

    BufferedReader reader;
    if (args.length == 1) {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
    } else {
      reader = new BufferedReader(new InputStreamReader(System.in));
    }

    String line;
    while ((line = reader.readLine()) != null) {
      try (Scanner scanner = new Scanner(line)) {
        double lat = scanner.nextDouble();
        double lon = scanner.nextDouble();
        String tzid = readAndLocate.process(lon, lat);
        System.out.printf("%s\t%s\n", line, tzid);
      } catch (NoSuchElementException e) {
        // ignore
      }
    }

    reader.close();
  }


  /**
   * Open the input shape file and load it into memory.
   */
  public void openInputShapefile(String inputShapefile) throws IOException {
    File file = new File(inputShapefile);

    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
    Map<String, Serializable> params = new HashMap<>();
    params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
    params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
    params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, Boolean.TRUE);
    params.put(ShapefileDataStoreFactory.MEMORY_MAPPED.key, Boolean.TRUE);
    params.put(ShapefileDataStoreFactory.CACHE_MEMORY_MAPS.key, Boolean.TRUE);

    ShapefileDataStore store = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
    featureSource = store.getFeatureSource();

    filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
    geometryFactory = JTSFactoryFinder.getGeometryFactory();
  }

  /**
   * Print info about the schema of the loaded shapefile.
   */
  public void printInputShapfileSchemaInfo() {
    SimpleFeatureType schema = featureSource.getSchema();
    System.out.println(schema.getTypeName() + ": " + DataUtilities.encodeType(schema));
  }

  /**
   * Process a single coordinate.
   * 
   * @param x
   *          Longitude in degrees.
   * @param y
   *          Latitude in degrees.
   * @return Timezone Id.
   * @throws IOException
   */
  public String process(double x, double y) throws IOException {
    String result = "";

    Point point = geometryFactory.createPoint(new Coordinate(x, y));
    Filter pointInPolygon = filterFactory.contains(filterFactory.property("the_geom"), filterFactory.literal(point));

    SimpleFeatureCollection features = featureSource.getFeatures(pointInPolygon);

    // search in coastal waters
    if (features.size() == 0) {
      // find polygon within distance 0.1 deg
      Filter dWithin = filterFactory.dwithin(filterFactory.property("the_geom"), filterFactory.literal(point),
          0.1, "");
      features = featureSource.getFeatures(dWithin);
    }

    try (FeatureIterator<SimpleFeature> iterator = features.features()) {
      if (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        String tzid = (String) feature.getAttribute("TZID");
        result = tzid;
      }
    }
    return result;
  }
}
