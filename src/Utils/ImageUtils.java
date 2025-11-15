package Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static void split( BufferedImage srcImg, int columns, int rows, String path ) throws IOException {
        int srcWidth = srcImg.getWidth();
        int srcHeight = srcImg.getHeight();

        int chunkWidth = 135;
        int chunkHeight = 100;
        if ( srcWidth / columns < chunkWidth || srcHeight / rows < chunkHeight  ) {
            Log.write( "Image are too small: " + srcWidth / columns + "x" + srcHeight / rows, "INFO" );
            return;
        }

        String folderPath = "output/" + path;
        File folder = new File( folderPath + "/temp" );

        if ( !folder.exists() ) folder.mkdirs();

        for ( int row = 0; row < rows; row++ ) {
            int y = chunkHeight * row;
            for ( int column = 0; column < columns; column++ ) {
                int x = chunkWidth * column;
                BufferedImage chunk = srcImg.getSubimage( x, y, chunkWidth, chunkHeight );
                int i = column + columns * row + 1;
                String output = folderPath + "/temp/" + i + ".png";
                ImageIO.write( chunk, "png", new File( output ) );
            }
        }
    }

    public static BufferedImage crop( BufferedImage original, double targetRatio ) {
        int width = original.getWidth();
        int height = original.getHeight();

        int newWidth = width, newHeight = height;
        int x = 0;
        int y = 0;

        double currentRatio = ( double ) width / height;

        if ( currentRatio > targetRatio ) {
            newWidth = ( int ) ( height * targetRatio );
            x = ( width - newWidth ) / 2;
        } else {
            newHeight = ( int ) ( width / targetRatio );
            y = ( height - newHeight ) / 2;
        }

        return original.getSubimage( x, y, newWidth, newHeight);
    }
}
