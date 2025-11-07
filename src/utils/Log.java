package utils;

import java.io.FileWriter;
import java.io.IOException;

public class Log {
    public static void write( Object obj ) {
        try ( FileWriter fileWriter = new FileWriter( "server.log", true ) ) {
            fileWriter.write( obj.toString() + "\n" );
        } catch ( IOException e ) {
            System.err.println( "Error during log writing " + e.getMessage() );
        };
        System.out.println( obj );
    }
}
