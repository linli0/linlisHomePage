#!/usr/bin/env bash
# Run AFTER changing GitHub default branch to `master` in repo settings.
set -euo pipefail

echo "Checking remote default branch..."
default=$(gh api repos/linli0/linlisHomePage -q '.default_branch' 2>/dev/null || echo "unknown")

if [[ "$default" != "master" ]]; then
  echo "Default branch is still '$default', not 'master'." >&2
  echo "Change it first: https://github.com/linli0/linlisHomePage/settings/branches" >&2
  exit 1
fi

echo "Deleting remote main..."
git push origin --delete main

echo "Cleaning up local main..."
git branch -D main 2>/dev/null || true
git fetch origin --prune
git symbolic-ref refs/remotes/origin/HEAD refs/remotes/origin/master

echo "Done. Remaining branches:"
git branch -a
