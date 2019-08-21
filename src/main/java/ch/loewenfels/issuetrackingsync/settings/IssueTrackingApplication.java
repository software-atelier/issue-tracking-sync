package ch.loewenfels.issuetrackingsync.settings;

public class IssueTrackingApplication {
    private String name;
    private String username;
    private String password;
    private String endpoint;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }
}
