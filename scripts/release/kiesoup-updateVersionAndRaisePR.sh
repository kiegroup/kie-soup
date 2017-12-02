#!/bin/bash -e

targetUser=kiereleaseuser
remoteUrl=git@github.com:kiereleaseuser/kie-soup.git
DATE=$(date "+%Y-%m-%d")

# clone the repository and branch for kie-soup
#IMOPRTANT: this is only important for community branches after a release
git clone git@github.com:kiegroup/kie-soup.git --branch $baseBranch
cd $WORKSPACE/kie-soup
prBranch=kiesoup-upgrade-version-$DATE-$baseBranch
git checkout -b $prBranch $baseBranch
git remote add $targetUser $remoteUrl

# upgrades the version to next development version of kie-soup
sh scripts/release/update-version.sh $newVersion

# git add and commit the version update changes
git add .
commitMsg="upgraded version to $newVersion"
git commit -m "$commitMsg"

# do a build of kie-soup
mvn -B -e -U clean install -Dmaven.test.failure.ignore=true -Dgwt.memory.settings="-Xmx2g -Xms1g -XX:MaxPermSize=256m -XX:PermSize=128m -Xss1M"

# Raise a PR
source=kie-soup
git push $targetUser $prBranch
hub pull-request -m "$commitMsg" -b $source:$baseBranch -h $targetUser:$prBranch