---
name: xray-entities-representation
description: Works with Xray entities, such as test run results, test step statuses, coverage statuses
---

# Representation of Xray entities

This skill provides information about Xray entities, such as test run results, test step statuses, coverage statuses, and how to represent them.
Always use the same colors whenever representing statuses for the same entities, as described below, to ensure a consistent representation of Xray entities across different outputs and visualizations.

- *always* use the following colors to represent the different statuses of test runs, test steps, and coverage statuses, as described in the sections below, no matter the output format (text, tables, charts, etc.) or the context in which they are presented.

## Test Run results and their statuses

Test results, also known as Test Runs, have a status, which can be one of the following,
- PASSED: green
- FAILED: red
- EXECUTING: yellow
- TO DO: gray
- ABORTED: blue

## Coverage status

Coverage statuses, that are applicable to requirements and other coverable items such as Stories, Epics, can be one of the following ones, where each one is represented by a different color:
- UNCOVERED: blue
- OK: green
- NOK: red
- NOTRUN: yellow
- UNKNOWN: gray