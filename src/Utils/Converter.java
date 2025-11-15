package Utils;

import Quantization.KMeans;
import Quantization.Palette;
import Quantization.Pixel;

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

    public static synchronized void convert( String imagePath, int screenWidth, int screenHeight ) throws IOException {
        if ( !imagePath.endsWith( ".png" ) && !imagePath.endsWith( ".jpg" ) ) {
            System.out.println( "Not an image format" );
            return;
        };
        String file = imagePath.substring( 0, imagePath.length() - 4 );
        imagePath = "input/" + imagePath;

        String croppedPath = "output/debug/" + file + "cropped.png";
        outputPath = "output/" + file + ".bin";
        resizedPath = "output/debug/" + file + "_resized.png";

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        File imageFile = new File( imagePath );
        if ( !imageFile.exists() ) { System.out.println( "Cannot find file" ); return; }

        BufferedImage ogImage = ImageIO.read( imageFile );

        int targetWidth = 135 * screenWidth;
        int targetHeight = 100 * screenHeight;

        double targetRatio = ( double ) targetWidth / targetHeight;
        BufferedImage croppedImage = ImageUtils.crop( ogImage, targetRatio );
        ImageIO.write( croppedImage, "png", new File( croppedPath ) );

        int width = Math.min( targetWidth, croppedImage.getWidth() );
        int height = Math.min( targetHeight, croppedImage.getHeight() );
        BufferedImage bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        graphics2D.drawImage( croppedImage, 0, 0, width, height, null );
        graphics2D.dispose();

        if ( screenWidth != 1 || screenHeight != 1 ) {
            ImageUtils.split( bufferedImage, screenWidth, screenHeight, file );
            convertDir( file );
            return;
        }

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

    public static void convertDir( String name ) throws IOException {

        String path = "output/" + name + "/";
        String inPath = path + "/temp/";
        String outPath = path + "/output/";

        File inDir = new File( inPath );
        File outDir = new File( outPath );
        if ( !inDir.exists() ) inDir.mkdirs();
        if ( !outDir.exists() ) outDir.mkdir();

        File[] chunks = inDir.listFiles();

        for ( int i = 0; i < chunks.length; i++ ) {
            File chunk = chunks[i];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            outputPath = outPath + chunk.getName().substring( 0, chunk.getName().lastIndexOf( '.' ) ) + ".bin";

            BufferedImage image = ImageIO.read( chunk );
            Raster raster = image.getRaster();

            Palette.initDef();

            pixels.clear();
            read( raster );

            KMeans kMeans = new KMeans( pixels );
            List<Pixel> palette = kMeans.run( 20000 );
            Palette.print( palette );

            header( 135, 100, palette, buffer );
            convert( raster, buffer );
            writeOutput( buffer );
        }
    }

    public static void header( int width, int height, List<Pixel> pallete, ByteArrayOutputStream buffer ) throws IOException {
        byte wHighByte = ( byte )( ( width >> 8 ) & 0xFF);
        byte wLowByte = ( byte ) ( width & 0xFF );
        buffer.write( wHighByte );
        buffer.write( wLowByte );

        byte hHighByte = ( byte ) ( ( height >> 8 ) & 0xFF );
        byte hLowByte = ( byte ) ( height & 0xFF );
        buffer.write( hHighByte );
        buffer.write( hLowByte );

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

    private static void convert( Raster rasterImage, ByteArrayOutputStream buffer ) throws IOException {
        for ( int y = 0; y < rasterImage.getHeight(); y += 2 ) {
            for ( int x = 0; x < rasterImage.getWidth(); x++ ) {

                int[] upperPixelColor = rasterImage.getPixel( x, y, ( int[] ) null );
                byte[] upperColorByte = toByte( upperPixelColor );

                int[] lowerPixelColor = rasterImage.getPixel( x, y + 1, ( int[] ) null );
                byte[] lowerColorByte = toByte( lowerPixelColor );

                buffer.write( upperColorByte );
                buffer.write( lowerColorByte );
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

    public static void reConvert( int width, int height ) throws IOException {
        BufferedImage reConverted = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        FileInputStream converted = new FileInputStream( outputPath );
        byte[] buffer = converted.readAllBytes();
        converted.close();

        int index = 52;
        for ( int y = 0; y < height; y += 2 ) {
            for ( int x = 0; x < width; x++ ) {

                int ur = buffer[ index++ ] & 0xFF;
                int ug = buffer[ index++ ] & 0xFF;
                int ub = buffer[ index++ ] & 0xFF;

                int lr = buffer[ index++ ] & 0xFF;
                int lg = buffer[ index++ ] & 0xFF;
                int lb = buffer[ index++ ] & 0xFF;

                int urgb = ( ur << 16 ) | ( ug << 8 ) | ub;
                int lrgb = ( lr << 16 ) | ( lg << 8 ) | lb;

                reConverted.setRGB( x, y, urgb );
                reConverted.setRGB( x, y + 1, lrgb );
            }
        }
        ImageIO.write( reConverted, "png", new File( resizedPath ) );
    }
}