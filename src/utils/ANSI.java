package utils;

import quantization.Pixel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;

public class ANSI {
    private static final List<Pixel> pixels = new ArrayList<>();
    private static int width, height;

    private static void draw2Pixels( Pixel up, Pixel dwn ) {
        int upR = up.getR(), upG = up.getG(), upB = up.getB();
        int dwnR = dwn.getR(), dwnG = dwn.getG(), dwnB = dwn.getB();
        out.print("\u001B[38;2;" + upR + ";" + upG + ";" + upB +
                ";48;2;" + dwnR + ";" + dwnG + ";" + dwnB + "m▀\u001B[0m");
    }

    private static void draw1Pixel( Pixel pixel ) {
        out.print( "\u001B[48;2;" + pixel.getR() + ";" + pixel.getG() + ";" + pixel.getB() + "m \u001B[0m" );
    }

    public static void draw2xHeightImage( String path ) throws IOException {
        readImage( "output/" + path );
        for ( int y = 0; y < height; y++ ) {
            for ( int x = 0; x < width; x ++ ) {
                Pixel pixel = pixels.get( x + width * y );
                draw1Pixel( pixel );
            }
            out.println();
        }
    }

    public static void drawImage( String path ) throws IOException {
        readImage( "output/" + path );
        for ( int y = 0; y < height - 1; y += 2 ) {
            for ( int x = 0; x < width; x++ ) {
                int idx = x + y * width;
                Pixel pixel = pixels.get( idx );
                Pixel below = pixels.get( idx + width );
                draw2Pixels( pixel, below );
            }
            out.println();
        }
    }

    private static void readImage( String path ) throws IOException {
        pixels.clear();

        byte[] buffer;
        try ( FileInputStream image = new FileInputStream( path ); ) {
            buffer = image.readAllBytes();
        } catch ( IOException e ) {
            Log.write( "Error occurred during image reading: " + e.getMessage(), "ERROR" );
            return;
        }

        width = buffer[ 0 ] & 0xFF;
        height = buffer[ 1 ] & 0xFF;

        int index = 50;
        for ( int y = 0; y < height; y++ ) {
            for ( int x = 0; x < width; x++ ) {
                int r = buffer[ index++ ] & 0xFF;
                int g = buffer[ index++ ] & 0xFF;
                int b = buffer[ index++ ] & 0xFF;
                Pixel pixel = new Pixel( r, g, b );
                pixels.add( pixel );
            }
        }
    }
}
