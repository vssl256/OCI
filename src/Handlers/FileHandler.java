package Handlers;

import com.sun.net.httpserver.HttpExchange;
import utils.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
    public static String file;

    public static void setFile( String file ) {
        FileHandler.file = file;
    }

    public static void handle( HttpExchange exchange ) throws IOException {
        String outputPath = "output/" + file.substring( 0, file.length() - 4 ) + ".bin";
        Path path = Path.of( outputPath );
        long size = Files.size( path );
        exchange.getResponseHeaders().add(
                "Content-Disposition",
                "attachment; filename=\"" + new java.io.File( outputPath ).getName() + "\""
        );
        exchange.sendResponseHeaders( 200, size );
        try ( OutputStream os = exchange.getResponseBody() ) {
            Files.copy( path, os );
            Log.write( "File downloaded", "INFO" );
        }
    }
}
