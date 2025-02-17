package com.parser.Parser.Application.model;

public class FileLocationEvent {

    private String tenantId;   
    private String filePath;
    private ToolType toolName;
    private String jobId;

    public FileLocationEvent() {}

    public FileLocationEvent(String tenantId, String filePath, ToolType toolName) {
        this.tenantId = tenantId;
        this.filePath = filePath;
        this.toolName = toolName;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public ToolType getToolName() {
        return toolName;
    }
    public void setToolName(ToolType toolName) {
        this.toolName = toolName;
    }

    public String getJobId() {
        return jobId;
    }
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public String toString() {
        return "FileLocationEvent{" +
                "tenantId='" + tenantId + '\'' +
                ", filePath='" + filePath + '\'' +
                ", toolName='" + toolName + '\'' +
                ", jobId='" + jobId + '\'' +
                '}';
    }
}
