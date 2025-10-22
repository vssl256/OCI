import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;

public class Main {
    static String imagePath = "output/test5.png";
    static String outputPath = imagePath.substring( 0, imagePath.length() - 4 ) + ".oci";
    static String reConvertedPath = imagePath.substring( 0, imagePath.length() - 4 ) + "_reconostruction.png";

    public static void main( String[] args ) throws IOException {
        System.out.println( outputPath );
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        BufferedImage bufferedImage = ImageIO.read( new File( imagePath ) );
        Raster rasterImage = bufferedImage.getRaster();
        int width = rasterImage.getWidth(), height = rasterImage.getHeight();

        header( width, height, buffer );
        convert( rasterImage, buffer );
        writeOutput( buffer );

        reConvert( width, height );
    }

    public static void header( int width, int height, ByteArrayOutputStream buffer ) throws IOException {
        byte wByte = ( byte )width;
        byte hByte = ( byte )height;
        buffer.write( wByte );
        buffer.write( hByte );
    }

    //p.s this method made by chatgpt
    public static void reConvert( int width, int height ) throws IOException {
        BufferedImage reConverted = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        FileInputStream converted = new FileInputStream( outputPath );
        byte[] buffer = converted.readAllBytes();
        converted.close();

        int index = 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = buffer[index++] & 0xFF;
                int g = buffer[index++] & 0xFF;
                int b = buffer[index++] & 0xFF;

                int rgb = (r << 16) | (g << 8) | b;
                reConverted.setRGB(x, y, rgb);
            }
        }
        ImageIO.write( reConverted, "png", new File( reConvertedPath ) );
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

    public static void writeOutput( ByteArrayOutputStream buffer ) throws IOException {
        try ( FileOutputStream fileOutputStream = new FileOutputStream( outputPath ) ) {
            buffer.writeTo( fileOutputStream );
        }
    }

    public static byte[] toByte( int[] input ) {
        return new byte[]{ ( byte )input[0], ( byte )input[1], ( byte )input[2] };
    }
}