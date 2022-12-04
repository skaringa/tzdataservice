package de.kompf.tzdata.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.kompf.tzdata.TzDataEtc;
import de.kompf.tzdata.TzDataShpFileReadAndLocate;

/**
 * REST service to compute the timezone id from latitude and longitude.
 * 
 */
public class TzDataService implements HttpHandler {

  private static final String URL_PATTERN = "/bylonlat/([-+]?[0-9]*\\.?[0-9]*)/([-+]?[0-9]*\\.?[0-9]*$)";
  private TzDataShpFileReadAndLocate tzdata;
  private final Pattern urlPattern;

  public TzDataService(TzDataShpFileReadAndLocate tzdata) {
    this.tzdata = tzdata;
    this.urlPattern = Pattern.compile(URL_PATTERN);
  }

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
      System.err.println("Usage: java [OPTION] -jar tzdataservice.jar path/to/combined-shapefile-with-oceans.shp");
      System.err.println("  OPTIONS");
      System.err.println("    -Dtzdata.extend=true  extend search to coastal waters (default false)");
      System.exit(1);
    }
    boolean extend = "true".equals(System.getProperty("tzdata.extend", "false"));
    TzDataShpFileReadAndLocate tzdata = new TzDataShpFileReadAndLocate(extend);
    tzdata.openInputShapefile(args[0]);

    TzDataService service = new TzDataService(tzdata);
    HttpServer server = createHttpServer(28100, "/tz", service);
    server.start();
  }
  
  /**
   * HTTP handler to compute the timezone id.
   * The input values lon and lat are taken from the 
   * request path "bylonlat/{lon}/{lat}".
   * The response body contains the computed timezone id.
   */
  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    if ("GET".equals(httpExchange.getRequestMethod())) {
      String path = httpExchange.getRequestURI().getPath();
      Matcher matcher = urlPattern.matcher(path);
      if (matcher.find()) {
        try {
          double x = Double.parseDouble(matcher.group(1));
          double y = Double.parseDouble(matcher.group(2));
          String tzid = tzdata.process(x, y);
          if (tzid.length() == 0) {
            tzid = TzDataEtc.tzNameFromLon(x);
          }
          final byte[] body = tzid.getBytes(StandardCharsets.UTF_8);
          
          httpExchange.sendResponseHeaders(200, body.length);
          OutputStream outputStream = httpExchange.getResponseBody();
          outputStream.write(body);
          outputStream.flush();
          outputStream.close();
        } catch (NumberFormatException e) {
          httpExchange.sendResponseHeaders(400, -1);
        }
      } else {
        httpExchange.sendResponseHeaders(400, -1);
      }
    }
  }

  /**
   * Create HTTP server that is bound to the loopback address only.
   */
  private static HttpServer createHttpServer(int port, String path, HttpHandler handler) throws IOException {
    // bind server to loopback interface only:
    InetSocketAddress bindAddr = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    // bind server to any interface (may be a security risk!):
    // InetSocketAddress bindAddr = new InetSocketAddress(port);
    HttpServer server = HttpServer.create(bindAddr, 0);
    server.setExecutor(Executors.newCachedThreadPool());
    server.createContext(path, handler);      
    return server;
  }

}
