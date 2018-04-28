package org.digitalcampus.oppia.model;

public class CourseBackup {
    private String shortname;
    private Double versionId;
    private String filename;
    private double fileSize;

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

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }


}
