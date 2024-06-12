package entity;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VersionInfo {
    private final String versionName;
    private final int versionID;
    private Date versionDate;
    private final List<RevCommit> commits = new ArrayList<>();

    public VersionInfo(String versionName, int versionID, Date versionDate) {
        this.versionName = versionName;
        this.versionID = versionID;
        this.versionDate = versionDate;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionID() {
        return versionID;
    }

    public void addCommit(RevCommit commit) {
        commits.add(commit);
    }

    public List<RevCommit> getCommits() {
        return commits;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }
}