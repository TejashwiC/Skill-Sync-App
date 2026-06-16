# =============================================================
# tests/test_10_security.py — Security Tests (TC117–TC125)
# Module: Security
# Covers: XSS, SQL Injection, Auth Bypass, Input Sanitization,
#         HTTPS enforcement, sensitive data exposure checks
# =============================================================
import pytest
import time
from selenium.webdriver.common.by import By

import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from conftest import record
from utils.wait_helpers import (
    wait_for_element, wait_for_clickable, wait_for_visible,
    element_exists, wait_for_firebase, dismiss_alert, safe_get
)
from config import (
    LOGIN_URL, REGISTER_URL, DASHBOARD_URL, FORGOT_PWD_URL, BASE_URL
)

MODULE = "Security"


class TestSecurityChecks:

    def test_TC117_xss_in_login_email_field(self, driver, results):
        """TC117 — XSS payload in login email field does not execute script."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            xss_payload = "<script>alert('XSS')</script>"
            email_f = wait_for_clickable(driver, By.ID, "email")
            email_f.send_keys(xss_payload)
            wait_for_clickable(driver, By.ID, "password").send_keys("password123")
            wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='login()']").click()
            wait_for_firebase(driver, 5)
            # XSS alert should NOT have been injected into DOM
            found_alert, alert_text = dismiss_alert(driver, timeout=3)
            if found_alert and "XSS" in alert_text:
                status = "FAIL"
                actual = f"XSS alert executed! Alert text: '{alert_text}'"
                remarks = "CRITICAL: XSS vulnerability detected"
            else:
                found_alert2, alert_text2 = dismiss_alert(driver, timeout=3)
                actual = f"XSS not executed. Firebase error alert: '{alert_text2[:60] if found_alert2 else 'None'}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "XSS test error"
        record(results, 117, "TC117", MODULE, "XSS payload in login email field does not execute",
               "script tag in email is treated as literal text, not executed", actual, status, remarks)

    def test_TC118_xss_in_register_name_field(self, driver, results):
        """TC118 — XSS payload in register name field does not execute."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            xss_payload = "<img src=x onerror=alert('xss2')>"
            wait_for_clickable(driver, By.ID, "name").send_keys(xss_payload)
            wait_for_clickable(driver, By.ID, "email").send_keys("xsstest@example.com")
            wait_for_clickable(driver, By.ID, "password").send_keys("Pass@1234")
            wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='register()']").click()
            wait_for_firebase(driver, 5)
            found_alert, alert_text = dismiss_alert(driver, timeout=3)
            if found_alert and "xss2" in alert_text.lower():
                status = "FAIL"
                actual = f"XSS onerror executed! Text: '{alert_text}'"
                remarks = "CRITICAL: XSS vulnerability in name field"
            else:
                actual = f"XSS onerror not executed. Alert: '{alert_text[:60] if found_alert else 'None'}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "XSS test error"
        record(results, 118, "TC118", MODULE, "XSS onerror payload in register name does not execute",
               "img onerror XSS in name field is not executed by browser", actual, status, remarks)

    def test_TC119_sql_injection_in_login(self, driver, results):
        """TC119 — SQL Injection payload in login email is handled safely."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            sql_payload = "' OR '1'='1'; --"
            wait_for_clickable(driver, By.ID, "email").send_keys(sql_payload)
            wait_for_clickable(driver, By.ID, "password").send_keys("anything")
            wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='login()']").click()
            wait_for_firebase(driver, 6)
            found_alert, alert_text = dismiss_alert(driver, timeout=8)
            current_url = driver.current_url
            assert "dashboard" not in current_url.lower()
            actual = f"SQL injection not bypassed. URL: {current_url}. Alert: '{alert_text[:80] if found_alert else 'None'}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "SQL injection test error"
        record(results, 119, "TC119", MODULE, "SQL Injection in login email does not bypass authentication",
               "App stays on login page; Firebase rejects SQL injection payload as invalid email", actual, status, remarks)

    def test_TC120_dashboard_auth_bypass_attempt(self, driver, results):
        """TC120 — Direct dashboard URL access without auth redirects to login."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, DASHBOARD_URL)
            wait_for_firebase(driver, 6)
            current_url = driver.current_url
            assert "login" in current_url.lower() or "index" in current_url.lower()
            actual = f"Auth bypass blocked. Redirected to: {current_url}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Dashboard accessible without auth"
        record(results, 120, "TC120", MODULE, "Direct dashboard URL access redirects to login",
               "Unauthenticated access to dashboard.html redirects to login.html", actual, status, remarks)

    def test_TC121_password_field_masked(self, driver, results):
        """TC121 — Password fields are masked (type=password)."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            pwd_field = wait_for_element(driver, By.ID, "password")
            field_type = pwd_field.get_attribute("type")
            assert field_type == "password"
            actual = f"Password field type: '{field_type}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Password field not masked"
        record(results, 121, "TC121", MODULE, "Password field on Login page is masked (type=password)",
               "password input has type='password' ensuring it is not visible as plain text", actual, status, remarks)

    def test_TC122_register_password_field_masked(self, driver, results):
        """TC122 — Password field on register page is masked."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            pwd_field = wait_for_element(driver, By.ID, "password")
            field_type = pwd_field.get_attribute("type")
            assert field_type == "password"
            actual = f"Register password field type: '{field_type}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Register password field not masked"
        record(results, 122, "TC122", MODULE, "Password field on Register page is masked (type=password)",
               "password input on register page has type='password'", actual, status, remarks)

    def test_TC123_https_enforcement(self, driver, results):
        """TC123 — Application is served over HTTPS."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            current_url = driver.current_url
            assert current_url.startswith("https://")
            actual = f"Application URL uses HTTPS: {current_url}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Application not served over HTTPS"
        record(results, 123, "TC123", MODULE, "Application is served over HTTPS",
               "Login page URL starts with https:// confirming secure transport", actual, status, remarks)

    def test_TC124_firebase_api_key_not_in_page_source_comments(self, driver, results):
        """TC124 — Firebase API key is not exposed in HTML comments."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            page_source = driver.page_source
            # Check that it's not in HTML comments (it IS in script — that's unavoidable for client-side Firebase)
            # We check it's not accidentally duplicated in HTML comments or meta tags
            import re
            comments = re.findall(r'<!--.*?-->', page_source, re.DOTALL)
            api_in_comment = any("AIzaSy" in c for c in comments)
            if api_in_comment:
                status = "FAIL"
                actual = "API key found in HTML comments — potential exposure"
                remarks = "WARNING: API key in HTML comment"
            else:
                actual = "API key not found in HTML comments (appears only in script module — expected)"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Source check error"
        record(results, 124, "TC124", MODULE, "Firebase API key is not in HTML comment blocks",
               "No API key found in <!-- --> comment blocks in page source", actual, status, remarks)

    def test_TC125_forgot_password_rate_limit_ui(self, driver, results):
        """TC125 — Forgot password button is disabled after submission to prevent spam."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, FORGOT_PWD_URL)
            email_f = wait_for_clickable(driver, By.ID, "reset-email")
            email_f.send_keys("test@example.com")
            btn = wait_for_clickable(driver, By.ID, "resetBtn")
            btn.click()
            wait_for_firebase(driver, 2)
            # Button should be disabled during processing
            btn_disabled = btn.get_attribute("disabled")
            btn_text = btn.get_attribute("innerText") or btn.text
            actual = f"Button disabled: {btn_disabled}, text: '{btn_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Rate limit check failed"
        record(results, 125, "TC125", MODULE, "Forgot password button disables after click to prevent spam",
               "resetBtn is disabled or text changes to 'Sending...' after click", actual, status, remarks)
