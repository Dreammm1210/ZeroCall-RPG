#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p /tmp/zerocall-rpg-v02-test
javac -encoding UTF-8 -d /tmp/zerocall-rpg-v02-test \
  src/main/java/vn/zerocall/rpg/RpgClasses.java \
  src/main/java/vn/zerocall/rpg/RpgProfile.java \
  tests/RpgProfileTest.java
java -ea -cp /tmp/zerocall-rpg-v02-test RpgProfileTest
