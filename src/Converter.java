import quantization.KMeans;
import quantization.Palette;
import quantization.Pixel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Converter {
    private static String outputPath;
    private static String resizedPath;

    public static ArrayList<Pixel> pixels = new ArrayList<>();

    public static void convert(String imagePath) throws IOException {
        if ( !imagePath.endsWith( ".png" ) && !imagePath.endsWith( ".jpg" ) ) {
            System.out.println( "Not an image format" );
            return;
        };
        String file = imagePath.substring( 0, imagePath.length() - 4 );
        imagePath = "input/" + imagePath;
        outputPath = "output/" + file + ".bin";
        resizedPath = "debug/" + file + "_resized.png";
        System.out.println( outputPath );
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        File imageFile = new File( imagePath );
        if ( !imageFile.exists() ) { System.out.println( "Cannot find file" ); return; }

        BufferedImage ogImage = ImageIO.read( imageFile );
        int width = 160, height = 50;
        BufferedImage bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        graphics2D.drawImage( ogImage, 0, 0, width, height, null );
        graphics2D.dispose();

        Raster rasterImage = bufferedImage.getRaster();

        Palette.initDef();
        pixels.clear();
        read( rasterImage );
        KMeans kMeans = new KMeans( pixels );
        List<Pixel> palette = kMeans.run( 2_000 );
        Palette.print(palette);

        header( width, height, palette, buffer );
        convert( rasterImage, buffer );
        writeOutput( buffer );

        reConvert( width, height );
    }

    public static void header( int width, int height, List<Pixel> pallete, ByteArrayOutputStream buffer ) throws IOException {
        byte wByte = ( byte )width;
        byte hByte = ( byte )height;
        buffer.write( wByte );
        buffer.write( hByte );
        for ( Pixel p : pallete ) {
            byte rByte = ( byte )p.getR();
            byte gByte = ( byte )p.getG();
            byte bByte = ( byte )p.getB();
            buffer.write( rByte );
            buffer.write( gByte );
            buffer.write( bByte );
        }
    }

    public static void read( Raster rasterImage ) throws  IOException {
        for ( int y = 0; y < rasterImage.getHeight(); y++ ) {
            for ( int x = 0; x < rasterImage.getWidth(); x++ ) {
                int[] pixelColor = rasterImage.getPixel( x, y, ( int[] ) null );
                int r = pixelColor[0], g = pixelColor[1], b = pixelColor[2];
                if ( !inPallete( r, g, b ) ) {
                    Pixel pixel = new Pixel( r, g, b );
                    pixels.add( pixel );
                }
            }
        }
    }

    public static void convert( Raster rasterImage, ByteArrayOutputStream buffer ) throws IOException {
        for ( int y = 0; y < rasterImage.getHeight(); y++ ) {
            for ( int x = 0; x < rasterImage.getWidth(); x++ ) {
                int[] pixelColor = rasterImage.getPixel( x, y, ( int[] ) null );
                byte[] colorsByte = toByte( pixelColor );
                buffer.write( colorsByte );
            }
        }
    }

    public static boolean inPallete( int r, int g, int b ) {
        boolean rIn = false, gIn = false, bIn = false;

        for ( int R : Palette.R )
            if ( r == R ) { rIn = true; break; }
        for ( int G : Palette.G )
            if ( g == G ) { gIn = true; break; }
        for ( int B : Palette.B )
            if ( b == B ) { bIn = true; break; }

        return rIn && gIn && bIn;
    }

    public static void writeOutput( ByteArrayOutputStream buffer ) throws IOException {
        try ( FileOutputStream fileOutputStream = new FileOutputStream( outputPath ) ) {
            buffer.writeTo( fileOutputStream );
        }
    }

    public static byte[] toByte( int[] input ) {
        return new byte[]{ ( byte )input[0], ( byte )input[1], ( byte )input[2] };
    }

    //p.s this method made by chatgpt
    public static void reConvert( int width, int height ) throws IOException {
        BufferedImage reConverted = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        FileInputStream converted = new FileInputStream( outputPath );
        byte[] buffer = converted.readAllBytes();
        converted.close();

        int index = 50;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = buffer[index++] & 0xFF;
                int g = buffer[index++] & 0xFF;
                int b = buffer[index++] & 0xFF;

                int rgb = (r << 16) | (g << 8) | b;
                reConverted.setRGB(x, y, rgb);
            }
        }
        ImageIO.write( reConverted, "png", new File( resizedPath ) );
    }
}