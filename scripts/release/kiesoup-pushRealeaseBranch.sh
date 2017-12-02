#!/bin/bash -e

# pushes the release-branche to github.com:jboss-integration or github.com:kiegroup/kie-soup [IMPORTANT: "push -n" (--dryrun) should be replaced by "push" when script is finished and will be applied]
if [ "$target" == "community" ]; then
   git push origin $releaseBranch
else
   git remote add upstream git@github.com:jboss-integration/kie-soup.git
   git push upstream $releaseBranch
fi