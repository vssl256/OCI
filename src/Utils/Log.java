package Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

    public static synchronized void write( Object obj, String level ) {
        String datetime = LocalDateTime.now().format( FORMAT );
        String message = obj.toString();
        String log = String.format( "[%s] [%s] %s%n", datetime, level, message );
        try ( FileWriter fileWriter = new FileWriter( "server.log", true ) ) {
            fileWriter.write( log );
        } catch ( IOException e ) {
            System.err.println( "Error during log writing " + e.getMessage() );
        };
        System.out.println( obj );
    }
}
