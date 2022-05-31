#!/bin/bash
set -e

#
# Param 1: Version (required)
#   e.g.: 9.2.0-SNAPSHOT
# 
# Param 2: Source Branch (required)
#   e.g.: master or release/8.0
#
# Param 3: dry run (optional)
#   e.g.: --dry-run
#

if [ $# -eq 0 ]; then
  echo "Parameter version required"
  exit
fi
if [ $# -eq 1 ]; then
  echo "Parameter source branch required"
  exit
fi

newVersion=$1
echo "raise version to ${newVersion}"

sourceBranch=$2
echo "source branch to checkout repos $2"

dryRun=0
if [ $# -eq 3 ]; then
  if [ $3 = "--dry-run" ]; then
    dryRun=1
  fi
fi
echo "dry run ${dryRun}"

uuid=$(date +%s%N)
newBranch=raise-version-${newVersion}-${uuid}

# switch to directory of this script
cd "$(dirname "$0")"

source "../raiseRepo.sh"

function raiseVersionOfOurRepos {
  repos=(
    "git@github.com:axonivy-market/demo-projects.git"
    "git@github.com:axonivy/glsp-editor-client.git"
    "git@github.com:axonivy/branding-images.git"
    "git@github.com:axonivy/doc-images.git"
    "git@github.com:axonivy/case-map-ui.git"
    "git@github.com:axonivy/primefaces-themes.git"
    "git@github.com:axonivy/engine-cockpit.git"
    "git@github.com:axonivy/dev-workflow-ui.git"
    "git@github.com:axonivy/webeditor.git"
    "git@github.com:axonivy/rules.git"
    "git@github.com:axonivy/glsp-editor.git"
    "git@github.com:axonivy/core.git"
  )
  runRepoUpdate 'updateSingleRepo' ${repos[@]}
}

function updateSingleRepo {
  ./raise-version.sh ${newVersion}
  git commit -a -m "Raise version to ${newVersion}"
}

raiseVersionOfOurRepos
