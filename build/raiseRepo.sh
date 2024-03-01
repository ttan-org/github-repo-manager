#!/bin/bash

if [ -z "$workDir" ]; then
  workDir=$(mktemp -d -t raiseRepoXXX)
fi
if [ "$DRY_RUN" = false ]; then
  echo ""; echo "This is NOT a DRY RUN! We will push to the origin Repos!"; echo "";
else
  echo ""; echo "This is a DRY RUN! Nothing is pushed!"
  echo "Change it by setting the variable 'DRY_RUN' to 'false' before executing this script. ('export DRY_RUN=false')"; echo ""
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

  echo ""
  echo "Run on the follwoing "${#repos[@]}" repos:"
  for repo in "${repos[@]}"; do
   echo " - ${repo}"
  done
  echo ""

  for repo in "${repos[@]}"; do
    echo "==> start with repo '${repo}'"
    
    branchExists=$(git ls-remote --heads ${repo} refs/heads/${sourceBranch})
    if [[ -z ${branchExists} ]]; then
      echo "--> skipping repo '${repo}' because it has no '${sourceBranch}' branch"; echo ""
      continue
    fi

    cloneDir="${workDir}/${repo}"
    echo "git: clone branch '${sourceBranch}' of repo '${repo}' to '${cloneDir}'"
    git clone -b ${sourceBranch} -q "${repo}" "${cloneDir}"

    cd "${cloneDir}"

    echo "git: create new branch '${newBranch}'"
    git checkout -q -b "${newBranch}" 

    skipReason=""
    ${updateAction}
    if [[ -n ${skipReason} ]]; then
      echo "--> skipping repo '${repo}' because: ${skipReason}"; echo ""
      continue
    fi

    if [ "$DRY_RUN" = false ]; then
      git push -q -u origin "${newBranch}"

      gh auth login --with-token < ${tokenFile}
      gh pr create --fill --base ${sourceBranch}

      if [ "$autoMerge" = "1" ]; then
        gh pr merge --merge
      fi
    fi

    echo ""; echo "--> finished with repo '${repo}'"; echo ""
  done
  cd "${currentDir}"
}
