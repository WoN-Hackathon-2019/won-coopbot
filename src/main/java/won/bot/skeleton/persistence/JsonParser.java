package won.bot.skeleton.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import won.bot.skeleton.persistence.model.Location;
import won.bot.skeleton.persistence.model.SportPlace;

/**
 * 
 * @author reini
 *
 */
public class JsonParser {

	private String url;

	public JsonParser(String url) {
		this.url = url;
	}

	public Set<SportPlace> parseData() {
		Set<SportPlace> sportPlaces = new HashSet<SportPlace>();

		try {
			JSONObject jsonObject = downloadFromUrl();
			JSONArray features = (JSONArray) jsonObject.get("features");
			Iterator<?> iterator = features.iterator();

			while (iterator.hasNext()) {
				JSONObject element = (JSONObject) iterator.next();
				SportPlace sportPlace = new SportPlace();

				sportPlace.setLocation(extractLocation(element));

				@SuppressWarnings("unchecked")
				Map propertyMap = (HashMap<String, ?>) element.get("properties");
				String category = (String) propertyMap.get("KATEGORIE_TXT");

				if (!category.contains("Öffentlich")) {
					continue;
				}
				sportPlace.setOutdoor(category.contains("outdoor"));
				sportPlace.setWeblink((String) propertyMap.get("WEBLINK1"));
				sportPlace.setAddress((String) propertyMap.get("ADRESSE"));
				sportPlace.getCategory().addAll(extractArten(propertyMap));

				sportPlaces.add(sportPlace);
			}

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return sportPlaces;
	}

	private Collection<? extends String> extractArten(Map propertyMap) {
		String art = (String) propertyMap.get("SPORTSTAETTEN_ART");
		art = Jsoup.clean(art, Whitelist.none());
		art = art.replaceAll("\\s+", " ");
		return Arrays.asList(art.split(","));
	}

	private Location extractLocation(JSONObject element) {
		@SuppressWarnings("unchecked")
		Map geoMap = (HashMap<String, ?>) element.get("geometry");
		JSONArray coord = (JSONArray) geoMap.get("coordinates");

		Location location = new Location();
		location.setLatitude((double) coord.get(0));
		location.setLongitude((double) coord.get(1));
		return location;
	}

	private JSONObject downloadFromUrl() throws MalformedURLException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		URL dataToParse = new URL(url);
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataToParse.openStream()));
		StringBuffer buffer = new StringBuffer();
		int read;
		char[] chars = new char[1024];
		while ((read = reader.read(chars)) != -1) {
			buffer.append(chars, 0, read);
		}

		JSONObject jsonObject = (JSONObject) parser.parse(buffer.toString());
		return jsonObject;
	}
}
