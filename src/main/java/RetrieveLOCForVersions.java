import entity.ClassLOC;
import entity.VersionInfo;
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


    /** @getLOCForVersion: Questo metodo ottiene il numero di righe di codice per ogni file Java
     * in una specifica versione. Per ogni commit nel repository, ottiene l'albero del commit e
     * percorre l'albero per trovare i file Java. Per ogni file Java, calcola il numero di righe
     * di codice e aggiunge queste informazioni a una lista di oggetti LOCData.*/
    private static List<ClassLOC> getLOCForVersionCommit(String repositoryPath) {

        List<ClassLOC> locDataList = new ArrayList<>();
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
                            locDataList.add(new ClassLOC(fileName, loc)); // Passa solo il nome del file
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
    public static List<ClassLOC> getLOCForVersion(String repositoryPath) {
        List<ClassLOC> locDataList = new ArrayList<>();
        File projectDir = new File(repositoryPath);

        try {
            Files.walk(projectDir.toPath())
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            long loc = Files.lines(path).count();
                            String className = path.toString();

                            className = className
                                    .replaceFirst(repositoryPath + "/", "") // Rimuovi il percorso del repository
                                    .replace(".java", "") // Rimuovi l'estensione del file
                                    .replace("/", "."); // Sostituisci i separatori di percorso con punti
                            locDataList.add(new ClassLOC(className, (int) loc));
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + path);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error walking the file tree: " + e.getMessage());
        }
        return locDataList;
    }
}