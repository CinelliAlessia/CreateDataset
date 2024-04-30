import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class RetrieveLOCForVersions {

    public static String prefixNameVersione = "release-"; // Per Bookkeeper

    /** @getLOCForVersion: genera un file CSV a partire dal @localRepositoryPath
     * Strutturato: VERSIONE | NOME CLASSE | LOC */
    public static void getLOCForVersions(String localRepositoryPath, String csvFilePath) {
        try {

            // Leggo il file CSV per ottenere le versioni
            List<VersionInfo> versions = readVersionsFromCSV(csvFilePath);

            if (!versions.isEmpty()) { // Se ci sono delle versioni:
                for (VersionInfo version : versions) {
                    // Checkout della versione specificata
                    System.out.println("Check-Out Version: " + version.getVersionName());
                    if (!checkoutVersion(localRepositoryPath, version.getVersionName())) {
                        System.out.println("Errore durante il checkout della versione: " + version.getVersionName());
                        continue;
                    }
                    // Ottenere il numero di righe di codice per ogni classe nella versione specificata
                    List<LOCData> locDataList = getLOCForVersion(localRepositoryPath);
                    // Scrivi i dati sulle LOC su un file CSV
                    writeLOCDataToCSV(version.getVersionName(), locDataList);
                }
            } else {
                System.out.println("Nessuna versione trovata nel file CSV");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Il file " + csvFilePath + " non esiste");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<VersionInfo> readVersionsFromCSV(String csvFilePath) throws IOException {
        System.out.println("Version Found:");
        List<VersionInfo> versions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Salta l'intestazione
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String versionName = parts[2];
                    System.out.println((versionName));
                    versions.add(new VersionInfo(versionName));
                }
            }
        }
        return versions;
    }

    /** Esegue il checkout della versione passata come argomento (checkout: va nella versione specificata) */
    private static boolean checkoutVersion(String repositoryPath, String versionName) {
        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(repositoryPath + "/.git")).build();
             Git git = new Git(repository)) {
            git.checkout().setName(prefixNameVersione+versionName).call();
            return true; // Checkout true: la versione è stata trovata
        } catch (GitAPIException | IOException e) {
            System.out.println("Errore in checkoutVersion: " + e.getMessage());
            //e.printStackTrace();
            return false;
        }
    }

    /** @getLOCForVersion: Questo metodo ottiene il numero di righe di codice per ogni file Java
     * in una specifica versione. Per ogni commit nel repository, ottiene l'albero del commit e
     * percorre l'albero per trovare i file Java. Per ogni file Java, calcola il numero di righe
     * di codice e aggiunge queste informazioni a una lista di oggetti LOCData.*/
    private static List<LOCData> getLOCForVersionCommit(String repositoryPath) {

        List<LOCData> locDataList = new ArrayList<>();
        try (
            Repository repository = new FileRepositoryBuilder().setGitDir(new File(repositoryPath + "/.git")).build();
            Git git = new Git(repository)) {

            // Ottiene un iterabile di tutti i commit nel repository
            Iterable<RevCommit> commits = git.log().all().call();

            for (RevCommit commit : commits) {

                RevTree tree = commit.getTree();
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    while (treeWalk.next()) {
                        String path = treeWalk.getPathString();
                        if (path.endsWith(".java")) {
                            ObjectId objectId = treeWalk.getObjectId(0);
                            ObjectLoader loader = repository.open(objectId);
                            byte[] bytes = loader.getBytes();
                            String[] lines = new String(bytes).split("\\r?\\n");
                            int loc = lines.length;
                            String fileName = new File(path).getName(); // Ottieni solo il nome del file
                            System.out.println(fileName + "    " + loc);
                            locDataList.add(new LOCData(fileName, loc)); // Passa solo il nome del file
                        }
                    }
                }
            }
        } catch (IOException | GitAPIException e) {
            System.out.println("Errore durante il recupero delle LOC delle versioni: " + e.getMessage());
            e.printStackTrace();
        }
        return locDataList;
    }

    /** La versione corrente del progetto è determinata dallo stato del repository Git al momento in
     * cui il metodo getLOCForVersion viene chiamato. Prima di chiamare getLOCForVersion,
     * il metodo getLOCForVersions esegue il checkout di una specifica versione del progetto
     * utilizzando il comando Git checkout. Questo cambia lo stato del repository Git alla versione
     * specificata. Quindi, quando getLOCForVersion viene chiamato, calcola le LOC per la versione del
     * progetto che è attualmente in checkout */
    private static List<LOCData> getLOCForVersion(String repositoryPath) {
        List<LOCData> locDataList = new ArrayList<>();
        File projectDir = new File(repositoryPath);
        try {
            Files.walk(projectDir.toPath())
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            long loc = Files.lines(path).count();
                            String className = path.toString()
                                    .replaceFirst(repositoryPath + "/", "") // Rimuovi il percorso del repository
                                    .replace(".java", "") // Rimuovi l'estensione del file
                                    .replace("/", "."); // Sostituisci i separatori di percorso con punti
                            System.out.println(className + " : " + loc);
                            locDataList.add(new LOCData(className, (int) loc));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locDataList;
    }

    private static void writeLOCDataToCSV(String versionName, List<LOCData> locDataList) throws IOException {
        String csvFilePath = "./Data-Set.csv";
        try (FileWriter writer = new FileWriter(csvFilePath, true)) {
            for (LOCData locData : locDataList) {
                writer.append(versionName).append(",").append(locData.getFileName()).append(",").append(String.valueOf(locData.getLoc())).append("\n");
            }
        }
    }

    private static class VersionInfo {
        private final String versionName;

        public VersionInfo(String versionName) {
            this.versionName = versionName;
        }

        public String getVersionName() {
            return versionName;
        }
    }

    private static class LOCData {
        private final String className;
        private final int loc;

        public LOCData(String fileName, int loc) {
            this.className = fileName;
            this.loc = loc;
        }

        public String getFileName() {
            return className;
        }

        public int getLoc() {
            return loc;
        }
    }
}
