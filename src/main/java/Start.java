import entity.ClassLOC;
import entity.VersionInfo;
import exception.NameProjectError;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Start {
    private static final ProjectName projectName = ProjectName.BOOKKEEPER; // "ZOOKEEPER" or "BOOKKEEPER"
    public static final String prefixNameVersione = "release-";
    private static String datasetName;
    private static final String splitChar = ",";

    public static void main(String[] string) {

        datasetName = projectName + "Dataset.csv";
        if(!initializeDataset(datasetName)){
            return;
        }

        try {
            RetrieveReleaseInfo.getReleaseInfo(projectName, getVersionInfoFilePath()); // Crea il CSV con le informazioni sulle versioni
            RetrieveTicketsID.getTicketsID(projectName);  // Crea e popola la entity TicketsIDBuggy id dei ticket di tipo bug

            popolateDataset();

        } catch (NameProjectError e){
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tronca il dataset se già esiste e inserisce header */
    private static Boolean initializeDataset(String csvDatasetName) {
        if (!deleteIfExists(csvDatasetName)) {
            return false;
        }
        FileWriter writer = null;

        try {
            writer = new FileWriter(csvDatasetName);
            writer.append("Version");
            writer.append(splitChar);
            writer.append("File Name");
            writer.append(splitChar);
            writer.append("LOC");
            writer.append("\n");

        } catch (IOException e) {
            System.out.println("Errore nell'inizializzazione del dataset " + e.getMessage());
        } finally {

            assert writer != null;
            try{
                writer.close();
            }
            catch (IOException e){
                System.out.println("An error occurred while closing the dataset: " + e.getMessage());
            }
        }
        return true;
    }

    private static void popolateDataset() throws NameProjectError {
        try {
            // Leggo il file CSV per ottenere le versioni
            List<VersionInfo> versions = readVersionsFromCSV(getVersionInfoFilePath());

            // Qui creo la entity lista di commit
            ComputeCommitAssignment commit = new ComputeCommitAssignment();
            commit.getCommitForVersion(getLocalRepositoryPath(), versions);

            if (!versions.isEmpty()) { // Se ci sono delle versioni:
                for (VersionInfo version : versions) {

                    // Checkout della versione specificata
                    if (!checkoutVersion(getLocalRepositoryPath(), version.getVersionName())) {
                        continue;
                    }

                    executeOperationForVersion(version);
                }
            } else {
                System.out.println("Nessuna versione trovata nel file CSV");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Il file " + datasetName + " non esiste");
        } catch (IOException e) {
            System.out.println("Errore durante la lettura del file CSV: " + e.getMessage());
        } catch (GitAPIException e) {
            System.out.println("Errore durante il checkout della versione: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void executeOperationForVersion(VersionInfo version){
        // Ottenere il numero di righe di codice per ogni classe nella versione specificata
        List<ClassLOC> locDataList = RetrieveLOCForVersions.getLOCForVersion(getLocalRepositoryPath());

        // Scrivi i dati sulle LOC su un file CSV
        writeLOCDataToCSV(version.getVersionID(), locDataList);
    }

    /** Metodo strettamente dipendente da come è costruito VersionInfo, legge il file csv e crea una lista di VersionInfo*/
    private static List<VersionInfo> readVersionsFromCSV(String csvFilePath) {
        System.out.println("Version Found:");

        List<VersionInfo> versions = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Salta l'intestazione
                }

                String[] parts = line.split(splitChar);
                if (parts.length >= 3) {
                    int versionID = Integer.parseInt(parts[0]);
                    String versionName = parts[2];
                    Date versionDate = parts[3].isEmpty() ? null : dateFormat.parse(parts[3]);
                    versions.add(new VersionInfo(versionName, versionID, versionDate));
                }
            }
        } catch (IOException e) {
            System.out.println("Errore durante la lettura del file CSV: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Errore durante il parsing della data: " + e.getMessage());
        }
        return versions;
    }

    /** Esegue il checkout della versione passata come argomento (checkout: va nella versione specificata) */
    //BUG:
    // Errore in checkoutVersion: Checkout conflict with files:
    // docs/zookeeperProgrammers.pdf
    private static boolean checkoutVersion(String repositoryPath, String versionName) {
        System.out.println("Check-Out Version: " + versionName);

        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(repositoryPath + "/.git")).build();
             Git git = new Git(repository)) {
            git.checkout().setName(prefixNameVersione+versionName).call();
            return true; // Checkout true: la versione è stata trovata
        } catch (GitAPIException | IOException e) {
            System.out.println("Errore in checkoutVersion: " + e.getMessage() + "versionError " + versionName);
            return false;
        }
    }


    private static String getLocalRepositoryPath() {
        String localRepositoryPath = null;
        if(Objects.equals(projectName, ProjectName.BOOKKEEPER)){
            localRepositoryPath = "C:/Users/cinel/Desktop/Università/ISW2/ProgettoFalessi/bookkeeper";

        } else if (Objects.equals(projectName, ProjectName.ZOOKEEPER)) {
            localRepositoryPath = "C:/Users/cinel/Desktop/Università/ISW2/ProgettoFalessi/zookeeper";

        }
        return localRepositoryPath;
    }

    private static String getVersionInfoFilePath() {
        String csvFilePath = ""; // Imposta il percorso del file CSV contenente le informazioni sulle versioni
        if(Objects.equals(projectName, ProjectName.BOOKKEEPER) || Objects.equals(projectName,  ProjectName.ZOOKEEPER)){
            csvFilePath = projectName + "VersionInfo.csv";
        }
        return csvFilePath;
    }

    private static Boolean deleteIfExists(String filePath){
        File file = new File(filePath);
        if (file.exists()) {
            if(!file.delete()){
                System.out.println("Errore nella cancellazione del file " + file);
                return false;
            }
        }
        return true;
    }

    private static void writeLOCDataToCSV(int versionID, List<ClassLOC> locDataList) {

        try (FileWriter writer = new FileWriter(datasetName, true)) {
            for (ClassLOC locData : locDataList) {
                writer
                        .append(String.valueOf(versionID))
                        .append(splitChar)
                        .append(locData.getFileName())
                        .append(splitChar)
                        .append(String.valueOf(locData.getLoc()))
                        .append("\n");
            }
        } catch (IOException e) {
            System.out.println("Errore durante la scrittura dei dati sulle LOC nel file CSV: " + e.getMessage());
        }
    }

}
