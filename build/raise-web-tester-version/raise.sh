#!/bin/bash
set -e

#
# Param 1: Release Version (required)
#   e.g.: 9.2.0
# 
# Param 2: Source Branch (required)
#   e.g.: master or release/8.0
#
# Param 3: GitHub Cli token file
#

if [ $# -eq 0 ]; then
  echo "Parameter release version required"
  exit
fi
if [ $# -eq 1 ]; then
  echo "Parameter source branch required"
  exit
fi
if [ $# -eq 2 ]; then
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

newBranch=raise-web-tester-version-${releaseVersion}

# switch to directory of this script
cd "$(dirname "$0")"
source "../raiseRepo.sh"

tmpDirectory=$workDir

function updateSingleRepo {
  .ivy/raise-web-tester.sh ${releaseVersion} ${snapshotVersion} >> 'maven.log'
  git commit -a -m "Raising web-tester version to ${releaseVersion}"
}

function raiseVersionOfOurRepos {
  rm -rf ${tmpDirectory}

  repos=(
    "git@github.com:axonivy/core"
    "git@github.com:axonivy/engine-cockpit"
    "git@github.com:axonivy/dev-workflow-ui"
    "git@github.com:axonivy/project-build-examples"
    "git@github.com:axonivy-market/demo-projects"
  )

  runRepoUpdate 'updateSingleRepo' ${repos[@]}
}

raiseVersionOfOurRepos
