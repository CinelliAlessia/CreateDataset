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
import java.util.*;

public class RetrieveLOCForVersions {
    public static void getLOCForVersions(String repositoryPath, String csvFilePath) throws IOException, GitAPIException {
        List<VersionInfo> versions = readVersionsFromCSV(csvFilePath);

        if (!versions.isEmpty()) {
            for (VersionInfo version : versions) {
                // Checkout della versione specificata
                System.out.println("Check-Out Version: " + version.getVersionName());
                if (!checkoutVersion(repositoryPath, version.getVersionName())) {
                    System.out.println("Errore durante il checkout della versione: " + version.getVersionName());
                    continue;
                }
                // Ottenere il numero di righe di codice per ogni classe nella versione specificata
                List<LOCData> locDataList = getLOCForVersion(repositoryPath);
                // Scrivi i dati sulle LOC su un file CSV
                writeLOCDataToCSV(version.getVersionName(), locDataList);
            }
        } else {
            System.out.println("Nessuna versione trovata nel file CSV");
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

    private static boolean checkoutVersion(String repositoryPath, String versionName) {
        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(repositoryPath + "/.git")).build();
             Git git = new Git(repository)) {
            git.checkout().setName("release-"+versionName).call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<LOCData> getLOCForVersion(String repositoryPath) throws IOException, GitAPIException {
        List<LOCData> locDataList = new ArrayList<>();
        try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(repositoryPath + "/.git")).build()) {
            try (Git git = new Git(repository)) {
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
            }
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
