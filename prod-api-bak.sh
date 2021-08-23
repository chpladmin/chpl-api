#!/bin/bash
#(set -o igncr) 2>/dev/null
set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

function doWeContinue {
    echo "Do you want to continue?"
    select yn in "Yes" "No"; do
        case $yn in
            Yes ) break;;
            No ) exit;;
        esac
    done
}

# Abort the script on any failure
set -e

# Make sure user is ready to start
git status
echo "=============="
echo "You're about to start the PRODUCTION deployment. Make sure your working tree is clean"
echo "Git status will not change at this time"
echo "=============="
doWeContinue

git checkout hot-fix
git pull ahrq hot-fix
git checkout staging
git pull ahrq staging
git pull upstream staging
git fetch upstream --tags
git fetch ahrq --tags

./generate-release-notes.sh

RELEASE_NOTES=./STAGED_RELEASE_NOTES.md
if [[ ! -f $RELEASE_NOTES ]]; then
    echo "No STAGED_RELEASE_NOTES file was found. Nothing to release."
    echo "git checkout staging"
    git checkout staging
    exit 0;
fi

# Set up variables
GIT_DATE=`date "+%d %B %Y"`
REL_DATE=`date "+%-d %B %Y"`
OLD_VER=$(sed -ne "0,/## Version/s/## Version \(.*\)/\1/p" RELEASE_NOTES.md)
NEW_VER=$(sed -ne "s/## Version \(.*\)/\1/p" STAGED_RELEASE_NOTES.md)

cat STAGED_RELEASE_NOTES.md
head RELEASE_NOTES.md
echo "These are the variables pulled from the system. Verify they are correct and expected."
echo "GIT_DATE: $GIT_DATE"
echo "REL_DATE: $REL_DATE"
echo "OLD_VER: $OLD_VER"
echo "NEW_VER: $NEW_VER"
printf "\n********************************************************************************\nLast chance to abort before changes are made\n********************************************************************************\n\n"
doWeContinue

# Insert new release notes
sed -i '/Release Notes/r./STAGED_RELEASE_NOTES.md' RELEASE_NOTES.md

# Update Date in Release Notes
sed -i 's/Date TBD/'"$REL_DATE"'/' RELEASE_NOTES.md

# Update Version in SwaggerConfig
sed -i "0,/$OLD_VER/s/$OLD_VER/$NEW_VER/" chpl/chpl-api/src/main/java/gov/healthit/chpl/SwaggerConfig.java

# Allow editing of RELEASE_NOTES for cleanup as needed
nano RELEASE_NOTES.md

# Git commands
echo "git checkout -b ver-$NEW_VER"
git checkout -b ver-$NEW_VER
echo "rm STAGED_RELEASE_NOTES.md"
rm STAGED_RELEASE_NOTES.md
echo "git add ."
git add .
echo "git diff --staged"
git diff --staged
doWeContinue
echo "git commit -m \"release: deploy version $NEW_VER on $GIT_DATE\"" -n
git commit -m "release: deploy version $NEW_VER on $GIT_DATE" -n
echo "git checkout production"
git checkout production
echo "git merge --no-ff ver-$NEW_VER"
git merge --no-ff ver-$NEW_VER
echo "git tag -a \"v$NEW_VER\" -m \"$GIT_DATE\""
git tag -a "v$NEW_VER" -m "$GIT_DATE"
echo "git checkout hot-fix"
git checkout hot-fix
echo "git merge production"
git merge production
echo "git checkout staging"
git checkout staging
echo "git merge production"
git merge production
echo "git push origin production"
git push origin production
echo "git push upstream production staging"
git push upstream production staging
echo "git push ahrq production staging hot-fix"
git push ahrq production staging hot-fix
echo "git push origin --tag"
git push origin --tag
echo "git push upstream --tag"
git push upstream --tag
echo "git push ahrq --tag"
git push ahrq --tag
echo "git branch -d ver-$NEW_VER"
git branch -d ver-$NEW_VER
