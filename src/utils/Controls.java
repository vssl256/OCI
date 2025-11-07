package utils;

import Handlers.FileHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.util.Scanner;

public class Controls {
    public static String file;

    public static void startLoop( Scanner sc, HttpServer server ) throws IOException {
        while ( sc.hasNext() ) {
            String peek = sc.nextLine();
            if ( peek.isBlank() ) continue;
            if ( peek.startsWith( "/" ) ) {
                commands( peek );
                continue;
            }
            file = peek;
            FileHandler.setFile( file );
            Converter.convert( file );
            if ( file.endsWith( ".png" ) || file.endsWith( ".jpg" ) ) {
                file = file.substring( 0, file.length() - 4 ) + ".bin";
            }
        }
    }

    private static void commands( String input ) throws IOException {
        switch ( input ) {
            case "/ansi" -> {
                if ( file != null ) {
                    ANSI.drawImage( file );
                } else {
                    Log.write( "Файл не найден", "WARN" );
                }
            }
            case "/ansi2x" -> {
                if ( file != null ) {
                    ANSI.draw2xHeightImage( file );
                } else {
                    Log.write( "Файл не найден", "WARN" );
                }
            }
            default -> Log.write( "Команда \"" + input + "\" не найдена", "INFO" );
        }
    }
}
