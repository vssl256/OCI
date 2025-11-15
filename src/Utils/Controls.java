package Utils;

import Handlers.FileHandler;
import Handlers.ImageParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.Stack;

public class Controls {
    public static String file;

    public static void startLoop( Scanner sc, HttpServer server ) throws IOException {
        while ( sc.hasNext() ) {
            String peek = sc.nextLine();
            if ( peek.isBlank() ) continue;
            String[] args = peek.split( " " );
            if ( peek.startsWith( "/" ) ) {
                commands( args[ 0 ], args );
                continue;
            }
            file = args[ 0 ];
            int w=1,h=1;
            if ( args.length > 1 ) {
                w = Integer.parseInt( args[ 1 ] );
                h = Integer.parseInt( args[ 2 ] );
            }
            FileHandler.setFile( file );
            Converter.convert( file, w, h );
            if ( file.endsWith( ".png" ) || file.endsWith( ".jpg" ) ) {
                file = file.substring( 0, file.length() - 4 ) + ".bin";
            }
        }
    }

    private static void commands( String input, String[] args ) throws IOException {
        if ( args.length < 1 ) return;
        switch ( input ) {
            case "/ansi" -> {
                if ( file != null ) {
                    if ( file.endsWith( ".png" ) || file.endsWith( ".jpg" ) ) {
                        file = file.substring( 0, file.length() - 4 ) + ".bin";
                    }
                    ANSI.drawImage( file );
                } else {
                    Log.write( "Файл не найден", "WARN" );
                }
            }
            case "/ansi2x" -> {
                if ( file != null ) {
                    if ( file.endsWith( ".png" ) || file.endsWith( ".jpg" ) ) {
                        file = file.substring( 0, file.length() - 4 ) + ".bin";
                    }
                    ANSI.draw2xHeightImage( file );
                } else {
                    Log.write( "Файл не найден", "WARN" );
                }
            }
            case "/connect" -> {
                try {
                    String filename = null;
                    if ( args[ 1 ].equals( "-r" ) ) {
                        filename = ImageParser.connect( args[ 2 ] );
                    } else filename = ImageParser.connect( new URI ( args[ 1 ] ) );

                    if ( filename != null ) {
                        file = filename;
                        FileHandler.setFile( file );
                        int w = 1, h = 1;
                        if ( args.length > 4 && args[ 3 ].equals( "-s" ) ) {
                            w = Integer.parseInt( args[ 4 ] );
                            h = Integer.parseInt( args[ 5 ] );
                        } else if ( args[ 2 ].equals( "-s" ) ) {
                            w = Integer.parseInt( args[ 3 ] );
                            h = Integer.parseInt( args[ 4 ] );
                        }
                        Converter.convert( filename, w, h );
                    }
                } catch ( InterruptedException | URISyntaxException e ) {
                    throw new RuntimeException( e );
                }
            }
            case "/selected" -> {
                Log.write( "Selected: " + file, "INFO" );
            }
            default -> Log.write( "Команда \"" + input + "\" не найдена", "INFO" );
        }
    }
}
