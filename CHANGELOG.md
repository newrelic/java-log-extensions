# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
