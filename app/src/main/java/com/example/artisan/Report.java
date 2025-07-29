package com.example.artisan;

public class Report {
    private String reason;
    private String type;
    private String reportedId;
    private String reportedBy;

    public Report() {

    }
    public Report(String reason, String type, String reportedId, String reportedBy) {
        this.reason = reason;
        this.type = type;
        this.reportedId = reportedId;
        this.reportedBy = reportedBy;
    }
    public String getReason() {
        return reason;
    }
    public String getType() {
        return type;
    }
    public String getReportedId() {
        return reportedId;
    }
    public String getReportedBy() {
        return reportedBy;
    }
}
