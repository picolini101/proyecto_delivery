#!/bin/sh
# Lab 9: every module must have the release version and must not depend on a SNAPSHOT.
# The version comes from the tag (vX.Y.Z) or, on branches, from RELEASE_VERSION.
# It prints what each team still has to change.
VERSION="${CI_COMMIT_TAG:-$RELEASE_VERSION}"
VERSION="${VERSION#v}"
status=0
for module in core-module user-module catalog-module order-module delivery-module payment-module delivery-system; do
  pom="$module/pom.xml"
  own=$(sed -n "/<artifactId>$module<\/artifactId>/{n;s/.*<version>\(.*\)<\/version>.*/\1/p;q;}" "$pom")
  if [ "$own" != "$VERSION" ]; then
    echo "$pom: version $own, expected $VERSION"
    status=1
  fi
  if grep -q 'SNAPSHOT' "$pom"; then
    grep -n 'SNAPSHOT' "$pom" | sed "s#^#$pom:#"
    status=1
  fi
done
[ "$status" -eq 0 ] && echo "All modules are at version $VERSION."
exit "$status"
