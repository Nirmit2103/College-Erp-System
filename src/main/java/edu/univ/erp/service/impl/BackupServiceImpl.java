package edu.univ.erp.service.impl;

import edu.univ.erp.service.BackupService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BackupServiceImpl implements BackupService {

    private static final String DATA_FOLDER = "data";
    private static final String BACKUP_FOLDER = "backups";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    @Override
    public Path createBackup() throws Exception {
        Path dataPath = Paths.get(DATA_FOLDER);
        if (!Files.exists(dataPath)) {
            throw new IOException("Data folder does not exist: " + dataPath);
        }

        Path backupsPath = Paths.get(BACKUP_FOLDER);
        if (!Files.exists(backupsPath)) {
            Files.createDirectories(backupsPath);
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupDirName = "backup_" + timestamp;
        Path backupPath = backupsPath.resolve(backupDirName);
        Files.createDirectories(backupPath);

        try (Stream<Path> files = Files.list(dataPath)) {
            files.filter(Files::isRegularFile)
                 .forEach(sourceFile -> {
                     try {
                         Path targetFile = backupPath.resolve(sourceFile.getFileName());
                         Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                     } catch (IOException e) {
                         throw new RuntimeException("Failed to copy file: " + sourceFile, e);
                     }
                 });
        }

        return backupPath;
    }

    @Override
    public void restoreBackup(String backupName) throws Exception {
        Path backupPath = Paths.get(BACKUP_FOLDER, backupName);
        if (!Files.exists(backupPath) || !Files.isDirectory(backupPath)) {
            throw new IOException("Backup not found: " + backupName);
        }

        Path dataPath = Paths.get(DATA_FOLDER);
        if (!Files.exists(dataPath)) {
            Files.createDirectories(dataPath);
        }

        try (Stream<Path> files = Files.list(backupPath)) {
            files.filter(Files::isRegularFile)
                 .forEach(sourceFile -> {
                     try {
                         Path targetFile = dataPath.resolve(sourceFile.getFileName());
                         Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                     } catch (IOException e) {
                         throw new RuntimeException("Failed to restore file: " + sourceFile, e);
                     }
                 });
        }
    }

    @Override
    public List<String> listBackups() {
        Path backupsPath = Paths.get(BACKUP_FOLDER);
        if (!Files.exists(backupsPath)) {
            return new ArrayList<>();
        }

        try (Stream<Path> entries = Files.list(backupsPath)) {
            return entries
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("backup_"))
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteBackup(String backupName) {
        Path backupPath = Paths.get(BACKUP_FOLDER, backupName);
        if (!Files.exists(backupPath)) {
            return false;
        }

        try {

            try (Stream<Path> files = Files.list(backupPath)) {
                files.forEach(file -> {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {

                    }
                });
            }

            Files.deleteIfExists(backupPath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
