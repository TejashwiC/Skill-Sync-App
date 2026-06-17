"""
test_12_api_unit.py — API & Unit Tests for SkillSync (15 tests)
Module: API Unit | Type: Unit / API
Tests run against the LIVE GitHub Pages deployment + Firebase endpoints.
No Selenium driver required — pure HTTP checks using requests.
"""
import logging
import pytest
import requests
from bs4 import BeautifulSoup

logger = logging.getLogger("SkillSync-E2E.APIUnit")

# ── Live site base URL ─────────────────────────────────────────────────────
LIVE_URL      = "https://tejashwic.github.io/Skill-Sync-App"
FIREBASE_HOST = "https://firestore.googleapis.com"
FIREBASE_AUTH = "https://identitytoolkit.googleapis.com"
FIREBASE_PROJECT_ID = "skillsync-dc26c"
FIREBASE_API_KEY    = "AIzaSyDcvwPSTgRqQB9gSzpk4u3eBJerNpgUr7U"

TIMEOUT = 15

# ── Shared session for connection reuse ───────────────────────────────────
@pytest.fixture(scope="module")
def session():
    s = requests.Session()
    s.headers.update({"User-Agent": "SkillSync-TestSuite/1.0"})
    yield s
    s.close()


# ─────────────────────────────────────────────────────────────────────────
class TestAPIUnit:

    # ── TC-API-001: Login page returns HTTP 200 ───────────────────────────
    def test_api_001_login_page_status(self, session):
        """API-001 [Unit]: login.html returns HTTP 200 from GitHub Pages."""
        url = f"{LIVE_URL}/login.html"
        resp = session.get(url, timeout=TIMEOUT)
        logger.info(f"GET {url} → {resp.status_code}")
        assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"

    # ── TC-API-002: Index page returns HTTP 200 ───────────────────────────
    def test_api_002_index_page_status(self, session):
        """API-002 [Unit]: index.html (register) returns HTTP 200."""
        url = f"{LIVE_URL}/index.html"
        resp = session.get(url, timeout=TIMEOUT)
        logger.info(f"GET {url} → {resp.status_code}")
        assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"

    # ── TC-API-003: Dashboard page returns HTTP 200 ───────────────────────
    def test_api_003_dashboard_page_status(self, session):
        """API-003 [Unit]: dashboard.html returns HTTP 200."""
        url = f"{LIVE_URL}/dashboard.html"
        resp = session.get(url, timeout=TIMEOUT)
        logger.info(f"GET {url} → {resp.status_code}")
        assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"

    # ── TC-API-004: Forgot-password page returns HTTP 200 ─────────────────
    def test_api_004_forgot_password_page_status(self, session):
        """API-004 [Unit]: forgot-password.html returns HTTP 200."""
        url = f"{LIVE_URL}/forgot-password.html"
        resp = session.get(url, timeout=TIMEOUT)
        logger.info(f"GET {url} → {resp.status_code}")
        assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"

    # ── TC-API-005: app.js asset is served ───────────────────────────────
    def test_api_005_appjs_served(self, session):
        """API-005 [Unit]: app.js is served with status 200."""
        url = f"{LIVE_URL}/app.js"
        resp = session.get(url, timeout=TIMEOUT)
        logger.info(f"GET {url} → {resp.status_code}")
        assert resp.status_code == 200, f"app.js not served: {resp.status_code}"

    # ── TC-API-006: style.css asset is served ────────────────────────────
    def test_api_006_css_served(self, session):
        """API-006 [Unit]: style.css is served with status 200."""
        url = f"{LIVE_URL}/style.css"
        resp = session.get(url, timeout=TIMEOUT)
        logger.info(f"GET {url} → {resp.status_code}")
        assert resp.status_code == 200, f"style.css not served: {resp.status_code}"

    # ── TC-API-007: app.js has correct Content-Type ───────────────────────
    def test_api_007_appjs_content_type(self, session):
        """API-007 [Unit]: app.js is served with a JS content-type header."""
        url = f"{LIVE_URL}/app.js"
        resp = session.get(url, timeout=TIMEOUT)
        ct = resp.headers.get("Content-Type", "")
        logger.info(f"Content-Type for app.js: {ct}")
        assert "javascript" in ct or "text/plain" in ct or "application/octet-stream" in ct, \
            f"Unexpected content-type: {ct}"

    # ── TC-API-008: login.html has correct <title> ───────────────────────
    def test_api_008_login_html_title(self, session):
        """API-008 [Unit]: login.html has a <title> tag containing 'SkillSync'."""
        url = f"{LIVE_URL}/login.html"
        resp = session.get(url, timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        title = soup.find("title")
        assert title is not None, "<title> tag missing from login.html"
        assert "SkillSync" in title.text, f"Title doesn't mention SkillSync: '{title.text}'"

    # ── TC-API-009: login.html has charset meta tag ───────────────────────
    def test_api_009_login_html_charset(self, session):
        """API-009 [Unit]: login.html declares a charset in <meta> tag."""
        url = f"{LIVE_URL}/login.html"
        resp = session.get(url, timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        charset = soup.find("meta", charset=True) or soup.find("meta", attrs={"http-equiv": "Content-Type"})
        assert charset is not None, "No charset meta tag found in login.html"

    # ── TC-API-010: index.html has meta viewport ──────────────────────────
    def test_api_010_index_html_viewport(self, session):
        """API-010 [Unit]: index.html has a viewport meta tag (mobile-ready)."""
        url = f"{LIVE_URL}/index.html"
        resp = session.get(url, timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        viewport = soup.find("meta", attrs={"name": "viewport"})
        assert viewport is not None, "No viewport meta tag in index.html"

    # ── TC-API-011: Firebase Firestore endpoint reachable ─────────────────
    def test_api_011_firebase_firestore_reachable(self, session):
        """API-011 [API]: Firebase Firestore REST endpoint responds (not 5xx)."""
        url = (
            f"{FIREBASE_HOST}/v1/projects/{FIREBASE_PROJECT_ID}"
            f"/databases/(default)/documents/users"
            f"?key={FIREBASE_API_KEY}&pageSize=1"
        )
        try:
            resp = session.get(url, timeout=TIMEOUT)
            logger.info(f"Firestore REST → {resp.status_code}")
            # 200 = success, 400/403 = expected auth rejection — both prove endpoint is live
            assert resp.status_code in (200, 400, 401, 403), \
                f"Unexpected status from Firestore: {resp.status_code}"
        except requests.ConnectionError as e:
            pytest.skip(f"Network unavailable: {e}")

    # ── TC-API-012: Firebase Auth endpoint reachable ──────────────────────
    def test_api_012_firebase_auth_reachable(self, session):
        """API-012 [API]: Firebase Auth REST endpoint responds (not 5xx)."""
        url = f"{FIREBASE_AUTH}/v1/accounts:signInWithPassword?key={FIREBASE_API_KEY}"
        try:
            resp = session.post(url, json={}, timeout=TIMEOUT)
            logger.info(f"Firebase Auth REST → {resp.status_code}")
            # 400 = missing fields (expected), proves endpoint is alive
            assert resp.status_code in (200, 400, 401, 403), \
                f"Unexpected status from Firebase Auth: {resp.status_code}"
        except requests.ConnectionError as e:
            pytest.skip(f"Network unavailable: {e}")

    # ── TC-API-013: 404 for non-existent page ─────────────────────────────
    def test_api_013_nonexistent_page_returns_404(self, session):
        """API-013 [Unit]: A non-existent page returns a 404 response."""
        url = f"{LIVE_URL}/this-page-does-not-exist-xyz.html"
        resp = session.get(url, timeout=TIMEOUT)
        logger.info(f"GET non-existent → {resp.status_code}")
        assert resp.status_code == 404, f"Expected 404 for non-existent page, got {resp.status_code}"

    # ── TC-API-014: login.html contains an email input field ─────────────
    def test_api_014_login_html_has_email_input(self, session):
        """API-014 [Unit]: login.html HTML source contains an email input element."""
        url = f"{LIVE_URL}/login.html"
        resp = session.get(url, timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        email_input = soup.find("input", attrs={"type": "email"}) or soup.find("input", attrs={"id": "email"})
        assert email_input is not None, "No email input found in login.html source"

    # ── TC-API-015: dashboard.html links to app.js ────────────────────────
    def test_api_015_dashboard_links_appjs(self, session):
        """API-015 [Unit]: dashboard.html source references app.js script."""
        url = f"{LIVE_URL}/dashboard.html"
        resp = session.get(url, timeout=TIMEOUT)
        assert "app.js" in resp.text, "dashboard.html does not reference app.js"
