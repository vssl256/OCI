package Handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import Utils.Log;

public class MainHandler implements HttpHandler {
    @Override
    public void handle( HttpExchange exchange ) throws IOException {
        String ip = exchange.getRequestHeaders().getFirst( "Host" );
        Log.write( "Attempt to connect using " + ip, "INFO" );
        if ( ip == null || !ip.equals( "helx.ddns.net" ) ) { Response.GFY( exchange ); }
        if ( exchange.getRequestURI().getPath().equals( "/file" ) ) {
            FileHandler.handle( exchange );
        } else Response.GFY( exchange );
    }
}
