package Handlers;

import Utils.Converter;
import com.sun.net.httpserver.HttpExchange;
import Utils.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileHandler {
    public static String file;

    public static void setFile( String file ) {
        FileHandler.file = file;
    }

    public static void handle( HttpExchange exchange ) throws IOException {
        if ( !exchange.getRequestMethod().equalsIgnoreCase( "GET" )) {
            Response.GFY( exchange );
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        System.out.println(query);
        if (query == null || !query.contains( "name=" )) {
            Response.GFY( exchange );
            return;
        }

        Map<String, String> params = parseQuery( query );
        String filename = params.getOrDefault( "name", "walt" );
        int screenWidth = Integer.parseInt( params.getOrDefault( "width", "1" ) );
        int screenHeight = Integer.parseInt( params.getOrDefault( "height", "1" ) );

        Path baseDir = Path.of( "output" ).toAbsolutePath().normalize();
        Path targetPath = baseDir.resolve( filename ).normalize();

        if ( !targetPath.startsWith( baseDir )) {
            Response.GFY( exchange );
            return;
        }

        if ( filename.endsWith( ".png" ) || filename.endsWith( ".jpg" ) || filename.endsWith( ".jpeg" ) ) {

            String baseName = filename.substring( 0, filename.lastIndexOf( '.' ) );

            if ( !Files.exists( Path.of( "output/" + baseName + "/output/" ) ) &&
                 !Files.exists( Path.of( "output/" + baseName + ".bin" ) ) ) {

                Converter.convert( filename, screenWidth, screenHeight );
            } else Log.write( "File already converted.", "INFO" );

            filename = baseName;
        }

        Path path = Path.of( "output", filename );

        exchange.getResponseHeaders().add( "Content-Type", "application/octet-stream" );
        exchange.getResponseHeaders().add(
                "Content-Disposition",
                "attachment; filename=\"" + path.getFileName().toString() + "\""
        );

        long size = Files.size( path );
        exchange.sendResponseHeaders( 200, size );

        try ( OutputStream os = exchange.getResponseBody() ) {
            Files.copy( path, os );
            Log.write( "File downloaded", "INFO" );
        }
    }

    private static Map<String, String> parseQuery( String query ) {
        Map<String, String> map = new HashMap<>();
        if ( query == null || query.isEmpty() ) return map;

        for ( String param : query.split( "&" ) ) {
            String[] pair = param.split( "=" );
            if ( pair.length > 1 ) {
                String key = URLDecoder.decode( pair[ 0 ] );
                String value = URLDecoder.decode( pair[ 1 ] );
                map.put( key, value );
            }
        }
        return map;
    }
}
