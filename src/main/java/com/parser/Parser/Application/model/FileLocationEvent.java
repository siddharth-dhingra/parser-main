package com.parser.Parser.Application.model;

public class FileLocationEvent {

    private String filePath;
    private String esIndex;
    private String toolName;

    public FileLocationEvent() {}

    public FileLocationEvent(String filePath, String esIndex, String toolName) {
        this.filePath = filePath;
        this.esIndex = esIndex;
        this.toolName = toolName;
    }

    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getEsIndex() {
        return esIndex;
    }
    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
    }

    public String getToolName() {
        return toolName;
    }
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    @Override
    public String toString() {
        return "FileLocationEvent{" +
                "filePath='" + filePath + '\'' +
                ", esIndex='" + esIndex + '\'' +
                ", toolName='" + toolName + '\'' +
                '}';
    }
}
