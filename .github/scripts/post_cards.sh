#!/usr/bin/env bash
# Posts the Teams cards written by run_scans.sh (OCD-5545) to a Teams Workflows webhook.
#
# Usage:  TEAMS_WEBHOOK_URL=... .github/scripts/post_cards.sh CARDS_DIR
#
# Each card file is the standard Teams envelope, {"type":"message","attachments":[...]}; the flow
# reads the card from attachments[0].content. The summary card goes first, then the continuation
# cards, with a short pause so Teams keeps them in order. This log is public in CI: only the HTTP
# status of each post is printed, never the card content or the response body.
set -u

cards_dir=${1:?usage: post_cards.sh CARDS_DIR}
: "${TEAMS_WEBHOOK_URL:?TEAMS_WEBHOOK_URL is not set}"
pause=${POST_PAUSE_SECONDS:-2}

posted=0
failed=0
for card in "$cards_dir"/card-*.json; do
  [ -e "$card" ] || { echo "no cards found in $cards_dir" >&2; exit 1; }
  status=$(curl --silent --show-error --output /dev/null --write-out '%{http_code}' \
    --max-time 30 --retry 2 --retry-delay 5 \
    --header 'Content-Type: application/json' --data-binary "@$card" "$TEAMS_WEBHOOK_URL")
  case "$status" in
    2??) posted=$((posted + 1)); echo "posted $(basename "$card"): HTTP $status" ;;
    *)   failed=$((failed + 1)); echo "post failed $(basename "$card"): HTTP $status" ;;
  esac
  sleep "$pause"
done

echo "cards_posted=$posted cards_failed=$failed"
[ "$failed" -eq 0 ]
