# Xray source-code snippets

[![license](https://img.shields.io/badge/License-BSD%203--Clause-green.svg)](https://opensource.org/licenses/BSD-3-Clause)
[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/Xray-App/community)

This repo contains several source-code snippets that show how to invoke [Xray Test Management for Jira](https://getxray.app) using one of the available APIs (see References at bottom), in different languages and, eventually, different HTTP libraries.

The purpose is to facilitate building out integrations with Xray. Nevertheless, since the open-source community is very active, please check first if there is something already available that meets your needs. There are many packages/libraries already available ([some contributions from the community for Xray server/DC](https://docs.getxray.app/display/XRAY/Integrations+from+the+community+and+other+products)).

Feel free to copy and adapt the code to your own needs.

## Background

### Xray server/DC vs Xray Cloud

Although similar, Xray for Jira server/data center (DC) and Xray for Jira Cloud are different products, essentially because they are built on top of different infrastructure and application capabilities. Jira server/datacenter and Jira Cloud are distinct product, with different roadmaps, built using distinct technologies, providing also different APIs. This has a consequence that apps for Jira server/DC and for Jira Cloud are essentially totally different from an architecture standpoint, but eventually also from a feature perspective.

## Use cases

- [Import test automation results](use_cases/import_automation_results/README.md)


## Contact

Any questions related with this code, please raise issues in this GitHub project. Feel free to contribute and submit PR's.
For Xray specific questions, please contact [Xray's support team](https://jira.getxray.app/servicedesk/customer/portal/2).

## References

- [Xray server/DC REST API](https://docs.getxray.app/display/XRAY/REST+API)
- [Xray cloud REST API](https://docs.getxray.app/display/XRAYCLOUD/REST+API)
- [Xray cloud GraphQL API](https://docs.getxray.app/display/XRAYCLOUD/GraphQL+API)


## LICENSE

[BSD 3-Clause](LICENSE)
