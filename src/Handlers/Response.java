package Handlers;

import com.sun.net.httpserver.HttpExchange;
import Utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class Response {
    public static void GFY( HttpExchange exchange ) throws IOException {
        String message = "Go fuck yourself";
        exchange.sendResponseHeaders( 403, message.length() );
        try ( OutputStream os = exchange.getResponseBody() ) {
            os.write( message.getBytes() );
        }
        Log.write( "Access denied", "WARN" );
    }
}
