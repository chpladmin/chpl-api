package gov.healthit.chpl.notifier;

import java.io.File;
import java.util.List;

public interface ChplTeamNotifierMessage {
    String getMessage();
    String getSubject();
    List<File> getFiles();
}
