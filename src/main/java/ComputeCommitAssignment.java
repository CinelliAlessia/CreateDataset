import entity.VersionInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ComputeCommitAssignment {

    // Metodo per ottenere i commit per una specifica versione
    public void getCommitForVersion(String localRepository, List<VersionInfo> versions) throws IOException, GitAPIException {

        // Crea un oggetto Repository per il repository Git locale
        Repository repository = new FileRepositoryBuilder().setGitDir(new File(localRepository + "/.git")).build();
        Git git = new Git(repository);

        // Controlla se il repository è vuoto o in uno stato inconsistente
        if (repository.getRefDatabase().getRefs().isEmpty()) {
            System.out.println("Il repository è vuoto o in uno stato inconsistente.");
            return;
        }

        // Controlla se HEAD è staccato
        if (repository.getFullBranch() == null) {
            System.out.println("Il riferimento HEAD del repository è in uno stato staccato.");
            return;
        }

        // Ottiene tutti i commit del repository
        Iterable<RevCommit> commits = git.log().all().call();
        for (RevCommit commit : commits) {
            // Verifica se il commit appartiene a una delle versioni
            isCommitInVersion(commit, versions);
        }

        // Chiude il repository
        repository.close();
    }

    // Metodo per verificare se un commit appartiene a una delle versioni
    private void isCommitInVersion(RevCommit commit, List<VersionInfo> versions) {
        VersionInfo nextVersion = null;

        // Itera su tutte le versioni in ordine inverso
        for (int i = versions.size() - 1; i >= 0; i--) {
            VersionInfo currentVersion = versions.get(i);
            // Se il commit corrisponde alla versione corrente e alla versione successiva, lo aggiunge alla versione successiva
            if (corrispondenzaVersione(commit, currentVersion, nextVersion)) {
                nextVersion.addCommit(commit);
                System.out.println("Commit " + commit.getName() + " added to version " + nextVersion.getVersionName());
            }
            // Imposta la versione corrente come versione successiva per il prossimo ciclo
            nextVersion = currentVersion;
        }
    }

    // Metodo per verificare se un commit corrisponde a una versione specifica
    private boolean corrispondenzaVersione(RevCommit commit, VersionInfo versionCurrent, VersionInfo versionNext) {
        // Converte il timestamp del commit in un oggetto Date
        Date commitDate = new Date(commit.getCommitTime() * 1000L);

        // Se non esiste una versione successiva, ritorna false
        if(versionNext == null){
            return false;
        }

        // Controlla se la data del commit è compresa tra la data della versione corrente e quella della versione successiva
        return commitDate.after(versionCurrent.getVersionDate()) && commitDate.before(versionNext.getVersionDate());
    }

}