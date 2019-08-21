package ch.loewenfels.issuetrackingsync.settings;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Settings {
    private IssueTrackingApplication rtc;
    private IssueTrackingApplication jira;

    /**
     * c'tor required for Jackson de-/serialization
     */
    public Settings() {
    }

    public static Settings loadFromFile(final String fileLocation, final ObjectMapper objectMapper) {
        final File settingsFile = new File(fileLocation);
        try {
            if (!settingsFile.exists()) {
                throw new IOException("Settings file " + settingsFile.getAbsolutePath() + " not found.");
            }
            return objectMapper.readValue(settingsFile, Settings.class);
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load settings", ex);
        }
    }

    public IssueTrackingApplication getRtc() {
        return rtc;
    }

    public void setRtc(final IssueTrackingApplication rtc) {
        this.rtc = rtc;
    }

    public IssueTrackingApplication getJira() {
        return jira;
    }

    public void setJira(final IssueTrackingApplication jira) {
        this.jira = jira;
    }
}
