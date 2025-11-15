package Handlers;

import Utils.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class ImageParser  {

    public static String connect( URI uri ) throws IOException, InterruptedException, URISyntaxException {
        URL url = uri.toURL();

        HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
        conn.setInstanceFollowRedirects( false );
        String type = conn.getContentType();

        if ( type == null ) type = "image/png";

        InputStream is = conn.getInputStream();
        Reader reader = new InputStreamReader( is );

        String filename = Paths.get( uri.getPath() ).getFileName().toString();

        if ( type.equals( "application/json" ) ) {

            JsonArray jsonArray = JsonParser.parseReader( reader ).getAsJsonArray();
            JsonObject json = jsonArray.get( 0 ).getAsJsonObject();
            String filePath = json.get( "file_url" ).getAsString();
            filename = json.get( "id" ).getAsString();

            uri = new URI( filePath );
            URL fileURL = uri.toURL();
            conn = ( HttpURLConnection ) fileURL.openConnection();
            type = conn.getContentType();
        }
        System.out.println("is " + type);
        BufferedImage image = null;
        if ( isImage( type ) ) {
            image = ImageIO.read( conn.getInputStream() );
        } else return null;

        if ( !filename.contains( "." ) ) filename = filename + ".png";

        ImageIO.write( image, "png", new File( "input/" + filename ) );

        return filename;
    }

    public static String connect( String id ) throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URI( getR34( id ) );
        return connect( uri );
    }

    private static final String[] IMAGE_FORMATS = { "png", "jpg", "jpeg", "webp" };
    private static boolean isImage( String type ) {
        for ( String format : IMAGE_FORMATS ) {
            if ( type.contains( format ) ) return true;
        }
        return false;
    }

    private static final String API_KEY = "api_key=003fbe5184f2dc4309f65597f9ed5915d9bf0e65bc10efe17a2c08db604afee4bdb86aabf9c4865f0192a862234a7b6a63f406b6bc972b07c89041374f12420e";
    private static final String USER_ID = "user_id=3289693";
    private static String getR34( String id ) {
        String uri = "https://api.rule34.xxx/index.php?page=dapi&s=post&q=index&&json=1&" +
                API_KEY + "&" + USER_ID +
                "&id=" + id;
        return uri;
    }
}
