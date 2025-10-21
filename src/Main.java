import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;

public class Main {
    static String imagePath = "test.png";
    static String outputPath = "out.txt";

    public static void main( String[] args ) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        BufferedImage bufferedImage = ImageIO.read( new File( imagePath ) );
        Raster rasterImage = bufferedImage.getRaster();

        convert( rasterImage, buffer );
        writeOutput( buffer );

        reConvert( rasterImage.getWidth(), rasterImage.getHeight() );
    }

    //p.s this method made by chatgpt
    public static void reConvert( int width, int height ) throws IOException {
        BufferedImage reConverted = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        FileInputStream converted = new FileInputStream( outputPath );
        byte[] buffer = converted.readAllBytes();
        converted.close();

        int index = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int r = buffer[index++] & 0xFF;
                int g = buffer[index++] & 0xFF;
                int b = buffer[index++] & 0xFF;

                int rgb = (r << 16) | (g << 8) | b;
                reConverted.setRGB(x, y, rgb);
            }
        }
        ImageIO.write( reConverted, "png", new File( "reconverted.png" ) );
    }

    public static void convert( Raster rasterImage, ByteArrayOutputStream buffer ) throws IOException {
        for ( int x = 0; x < rasterImage.getWidth(); x++ ) {
            for ( int y = 0; y < rasterImage.getHeight(); y++ ) {
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