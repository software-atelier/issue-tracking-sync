# Synchronizing issues between multiple tracking platforms

This tool synchronizes issues between [Atlassian JIRA](https://www.atlassian.com/software/jira) and [IBM RTC](https://jazz.net/products/rational-team-concert/). Synchronisation is configured field-per-field in one JSON file. The tool runs as a small web server to allow for processing of (JIRA) webhook calls. 

## Configuration

The application will look for a file named application.properties or application.yml in

    * a /config subdirectory of the current directory
    * the current directory

Place a file in either of the two locations, and define the path to your settings JSON along with application-wide properties:

```
spring:
  security:
    user:
      password: a-better-admin-password

sync:
  settingsLocation: /opt/issue-tracking-sync/config/settings.json
  pollingCron: 0 0/15 0 * * ?
  notificationChannelProperties:
    - classname: ch.loewenfels.issuetrackingsync.notification.SlackChannel
      endpoint: https://hooks.slack.com/services/mywebhookthingy
      username: The sync tool
      subject: jira2rtc
      avatar: ":cyclone:"
```

Then, define the settings.json. An example file can be found [here](https://github.com/loewenfels/issue-tracking-sync/blob/master/src/test/resources/settings.json)

### settings.json

#### trackingApplications

Define the basic issue tracking applications in use in the section "trackingApplications" of the settings.json 

```json
  "trackingApplications": [
    {
      "name": "JIRA",
      "className": "ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient",
      "role": "SLAVE",
      "username": "foobar",
      "password": "mysecret",
      "endpoint": "http://localhost:8080/jira",
      "fieldsHoldingPartnerApplicationKey": {
        "RTC": "custom_field_1004"
      }
    }
    ...
  ]
```

The currently defined client implementations are:
- ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
- ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient

#### syncFlowDefinitions

Individual synchronization streams are defined as "flows". Apart from defining the source and target application
(referring to the trackingApplication#name), the configuration *can* define a filter class, and *must* define an
action class. 

```json
  "syncFlowDefinitions": [
    {
      "name": "Sync changes from RTC to JIRA",
      "source": "RTC",
      "target": "JIRA",
      "filterClassname": "ch.loewenfels.issuetrackingsync.custom.NotClosedFilter",
      "actionClassname": "ch.loewenfels.issuetrackingsync.executor.SimpleSynchronizationAction",
      "keyFieldMappingDefinition": {
        "sourceName": "id",
        "targetName": "custom_field_12044"
      },
      "defaultsForNewIssue": {
        "issueType": "change",
        "project": "TST",
        "category": ""
      },
      "fieldMappingDefinitions": [
        {
          "sourceName": "summary",
          "targetName": "title"
        }
      ]
    }
    ...
  ]
```

Finally, the [fieldMappings] define the synchronization mapping on a per-field level.

## For contributors

This project aims at allowing for simple issue synchronization using a purely configurative approach, while also embracing 
proprietary extensions. The latter can be defined using class FQNs in the settings.json, and making sure the application
finds the class on the classpath (but possibly outside of this project's fat JAR).

### Processing queue

Individual synchronization requests are processed in a queue (backed by ActiveMQ) with concurrency of one. This ensures 
that no two synchronization threads might affect the same items (configure in application.yml).

See the IssuePoller as an entry point, which runs based on a CRON expression. Found issues are then run through
the SynchronizationFlowFactory, and for all issues with a matching flow, a SyncRequest is produced on the queue.

### Processing a single issue

From a SyncRequest, an issue is derived and the matching SynchronizationFlow is retrieved from the 
SynchronizationFlowFactory. As described in [syncFlowDefinitions](#syncflowdefinitions), a SynchronizationFlow can
define an issue filter, and must define a SynchronizationAction class. 

Execution of a flow is delegated to the SynchronizationAction, which:

1. Loads the source issue along with the key (=unique identifier) mapping. This step also verifies that the 
   "last updated" timestamp of the synchronization request matches that of the loaded issue (if not, a 
   SynchronizationAbortedException is thrown)
2. Load all mapped source fields into the Issue object
3. Pass the Issue object to the target client, along with the (nullable) defaults for new issues. If the latter are 
   missing and the target client fails to locate a target issue from the key field mapping, a  
   SynchronizationAbortedException is thrown   