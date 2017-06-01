# Watchdog

This tool is designed to help in a scenario where you have some phenomena to be watched for failures; when a failure is detected, an alarm must be raised. The objects doing this are called *watchdogs*.

The current version includes two watchdog implementations, one for Jenkins builds and one for SonrQube analyses. I am fully aware that Jenkins already provides email alerts, but in the scenario I had to tackle the machine running Jenkins has no access to a mail server; it can instead be reached from a machine, which can send mails on its behalf. This is where the Watchdog tool runs.

Building the tool produces an executable jar wich runs in the background. A tray icon is created to help stopping it.

The tool attempts to be resilient against network failures, both in watching the status of the phenomenon and in sending the alerts. In fact, in my initial use case, the machine running the Watchdog had mutually exclusive access to Jenkins/SonarQube or the mail server, according to its VPN status.

## Execution

Build with

`mvn clean install`

then run the resulting jar with

`java -jar watchdog-1.0.jar -DpropertiesFile=<path-to-configuration-file>`

If you omit the `propertiesFile` property, it will default to `config.properties`.

To start the watchdog in the background, you can either run it with

`javaw -jar watchdog-1.0.jar`

or just double-click on it.

## Configuration file

A configuration file is needed, including some mandatory and some optional properties.

Mandatory properties are:
* `jenkins.base.url` the base url for your Jenkins installation, i.e. the one for its dashboard
* `jenkins.job.name` the name of the Jenkins job to monitor
* `sonarqube.base.url` the base url for your SonarQube installation, i.e. the one for its dashboard
* `sonarqube.project.key` the key of the SonarQube project to monitor
* `mail.smtps.host` your SMTP server hostname
* `mail.username` the username of the account used to send the alert mails
* `mail.password` the password of the account used to send the alert mails
* `mail.sender` the email address that will be sending the alert mails
* `mail.recipients` the comma separated list of recipients for the alert mails

Optional properties are:
* `polling.interval.minutes` the interval between polling cycles, in minutes (defaults to 15)
* `mail.smtps.port` your SMTP server port (defaults to 25)
* `jenkins.file.latest.failure` the name of the file used to store the latest failed Jenkins build (defaults to `jenkinsLatestFailure.ser`)
* `jenkins.mail.file.pending` the name of the file used to store the latest Jenkins alert mail which could not be sent (defaults to `jenkinsPendingEmail.ser`)
* `jenkins.mail.subject` the subject of the Jenkins alert mails (defaults to `Jenkins build failed`)
* `jenkins.mail.body` the body of the Jenkins alert mails, can include a placeholder for the failed Jenkins build url (defaults to `A Jenkins build (%s) has failed, please take action!`)
* `sonarqube.file.latest.failure` the name of the file used to store the latest failed SonarQube analysis (defaults to `sonarLatestFailure.ser`)
* `sonarqube.mail.file.pending` the name of the file used to store the latest SonarQube alert mail which could not be sent (defaults to `sonarPendingEmail.ser`)
* `sonarqube.mail.subject` the subject of the Jenkins alert mails (defaults to `SonarQube quality gate failed`)
* `sonarqube.mail.body` the body of the Jenkins alert mails, can include a placeholder for the failed Jenkins build url (defaults to `A SonarQube project (%s) did not pass the quality gate measures!`)
 
## Disclaimer

This tool is still in an experimental phase, and might include several bugs. It is not guaranteed to be working in any setting dissimilar from the one I developed and tested it in.

The code would definitely benefit from some improvements (e.g. unit testing and an improved CLI allowing to select which watchdogs to activate).
