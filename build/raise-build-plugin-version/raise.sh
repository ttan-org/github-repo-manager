#!/bin/bash
set -e

#
# Param 1: Release Version (required)
#   e.g.: 9.2.0
# 
# Param 2: Snapshot Version (required)
#   e.g.: 9.2.0-SNAPSHOT
# 
# Param 3: Source Branch (required)
#   e.g.: master or release/8.0
#
# Param 4: Token File for auth of gh cli (required)
#   e.g.: ~/.gh-token-file
#

if [ $# -eq 0 ]; then
  echo "Parameter release version required"
  exit
fi
if [ $# -eq 1 ]; then
  echo "Parameter snapshot version required"
  exit
fi
if [ $# -eq 2 ]; then
  echo "Parameter source branch required"
  exit
fi
if [ $# -eq 3 ]; then
  echo "Parameter tokenFile required"
  exit
fi

releaseVersion=$1
echo "raise release version to ${releaseVersion}"

snapshotVersion=$2
echo "raise snapshot version to ${snapshotVersion}"

sourceBranch=$3
echo "source branch to checkout repos ${sourceBranch}"

tokenFile=$4
echo "token file to auth for gh cli ${tokenFile}"

uuid=$(date +%s%N)
newBranch=raise-project-build-plugin-version-${uuid}

# switch to directory of this script
cd "$(dirname "$0")"
source "../raiseRepo.sh"

tmpDirectory=$workDir

function updateSingleRepo {
  .ivy/raise-build-plugin-version.sh ${releaseVersion} ${snapshotVersion} >> 'maven.log'
  git commit -a -m "Raising project-build-plugin version to ${releaseVersion} / ${snapshotVersion}"
}

function raiseVersionOfOurRepos {
  rm -rf ${tmpDirectory}

  repos=(
    "git@github.com:axonivy-market/demo-projects.git"
    "git@github.com:axonivy/project-build-examples.git"
    "git@github.com:axonivy/performance-tests.git"
    "git@github.com:axonivy/primefaces-themes.git"
    "git@github.com:axonivy/web-tester.git"
    "git@github.com:axonivy/engine-cockpit.git"
    "git@github.com:axonivy/dev-workflow-ui.git"
    "git@github.com:axonivy/process-editor-client.git"
    "git@github.com:axonivy/inscription-client.git"
    "git@github.com:axonivy/cluster-tests.git"
    "git@github.com:axonivy/core"
  )

  runRepoUpdate 'updateSingleRepo' ${repos[@]}
}

raiseVersionOfOurRepos
