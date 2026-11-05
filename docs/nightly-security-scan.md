# Nightly security scan

A GitHub Actions workflow (`.github/workflows/nightly-security-scan.yml`) runs
[Trivy](https://trivy.dev) every night at 08:00 UTC and posts a digest of CRITICAL and HIGH findings to a
private Teams channel. It is detection and reporting only; findings are fixed through normal tickets.

## What is scanned

- the repository source and both Dockerfiles (`Dockerfile`, `docker/Dockerfile`)
- `ghcr.io/chpladmin/chpl-api` tags `latest-development`, `latest-qa`, `latest-staging` and
  `latest-production`, each resolved to its digest first so a result describes exactly one image

## This repository is public

Workflow logs, step summaries and artifacts are world-readable, so findings must never appear in them.
The scripts write reports to files on the runner and print counts only. The findings leave the runner
only inside the Teams cards. Do not add steps that print, summarize or upload scan output.

## Reading the digest

The first card is a summary: per scan, the image digest and the CRITICAL/HIGH counts split into
fixable and "no fix yet". Up to six more cards list the fixable findings, most severe first, one entry per
CVE and package with the environments it was found in. Findings beyond the first 60 are counted but not
listed; run `SHOW_FINDINGS=1 .github/scripts/run_scans.sh` locally for the full list (see below).
A scan that fails is reported as "SCAN FAILED" on the summary card and the run is marked failed.

## Running it locally

Needs `trivy`, `jq`, `python3`, and `docker` logged in to GHCR (`docker login ghcr.io`, a token with
`read:packages` that is SSO-authorized for the organization).

    SHOW_FINDINGS=1 REPO_SKIP_DIRS=chpl .github/scripts/run_scans.sh

`REPO_SKIP_DIRS=chpl` skips the Maven modules: without a populated `~/.m2`, Trivy downloads POMs from Maven
Central and is rate limited (HTTP 429). Reports and cards are written to `OUT_DIR`. `SHOW_FINDINGS` is
refused inside GitHub Actions. Tests for the digest script: `python -m unittest discover -s .github/scripts/tests`.

## Configuration

| What | Where |
| --- | --- |
| Teams webhook | repository secret `TEAMS_WEBHOOK_URL` |
| Accepted findings | `.trivyignore.yaml`: each entry has a statement (reason and owner) and an `expired_at` date |
| Trivy version, action versions | the workflow (actions are pinned by SHA; Dependabot updates them monthly) |

The `DS-0002` (runs as root) suppressions expire on 2027-02-02; see OCD-5547.

## Teams flow setup

Create the webhook in the Teams channel (Workflows, "Post to a channel when a webhook request is received"):

- Trigger "When a Teams webhook request is received": who can trigger the flow is "Anyone"
- Action "Post card in a chat or channel": Post as Flow bot, in the channel, with the **Adaptive card**
  field set to the expression `string(triggerBody()?['attachments']?[0]?['content'])` (the `fx` editor)

`post_cards.sh` posts the standard Teams envelope, so the card is `attachments[0].content`. Store the flow's URL
as the secret `TEAMS_WEBHOOK_URL`; anyone holding it can post to the channel.

## Risks and troubleshooting

- **The Teams flow is owned by one person.** The Power Automate flow behind the webhook belongs to the account
  that created it. If that person leaves or the account is disabled, posting silently stops. Transfer or
  re-create the flow when ownership changes.
- **Scheduled workflows are disabled after 60 days without repository activity** on public repositories.
  Re-enable under the Actions tab if the digest stops arriving.
- **A missing digest** with a green run is not expected: posting failures fail the run. Check the run first.
- **HTTP 429 from Maven Central or the DB mirrors**: the workflow populates the Maven cache before scanning and
  falls back to a second vulnerability DB mirror. Re-run the workflow later.
- **Image scan "denied" or digest not resolved**: the workflow's `GITHUB_TOKEN` needs read access to the package.
  In the package settings, add this repository under "Manage Actions access".
- **Run it on demand**: Actions tab, "Nightly security scan", "Run workflow".
