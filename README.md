# Synchronizing issues between multiple tracking platforms

This tool synchronizes issues between [Atlassian JIRA](https://www.atlassian.com/software/jira) and [IBM RTC](https://jazz.net/products/rational-team-concert/). Synchronisation is configured field-per-field in one JSON file. The tool runs as a small web server to allow for processing of (JIRA) webhook calls. 

## Configuration

The application will look for a file named application.properties or application.yml in

    * a /config subdirectory of the current directory
    * the current directory

Place a file in either of the two locations, and define the path to your settings JSON:

```
sync:
  settingsLocation: /opt/issue-tracking-sync/config/settings.json
```

Then, define the settings.json. An example file can be found



## For contributors