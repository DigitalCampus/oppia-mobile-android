package org.digitalcampus.oppia.model;

import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseTransferableFile implements Serializable {

    static final long serialVersionUID = 123456789123456789L;

    public static final String TYPE_COURSE_BACKUP = "backup";
    public static final String TYPE_COURSE_MEDIA = "media";

    private String title;
    private String shortname;
    private Double versionId;
    private String type = TYPE_COURSE_BACKUP;
    private String filename;
    private long fileSize;
    private File file;
    private List<String> relatedMedia;


    public List<String> getRelatedMedia() {

        return (relatedMedia != null) ?
                relatedMedia
                : new ArrayList<String>();
    }

    public void setRelatedMedia(List<String> relatedMedia) {
        this.relatedMedia = relatedMedia;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public Double getVersionId() {
        return versionId;
    }

    public void setVersionId(Double versionId) {
        this.versionId = versionId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDisplayFileSize(){
        return FileUtils.readableFileSize(fileSize);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof CourseTransferableFile){
            CourseTransferableFile other = (CourseTransferableFile) o;
            return other.getFilename().equals(this.filename);
        }
        return false;
    }
}
