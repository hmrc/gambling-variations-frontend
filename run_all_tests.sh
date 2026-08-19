#!/usr/bin/env bash
set -v

sbt compile coverage test it/test coverageReport dependencyUpdates