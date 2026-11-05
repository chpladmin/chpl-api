#!/usr/bin/env bash
# Nightly security scan (OCD-5545): Trivy over the repository and the published chpl-api images.
#
# Runs the same way locally and in GitHub Actions. This repository is public, so in CI the
# output of this script is world-readable: it prints counts and pass/fail only. Findings go to
# JSON files under OUT_DIR and, from there, only into the Teams cards built by trivy_digest.py.
#
# Usage:  .github/scripts/run_scans.sh
#
# Environment (all optional):
#   OUT_DIR        where reports and cards are written (default: $RUNNER_TEMP or $TMPDIR/chpl-scans)
#   IMAGE          image repository (default: ghcr.io/chpladmin/chpl-api)
#   TAGS           space separated image tags (default: the four latest-<environment> tags)
#   SCAN_REPO      1 (default) or 0: scan the repository source and Dockerfiles
#   SCAN_IMAGES    1 (default) or 0: scan the images
#   REPO_SKIP_DIRS extra directories the repo scan skips, space separated (e.g. chpl when there is no
#                  local Maven cache; Trivy otherwise downloads POMs from Maven Central)
#   TRIVY_BIN      trivy executable (default: trivy)
#   IGNORE_FILE    Trivy ignore file (default: .trivyignore.yaml in the repository root)
#   RUN_URL        link shown on the summary card (default: the GitHub run, or a placeholder)
#   SHOW_FINDINGS  1 to also print every finding (local use only; refused in GitHub Actions)
#
# Exit status: 0 all scans completed, 3 at least one scan failed (cards are still written),
# 2 bad setup. Having findings is not a failure.
set -u

HERE=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$HERE/../.." && pwd)
OUT_DIR=${OUT_DIR:-${RUNNER_TEMP:-${TMPDIR:-/tmp}}/chpl-scans}
IMAGE=${IMAGE:-ghcr.io/chpladmin/chpl-api}
TAGS=${TAGS:-latest-development latest-qa latest-staging latest-production}
SCAN_REPO=${SCAN_REPO:-1}
SCAN_IMAGES=${SCAN_IMAGES:-1}
TRIVY_BIN=${TRIVY_BIN:-trivy}
IGNORE_FILE=${IGNORE_FILE:-$REPO_ROOT/.trivyignore.yaml}
SHOW_FINDINGS=${SHOW_FINDINGS:-0}
if [ -n "${GITHUB_SERVER_URL:-}" ] && [ -n "${GITHUB_REPOSITORY:-}" ] && [ -n "${GITHUB_RUN_ID:-}" ]; then
  RUN_URL=${RUN_URL:-$GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID}
else
  RUN_URL=${RUN_URL:-local run}
fi

if [ "$SHOW_FINDINGS" = 1 ] && [ "${GITHUB_ACTIONS:-}" = true ]; then
  echo "SHOW_FINDINGS is refused in GitHub Actions: this log is public" >&2
  exit 2
fi
for tool in jq "$TRIVY_BIN"; do
  command -v "$tool" >/dev/null 2>&1 || { echo "missing required tool: $tool" >&2; exit 2; }
done
PYTHON=${PYTHON:-}
if [ -z "$PYTHON" ]; then
  for candidate in python3 python; do
    command -v "$candidate" >/dev/null 2>&1 && { PYTHON=$candidate; break; }
  done
fi
[ -n "$PYTHON" ] || { echo "missing required tool: python3" >&2; exit 2; }

# Native Windows programs (python, trivy) need Windows paths when this runs under Git Bash.
native_path() { if command -v cygpath >/dev/null 2>&1; then cygpath -w "$1"; else printf '%s' "$1"; fi; }

# The Trivy settings shared by every scan. Findings never reach stdout: --quiet, JSON to a file.
common_args=(--severity CRITICAL,HIGH --quiet --format json --exit-code 0)
[ -f "$IGNORE_FILE" ] && common_args+=(--ignorefile "$(native_path "$IGNORE_FILE")")

mkdir -p "$OUT_DIR" || exit 2
rm -f "$OUT_DIR"/*.json
entries="$OUT_DIR/entries.ndjson"
: > "$entries"

failed=0

# record NAME KIND TAG EXIT_CODE REPORT   (REPORT empty when there is none)
record() {
  jq -nc --arg name "$1" --arg kind "$2" --arg tag "$3" --argjson code "$4" --arg report "$5" \
    '{name: $name, kind: $kind, tag: (if $tag == "" then null else $tag end), exit_code: $code,
      report: (if $report == "" then null else $report end)}' >> "$entries"
}

# run_scan NAME KIND TAG TRIVY_ARGS...   Runs Trivy; a missing report counts as a failure even
# if the exit code was 0.
run_scan() {
  local name=$1 kind=$2 tag=$3; shift 3
  local report="$OUT_DIR/${name//[^A-Za-z0-9._-]/_}.json" code
  "$TRIVY_BIN" "$@" "${common_args[@]}" --output "$(native_path "$report")"
  code=$?
  if [ "$code" -eq 0 ] && [ ! -s "$report" ]; then
    code=1
  fi
  if [ "$code" -ne 0 ]; then
    failed=$((failed + 1))
    echo "scan failed: $name (exit code $code)"
    record "$name" "$kind" "$tag" "$code" ""
  else
    echo "scan ok: $name"
    record "$name" "$kind" "$tag" 0 "$(native_path "$report")"
  fi
}

if [ "$SCAN_REPO" = 1 ]; then
  cd "$REPO_ROOT" || exit 2
  skip_args=(--skip-dirs node_modules --skip-dirs .git)
  # Split on spaces without letting the shell expand globs such as chpl/**.
  read -r -a extra_skips <<< "${REPO_SKIP_DIRS:-}"
  for dir in "${extra_skips[@]:-}"; do
    [ -n "$dir" ] && skip_args+=(--skip-dirs "$dir")
  done
  run_scan repo fs "" fs --scanners vuln,misconfig,secret "${skip_args[@]}" .
fi

if [ "$SCAN_IMAGES" = 1 ]; then
  for tag in $TAGS; do
    env_tag=${tag#latest-}
    name="image:$tag"
    digest=$(docker buildx imagetools inspect "$IMAGE:$tag" --format '{{.Manifest.Digest}}' 2>/dev/null)
    if ! [[ $digest =~ ^sha256:[0-9a-f]{64}$ ]]; then
      failed=$((failed + 1))
      echo "scan failed: $name (could not resolve the image digest)"
      record "$name" image "$env_tag" 2 ""
      continue
    fi
    # Scan by digest so the result describes exactly one image, even if the tag moves mid-run.
    run_scan "$name" image "$env_tag" image --scanners vuln,secret "$IMAGE@$digest"
  done
fi

# Build the Teams cards (card-NN.json). Prints counts only unless SHOW_FINDINGS=1.
digest_args=(--manifest "$(native_path "$OUT_DIR/manifest.json")" --run-url "$RUN_URL"
             --out-dir "$(native_path "$OUT_DIR/cards")")
[ "$SHOW_FINDINGS" = 1 ] && digest_args+=(--text)
rm -rf "$OUT_DIR/cards"
jq -s '.' "$entries" > "$OUT_DIR/manifest.json" || exit 2
"$PYTHON" "$(native_path "$HERE/trivy_digest.py")" "${digest_args[@]}" || exit 2

echo "scans_failed=$failed"
[ "$failed" -eq 0 ] || exit 3
exit 0
