# GitHub Repo Manager

Managing settings of GitHub repositories.

## Setup

Place your GitHub OAuth Token in the file
`github.token` in the root of this project
and run the java application.

## New GitHub repositories

If the GitHub Repo Manager need to include 
a new repository, the following three files 
need to be updated manually:
- [github-repo-manager/src/main/java/com/axonivy/github/GitHubRepos.java](github-repo-manager/src/main/java/com/axonivy/github/GitHubRepos.java#L34C1-L50C37)
- [build/raise-deps/raise.sh](build/raise-deps/raise.sh#L41C2-L51C4)
- [build/raise-version/raise.sh](build/raise-version/raise.sh#L44C2-L66C4)