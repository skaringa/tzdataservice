package de.kompf.tzdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Get the timezone of a given coordinate by using the timezone shapefile
 * from {@link http://efele.net/maps/tz/world/tz_world.zip}
 * 
 */
public class TzDataShpFileReadAndLocate {

  private String tzidAttr = "tzid";
  private boolean searchCoastalWaters;
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
   */
  public static void main(String[] args) throws IOException {
    // Download from http://efele.net/maps/tz/world/tz_world.zip (old style)
    // or https://github.com/evansiroky/timezone-boundary-builder/releases (new
    // style)
    final String shpfile = System.getProperty("user.home") + "/projects/tz/dist/combined_shapefile.shp";
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
   * Create new instance without additional search in coastal waters (new
   * evansiroky database).
   */
  public TzDataShpFileReadAndLocate() {
    this(false);
  }

  /**
   * Create new instance.
   * 
   * @param searchCoastalWaters
   *          Whether to extend search to coastal waters or not.
   */
  public TzDataShpFileReadAndLocate(boolean searchCoastalWaters) {
    this.searchCoastalWaters = searchCoastalWaters;
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
    // determine the correct case of the tz attribute because its case has been
    // changed from efele to evansiroky
    SimpleFeatureType schema = featureSource.getSchema();
    List<AttributeDescriptor> attributeDescriptorList = schema.getAttributeDescriptors();
    for (AttributeDescriptor attributeDescriptor : attributeDescriptorList) {
      if ("tzid".equalsIgnoreCase(attributeDescriptor.getLocalName())) {
        tzidAttr = attributeDescriptor.getLocalName();
      }
    }

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
   */
  public String process(double x, double y) throws IOException {
    Point point = geometryFactory.createPoint(new Coordinate(x, y));
    Filter pointInPolygon = filterFactory.contains(filterFactory.property("the_geom"), filterFactory.literal(point));

    SimpleFeatureCollection features = featureSource.getFeatures(pointInPolygon);
    SimpleFeature result;
    
    // search in coastal waters
    // no longer necessary with new shapes from evansiroky
    if (searchCoastalWaters && features.isEmpty()) {
      // find polygon within distance 0.1 deg
      Filter dWithin = filterFactory.dwithin(filterFactory.property("the_geom"), filterFactory.literal(point),
          0.1, "");
      features = featureSource.getFeatures(dWithin);

      if (features.size() > 1) {
        // if more than one polygon was found, then choose the nearest one
        result = selectMinDistance(features, point);
      } else {
        result = selectFirst(features);
      }
    } else {
      result = selectFirst(features);
    }

    return result == null ? "" : (String) result.getAttribute(tzidAttr);
  }

  private SimpleFeature selectFirst(SimpleFeatureCollection features) {
    SimpleFeature first = null;
    try (FeatureIterator<SimpleFeature> iterator = features.features()) {
      if (iterator.hasNext()) {
        first = iterator.next();
      }
    }
    return first;
  }

  private SimpleFeature selectMinDistance(SimpleFeatureCollection features, Point point) {
    SimpleFeature nearest = null;
    double minDistance = Double.MAX_VALUE;
    try (FeatureIterator<SimpleFeature> iterator = features.features()) {
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        double distance = point.distance((Geometry) feature.getDefaultGeometry());
        if (distance < minDistance) {
          minDistance = distance;
          nearest = feature;
        }
      }
    }
    return nearest;
  }
}
