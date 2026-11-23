#!/usr/bin/env python3
"""Turn Trivy JSON reports into Microsoft Teams Adaptive Cards (OCD-5545).

This repository is public, so findings must only ever leave the runner inside the
cards this script builds. It therefore never prints findings (stdout and stderr
carry counts only) and never reads secret values: a secret finding is reported by
its rule ID and file path alone.

Usage:
    trivy_digest.py --manifest scans.json --run-url URL --out-dir DIR

The manifest is a JSON list of {"name", "kind" ("fs" | "image"), "tag", "exit_code",
"report" (path to the Trivy JSON, or null)}. One card-NN.json is written per card,
summary first, ready to POST to a Teams Workflows webhook.
"""
import argparse
import json
import os
import re
import sys
from dataclasses import dataclass
from datetime import date, datetime, timezone
from pathlib import Path

SEVERITIES = ('CRITICAL', 'HIGH')
ENVIRONMENT_ORDER = ('development', 'qa', 'staging', 'production')
MAX_PER_CARD = 10
MAX_CARDS = 6
CARD_BYTE_LIMIT = 27000  # Teams rejects payloads around 28 KB
FIELD_LIMITS = {'id': 60, 'package': 200, 'version': 80, 'url': 200, 'label': 40}
DIGEST_PATTERN = re.compile(r'sha256:[0-9a-f]{64}')
SEVERITY_COLORS = {'CRITICAL': 'Attention', 'HIGH': 'Warning'}


@dataclass
class Scan:
    name: str
    kind: str  # 'fs' or 'image'
    tag: str
    exit_code: int
    report: dict


@dataclass
class Finding:
    kind: str  # 'vuln', 'misconfig' or 'secret'
    id: str
    package: str
    installed: str
    fixed: str
    severity: str
    url: str
    label: str  # where it was found: an image tag, or the scan name for repo scans
    published: str = ''  # CVE publish date (YYYY-MM-DD) when Trivy reports one

    @property
    def fixable(self):
        # Only vulnerabilities can lack a fix; misconfigurations and secrets are always actionable.
        return self.kind != 'vuln' or bool(self.fixed)


@dataclass
class Group:
    kind: str
    id: str
    package: str
    installed: str
    fixed: str
    severity: str
    url: str
    tags: list
    published: str = ''

    @property
    def fixable(self):
        return self.kind != 'vuln' or bool(self.fixed)


def scan_label(scan):
    return scan.tag if scan.kind == 'image' and scan.tag else scan.name


def _published(value):
    """The YYYY-MM-DD part of a Trivy PublishedDate, or '' when absent or malformed."""
    try:
        return date.fromisoformat(str(value)[:10]).isoformat()
    except ValueError:
        return ''


def load_findings(scan):
    """Every CRITICAL/HIGH finding in the scan's report, as reported (not de-duplicated)."""
    if not scan.report:
        return []
    label = scan_label(scan)
    findings = []
    for result in scan.report.get('Results') or []:
        target = result.get('Target', '')
        for v in result.get('Vulnerabilities') or []:
            findings.append(Finding('vuln', v.get('VulnerabilityID', ''), v.get('PkgName', ''),
                                    v.get('InstalledVersion', ''), v.get('FixedVersion') or '',
                                    v.get('Severity', ''), v.get('PrimaryURL') or '', label,
                                    _published(v.get('PublishedDate'))))
        for m in result.get('Misconfigurations') or []:
            if m.get('Status') == 'PASS':
                continue
            findings.append(Finding('misconfig', m.get('ID', ''), target, '', '',
                                    m.get('Severity', ''), m.get('PrimaryURL') or '', label))
        for s in result.get('Secrets') or []:
            # Deliberately never reads Match or Code: the secret value must not leave the runner.
            findings.append(Finding('secret', s.get('RuleID', ''), target, '', '',
                                    s.get('Severity', ''), '', label))
    return [f for f in findings if f.severity in SEVERITIES]


def _tag_sort_key(tag):
    if tag in ENVIRONMENT_ORDER:
        return (0, ENVIRONMENT_ORDER.index(tag), tag)
    return (1, 0, tag)


def _group_sort_key(group):
    """Severity first, then newest CVE first; findings with no publish date go last."""
    published = date.fromisoformat(group.published) if group.published else None
    newest_first = -published.toordinal() if published else 0
    return (SEVERITIES.index(group.severity), published is None, newest_first, group.id, group.package)


def group_findings(scans):
    """One group per (kind, id, package), listing every tag it was found in."""
    groups = {}
    for scan in scans:
        for f in load_findings(scan):
            key = (f.kind, f.id, f.package)
            group = groups.get(key)
            if group is None:
                groups[key] = Group(f.kind, f.id, f.package, f.installed, f.fixed,
                                    f.severity, f.url, [f.label], f.published)
                continue
            if f.label not in group.tags:
                group.tags.append(f.label)
            if f.severity == 'CRITICAL':
                group.severity = 'CRITICAL'
            if f.fixed and not group.fixed:
                group.fixed, group.installed = f.fixed, f.installed
            group.published = group.published or f.published
    ordered = list(groups.values())
    for g in ordered:
        g.tags.sort(key=_tag_sort_key)
    ordered.sort(key=_group_sort_key)
    return ordered


def summarize(scans):
    """Per scan name, per severity: how many distinct findings have a fix and how many do not."""
    summary = {}
    for scan in scans:
        counts = {sev: {'fixable': 0, 'unfixable': 0} for sev in SEVERITIES}
        for g in group_findings([scan]):
            counts[g.severity]['fixable' if g.fixable else 'unfixable'] += 1
        summary[scan.name] = counts
    return summary


def digest_of(scan):
    """The image digest (sha256:...) recorded in the report, or None."""
    for repo_digest in ((scan.report or {}).get('Metadata') or {}).get('RepoDigests') or []:
        match = DIGEST_PATTERN.search(repo_digest)
        if match:
            return match.group(0)
    return None


def _failed(scan):
    return scan.exit_code != 0 or scan.report is None


def has_errors(scans):
    return any(_failed(s) for s in scans)


def _clip(text, key):
    text = str(text)
    limit = FIELD_LIMITS[key]
    return text if len(text) <= limit else text[:limit - 3] + '...'


def _md(text):
    """Escape the characters Adaptive Card TextBlock markdown would otherwise interpret."""
    return re.sub(r'([\\*_\[\]])', r'\\\1', text)


def _text(text, **options):
    return dict({'type': 'TextBlock', 'text': text, 'wrap': True}, **options)


def _envelope(body):
    return {'type': 'message', 'attachments': [{
        'contentType': 'application/vnd.microsoft.card.adaptive',
        'contentUrl': None,
        'content': {'$schema': 'http://adaptivecards.io/schemas/adaptive-card.json',
                    'type': 'AdaptiveCard', 'version': '1.4', 'body': body},
    }]}


def _size(blocks):
    return len(json.dumps(blocks).encode('utf-8'))


def _age(published, today):
    """'Published 2026-08-04 (93 days ago)', or '' when the CVE has no publish date."""
    if not published:
        return ''
    days = max((today - date.fromisoformat(published)).days, 0)
    ago = 'today' if days == 0 else '1 day ago' if days == 1 else f'{days} days ago'
    return f'Published {published} ({ago})'


def _finding_block(group, today=None):
    today = today or datetime.now(timezone.utc).date()
    heading = _md(_clip(group.id, 'id'))
    if group.url:
        heading = f"[{heading}]({_clip(group.url, 'url')})"
    package = _md(_clip(group.package, 'package'))
    if group.kind == 'vuln':
        detail = f"{package} {_md(_clip(group.installed, 'version'))} " \
                 f"-> fixed in {_md(_clip(group.fixed, 'version'))}"
    elif group.kind == 'secret':
        detail = f'secret in {package}'
    else:
        detail = f'misconfiguration in {package}'
    found_in = ', '.join(_md(_clip(t, 'label')) for t in group.tags)
    items = [_text(f'**{group.severity}**  {heading}', color=SEVERITY_COLORS[group.severity]),
             _text(detail, spacing='None')]
    if group.kind == 'vuln' and group.published:
        items.append(_text(_age(group.published, today), isSubtle=True, spacing='None'))
    items.append(_text(f'Found in: {found_in}', isSubtle=True, spacing='None'))
    return {'type': 'Container', 'separator': True, 'items': items}


def _scan_line(scan, counts):
    name = _md(scan.name)
    if _failed(scan):
        return _text(f'**{name}** SCAN FAILED (exit code {scan.exit_code})', color='Attention')
    digest = digest_of(scan)
    prefix = f' {digest[:19]}' if digest else ''
    parts = [f"{sev} {counts[sev]['fixable']} fixable, {counts[sev]['unfixable']} no fix yet"
             for sev in SEVERITIES]
    return _text(f"**{name}**{prefix}: " + ' | '.join(parts))


def _chunk(groups, max_per_card):
    """Split groups into chunks of at most max_per_card, also bounded by the card byte limit."""
    chunks, current = [], []
    for group in groups:
        block = _finding_block(group)
        too_big = current and _size([_finding_block(g) for g in current] + [block]) > CARD_BYTE_LIMIT
        if len(current) >= max_per_card or too_big:
            chunks.append(current)
            current = []
        current.append(group)
    if current:
        chunks.append(current)
    return chunks


def build_cards(scans, run_url, max_per_card=MAX_PER_CARD, max_cards=MAX_CARDS,
                title='Nightly security scan', generated_at=None):
    """A summary card first, then up to max_cards continuation cards of fixable findings."""
    generated_at = generated_at or datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC')
    summary = summarize(scans)
    listed = [g for g in group_findings(scans) if g.fixable]
    chunks = _chunk(listed, max_per_card)[:max_cards]
    shown = sum(len(c) for c in chunks)
    not_shown = len(listed) - shown

    body = [_text(f'**{_md(title)}**  {generated_at}', size='Medium'),
            _text(f'[Open the run]({run_url})')]
    body += [_scan_line(s, summary[s.name]) for s in scans]
    total = sum(n for counts in summary.values() for sev in counts.values() for n in sev.values())
    if has_errors(scans):
        body.append(_text('One or more scans failed. Results below may be incomplete.', color='Attention'))
    elif total == 0:
        body.append(_text('No CRITICAL or HIGH findings'))
    if chunks:
        body.append(_text('Fixable CRITICAL/HIGH findings follow: most severe first, newest CVE first within a severity.', isSubtle=True))
    if not_shown:
        body.append(_text(f'{not_shown} more not shown (fixable CRITICAL/HIGH beyond the first {shown}). '
                          'For the full list, run .github/scripts/run_scans.sh locally.',
                          color='Warning'))

    cards = [_envelope(body)]
    for number, chunk in enumerate(chunks, start=1):
        cards.append(_envelope([_text(f'**Part {number} of {len(chunks)}**', size='Medium')]
                               + [_finding_block(g) for g in chunk]))
    return cards


def format_full_list(scans):
    """Every CRITICAL/HIGH finding as plain text, including those with no fix yet. Local use only."""
    lines = []
    for scan in scans:
        if _failed(scan):
            lines.append(f'!! {scan.name}: SCAN FAILED (exit code {scan.exit_code})')
    for g in group_findings(scans):
        fix = f'fixed in {g.fixed}' if g.fixable and g.kind == 'vuln' else (
            'no fix yet' if g.kind == 'vuln' else g.kind)
        published = f'  published {g.published}' if g.kind == 'vuln' and g.published else ''
        lines.append(f'{g.severity:8} {g.id}  {g.package} {g.installed}  [{fix}]{published}  '
                     f'found in: {", ".join(g.tags)}')
    lines.append(f'{len(group_findings(scans))} findings')
    return '\n'.join(lines)


def _load_scans(manifest_path):
    scans = []
    for entry in json.loads(Path(manifest_path).read_text(encoding='utf-8')):
        report, exit_code = None, int(entry.get('exit_code', 0))
        if entry.get('report'):
            try:
                report = json.loads(Path(entry['report']).read_text(encoding='utf-8'))
            except (OSError, ValueError):
                exit_code = exit_code or 1
        scans.append(Scan(entry['name'], entry.get('kind', 'image'), entry.get('tag'), exit_code, report))
    return scans


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument('--manifest', required=True)
    parser.add_argument('--run-url', required=True)
    parser.add_argument('--out-dir', required=True)
    parser.add_argument('--text', action='store_true',
                        help='also print every finding to stdout (local use only)')
    args = parser.parse_args(argv)

    if args.text and os.environ.get('GITHUB_ACTIONS') == 'true':
        # Workflow logs of this public repository are world-readable.
        print('--text prints findings and is refused in GitHub Actions', file=sys.stderr)
        return 2

    scans = _load_scans(args.manifest)
    cards = build_cards(scans, args.run_url)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    for index, card in enumerate(cards):
        (out_dir / f'card-{index:02d}.json').write_text(json.dumps(card), encoding='utf-8')
    # Counts only: this output ends up in the public workflow log.
    print(f'cards={len(cards)} errors={sum(1 for s in scans if _failed(s))}')
    if args.text:
        print(format_full_list(scans))
    return 0


if __name__ == '__main__':
    sys.exit(main())
