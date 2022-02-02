# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
