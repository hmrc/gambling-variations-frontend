#!/usr/bin/env bash
set -v

sbt scalafmtCheckAll
msgman verify
