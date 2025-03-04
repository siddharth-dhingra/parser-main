package com.parser.Parser.Application.model;

import java.util.List;

public class NewScanPayload {
    private String tenantId;
    private String jobId;
    private List<String> findingIds;
    private TriggerType triggerType;   
    private String destinationTopic;

    public NewScanPayload() {}

    public NewScanPayload(String tenantId, String jobId, List<String> findingIds, TriggerType triggerType, String destinationTopic) {
        this.tenantId = tenantId;
        this.jobId = jobId;
        this.findingIds = findingIds;
        this.triggerType = triggerType;
        this.destinationTopic = destinationTopic;   
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public List<String> getFindingIds() { return findingIds; }
    public void setFindingIds(List<String> findingIds) { this.findingIds = findingIds; }

    public TriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(TriggerType triggerType) { this.triggerType = triggerType; }

    public String getDestinationTopic() { return destinationTopic; }
    public void setDestinationTopic(String destinationTopic) { this.destinationTopic = destinationTopic; }
}