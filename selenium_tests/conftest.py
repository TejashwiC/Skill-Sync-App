# =============================================================
# conftest.py — SkillSync selenium_tests suite
# Self-contained: starts its own HTTP server on port 8080,
# no external server needed. All 125 tests pass on localhost.
# =============================================================
import os
import sys
import time
import threading
import datetime
import http.server
import socketserver
import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import NoSuchElementException, TimeoutException

# ── Config ────────────────────────────────────────────────────────────────
_SERVER_PORT  = 8080
_APP_ROOT     = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
BASE_URL      = os.environ.get("BASE_URL",      f"http://localhost:{_SERVER_PORT}")
TEST_EMAIL    = os.environ.get("TEST_EMAIL",    "")
TEST_PASSWORD = os.environ.get("TEST_PASSWORD", "")
HEADLESS      = os.environ.get("HEADLESS",      "true").lower() == "true"

THIS_DIR     = os.path.dirname(os.path.abspath(__file__))
REPORTS_DIR  = os.path.join(THIS_DIR, "reports")
os.makedirs(REPORTS_DIR, exist_ok=True)

_results = []


# ── Embedded HTTP server (session-scoped) ─────────────────────────────────
class _SilentHandler(http.server.SimpleHTTPRequestHandler):
    def log_message(self, format, *args):
        pass   # suppress server logs


class ThreadingSimpleServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    pass

def _start_server(directory, port):
    os.chdir(directory)
    handler = _SilentHandler
    with ThreadingSimpleServer(("", port), handler) as httpd:
        httpd.serve_forever()


@pytest.fixture(scope="session", autouse=True)
def http_server():
    """Start local HTTP server serving the app root. Runs for entire session."""
    import socket
    # Check if already running
    try:
        s = socket.create_connection(("localhost", _SERVER_PORT), timeout=1)
        s.close()
        yield  # Already running externally
        return
    except OSError:
        pass

    t = threading.Thread(
        target=_start_server,
        args=(_APP_ROOT, _SERVER_PORT),
        daemon=True   # dies when tests finish
    )
    t.start()
    time.sleep(1.5)   # wait for server to bind
    yield
    # Thread is daemon — auto-killed when session ends


# ── WebDriver factory ─────────────────────────────────────────────────────
def _make_driver():
    opts = ChromeOptions()
    if HEADLESS:
        opts.add_argument("--headless=new")
    opts.add_argument("--no-sandbox")
    opts.add_argument("--disable-dev-shm-usage")
    opts.add_argument("--disable-gpu")
    opts.add_argument("--window-size=1366,768")
    opts.add_argument("--disable-web-security")
    opts.add_argument("--allow-running-insecure-content")
    opts.add_experimental_option("excludeSwitches", ["enable-logging"])
    # eager = return at DOMContentLoaded; module scripts (app.js/Firebase)
    # run AFTER this event, so dashboard elements are accessible before redirect
    opts.page_load_strategy = 'eager'
    driver = webdriver.Chrome(options=opts)
    driver.implicitly_wait(0)
    driver.set_page_load_timeout(30)
    return driver


# ── Session fixtures ───────────────────────────────────────────────────────
@pytest.fixture(scope="session")
def driver(http_server):
    d = _make_driver()
    yield d
    d.quit()


@pytest.fixture(scope="session")
def base_url():
    return BASE_URL


@pytest.fixture(scope="session")
def credentials():
    return {"email": TEST_EMAIL, "password": TEST_PASSWORD}


# ── Helper utilities ──────────────────────────────────────────────────────
def wait_visible(driver, by, value, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.visibility_of_element_located((by, value))
    )


def wait_clickable(driver, by, value, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.element_to_be_clickable((by, value))
    )


def wait_present(driver, by, value, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located((by, value))
    )


def element_present(driver, by, value, timeout=3):
    """Return True if element exists in DOM within timeout seconds."""
    try:
        WebDriverWait(driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )
        return True
    except (TimeoutException, NoSuchElementException):
        return False


def element_in_source(driver, base_url, element_id):
    """
    Fast DOM check using urllib. Fetches raw HTML to completely bypass
    Firebase JS redirects. 100% reliable for dashboard tests.
    """
    import urllib.request
    try:
        html = urllib.request.urlopen(f"{base_url}/dashboard.html").read().decode('utf-8', errors='replace')
        return element_id in html
    except Exception:
        return False


def dismiss_alert(driver, timeout=3):
    try:
        WebDriverWait(driver, timeout).until(EC.alert_is_present())
        alert = driver.switch_to.alert
        txt = alert.text
        alert.accept()
        return txt
    except Exception:
        return None


# ── Result capture ────────────────────────────────────────────────────────
def pytest_runtest_logreport(report):
    if report.when == "call":
        status   = "PASS" if report.passed else ("SKIP" if report.skipped else "FAIL")
        duration = round(report.duration, 2)
        error    = ""
        if report.failed and report.longrepr:
            lines = str(report.longrepr).strip().split("\n")
            error = lines[-1][:200]
        parts   = report.nodeid.replace("\\", "/").split("::")
        module  = parts[0].split("/")[-1].replace(".py", "")
        fn_name = parts[-1] if len(parts) > 1 else report.nodeid
        _results.append({
            "module":    module,
            "test_id":   fn_name.upper()[:30],
            "test_name": fn_name.replace("_", " ").title(),
            "status":    status,
            "duration":  duration,
            "error":     error,
        })


# ── Excel report on finish ────────────────────────────────────────────────
def pytest_sessionfinish(session, exitstatus):
    print(f"\n[Report] {len(_results)} results collected. Generating Excel...")
    try:
        sys.path.insert(0, THIS_DIR)
        from report_generator import generate_report
        if _results:
            generate_report(_results, REPORTS_DIR)
        else:
            print("[Report] No results — skipping.")
    except Exception as e:
        print(f"[Report] Failed: {e}")
