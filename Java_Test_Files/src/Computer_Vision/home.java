package Computer_Vision;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class home {

	public static void main(String[] args) throws IOException {
		// TODO: Retrieve images from Glass
		File fi = new File("/Users/arjit/Downloads/4signs.jpg");
		byte[] fileContent = Files.readAllBytes(fi.toPath());
        
        JSONObject object_dict = sample.get_objects(fileContent);
        JSONArray categories = (JSONArray) object_dict.get("categories");
        
        boolean has_text = helper.has_text(categories);
        
        if(has_text) {
        	JSONObject text_dict = ocr.get_text(fileContent);
        	System.out.println(text_dict);
        }
	}

}
