module issueTrackingSync {
    exports ch.loewenfels.issuetrackingsync.app;
    exports ch.loewenfels.issuetrackingsync.settings;

    opens ch.loewenfels.issuetrackingsync.app to spring.core;

    requires java.annotation;
    requires spring.beans;
    requires spring.core;
    requires spring.context;
    requires spring.web;
    requires transitive spring.webmvc;
    requires transitive spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.jms;
    // 'transitive' needed as our own modules have public methods with parameter types from these modules
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    // spring requires several modules, but as automatic module doesn't resolve dependencies itself
    requires java.sql;
    requires java.instrument;
}