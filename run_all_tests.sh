#!/bin/bash

sbt clean scalastyle coverage test component:test coverageReport dependencyUpdates
