# This library is currently in early access

# Backtrace Log4j2 support
[Backtrace](http://backtrace.io/) provides support for log4j2 by providing a special appender that can be connected to any application to send logged information also to the Backtrace dashboard. BacktraceAppender sends also MDC properties and using offline database for error report storage and re-submission in case of network outage. 

# Installation via Gradle or Maven<a name="installation"></a>


* Gradle
```
dependencies {
    implementation 'com.github.backtrace-labs.backtrace-log4j2:backtrace-log4j2:0.9.4'
}
```

* Maven
```
<dependency>
  <groupId>com.github.backtrace-labs.backtrace-log4j2</groupId>
  <artifactId>backtrace-log4j2</artifactId>
  <version>0.9.4</version>
</dependency>
```


# Configuration

In order to configure the BacktraceAppender, it is mandatory to set the `submissionUrl` parameter or two parameters such as `endpointUrl` and `submissionToken`. Other parameters are optional.

### Example basic configuration using the log4j.properties
```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" packages="backtrace.io.log4j2">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" />
        </Console>
        <BacktraceAppender name="BacktraceAppender" endpointUrl="https://<yourInstance>.sp.backtrace.io:6098/" submissionToken="<submissionToken>"/>
    </Appenders>
    <Loggers>
        <Logger name="backtrace.io" level="off" additivity="false"/>
        <Root level="debug">
            <AppenderRef ref="Console" />
            <AppenderRef ref="BacktraceAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

### Optional parameters:
- `appVersion` - string value which represents your's application version
- `appName` - string value which represents your's application name
- `enableUncaughtExceptionHandler` - boolean value, if `true` library will catch all uncaught exceptions and send to server
- `useDatabase` - boolean value, default `true` (also if this parameter is not specified) the library will store unsent messages in files, if `false` library will not be using any files to store reports 
- `maxDatabaseSize` - long value, maximum database size in bytes, by default size is unlimited
- `maxDatabaseRecordCount` - integer value, number of times library will try to send the error report again if sending will finished with fail
- `maxDatabaseRetryLimit` - integer value, maximum number of messages in database. If a limit is set, the oldest error reports will be deleted if there will be try to exceed the limit
- `uncaughtExceptionHandlerBlockThread` - boolean value, if `true` thread should be blocked until unhandled exception will be sent
- `awaitMessagesOnClose` - boolean value, if `true` main thread will wait until all messages will be sent before closing Backtrace Client


# Waiting 
By default Backtrace is sending all messages asynchronously and doesn't block main thread. You can wait until all currently messages will be sent with executing method `await`. Optionally as a parameter to `await` method you can pass the maximum time you want to wait for that.

```java
Logger rootLogger = (Logger) LogManager.getRootLogger();
Appender backtraceAppender = (Appender) rootLogger.getAppenders().get("BacktraceAppender");
backtraceAppender.await();
```

# Closing 
To make sure that all resources allocated by the library are released, call the `close` method. This method will send the currently processed message and then free all resources. Below you can find example how to do it.

```java
Logger rootLogger = (Logger) LogManager.getRootLogger();
Appender backtraceAppender = (Appender) rootLogger.getAppenders().get("BacktraceAppender");
backtraceAppender.stop();
```

# Debugging library
If you want to disable all logs which comes from this library add to log4j.properties file following line.

```xml
<Logger name="backtrace.io" level="off" additivity="false"/>
```

You can also set the level with which you want to receive logs from this library, eg. `ERROR`, `INFORM` or `DEBUG`.
```xml
<Logger name="backtrace.io" level="error" additivity="true"/>
```