"""Tests for .github/scripts/trivy_digest.py (OCD-5545).

The digest script turns Trivy JSON reports into Teams Adaptive Cards. Findings
may only ever leave the runner inside those cards, so these tests also pin down
what the script must never emit (secret values, findings on stdout).

Run from the repository root:
    python -m unittest discover -s .github/scripts/tests -v
"""
import json
import re
import subprocess
import sys
import tempfile
import unittest
from datetime import date
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))

import trivy_digest as td  # noqa: E402

FIXTURES = HERE / 'fixtures'
SENTINEL = 'FAKE-SECRET-VALUE-DO-NOT-LEAK'
ENVIRONMENTS = ['development', 'qa', 'staging', 'production']
TEAMS_LIMIT_BYTES = 28000


def load(name):
    return json.loads((FIXTURES / name).read_text(encoding='utf-8'))


def image_scan(tag, fixture='image_fake_critical.json', exit_code=0):
    return td.Scan(name=f'image:latest-{tag}', kind='image', tag=tag,
                   exit_code=exit_code, report=load(fixture) if fixture else None)


def repo_scan(fixture='fs_real_dockerfiles.json', exit_code=0):
    return td.Scan(name='repo', kind='fs', tag=None, exit_code=exit_code,
                   report=load(fixture) if fixture else None)


def card_texts(cards):
    """All TextBlock text values in each card, joined, in document order."""
    def walk(node, acc):
        if isinstance(node, dict):
            if node.get('type') == 'TextBlock' and 'text' in node:
                acc.append(node['text'])
            for value in node.values():
                walk(value, acc)
        elif isinstance(node, list):
            for item in node:
                walk(item, acc)
        return acc
    return ['\n'.join(walk(card, [])) for card in cards]


def many_findings_report(count, package_prefix='pkg', severity='HIGH'):
    return {
        'SchemaVersion': 2,
        'ArtifactName': 'fake',
        'ArtifactType': 'container_image',
        'Metadata': {'RepoDigests': ['fake@sha256:' + 'ab' * 32]},
        'Results': [{'Target': 'fake', 'Class': 'os-pkgs', 'Type': 'ubuntu', 'Vulnerabilities': [
            {'VulnerabilityID': f'CVE-2099-{1000 + i}', 'PkgName': f'{package_prefix}{i}',
             'InstalledVersion': '1', 'FixedVersion': '2', 'Status': 'fixed',
             'Severity': severity, 'Title': 'Fake', 'PrimaryURL': 'https://example.invalid/x'}
            for i in range(count)]}],
    }


class LoadFindingsTest(unittest.TestCase):
    def test_real_image_report_yields_high_vulnerabilities_with_fix_versions(self):
        findings = td.load_findings(image_scan('development', 'image_real_high.json'))
        self.assertEqual(7, len(findings))
        self.assertTrue(all(f.kind == 'vuln' and f.severity == 'HIGH' for f in findings))
        self.assertTrue(all(f.fixed for f in findings))
        pebble = next(f for f in findings if f.id == 'CVE-2026-111943')
        self.assertEqual('1.26.10, 1.27.3', pebble.fixed)
        self.assertTrue(pebble.url.startswith('https://avd.aquasec.com/'))

    def test_dockerfile_misconfigurations_use_the_file_path_as_package(self):
        findings = td.load_findings(repo_scan())
        self.assertEqual({'misconfig'}, {f.kind for f in findings})
        self.assertEqual({'DS-0002'}, {f.id for f in findings})
        self.assertEqual({'Dockerfile', 'docker/Dockerfile'}, {f.package for f in findings})

    def test_secret_finding_carries_rule_id_and_path(self):
        findings = td.load_findings(repo_scan('fs_fake_secret.json'))
        self.assertEqual(1, len(findings))
        self.assertEqual('secret', findings[0].kind)
        self.assertEqual('fake-api-token', findings[0].id)
        self.assertEqual('chpl/example/application.properties', findings[0].package)

    def test_report_without_results_yields_no_findings(self):
        self.assertEqual([], td.load_findings(image_scan('qa', 'image_empty.json')))

    def test_scan_without_report_yields_no_findings(self):
        self.assertEqual([], td.load_findings(image_scan('qa', None, exit_code=1)))


class GroupingTest(unittest.TestCase):
    def test_same_finding_in_four_tags_is_one_group_listing_the_tags_in_environment_order(self):
        scans = [image_scan(tag) for tag in reversed(ENVIRONMENTS)]
        group = next(g for g in td.group_findings(scans) if g.id == 'CVE-2099-0001')
        self.assertEqual(ENVIRONMENTS, group.tags)

    def test_same_cve_and_package_in_two_results_of_one_scan_is_not_duplicated(self):
        groups = td.group_findings([image_scan('development')])
        self.assertEqual(1, len([g for g in groups if g.id == 'CVE-2099-0003']))

    def test_same_cve_in_different_packages_stays_separate(self):
        report = many_findings_report(1)
        report['Results'][0]['Vulnerabilities'].append(
            dict(report['Results'][0]['Vulnerabilities'][0], PkgName='other-package'))
        scan = td.Scan('image:latest-qa', 'image', 'qa', 0, report)
        self.assertEqual(2, len(td.group_findings([scan])))


class SummaryTest(unittest.TestCase):
    def test_counts_split_fixable_from_unfixable_per_severity(self):
        counts = td.summarize([image_scan('development')])['image:latest-development']
        self.assertEqual({'fixable': 2, 'unfixable': 1}, counts['CRITICAL'])
        self.assertEqual({'fixable': 1, 'unfixable': 1}, counts['HIGH'])

    def test_misconfigurations_count_as_fixable(self):
        counts = td.summarize([repo_scan()])['repo']
        self.assertEqual({'fixable': 2, 'unfixable': 0}, counts['HIGH'])
        self.assertEqual({'fixable': 0, 'unfixable': 0}, counts['CRITICAL'])


class CardsTest(unittest.TestCase):
    def setUp(self):
        self.scans = [repo_scan(), image_scan('development')]
        self.cards = td.build_cards(self.scans, run_url='https://example.invalid/run/1')

    def test_every_card_is_a_teams_message_with_an_adaptive_card(self):
        for card in self.cards:
            self.assertEqual('message', card['type'])
            attachment = card['attachments'][0]
            self.assertEqual('application/vnd.microsoft.card.adaptive', attachment['contentType'])
            self.assertEqual('AdaptiveCard', attachment['content']['type'])

    def test_summary_card_comes_first_and_links_the_run(self):
        summary = card_texts(self.cards)[0]
        self.assertIn('https://example.invalid/run/1', summary)
        self.assertIn('repo', summary)
        self.assertIn('image:latest-development', summary)

    def test_summary_shows_a_digest_prefix_but_not_the_full_digest(self):
        summary = card_texts(self.cards)[0]
        full = 'a1b2c3d4e5f6' * 5 + 'a1b2'
        self.assertIn('sha256:a1b2c3d4e5f6', summary)
        self.assertNotIn(full, summary)

    def test_fixable_critical_are_listed_before_fixable_high(self):
        text = '\n'.join(card_texts(self.cards)[1:])
        for critical in ('CVE-2099-0001', 'CVE-2099-0005'):
            self.assertLess(text.index(critical), text.index('CVE-2099-0003'))

    def test_unfixable_findings_are_counted_but_never_listed(self):
        text = '\n'.join(card_texts(self.cards))
        self.assertNotIn('CVE-2099-0002', text)
        self.assertNotIn('CVE-2099-0004', text)

    def test_listed_finding_shows_package_versions_tags_and_link(self):
        text = '\n'.join(card_texts(self.cards)[1:])
        self.assertIn('CVE-2099-0001', text)
        self.assertIn('libexample', text)
        self.assertIn('2.3.1', text)
        self.assertIn('2.3.7', text)
        self.assertIn('development', text)
        self.assertIn('https://avd.aquasec.com/nvd/cve-2099-0001', text)

    def test_dockerfile_misconfigurations_are_listed_with_their_file(self):
        text = '\n'.join(card_texts(self.cards)[1:])
        self.assertIn('DS-0002', text)
        self.assertIn('docker/Dockerfile', text)

    def test_zero_findings_gives_a_single_card_that_says_so(self):
        cards = td.build_cards([repo_scan('image_empty.json'), image_scan('qa', 'image_empty.json')],
                               run_url='https://example.invalid/run/2')
        self.assertEqual(1, len(cards))
        self.assertIn('No CRITICAL or HIGH findings', card_texts(cards)[0])

    def test_failed_scan_is_reported_and_blocks_the_all_clear(self):
        scans = [repo_scan('image_empty.json'), image_scan('qa', None, exit_code=1)]
        self.assertTrue(td.has_errors(scans))
        text = card_texts(td.build_cards(scans, run_url='https://example.invalid/run/3'))[0]
        self.assertIn('SCAN FAILED', text)
        self.assertIn('image:latest-qa', text)
        self.assertNotIn('No CRITICAL or HIGH findings', text)

    def test_has_errors_is_false_when_every_scan_succeeded(self):
        self.assertFalse(td.has_errors(self.scans))

    def test_secret_value_is_never_emitted(self):
        cards = td.build_cards([repo_scan('fs_fake_secret.json')], run_url='https://example.invalid/run/4')
        self.assertNotIn(SENTINEL, json.dumps(cards))
        text = '\n'.join(card_texts(cards))
        self.assertIn('fake-api-token', text)
        self.assertIn('chpl/example/application.properties', text)


class ContinuationTest(unittest.TestCase):
    def finding_counts(self, cards):
        return [len(re.findall(r'CVE-2099-1\d{3}', text)) for text in card_texts(cards)]

    def test_findings_beyond_ten_spill_into_continuation_cards_capped_at_six(self):
        scan = td.Scan('image:latest-development', 'image', 'development', 0, many_findings_report(63))
        cards = td.build_cards([scan], run_url='https://example.invalid/run/5')
        texts = card_texts(cards)
        self.assertEqual(7, len(cards))
        self.assertEqual([0, 10, 10, 10, 10, 10, 10], self.finding_counts(cards))
        for part in range(1, 7):
            self.assertIn(f'Part {part} of 6', texts[part])
        self.assertIn('3 more not shown', texts[0])
        self.assertIn('.github/scripts/run_scans.sh', texts[0])

    def test_seven_findings_need_one_continuation_card_and_no_not_shown_note(self):
        scan = td.Scan('image:latest-development', 'image', 'development', 0, many_findings_report(7))
        cards = td.build_cards([scan], run_url='https://example.invalid/run/6')
        texts = card_texts(cards)
        self.assertEqual(2, len(cards))
        self.assertIn('Part 1 of 1', texts[1])
        self.assertNotIn('not shown', texts[0])

    def test_custom_limits_are_honoured(self):
        scan = td.Scan('image:latest-development', 'image', 'development', 0, many_findings_report(8))
        cards = td.build_cards([scan], run_url='https://example.invalid/run/7',
                               max_per_card=3, max_cards=2)
        self.assertEqual([0, 3, 3], self.finding_counts(cards))
        self.assertIn('2 more not shown', card_texts(cards)[0])

    def test_every_card_stays_under_the_teams_size_limit_even_with_huge_fields(self):
        scan = td.Scan('image:latest-development', 'image', 'development', 0,
                       many_findings_report(40, package_prefix='x' * 1000))
        for card in td.build_cards([scan], run_url='https://example.invalid/run/8'):
            self.assertLess(len(json.dumps(card).encode('utf-8')), TEAMS_LIMIT_BYTES)


class DigestOfTest(unittest.TestCase):
    def test_extracts_the_sha256_from_a_repo_digest(self):
        scan = image_scan('development', 'image_real_high.json')
        self.assertEqual('sha256:85bca16d6dda8eec14ca205d424d9803ebee9174662dc7647fc8c07ffe560e0e',
                         td.digest_of(scan))

    def test_is_none_for_a_scan_without_a_digest(self):
        self.assertIsNone(td.digest_of(repo_scan()))
        self.assertIsNone(td.digest_of(image_scan('qa', None, exit_code=1)))


class CommandLineTest(unittest.TestCase):
    def run_cli(self, entries):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        out_dir = Path(tmp.name) / 'cards'
        manifest = Path(tmp.name) / 'manifest.json'
        manifest.write_text(json.dumps(entries), encoding='utf-8')
        result = subprocess.run(
            [sys.executable, str(HERE.parent / 'trivy_digest.py'), '--manifest', str(manifest),
             '--run-url', 'https://example.invalid/run/9', '--out-dir', str(out_dir)],
            capture_output=True, text=True)
        return result, out_dir

    def entry(self, name, kind, tag, fixture, exit_code=0):
        return {'name': name, 'kind': kind, 'tag': tag, 'exit_code': exit_code,
                'report': str(FIXTURES / fixture) if fixture else None}

    def test_writes_one_json_file_per_card_and_prints_counts_only(self):
        result, out_dir = self.run_cli([
            self.entry('repo', 'fs', None, 'fs_real_dockerfiles.json'),
            self.entry('image:latest-development', 'image', 'development', 'image_fake_critical.json'),
        ])
        self.assertEqual(0, result.returncode, result.stderr)
        files = sorted(out_dir.glob('card-*.json'))
        self.assertEqual(['card-00.json', 'card-01.json'], [f.name for f in files])
        self.assertEqual('message', json.loads(files[0].read_text(encoding='utf-8'))['type'])
        self.assertIn('cards=2', result.stdout)
        self.assertIn('errors=0', result.stdout)
        for forbidden in ('CVE-', 'libexample', 'DS-0002', 'Dockerfile'):
            self.assertNotIn(forbidden, result.stdout + result.stderr)

    def test_reports_the_number_of_failed_scans_without_failing_itself(self):
        result, _ = self.run_cli([
            self.entry('repo', 'fs', None, 'image_empty.json'),
            self.entry('image:latest-qa', 'image', 'qa', None, exit_code=1),
        ])
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn('errors=1', result.stdout)


class FullListTest(unittest.TestCase):
    """--text prints every finding for local use, and must refuse to run in GitHub Actions."""

    def run_text(self, env_extra=None):
        import os
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        manifest = Path(tmp.name) / 'manifest.json'
        manifest.write_text(json.dumps([
            {'name': 'image:latest-development', 'kind': 'image', 'tag': 'development', 'exit_code': 0,
             'report': str(FIXTURES / 'image_fake_critical.json')},
            {'name': 'image:latest-production', 'kind': 'image', 'tag': 'production', 'exit_code': 0,
             'report': str(FIXTURES / 'image_fake_critical.json')},
        ]), encoding='utf-8')
        env = {k: v for k, v in os.environ.items() if k != 'GITHUB_ACTIONS'}
        env.update(env_extra or {})
        return subprocess.run(
            [sys.executable, str(HERE.parent / 'trivy_digest.py'), '--manifest', str(manifest),
             '--run-url', 'unused', '--out-dir', str(Path(tmp.name) / 'cards'), '--text'],
            capture_output=True, text=True, env=env)

    def test_lists_every_finding_including_those_with_no_fix_yet(self):
        result = self.run_text()
        self.assertEqual(0, result.returncode, result.stderr)
        for expected in ('CVE-2099-0001', 'CVE-2099-0002', 'CVE-2099-0004', 'CVE-2099-0005',
                         'libnofix', 'no fix yet', 'development, production'):
            self.assertIn(expected, result.stdout)

    def test_lists_critical_before_high(self):
        out = self.run_text().stdout
        self.assertLess(out.index('CVE-2099-0001'), out.index('CVE-2099-0003'))

    def test_refuses_to_run_in_github_actions_and_prints_no_findings(self):
        result = self.run_text({'GITHUB_ACTIONS': 'true'})
        self.assertNotEqual(0, result.returncode)
        self.assertNotIn('CVE-', result.stdout + result.stderr)


class PostCardsScriptTest(unittest.TestCase):
    """post_cards.sh posts each card file unchanged, as the Teams envelope."""

    @classmethod
    def setUpClass(cls):
        import shutil
        if not all(shutil.which(tool) for tool in ('curl', 'jq')):
            raise unittest.SkipTest('curl and jq are required')
        # On Windows the bash on PATH may be WSL's, which cannot see C:/ paths; prefer Git Bash.
        candidates = []
        git = shutil.which('git')
        if git:
            for parent in Path(git).resolve().parents[:3]:
                candidates.append(parent / 'bin' / 'bash.exe')
        candidates.append(shutil.which('bash'))
        script = (HERE.parent / 'post_cards.sh').as_posix()
        for candidate in filter(None, candidates):
            if not Path(candidate).exists():
                continue
            probe = subprocess.run([str(candidate), '-c', f'test -f "{script}"'])
            if probe.returncode == 0:
                cls.bash = str(candidate)
                return
        raise unittest.SkipTest('no bash that can run post_cards.sh')

    def post(self, **env_extra):
        import http.server
        import os
        import threading
        received = []

        class Handler(http.server.BaseHTTPRequestHandler):
            def do_POST(self):
                received.append(json.loads(self.rfile.read(int(self.headers['Content-Length']))))
                self.send_response(202)
                self.end_headers()

            def log_message(self, *args):
                pass

        server = http.server.HTTPServer(('127.0.0.1', 0), Handler)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.server_close)
        self.addCleanup(server.shutdown)
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        cards = Path(tmp.name)
        scans = [image_scan('development')]
        for index, card in enumerate(td.build_cards(scans, run_url='https://example.invalid/run/1')):
            (cards / f'card-{index:02d}.json').write_text(json.dumps(card), encoding='utf-8')
        env = dict(os.environ, TEAMS_WEBHOOK_URL=f'http://127.0.0.1:{server.server_port}/hook',
                   POST_PAUSE_SECONDS='0', **env_extra)
        script = HERE.parent / 'post_cards.sh'
        result = subprocess.run([self.bash, script.as_posix(), cards.as_posix()],
                                capture_output=True, text=True, env=env)
        return result, received

    def test_posts_the_teams_envelope_with_the_card_in_attachments(self):
        result, received = self.post()
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        for body in received:
            self.assertEqual('message', body['type'])
            self.assertEqual('AdaptiveCard', body['attachments'][0]['content']['type'])

    def test_log_shows_statuses_only(self):
        result, _ = self.post()
        self.assertNotIn('CVE-', result.stdout + result.stderr)
        self.assertIn('HTTP 202', result.stdout)


class PublishedDateTest(unittest.TestCase):
    TODAY = date(2026, 11, 9)

    def report(self, entries):
        """entries: (id, severity, PublishedDate or None) -> a one-package-per-CVE image report."""
        report = many_findings_report(0)
        for index, (cve, severity, published) in enumerate(entries):
            vuln = {'VulnerabilityID': cve, 'PkgName': f'pkg{index}', 'InstalledVersion': '1',
                    'FixedVersion': '2', 'Severity': severity, 'PrimaryURL': 'https://example.invalid/x'}
            if published:
                vuln['PublishedDate'] = published
            report['Results'][0]['Vulnerabilities'].append(vuln)
        return report

    def scan(self, entries, tag='development'):
        return td.Scan(f'image:latest-{tag}', 'image', tag, 0, self.report(entries))

    def order(self, scans):
        return [g.id for g in td.group_findings(scans)]

    def test_published_date_is_read_from_the_report_as_a_plain_date(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', '2026-08-04T15:16:41.443Z')])
        self.assertEqual('2026-08-04', td.group_findings([scan])[0].published)

    def test_malformed_or_missing_dates_become_empty(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', 'not a date'), ('CVE-2099-2002', 'HIGH', None)])
        self.assertEqual(['', ''], [g.published for g in td.group_findings([scan])])

    def test_within_a_severity_the_newest_cve_comes_first(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', '2026-01-01T00:00:00Z'),
                          ('CVE-2099-2002', 'HIGH', '2026-10-01T00:00:00Z'),
                          ('CVE-2099-2003', 'HIGH', '2026-05-01T00:00:00Z')])
        self.assertEqual(['CVE-2099-2002', 'CVE-2099-2003', 'CVE-2099-2001'], self.order([scan]))

    def test_severity_still_beats_age(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', '2026-11-01T00:00:00Z'),
                          ('CVE-2099-2002', 'CRITICAL', '2020-01-01T00:00:00Z')])
        self.assertEqual(['CVE-2099-2002', 'CVE-2099-2001'], self.order([scan]))

    def test_findings_without_a_date_come_last_within_their_severity(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', None),
                          ('CVE-2099-2002', 'HIGH', '2020-01-01T00:00:00Z'),
                          ('CVE-2099-2003', 'CRITICAL', None)])
        self.assertEqual(['CVE-2099-2003', 'CVE-2099-2002', 'CVE-2099-2001'], self.order([scan]))

    def test_number_of_environments_no_longer_affects_the_order(self):
        wide = [self.scan([('CVE-2099-2001', 'HIGH', '2020-01-01T00:00:00Z'),
                           ('CVE-2099-2002', 'HIGH', '2026-10-01T00:00:00Z')], tag=t)
                for t in ('development', 'qa')]
        self.assertEqual(['CVE-2099-2002', 'CVE-2099-2001'], self.order(wide))

    def test_a_date_found_in_any_environment_is_kept_for_the_group(self):
        scans = [self.scan([('CVE-2099-2001', 'HIGH', None)], tag='development'),
                 self.scan([('CVE-2099-2001', 'HIGH', '2026-08-04T00:00:00Z')], tag='qa')]
        self.assertEqual('2026-08-04', td.group_findings(scans)[0].published)

    def test_age_text_gives_date_and_days(self):
        self.assertEqual('Published 2026-08-04 (97 days ago)', td._age('2026-08-04', self.TODAY))
        self.assertEqual('Published 2026-11-08 (1 day ago)', td._age('2026-11-08', self.TODAY))
        self.assertEqual('Published 2026-11-09 (today)', td._age('2026-11-09', self.TODAY))
        self.assertEqual('', td._age('', self.TODAY))

    def test_a_future_date_is_shown_as_today_not_negative_days(self):
        self.assertEqual('Published 2027-01-01 (today)', td._age('2027-01-01', self.TODAY))

    def test_vulnerability_card_shows_the_published_line_before_found_in(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', '2026-08-04T00:00:00Z')])
        text = card_texts(td.build_cards([scan], run_url='https://example.invalid/run/9'))[1]
        self.assertRegex(text, r'Published 2026-08-04 \(\d+ days ago\)\nFound in: development')

    def test_card_without_a_date_has_no_published_line(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', None)])
        text = card_texts(td.build_cards([scan], run_url='https://example.invalid/run/9'))[1]
        self.assertNotIn('Published', text)

    def test_misconfigurations_never_get_a_published_line(self):
        cards = td.build_cards([repo_scan()], run_url='https://example.invalid/run/9')
        text = '\n'.join(card_texts(cards)[1:])
        self.assertIn('DS-0002', text)
        self.assertNotIn('Published', text)

    def test_full_list_includes_the_published_date(self):
        scan = self.scan([('CVE-2099-2001', 'HIGH', '2026-08-04T00:00:00Z')])
        self.assertIn('published 2026-08-04', td.format_full_list([scan]))


if __name__ == '__main__':
    unittest.main()
