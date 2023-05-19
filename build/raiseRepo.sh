#!/bin/bash

if [ -z "$workDir" ]; then
  workDir=$(mktemp -d -t raiseRepoXXX)
fi
if [ -z "$dryRun" ]; then
  dryRun=0
fi
if [ -z "$autoMerge" ]; then
  autoMerge=0
fi
if [ -z "$sourceBranch" ]; then
  sourceBranch="master"
fi
if [ -z "$newBranch" ]; then
  uuid=$(date +%s%N)
  newBranch="${sourceBranch}-${uuid}"
fi

function runRepoUpdate {
  currentDir=$(pwd)
  updateAction=$1
  shift
  repos=("$@")

  for repo in "${repos[@]}"; do
    cloneDir="${workDir}/${repo}"
    echo "clone repository ${repo} to ${cloneDir}"
    git clone -q "${repo}" "${cloneDir}"

    cd "${cloneDir}"

    branchExists=$(git ls-remote --heads origin ${sourceBranch})
    if [[ -z ${branchExists} ]]; then
      echo "skipping repository ${repo} because it has no ${sourceBranch} branch"
      continue
    fi

    git checkout -q "${sourceBranch}"
    git checkout -q -b "${newBranch}" 

    ${updateAction}

    if [ "$dryRun" = "0" ]; then
      git push -q -u origin "${newBranch}"

      gh auth login --with-token < ${tokenFile}
      gh pr create --title "${message}" --body "${message}" --base ${sourceBranch}

      if [ "$autoMerge" = "1" ]; then
        gh pr merge
      fi
    fi

  done
  cd "${currentDir}"
}
