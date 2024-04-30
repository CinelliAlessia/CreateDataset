import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class RetrieveTicketsID {

    public static int maxResults = 1000; // Da jira è possibile prendere massimo 1000 risultati alla volta, nel caso ce ne siano di più
    // (leggere il campo "total") va gestito

    // https://issues.apache.org/jira/rest/api/2/search?jql=project=%22ZOOKEEPER%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt=0&maxResults=1500

    /** Recupera dalla url query tutti gli ID dei ticket di tipo BUG, con stato CLOSED or RESOLVED e risoluzione FIXED
     * Di tutti gli oggetti recuperati, prende i campi: key, resolutiondate, versions */
    public static void getTicketsID(ProjectName projName) throws IOException, JSONException {
        int j, i = 0, total;
        //Get JSON API for closed bugs w/ AV in the project
        do {
            j = i + maxResults;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + projName
                    + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR%22status%22=%22resolved%22)"
                    + "AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + j; // Con questo link prendo i primi 1000 risultati

            // Realizzo un file json dal url
            JSONObject json = CommonResources.readJsonFromUrl(url);
            total = json.getInt("total");

            JSONArray issues = json.getJSONArray("issues"); // Tutto l'array contenente gli issues

            for (; i < total && i < j; i++) { //Iterate through each bug
                String key = issues.getJSONObject(i % maxResults).get("key").toString();
                System.out.println(key);
            }

        } while (i < total); // Continua il ciclo fin quando non sono stati visti tutti i ticket
    }
}