import Handlers.FileHandler;
import Handlers.MainHandler;
import utils.Log;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.*;

public class Main {
    public static String file;

    public static void main( String[] args ) throws IOException {
        int port = 8082;
        String ip = "192.168.0.10";
        HttpServer server = HttpServer.create( new InetSocketAddress( ip, port ), 0 );
        server.createContext( "/", new MainHandler() );
        server.start();
        Log.write( "Server started on port " + port );

        Scanner sc = new Scanner( System.in );
        while ( true ) {
            if ( sc.hasNext() ) {
                file = sc.nextLine();
                FileHandler.setFile( file );
            } else {
                Log.write( "\nTerminated" + "\nat " + new Timestamp( System.currentTimeMillis() ) );
                server.stop( 0 );
                break;
            }
            Converter.convert( file );
            if ( file.endsWith( ".png" ) || file.endsWith( ".jpg" ) ) {
                file = file.substring( 0, file.length() - 4 ) + ".bin";
            }
        }
    }
}