[![Community Plus header](https://github.com/newrelic/opensource-website/raw/master/src/images/categories/Community_Plus.png)](https://opensource.newrelic.com/oss-category/#community-plus)

# New Relic logging extensions

The New Relic logging plugins are extensions for common Java logging frameworks. They are designed to capture app,
transaction trace, and span information as part of your application log messages.

For the latest information, please see the [New Relic Documentation](https://docs.newrelic.com/docs/logs/new-relic-logs/enable-logs-context/enable-logs-context-java).

We support:

* [java.util.logging](jul/README.md)
* [Apache Log4j 2.13.2 or higher](log4j2/README.md)
  * We strongly suggest that log4j 2.15.x or higher be used to avoid [CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228)
* [Apache Log4j 1.x](log4j1/README.md)
* [Logback 1.2](logback/README.md)
* [Logback 1.1](logback11/README.md)
* [Dropwizard 1.3](dropwizard/README.md)

## Support

Should you need assistance with New Relic products, you are in good hands with several diagnostic tools and support channels.

If the issue has been confirmed as a bug or is a Feature request, please file a Github issue.

**Support Channels**

* [New Relic Documentation](https://docs.newrelic.com/docs/logs/enable-log-management-new-relic/logs-context-java/configure-logs-context-java): Comprehensive guidance for using our platform
* [New Relic Community](https://discuss.newrelic.com/tags/logs): The best place to engage in troubleshooting questions
* [New Relic University](https://learn.newrelic.com/): A range of online training for New Relic users of every level
* [New Relic Technical Support](https://support.newrelic.com/) 24/7/365 ticketed support. Read more about our [Technical Support Offerings](https://docs.newrelic.com/docs/licenses/license-information/general-usage-licenses/support-plan). 

## Contributing

We encourage your [contributions](CONTRIBUTING.md) to improve the log extensions. Keep in mind when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project.
If you have any questions, or to execute our corporate CLA, required if your contribution is on behalf of a company, please drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](https://github.com/newrelic/java-log-extensions/security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).
