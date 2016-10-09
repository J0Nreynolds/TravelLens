package Computer_Vision;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class home {

	public static void main(String[] args) throws Exception {
		// TODO: Retrieve images from Glass
		File fi = new File("/Users/arjit/Downloads/4signs.jpg");
		byte[] fileContent = Files.readAllBytes(fi.toPath());
        
        JSONObject object_dict = sample.get_objects(fileContent);
        JSONArray categories = (JSONArray) object_dict.get("categories");
        
        boolean has_text = helper.has_text(categories);
        
        JSONArray translated_dict = new JSONArray();
        if(has_text) {
        	JSONObject text_dict = ocr.get_text(fileContent);
        	translated_dict = helper.add_translations(text_dict);
        }
        
        System.out.println(translated_dict);
	}

}
