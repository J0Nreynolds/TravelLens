package Computer_Vision;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
}
