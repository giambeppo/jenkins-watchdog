# Jenkins Watchdog

This tool is designed to help in a scenario where the machine running Jenkins has no access to a mail server, but it can be reached from a machine which can send mails on its behalf.

Building the tool produces an executable jar wich runs in the background. A tray icon is created to help stopping it.

The tool attempts to be resilient against network failures, both in retrieving Jenkins status and in sending alert mails. In fact, in my initial use case, the machine running the watchdog had mutually exclusive access to Jenkins or the mail server, according to its VPN status.

## Execution

Build with

`mvn clean install`

then run the resulting jar with

`java -jar jenkins-watchdog-1.0.jar -DpropertiesFile=<path-to-configuration-file>`

If you omit the `propertiesFile` property, it will default to `config.properties`.

To start the watchdog in the background, you can either run it with

`javaw -jar jenkins-watchdog-1.0.jar`

or just double-click on it.

## Configuration file

A configuration file is needed, including some mandatory and some optional properties.

Mandatory properties are:
* `jenkins.base.url` the base url for your Jenkins installation, i.e. the one for its dashboard
* `jenkins.job.name` the name of the Jenkins job to monitor
* `mail.smtps.host` your SMTP server hostname
* `mail.username` the username of the account used to send the alert mails
* `mail.password` the password of the account used to send the alert mails
* `mail.sender` the email address that will be sending the alert mails
* `mail.recipients` the comma separated list of recipients for the alert mails

Optional properties are:
* `polling.interval.minutes` the interval between polling cycles, in minutes (defaults to 15)
* `jenkins.file.latest.failure` the name of the file used to store the latest failed build (defaults to `latestFailure.ser`)
* `mail.file.pending` the name of the file used to store the latest alert mail which could not be sent (defaults to `pendingEmail.ser`)
* `mail.smtps.port` your SMTP server port (defaults to 25)
* `mail.subject` the subject of the alert mails (defaults to `Jenkins build failed`)
* `mail.body` the body of the alert mails, can include a placeholder for the failed build url (defaults to `A Jenkins build (%s) has failed, please take action!`)

## Disclaimer

This tool is still in an experimental phase. It is not guaranteed to be working in any setting dissimilar from the one I developed it for.

The code would definitely benefit from unit testing and a revised, more extensible architecture, which I might or might not work on in the near future.
