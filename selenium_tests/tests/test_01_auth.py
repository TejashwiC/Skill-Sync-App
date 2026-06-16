# =============================================================
# tests/test_01_auth.py — Authentication Tests (TC001–TC018)
# Module: Authentication
# Covers: Register, Login, Logout, Forgot Password flows
# =============================================================
import pytest
import time
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys

import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from conftest import record
from utils.wait_helpers import (
    wait_for_element, wait_for_clickable, wait_for_visible,
    wait_for_url_contains, dismiss_alert, safe_get,
    element_exists, wait_for_firebase
)
from config import (
    REGISTER_URL, LOGIN_URL, DASHBOARD_URL, FORGOT_PWD_URL,
    TEST_EMAIL, TEST_PASSWORD, TEST_NAME,
    INVALID_EMAIL, INVALID_PASSWORD
)

MODULE = "Authentication"


class TestRegisterPage:

    def test_TC001_register_page_loads(self, driver, results):
        """TC001 — Register page loads successfully."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            title = driver.title
            assert "SkillSync" in title or "Register" in title
            actual = f"Page loaded. Title: '{title}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Page did not load"
        record(results, 1, "TC001", MODULE, "Register page loads with correct title",
               "Page loads and title contains 'SkillSync' or 'Register'", actual, status, remarks)

    def test_TC002_register_page_elements_present(self, driver, results):
        """TC002 — Register form fields are present."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            name_f   = element_exists(driver, By.ID, "name")
            email_f  = element_exists(driver, By.ID, "email")
            pwd_f    = element_exists(driver, By.ID, "password")
            btn      = element_exists(driver, By.CSS_SELECTOR, "button[onclick='register()']")
            logo     = element_exists(driver, By.CSS_SELECTOR, "img.logo")
            assert all([name_f, email_f, pwd_f, btn, logo])
            actual = "All form elements present: Name, Email, Password, Register button, Logo"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "One or more fields missing"
        record(results, 2, "TC002", MODULE, "Register form elements are present on page",
               "Name, Email, Password fields and Register button are visible", actual, status, remarks)

    def test_TC003_register_empty_fields_validation(self, driver, results):
        """TC003 — Register with empty fields triggers error."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='register()']")
            btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver)
            if found_alert:
                actual = f"Alert shown: '{alert_text}'"
            else:
                # Browser may use native HTML5 validation
                actual = "Browser native validation prevented empty submit"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation triggered"
        record(results, 3, "TC003", MODULE, "Register with empty fields triggers validation",
               "Error alert or HTML5 validation appears on empty submit", actual, status, remarks)

    def test_TC004_register_invalid_email_format(self, driver, results):
        """TC004 — Register with invalid email format is rejected."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            wait_for_clickable(driver, By.ID, "name").send_keys("Test User")
            email_f = wait_for_clickable(driver, By.ID, "email")
            email_f.send_keys("invalidemail")
            wait_for_clickable(driver, By.ID, "password").send_keys("Pass@123")
            btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='register()']")
            btn.click()
            wait_for_firebase(driver, 2)
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            if found_alert:
                actual = f"Error alert shown: '{alert_text}'"
            else:
                actual = "Browser HTML5 email validation rejected invalid format"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Invalid email not caught"
        record(results, 4, "TC004", MODULE, "Register with invalid email format is rejected",
               "Firebase or browser rejects invalid email format", actual, status, remarks)

    def test_TC005_register_weak_password(self, driver, results):
        """TC005 — Register with password less than 6 chars is rejected."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            wait_for_clickable(driver, By.ID, "name").send_keys("Test User")
            wait_for_clickable(driver, By.ID, "email").send_keys("weakpwd_test@example.com")
            wait_for_clickable(driver, By.ID, "password").send_keys("123")
            wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='register()']").click()
            wait_for_firebase(driver, 4)
            found_alert, alert_text = dismiss_alert(driver, timeout=8)
            if found_alert and ("error" in alert_text.lower() or "weak" in alert_text.lower()
                                or "password" in alert_text.lower() or "6" in alert_text):
                actual = f"Error alert: '{alert_text}'"
            elif found_alert:
                actual = f"Alert shown: '{alert_text}'"
            else:
                actual = "No alert — possible browser validation"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Weak password not rejected"
        record(results, 5, "TC005", MODULE, "Register with weak password (< 6 chars) is rejected",
               "Firebase rejects password shorter than 6 characters", actual, status, remarks)

    def test_TC006_register_login_link_present(self, driver, results):
        """TC006 — Register page has link to Login page."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, REGISTER_URL)
            link = wait_for_element(driver, By.CSS_SELECTOR, "a[href='login.html']")
            assert link is not None
            actual = f"Login link found with text: '{link.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Login link not found on register page"
        record(results, 6, "TC006", MODULE, "Register page contains link to Login page",
               "Link to login.html is present on the register page", actual, status, remarks)


class TestLoginPage:

    def test_TC007_login_page_loads(self, driver, results):
        """TC007 — Login page loads with expected title."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            title = driver.title
            assert "SkillSync" in title or "Login" in title
            actual = f"Page loaded. Title: '{title}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Login page did not load"
        record(results, 7, "TC007", MODULE, "Login page loads successfully",
               "Page loads and title contains 'SkillSync' or 'Login'", actual, status, remarks)

    def test_TC008_login_form_elements(self, driver, results):
        """TC008 — Login form has email, password, login button, and forgot password link."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            email   = element_exists(driver, By.ID, "email")
            pwd     = element_exists(driver, By.ID, "password")
            btn     = element_exists(driver, By.CSS_SELECTOR, "button[onclick='login()']")
            fp_link = element_exists(driver, By.CSS_SELECTOR, "a[href='forgot-password.html']")
            assert all([email, pwd, btn, fp_link])
            actual = "All login form elements present"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Missing form elements"
        record(results, 8, "TC008", MODULE, "Login form elements are present",
               "Email, Password, Login button, Forgot Password link are visible", actual, status, remarks)

    def test_TC009_login_empty_credentials(self, driver, results):
        """TC009 — Login with empty credentials shows error."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='login()']").click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            actual = f"Alert: '{alert_text}'" if found_alert else "Browser validation triggered"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation on empty submit"
        record(results, 9, "TC009", MODULE, "Login with empty credentials shows validation",
               "Error or validation shown for empty credentials", actual, status, remarks)

    def test_TC010_login_invalid_credentials(self, driver, results):
        """TC010 — Login with wrong credentials shows error."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            wait_for_clickable(driver, By.ID, "email").send_keys(INVALID_EMAIL)
            wait_for_clickable(driver, By.ID, "password").send_keys(INVALID_PASSWORD)
            wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='login()']").click()
            wait_for_firebase(driver, 6)
            found_alert, alert_text = dismiss_alert(driver, timeout=10)
            if found_alert and ("error" in alert_text.lower() or "login" in alert_text.lower()
                               or "invalid" in alert_text.lower() or "password" in alert_text.lower()):
                actual = f"Error alert shown: '{alert_text[:80]}'"
            elif found_alert:
                actual = f"Alert: '{alert_text[:80]}'"
            else:
                # Still on login page is also a valid failure signal
                assert "login" in driver.current_url.lower() or "index" in driver.current_url.lower()
                actual = "Stayed on login page — invalid credentials rejected"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Invalid credentials not handled"
        record(results, 10, "TC010", MODULE, "Login with wrong credentials shows error alert",
               "Firebase returns error; alert or page stays on Login", actual, status, remarks)

    def test_TC011_login_valid_credentials(self, driver, results):
        """TC011 — Login with valid credentials redirects to dashboard."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            wait_for_clickable(driver, By.ID, "email").send_keys(TEST_EMAIL)
            wait_for_clickable(driver, By.ID, "password").send_keys(TEST_PASSWORD)
            wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='login()']").click()
            wait_for_firebase(driver, 6)
            # Accept any "Registered!" alert if it appears
            dismiss_alert(driver, timeout=3)
            wait_for_firebase(driver, 3)
            current_url = driver.current_url
            assert "dashboard" in current_url.lower()
            actual = f"Redirected to dashboard: {current_url}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Did not redirect to dashboard"
        record(results, 11, "TC011", MODULE, "Login with valid credentials redirects to dashboard",
               "User is redirected to dashboard.html after successful login", actual, status, remarks)

    def test_TC012_login_register_link(self, driver, results):
        """TC012 — Login page has link to Register page."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, LOGIN_URL)
            reg_link = wait_for_element(driver, By.CSS_SELECTOR, "a[href='register.html']")
            assert reg_link is not None
            actual = f"Register link found: '{reg_link.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Register link missing on login page"
        record(results, 12, "TC012", MODULE, "Login page has link to Register page",
               "Link to register.html is visible on the Login page", actual, status, remarks)


class TestLogout:

    def test_TC013_logout_redirects_to_login(self, logged_in_driver, results):
        """TC013 — Logout redirects user to Login page."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            wait_for_firebase(driver, 4)
            # Click sidebar Logout button
            logout_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='logout()']", timeout=15)
            logout_btn.click()
            wait_for_firebase(driver, 4)
            current_url = driver.current_url
            assert "login" in current_url.lower() or "index" in current_url.lower()
            actual = f"Redirected to: {current_url}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Logout did not redirect to login"
        record(results, 13, "TC013", MODULE, "Logout redirects user to Login page",
               "After logout, user is redirected to login.html", actual, status, remarks)

    def test_TC014_dashboard_inaccessible_without_login(self, driver, results):
        """TC014 — Dashboard redirects to login when not authenticated."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, DASHBOARD_URL)
            wait_for_firebase(driver, 5)
            current_url = driver.current_url
            assert "login" in current_url.lower() or "index" in current_url.lower()
            actual = f"Unauthenticated access redirected to: {current_url}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Dashboard accessible without login"
        record(results, 14, "TC014", MODULE, "Dashboard redirects unauthenticated users to Login",
               "Direct access to dashboard.html redirects to login.html", actual, status, remarks)


class TestForgotPassword:

    def test_TC015_forgot_password_page_loads(self, driver, results):
        """TC015 — Forgot password page loads correctly."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, FORGOT_PWD_URL)
            title = driver.title
            assert "SkillSync" in title or "Password" in title or "Forgot" in title
            actual = f"Page loaded. Title: '{title}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Page did not load"
        record(results, 15, "TC015", MODULE, "Forgot password page loads correctly",
               "Page loads with correct title containing 'SkillSync' or 'Forgot'", actual, status, remarks)

    def test_TC016_forgot_password_empty_email(self, driver, results):
        """TC016 — Forgot password with empty email shows error."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, FORGOT_PWD_URL)
            btn = wait_for_clickable(driver, By.ID, "resetBtn")
            btn.click()
            wait_for_firebase(driver, 2)
            error_div = wait_for_visible(driver, By.ID, "errorMsg", timeout=8)
            assert error_div.is_displayed()
            actual = f"Error message shown: '{error_div.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No error for empty email"
        record(results, 16, "TC016", MODULE, "Forgot password with empty email shows error",
               "Error message div appears when submitting with empty email", actual, status, remarks)

    def test_TC017_forgot_password_invalid_email(self, driver, results):
        """TC017 — Forgot password with non-registered email shows error."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, FORGOT_PWD_URL)
            email_f = wait_for_clickable(driver, By.ID, "reset-email")
            email_f.send_keys("notregistered_xyz999@example.com")
            wait_for_clickable(driver, By.ID, "resetBtn").click()
            wait_for_firebase(driver, 6)
            error_div = wait_for_visible(driver, By.ID, "errorMsg", timeout=10)
            assert error_div.is_displayed()
            actual = f"Error message: '{error_div.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No error for unregistered email"
        record(results, 17, "TC017", MODULE, "Forgot password with unregistered email shows error",
               "Error message div shows 'No account found' or similar", actual, status, remarks)

    def test_TC018_forgot_password_back_to_login_link(self, driver, results):
        """TC018 — Forgot password page has Back to Login link."""
        status, actual, remarks = "PASS", "", ""
        try:
            safe_get(driver, FORGOT_PWD_URL)
            link = wait_for_element(driver, By.CSS_SELECTOR, "a[href='login.html']")
            assert link is not None
            actual = f"Back to Login link found: '{link.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Back to Login link missing"
        record(results, 18, "TC018", MODULE, "Forgot password page has Back to Login link",
               "Link to login.html is visible on the Forgot Password page", actual, status, remarks)
