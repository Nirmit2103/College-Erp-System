package edu.univ.erp.service;

import java.nio.file.Path;
import java.util.List;

public interface BackupService {

    Path createBackup() throws Exception;

    void restoreBackup(String backupName) throws Exception;

    List<String> listBackups();

    boolean deleteBackup(String backupName);
}
