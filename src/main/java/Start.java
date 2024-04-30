import exception.NameProjectError;

import java.io.IOException;
import java.util.Objects;

public class Start {
    public static ProjectName projectName = ProjectName.BOOKKEEPER; // "ZOOKEEPER" or "BOOKKEEPER"
    public static void main(String[] string) {
        try {
            RetrieveReleaseInfo.getReleaseInfo(projectName, getCsvFilePath()); // Crea il CSV

            RetrieveTicketsID.getTicketsID(projectName);     // Stampa tutti gli id dei ticket di tipo bug

            RetrieveLOCForVersions.getLOCForVersions(getLocalRepositoryPath(), getCsvFilePath());
        } catch (NameProjectError e){
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getLocalRepositoryPath() throws NameProjectError {
        String localRepositoryPath;
        if(Objects.equals(projectName, ProjectName.BOOKKEEPER)){
            localRepositoryPath = "C:/Users/cinel/Desktop/Università/ISW2/ProgettoFalessi/bookkeeper";

        } else if (Objects.equals(projectName,  ProjectName.ZOOKEEPER)) {
            localRepositoryPath = "C:/Users/cinel/Desktop/Università/ISW2/ProgettoFalessi/zookeeper";

        } else {
            throw new NameProjectError("Il nome del progetto è errato");
        }
        return localRepositoryPath;
    }


    private static String getCsvFilePath() throws NameProjectError {
        String csvFilePath; // Imposta il percorso del file CSV contenente le informazioni sulle versioni
        if(Objects.equals(projectName, ProjectName.BOOKKEEPER) || Objects.equals(projectName,  ProjectName.ZOOKEEPER)){
            csvFilePath = projectName+ "VersionInfo.csv";
        } else {
            throw new NameProjectError("Il nome del progetto è errato");
        }
        return csvFilePath;
    }

}
