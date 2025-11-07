package Handlers;

import com.sun.net.httpserver.HttpExchange;
import utils.Log;

import java.io.IOException;

public class Response {
    public static void GFY( HttpExchange exchange ) throws IOException {
        exchange.sendResponseHeaders( 403, -1 );
        exchange.getResponseBody().close();
        Log.write( "Access denied", "WARN" );
    }
}
