#!/bin/bash -e

# removing kie-soup artifacts from local maven repo (basically all possible SNAPSHOTs)
if [ -d $MAVEN_REPO_LOCAL ]; then
    rm -rf $MAVEN_REPO_LOCAL/org/kie/soup
fi

# checkout kie-soup
git clone git@github.com:kiegroup/kie-soup.git --branch $baseBranch --depth 70
# checkout the release branch
cd $WORKSPACE/kie-soup
git checkout -b $releaseBranch $baseBranch

# upgrades the version to the release/tag version
sh scripts/release/update-version.sh $newVersion

# git add and commit the version update changes
git add .
commitMsg="update to version $newVersion"
git commit -m "$commitMsg"

