# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 3.1.0
* Exception Stack Traces
  * Capture the full (nested) stack trace for exceptions.  
  * Add configuration option to allow unlimited stack trace size, by setting the maximum stack trace option to `0`.
    * This can be configured via
      * System property: `-Dnewrelic.log_extension.max_stack_size=0`
      * Environment variable: `NEW_RELIC_LOG_EXTENSION_MAX_STACK_SIZE=0`
* Logback MDC (Mapped Diagnostic Context)
  * Fix bug where MDC values were not retained when using the extension with Logback v1.4.8 and higher.  

## 3.0.0
* Exception Stack Trace Size - this has been changed from a default of `10` to `300`. This applies to all logging libraries supported by the `java-log-extension` project.
    * This is configurable via:
      * System property: `-Dnewrelic.log_extension.max_stack_size=integer`
      * Environment Variable: `NEW_RELIC_LOG_EXTENSION_MAX_STACK_SIZE=integer`
* Mapped Diagnostic Context (MDC) - decorating logs with MDC data is now generally supported but disabled by default. MDC keys will be prefixed by `context.` to prevent clashes with New Relic specific attributes. This applies to all logging libraries supported by the `java-log-extension` project, except for Java Util Logging (JUL) which does not provide an MDC mechanism. 
  * This is configurable via:
      * System property: `-Dnewrelic.log_extension.add_mdc=boolean`
      * Environment Variable: `NEW_RELIC_LOG_EXTENSION_ADD_MDC=boolean`
  * Note: This is considered a breaking change as previously some of the logging libraries automatically added MDC. If you upgrade to this version of the `java-log-extension` and wish to have MDC added to your logs then you will need to explicitly enable it. Currently, this will add all MDC as filtering out specific keys is not yet supported.

## 2.6.0
* Removed the log forwarder. Please use the [New Relic Java Agent](https://github.com/newrelic/newrelic-java-agent) if you want log forwarding to be used.
* Upgrade Gradle to version 7.5.1

## 2.5.0
* Add logback marker to JSON layout if present
* Update to TelemetrySDK 0.13.2 to address [CVE-2022-25647](https://github.com/advisories/GHSA-4jrv-ppp4-jm57)

## 2.4.0
- Add a `forwarder` module that does log forwarding using the [New Relic Java Telemetry SDK](https://github.com/newrelic/newrelic-telemetry-sdk-java)
- Implement `com.newrelic.logging.logback.NewRelicHttpAppender` to do log forwarding for logback
- Update `log4j2` dependencies to version `2.17.2`
- Update Java agent API dependencies to version `7.6.0`

## 2.3.2
- Update documentation to specify support for Log4J 2.13.2 or higher
- Add warning for CVE-2021-44228
- Update log4j compile time dependencies to use 2.17.1
- Update examples runtime dependency on log4j to use 2.17.1
- Update newrelic-api.jar so that transitive log4j dependencies use the latest approved log4j version for agent apis.
- 
## 2.3.1
- Updates log4j compile time dependencies to use 2.16. While these are not used at runtime, we are updating dependency versions to avoid any concern in the community that the vulnerability from CVE-2021-44228 exists in the jar for this extension.

- This release also bumps the newrelic-api:7.4.2 jar which coincides with the same patch for log4j in the newrelic-java-agent.

- This point release also updates the log4j dependency version for examples included in the repository, which in fact did use a bundled log4j.

- Update gradle version

## 2.3
-Add support for custom arguments in the JSON log for logback.


## 2.2 (2021-04-21)
- Make [maxStackSize configurable](https://github.com/newrelic/java-log-extensions/pull/30). Thank you longwa!
- [Use singleton JsonFactory instance](https://github.com/newrelic/java-log-extensions/pull/32). Thank you gstoupis!

## 2.1 (2020-08-27)
- Add support for Dropwizard request logging

## 2.0 (2020-07-20)
- log4j2 minimum version updated to 2.13.1
- add `ContextDataProvider` and remove requirement of setting `messageFactory`
- jackson minimum version updated to 2.11.1

## 1.0-rc2 (2019-10-03)
### log4j2
- Reduce requirement to 2.8 from 2.12
- Address a potential concurrency issue

### logback
- Reduce requirement to 1.2.0 from 1.2.3

## 1.0-rc1  (2019-09-18)
### Initial public release of the extensions
- Support for decorating logs with New Relic metadata.
