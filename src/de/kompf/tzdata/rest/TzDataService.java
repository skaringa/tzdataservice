package de.kompf.tzdata.rest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.kompf.tzdata.TzDataEtc;
import de.kompf.tzdata.TzDataShpFileReadAndLocate;

/**
 * REST service to compute the timezone id from latitude and longitude.
 * 
 */
@Path("/tz")
public class TzDataService {

  private static TzDataShpFileReadAndLocate tzdata;

  /**
   * MAIN program.
   * Starts the rest service and runs forever.
   * 
   * @param args
   *          path to the tz_world.shp file
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: java [OPTION] -jar tzdataservice.jar path/to/tz_world.shp");
      System.err.println("  OPTIONS");
      System.err.println("    -Dtzdata.extend=true  extend search to coastal waters (default false)");
      System.exit(1);
    }
    boolean extend = "true".equals(System.getProperty("tzdata.extend", "false"));
    tzdata = new TzDataShpFileReadAndLocate(extend);
    tzdata.openInputShapefile(args[0]);

    HttpServer server = createHttpServer(28100, "/");
    server.start();
  }

  /**
   * REST method to compute the timezone id.
   * 
   * @param lon
   *          Latitude (deg)
   * @param lat
   *          Longitude (deg)
   * @return Timezone id.
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("bylonlat/{lon}/{lat}")
  public String bylonlat(@PathParam("lon") String lon,
      @PathParam("lat") String lat)
  {
    try {
      double x = Double.parseDouble(lon);
      double y = Double.parseDouble(lat);
      String tzid = tzdata.process(x, y);
      if (tzid.length() == 0) {
        tzid = TzDataEtc.tzNameFromLon(x);
      }
      return tzid;
    } catch (NumberFormatException e) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    } catch (IOException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Create HTTP server that is bound to the loopback address only.
   */
  private static HttpServer createHttpServer(int port, String path) throws IOException {
    // bind server to loopback interface only:
    InetSocketAddress bindAddr = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    // bind server to any interface (may be a security risk!):
    // InetSocketAddress bindAddr = new InetSocketAddress(port);
    HttpServer server = HttpServer.create(bindAddr, 0);
    server.setExecutor(Executors.newCachedThreadPool());
    HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class);
    server.createContext(path, handler);      
    return server;
  }
}
