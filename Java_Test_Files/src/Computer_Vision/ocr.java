package Computer_Vision;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ocr 
{
    public static JSONObject get_text(byte[] image) 
    {
        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/vision/v1.0/ocr");


            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "a8e94b0447b54941b0b5edf708503114");

            InputStream myInputStream = new ByteArrayInputStream(image); 
            // Request body
            InputStreamEntity reqEntity = new InputStreamEntity(myInputStream);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) 
            {
            	JSONObject result = (JSONObject) JSONValue.parse(EntityUtils.toString(entity));
                return result;
            }
            return null;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
