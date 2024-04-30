import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class Start {
    public static String projectName = "BOOKKEEPER"; // "ZOOKEEPER" or "BOOKKEEPER"
    public static String repositoryPath = "C:\\Users\\cinel\\Desktop\\Università\\Progetti\\Bookkeeper";
    // C:\Users\cinel\Desktop\Università\Progetti\Bookkeeper
    // C:\Users\cinel\Desktop\Università\Progetti\Zookeeper
    public static String csvFilePath = "./BOOKKEEPERVersionInfo.csv"; // Imposta il percorso del file CSV contenente le informazioni sulle versioni
    // "./ZOOKEEPER_VersionInfo.csv"

    public static void main(String[] string) throws IOException, GitAPIException {
        //RetrieveReleaseInfo.getReleaseInfo(projectName); // Crea il CSV
        //RetrieveTicketsID.getTicketsID(projectName);     // Stampa tutti gli id dei ticket di tipo bug
        RetrieveLOCForVersions.getLOCForVersions(repositoryPath,csvFilePath);
    }

}
