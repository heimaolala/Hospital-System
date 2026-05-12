package com.hospital.config;

import org.springframework.stereotype.Component;

@Component
public class AutoAuditConfig {
    private int maxDays = 30;
    private int maxQuota = 15;
    private boolean autoApprovePatient = false;
    private boolean autoApproveDoctor = false;

    public int getMaxDays() { return maxDays; }
    public void setMaxDays(int maxDays) { this.maxDays = maxDays; }
    public int getMaxQuota() { return maxQuota; }
    public void setMaxQuota(int maxQuota) { this.maxQuota = maxQuota; }
    public boolean isAutoApprovePatient() { return autoApprovePatient; }
    public void setAutoApprovePatient(boolean autoApprovePatient) { this.autoApprovePatient = autoApprovePatient; }
    public boolean isAutoApproveDoctor() { return autoApproveDoctor; }
    public void setAutoApproveDoctor(boolean autoApproveDoctor) { this.autoApproveDoctor = autoApproveDoctor; }
}
