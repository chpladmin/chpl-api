#!/usr/bin/env bash
#
# Guides a developer through a Tomcat version upgrade, following
# "Tomcat version updates" in chpl-documentation/docs/ghcr_container_images.md.
#
# Usage: docker/upgrade-tomcat.sh [new-version]      e.g. docker/upgrade-tomcat.sh 11.0.27
#
# Tomcat is pinned in four places, which must always be equal:
#   1. docker/Dockerfile                  (runtime image used by CI/GHCR)
#   2. Dockerfile                         (local-development image)
#   3. chpl/chpl-service/pom.xml          tomcat-dbcp
#   4. chpl/chpl-service/pom.xml          tomcat-catalina (provided scope)
#
# The config step compares stock Tomcat <current> with stock Tomcat <new> (the three files we
# override are rarely touched upstream) and reports whether anything must be migrated.
#
# Requires: docker, git, sed, awk.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

DOCKERFILE_CI="docker/Dockerfile"
DOCKERFILE_LOCAL="Dockerfile"
POM="chpl/chpl-service/pom.xml"
CONF_DIR="docker/tomcat-conf"
CONF_FILES=(server.xml context.xml catalina.properties)
STOCK_DIR="${TMPDIR:-/tmp}/tomcat-conf-stock"          # stock config of the NEW version
OLD_STOCK_DIR="${TMPDIR:-/tmp}/tomcat-conf-stock-old"   # stock config of the CURRENT version
CONTAINER="tc-stock-$$"
WAR_BUILT="chpl/chpl-api/target/chpl-service.war"
WAR_DEST="docker/build-context/chpl-service.war"

VERSION_RE='[0-9]+\.[0-9]+\.[0-9]+'

function info { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
function warn { printf '\033[33m%s\033[0m\n' "$*"; }
function die  { printf '\033[31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }

function confirm {
    local reply
    read -r -p "$1 [y/N] " reply
    [[ "$reply" =~ ^[Yy]$ ]]
}

function pause {
    read -r -p "$1 Press Enter to continue (Ctrl-C to abort)... " _
}

function cleanup { docker rm -f "$CONTAINER" >/dev/null 2>&1 || true; }
trap cleanup EXIT

# extract_stock <tomcat-version> <dest-dir>: copies that image's stock conf/ directory out of it.
function extract_stock {
    local version="$1" dest="$2"
    rm -rf "$dest"; mkdir -p "$dest"
    docker create --name "$CONTAINER" "tomcat:${version}-jdk21" >/dev/null \
        || die "could not create a container from tomcat:${version}-jdk21"
    docker cp "$CONTAINER:/usr/local/tomcat/conf/." "$dest/"
    docker rm "$CONTAINER" >/dev/null
}

# Git (not diff) because it colors portably, and only on a terminal (--color=auto).
# --ignore-cr-at-eol: Windows checkouts have CRLF here but the stock files are LF.
# Exits 1 when the files differ, which is expected, hence "|| true".
# files_differ <path-a> <path-b>: quiet; succeeds only if the files differ.
function files_differ {
    ! git -c core.safecrlf=false --no-pager diff --no-index --quiet --ignore-cr-at-eol -- "$1" "$2"
}
# show_diff <label-a> <label-b> <path-a> <path-b>
function show_diff {
    git -c core.safecrlf=false --no-pager diff --no-index --ignore-cr-at-eol --color=auto \
        --src-prefix="$1/" --dst-prefix="$2/" -- "$3" "$4" || true
}

# Prints the pinned version from each of the four locations, one per line, as "label=version".
function current_pins {
    local pin
    pin=$(sed -nE "s/^FROM tomcat:(${VERSION_RE})-jdk21.*/\1/p" "$DOCKERFILE_CI");    echo "$DOCKERFILE_CI=$pin"
    pin=$(sed -nE "s/^FROM tomcat:(${VERSION_RE})-jdk21.*/\1/p" "$DOCKERFILE_LOCAL"); echo "$DOCKERFILE_LOCAL=$pin"
    pin=$(pom_version tomcat-dbcp);     echo "$POM (tomcat-dbcp)=$pin"
    pin=$(pom_version tomcat-catalina); echo "$POM (tomcat-catalina)=$pin"
}

# Version of the given org.apache.tomcat artifact in the pom.
function pom_version {
    awk -v a="<artifactId>$1</artifactId>" '
        index($0, a) { found = 1; next }
        found && /<version>/ {
            v = $0; gsub(/.*<version>|<\/version>.*/, "", v)
            print v; exit
        }' "$POM"
}

function show_pins {
    local line
    while IFS= read -r line; do
        printf '  %-45s %s\n' "${line%%=*}" "${line#*=}"
    done < <(current_pins)
}

function distinct_pin_count { current_pins | cut -d= -f2 | sort -u | wc -l | tr -d ' '; }

# ---------------------------------------------------------------- preflight
command -v docker >/dev/null || die "docker is required"
docker info >/dev/null 2>&1 || die "docker daemon is not running"
for f in "$DOCKERFILE_CI" "$DOCKERFILE_LOCAL" "$POM"; do
    [[ -f "$f" ]] || die "$f not found (run from a chpl-api checkout)"
done

info "Current Tomcat pins"
show_pins
CURRENT=$(sed -nE "s/^FROM tomcat:(${VERSION_RE})-jdk21.*/\1/p" "$DOCKERFILE_CI")
[[ -n "$CURRENT" ]] || die "could not read the current version from $DOCKERFILE_CI"
if [[ "$(distinct_pin_count)" != "1" ]]; then
    warn "The pins above are NOT all equal. They will all be set to the new version."
fi

if [[ -n "$(git status --porcelain -- "$DOCKERFILE_CI" "$DOCKERFILE_LOCAL" "$POM" "$CONF_DIR")" ]]; then
    warn "Files this script edits have uncommitted changes."
    confirm "Continue anyway?" || exit 1
fi

# ------------------------------------------------- step 1: pick the version
info "Step 1: pick the version"
echo "Check the changelog first: https://tomcat.apache.org/tomcat-11.0-doc/changelog.html"
NEW="${1:-}"
if [[ -z "$NEW" ]]; then
    read -r -p "New Tomcat version (current is $CURRENT): " NEW
fi
[[ "$NEW" =~ ^${VERSION_RE}$ ]] || die "'$NEW' is not a version like 11.0.27"

echo "Checking that tomcat:${NEW}-jdk21 exists on Docker Hub..."
docker manifest inspect "tomcat:${NEW}-jdk21" >/dev/null 2>&1 \
    || die "tomcat:${NEW}-jdk21 was not found. Check https://hub.docker.com/_/tomcat"
echo "Found."

if [[ "$NEW" == "$CURRENT" && "$(distinct_pin_count)" == "1" ]]; then
    die "Already on $NEW everywhere; nothing to do."
fi

# --------------------------------------- steps 2-4: refresh our Tomcat config
info "Step 2: extract the stock config from Tomcat $CURRENT (current) and $NEW (new)"
extract_stock "$NEW" "$STOCK_DIR"
if [[ "$NEW" != "$CURRENT" ]]; then
    extract_stock "$CURRENT" "$OLD_STOCK_DIR"
fi
echo "Stock config for $NEW is in $STOCK_DIR"

# What matters for an upgrade is what Tomcat itself changed between the two versions,
# not how our files differ from stock (they always differ: ours drop the commented-out examples).
info "Step 3: compare stock Tomcat $CURRENT with stock Tomcat $NEW"
CHANGED=()
if [[ "$NEW" != "$CURRENT" ]]; then
    for f in "${CONF_FILES[@]}"; do
        if files_differ "$OLD_STOCK_DIR/$f" "$STOCK_DIR/$f"; then
            CHANGED+=("$f")
        fi
    done
fi

if [[ "$NEW" == "$CURRENT" ]]; then
    printf '\n\033[32m%s\033[0m\n' "NO CONFIG CHANGES to migrate: $DOCKERFILE_CI is already on Tomcat $NEW,"
    printf '\033[32m%s\033[0m\n' "so only the other pins are being aligned."
elif [[ ${#CHANGED[@]} -eq 0 ]]; then
    printf '\n\033[32m%s\033[0m\n' "No new config changes in version $NEW that need to be migrated."
    echo "server.xml, context.xml and catalina.properties are identical in Tomcat $CURRENT and $NEW,"
    echo "so nothing in $CONF_DIR needs to change for this upgrade."
else
    warn ""
    warn "CONFIG CHANGES FOUND: Tomcat $NEW changed ${#CHANGED[@]} of ${#CONF_FILES[@]} config files we override, compared with $CURRENT:"
    warn "  ${CHANGED[*]}"
    echo "Lines starting with '-' are Tomcat $CURRENT; '+' are Tomcat $NEW."
    echo "Each change must be carried forward into $CONF_DIR (catalina.properties in"
    echo "particular carries version-specific JAR scan lists)."
    warn "Do NOT revert catalina.properties' common.loader. It intentionally keeps"
    warn "\${catalina.home}/lib so Quartz can find user-triggers.xml; dropping it breaks"
    warn "Quartz at runtime with nothing failing at build time."
    for f in "${CHANGED[@]}"; do
        printf '\n===== %s =====\n' "$f"
        show_diff "tomcat-$CURRENT" "tomcat-$NEW" "$OLD_STOCK_DIR/$f" "$STOCK_DIR/$f"
    done
fi

echo
echo "Optional: see what we have customized. This is separate from the upgrade check above."
echo "It compares our files in $CONF_DIR with Tomcat $NEW's own files. The output is long"
echo "(hundreds of lines) because we deleted Tomcat's commented-out examples and added our"
echo "own settings, and it looks the same on every upgrade, so it says nothing about whether"
echo "Tomcat changed. In it, '-' is stock Tomcat and '+' is ours."
if confirm "Show our customizations of the config files?"; then
    for f in "${CONF_FILES[@]}"; do
        printf '\n===== %s =====\n' "$f"
        show_diff "stock" "ours" "$STOCK_DIR/$f" "$CONF_DIR/$f"
    done
fi

info "Step 4: migrate config changes"
if [[ ${#CHANGED[@]} -eq 0 ]]; then
    echo "Skipped: no config changes to migrate."
else
    echo "Edit the files under $CONF_DIR now, carrying forward each change shown above."
    echo "The new stock files are in $STOCK_DIR if you need them as a starting point."
    echo
    echo "The local-development image (Dockerfile) uses a developer-supplied, gitignored"
    echo "tomcat-config/ directory, so it is not checked here. Developers should refresh"
    echo "their own copy."
    pause "Done with the config?"
fi

# --------------------------------------------------------- step 5: the pins
info "Step 5: bump the pins ($CURRENT -> $NEW)"
sed -b -i -E "s/^(FROM tomcat:)${VERSION_RE}(-jdk21.*)/\1${NEW}\2/" "$DOCKERFILE_CI" "$DOCKERFILE_LOCAL"
for artifact in tomcat-dbcp tomcat-catalina; do
    # Rewrite the first <version> after the artifactId line.
    sed -b -i -E "/<artifactId>${artifact}<\/artifactId>/{n;s#<version>${VERSION_RE}</version>#<version>${NEW}</version>#}" "$POM"
done

show_pins
[[ "$(distinct_pin_count)" == "1" && "$(current_pins | head -1 | cut -d= -f2)" == "$NEW" ]] \
    || die "The pins do not all equal $NEW. Inspect them with 'git diff' and fix by hand."
echo "All four pins are $NEW."

# --------------------------------------------------- step 6: local verification
info "Step 6: verify locally"
echo "docker/Dockerfile packages a prebuilt WAR from $WAR_DEST. CI builds it with the"
echo "Maven command below; building it now also compiles against the new tomcat-* pom pins."
if confirm "Build the WAR with Maven now? (same command as CI; takes several minutes)"; then
    command -v mvn >/dev/null || die "mvn is required to build the WAR"
    mvn -B clean package -DskipTests -Denv=dev --file chpl/pom.xml
    [[ -f "$WAR_BUILT" ]] || die "Maven finished but $WAR_BUILT was not produced"
    mkdir -p "$(dirname "$WAR_DEST")"
    cp "$WAR_BUILT" "$WAR_DEST"
elif [[ -f "$WAR_DEST" ]]; then
    warn "Using the existing $WAR_DEST. It may predate this upgrade."
fi

if [[ -f "$WAR_DEST" ]] && confirm "Build docker/Dockerfile now as chpl-api-test?"; then
    docker build -f docker/Dockerfile -t chpl-api-test .
    cat <<EOF

Now run it and check the three things from the doc:
  docker run --rm -p 8080:8080 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... chpl-api-test

  - the datasource resolves (a broken PROPERTY_SOURCE shows up as a literal \${DB_URL} connection failure)
  - Quartz starts with no user-triggers.xml not-found error (common.loader survived)
  - the app responds on 8080
EOF
else
    echo "Skipped. Run it yourself before merging:"
    echo "  mvn -B clean package -DskipTests -Denv=dev --file chpl/pom.xml"
    echo "  mkdir -p docker/build-context && cp $WAR_BUILT $WAR_DEST"
    echo "  docker build -f docker/Dockerfile -t chpl-api-test ."
fi

# ----------------------------------------------------------------- next steps
info "Remaining manual steps"
cat <<EOF
  1. Review 'git diff' and commit (Dockerfiles, pom, and any $CONF_DIR changes together).
  2. Merge to the lowest environment branch and watch that environment's next deploy.
  3. The first time each environment's Bamboo plan runs after picking up the upgrade,
     set that environment's UPDATE_DATADOG variable to true for that run, so the Datadog
     agent jar is refreshed. Repeat as the change promotes through each environment.
     (PROD refreshes it on every deploy by default.)
  4. AQA bind-mounts the host's Tomcat config over the image's, so verifying on a
     deployment environment does not verify AQA.
  5. Environments that build from source pin Tomcat in chpl-build's own Dockerfiles;
     that is a separate change.
EOF
