import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RetrieveReleaseInfo {

	public static HashMap<LocalDateTime, String> releaseNames;
	public static HashMap<LocalDateTime, String> releaseID;
	public static ArrayList<LocalDateTime> releases;
	public static Integer numVersions;

	/** getReleaseInfo Crea il file CSV con index, version ID, versionName, Date*/
	public static void getReleaseInfo(ProjectName projName, String csvPath) throws IOException, JSONException {

		//Fills the arraylist with releases dates and orders them
		//Ignores releases with missing dates
		releases = new ArrayList<>();

		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		JSONObject json = CommonResources.readJsonFromUrl(url);

		// Versions è un array che contiene campi di una singola versione
		JSONArray versions = json.getJSONArray("versions");

		releaseNames = new HashMap<>(); // es. 4.0.0
		releaseID = new HashMap<> (); // id univoco identificativo della versione

		for (int i = 0; i < versions.length(); i++ ) { // Ciclo su tutte le versioni estrapolate da jira
			String name = "";
			String id = "";

			// Se la versione i-esima ha una data controlla i campi, se ci sono tutti lo aggiungi alla coda delle versioni valide
			if(versions.getJSONObject(i).has("releaseDate")) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name, id);
			}
		}

		// L'Array releases ora è popolato, lo ordiniamo in base alla data
		releases.sort(new Comparator<>() {
            @Override
            public int compare(LocalDateTime o1, LocalDateTime o2) {
                return o1.compareTo(o2);
            }
        });

		// Se ho meno di 6 release
		if (releases.size() < 6){
			System.out.println("Il progetto valutato ha meno di sei versioni");
			return;
		}

		FileWriter fileWriter = null;
		try {

            // Inizializzazione della prima riga del file
			fileWriter = new FileWriter(csvPath);
			fileWriter.append("Index,Version ID,Version Name,Date");
			fileWriter.append("\n");

			numVersions = releases.size(); // Numero di versioni totali

			// Popolazione del file con i campi estrapolati
			for (int i = 0; i < releases.size(); i++) {
				int index = i + 1;
				fileWriter.append(Integer.toString(index));
				fileWriter.append(",");
				fileWriter.append(releaseID.get(releases.get(i)));
				fileWriter.append(",");
				fileWriter.append(releaseNames.get(releases.get(i)));
				fileWriter.append(",");
				fileWriter.append(releases.get(i).toString());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			System.out.println("Error in csv writer");
			e.printStackTrace();
		} finally {
			try {
				assert fileWriter != null;
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter");
				e.printStackTrace();
			}
		}
    }

	public static void addRelease(String strDate, String name, String id) {

		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();

		if (!releases.contains(dateTime))
			releases.add(dateTime);

		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
    }

}