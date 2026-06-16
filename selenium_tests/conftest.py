# =============================================================
# conftest.py — Shared pytest fixtures for SkillSync Selenium tests
# =============================================================
import pytest
import sys
import os

sys.path.insert(0, os.path.dirname(__file__))

from utils.driver_setup import create_driver
from utils.wait_helpers import (
    wait_for_element, wait_for_clickable, wait_for_firebase,
    dismiss_alert, safe_get, element_exists
)
from selenium.webdriver.common.by import By
from config import LOGIN_URL, TEST_EMAIL, TEST_PASSWORD, FIREBASE_TIMEOUT

# ── Global result collector ────────────────────────────────
_test_results = []


def pytest_configure(config):
    """Store results list in pytest config so plugins can access it."""
    config._test_results = _test_results


@pytest.fixture(scope="session")
def results():
    """Provide the shared results list to all tests."""
    return _test_results


@pytest.fixture(scope="function")
def driver():
    """Provide a fresh Chrome WebDriver for each test function."""
    d = create_driver()
    yield d
    d.quit()


@pytest.fixture(scope="function")
def logged_in_driver(driver):
    """Provide a WebDriver that is already logged in as TEST_EMAIL."""
    safe_get(driver, LOGIN_URL)
    try:
        email_field = wait_for_clickable(driver, By.ID, "email", timeout=15)
        email_field.clear()
        email_field.send_keys(TEST_EMAIL)

        pwd_field = wait_for_clickable(driver, By.ID, "password", timeout=10)
        pwd_field.clear()
        pwd_field.send_keys(TEST_PASSWORD)

        login_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='login()']", timeout=10)
        login_btn.click()

        # Wait for dashboard to load (Firebase auth)
        wait_for_firebase(driver, 5)
    except Exception as e:
        print(f"[conftest] Login setup failed: {e}")
    yield driver


# ── Helper used by individual tests to record results ─────
def record(results_list, sno, tc_id, module, scenario, expected,
           actual="", status="PASS", remarks=""):
    results_list.append({
        "sno": sno,
        "tc_id": tc_id,
        "module": module,
        "scenario": scenario,
        "expected": expected,
        "actual": actual if actual else expected,
        "status": status,
        "remarks": remarks,
    })


# ── Generate Excel report after all tests complete ─────────
def pytest_sessionfinish(session, exitstatus):
    """Hook: generate Excel report once all tests are done."""
    try:
        from utils.report_generator import generate_report
        if _test_results:
            generate_report(_test_results)
        else:
            print("\n⚠️  No test results collected — Excel report not generated.")
    except Exception as e:
        print(f"\n❌ Report generation failed: {e}")
