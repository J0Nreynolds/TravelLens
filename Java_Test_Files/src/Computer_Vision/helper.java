package Computer_Vision;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class helper {
	public static boolean has_text(JSONArray categories) {
		for(int i=0; i < categories.size(); i++) {
			JSONObject current = (JSONObject) categories.get(i);
			String tag = (String) current.get("name");
			if(tag.toLowerCase().contains("text")) {
				double score = (double) current.get("score");
				if(score>=.2) {
					return true;
				} else {
					System.out.println("text but not prominent");
				}
			}
		}
		return false;
	}
	
	public static JSONArray add_translations(JSONObject text_dict) throws Exception {
		JSONArray translated_dict = new JSONArray();
        JSONArray regions = (JSONArray) text_dict.get("regions");
        for(int i = 0; i < regions.size(); i++) {
        	String original = "";
        	JSONObject region = (JSONObject) regions.get(i);
        	JSONArray lines = (JSONArray) region.get("lines");
        	for(int j=0; j< lines.size(); j++) {
        		JSONObject line = (JSONObject) lines.get(j);
            	JSONArray words = (JSONArray) line.get("words");
            	for(int k = 0; k < words.size(); k++) {
            		JSONObject word = (JSONObject) words.get(k);
            		original += (word.get("text") + " ");
            	}
        	}
        	
        	Translate.setClientId("mhacks2016couchsquad");
        	Translate.setClientSecret("ZDlol0NO05RJeClPXuam5YY9JRRi6bI3PcXyjF/kpWk=");
        	String translated = Translate.execute(original, Language.AUTO_DETECT, Language.ENGLISH);
        	region.put("translation", translated);

        	translated_dict.add(i, region);
        }
		return translated_dict;
	}
}
