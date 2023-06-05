package org.digitalcampus.oppia.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OfflineCourseFile {

    public enum FileType {COURSE, MEDIA, INVALID}
    public enum Status {SELECTED, IMPORTING, IMPORTED}
    private static final String MODULES_XML_PATH = ".*/module.xml";
    private static final String MEDIA_REGEX = ".*\\.(?:mp4|m4v|mpeg|3gp|3gpp)$";

    private final File file;
    private final FileType type;
    private Status status;

    public OfflineCourseFile(File file){
        this.file = file;
        this.type = getFileType(file);
        this.status = Status.SELECTED;
    }

    public FileType getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public void updateStatus(Status newStatus) {
        status = newStatus;
    }

    public Status getStatus() {
        return status;
    }

    private OfflineCourseFile.FileType getFileType(File file){
        OfflineCourseFile.FileType fileType = OfflineCourseFile.FileType.INVALID;
        try {
            InputStream inputStream = new FileInputStream(file);
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.matches(MODULES_XML_PATH) && !entry.isDirectory()) {
                    fileType = OfflineCourseFile.FileType.COURSE;
                    break;
                }

                if (name.matches(MEDIA_REGEX) && !entry.isDirectory()) {
                    fileType = OfflineCourseFile.FileType.MEDIA;
                    break;
                }
            }
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileType;
    }
}
