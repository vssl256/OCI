import Handlers.MainHandler;
import Utils.Controls;
import Utils.Log;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {

    public static void main( String[] args ) throws IOException {
        int port = 8082;
        String ip = "192.168.0.10";
        HttpServer server = HttpServer.create( new InetSocketAddress( ip, port ), 0 );
        server.createContext( "/", new MainHandler() );
        server.start();
        Log.write( "Server started on port " + port , "INFO");

        try ( Scanner sc = new Scanner( System.in ) ) {
            Controls.startLoop( sc, server );
        } finally {
            server.stop( 0 );
            Log.write( "Terminated", "INFO" );
        }
    }
}