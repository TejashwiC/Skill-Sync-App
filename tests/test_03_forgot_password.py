"""
test_03_forgot_password.py — Forgot Password Tests (13 tests)
Module: Forgot Password | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.ForgotPassword")


class TestForgotPassword:

    def test_fp_001_page_loads(self, driver, base_url):
        """FP-001 [Functional]: Forgot Password page loads correctly."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            wait_visible(driver, By.CSS_SELECTOR, ".container")
            assert "SkillSync" in driver.title
        except Exception as e:
            take_screenshot(driver, "test_fp_001")
            raise AssertionError(f"Page load failed: {e}")

    def test_fp_002_logo_visible(self, driver, base_url):
        """FP-002 [Unit]: Logo is displayed on forgot password page."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            logo = wait_visible(driver, By.CSS_SELECTOR, "img.logo")
            assert logo.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_fp_002")
            raise AssertionError(f"Logo not visible: {e}")

    def test_fp_003_subtitle_shown(self, driver, base_url):
        """FP-003 [Unit]: Reset-related subtitle is displayed."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            subtitle = wait_visible(driver, By.CSS_SELECTOR, ".subtitle")
            assert len(subtitle.text.strip()) > 0
        except Exception as e:
            take_screenshot(driver, "test_fp_003")
            raise AssertionError(f"Subtitle not found: {e}")

    def test_fp_004_email_field_present(self, driver, base_url):
        """FP-004 [Unit]: Email input field is present with type=email."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            field = wait_visible(driver, By.ID, "reset-email")
            assert field.get_attribute("type") == "email"
        except Exception as e:
            take_screenshot(driver, "test_fp_004")
            raise AssertionError(f"Email field issue: {e}")

    def test_fp_005_send_reset_button_present(self, driver, base_url):
        """FP-005 [Unit]: 'Send Reset Link' button is present."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            btn = wait_visible(driver, By.ID, "resetBtn")
            assert btn.is_displayed() and len(btn.text) > 0
        except Exception as e:
            take_screenshot(driver, "test_fp_005")
            raise AssertionError(f"Reset button not found: {e}")

    def test_fp_006_success_message_hidden_default(self, driver, base_url):
        """FP-006 [Unit]: Success message is hidden on page load."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            wait_visible(driver, By.CSS_SELECTOR, ".container")
            success = driver.find_element(By.ID, "successMsg")
            assert not success.is_displayed(), "Success message should be hidden initially"
        except Exception as e:
            take_screenshot(driver, "test_fp_006")
            raise AssertionError(f"Success message visible on load: {e}")

    def test_fp_007_error_message_hidden_default(self, driver, base_url):
        """FP-007 [Unit]: Error message is hidden on page load."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            wait_visible(driver, By.CSS_SELECTOR, ".container")
            error = driver.find_element(By.ID, "errorMsg")
            assert not error.is_displayed(), "Error message should be hidden initially"
        except Exception as e:
            take_screenshot(driver, "test_fp_007")
            raise AssertionError(f"Error message visible on load: {e}")

    def test_fp_008_empty_email_shows_error(self, driver, base_url):
        """FP-008 [Validation]: Submitting empty email shows error message."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            wait_clickable(driver, By.ID, "resetBtn").click()
            time.sleep(2)
            error = driver.find_element(By.ID, "errorMsg")
            assert error.is_displayed() and len(error.text.strip()) > 0
        except Exception as e:
            take_screenshot(driver, "test_fp_008")
            raise AssertionError(f"Empty email error not shown: {e}")

    def test_fp_009_invalid_email_format(self, driver, base_url):
        """FP-009 [Validation]: Invalid email format is caught."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            email_f = wait_visible(driver, By.ID, "reset-email")
            email_f.send_keys("invalid-format")
            driver.find_element(By.ID, "resetBtn").click()
            time.sleep(2)
            is_valid = driver.execute_script("return arguments[0].validity.valid;", email_f)
            error    = driver.find_element(By.ID, "errorMsg")
            assert not is_valid or error.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_fp_009")
            raise AssertionError(f"Invalid email not caught: {e}")

    def test_fp_010_back_to_login_link(self, driver, base_url):
        """FP-010 [Functional]: 'Back to Login' link is present."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            link = wait_visible(driver, By.LINK_TEXT, "← Back to Login")
            assert link.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_fp_010")
            raise AssertionError(f"Back to Login link missing: {e}")

    def test_fp_011_back_to_login_navigates(self, driver, base_url):
        """FP-011 [Functional]: Back to Login navigates to login.html."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            wait_clickable(driver, By.LINK_TEXT, "← Back to Login").click()
            WebDriverWait(driver, 10).until(EC.url_contains("login.html"))
            assert "login.html" in driver.current_url
        except Exception as e:
            take_screenshot(driver, "test_fp_011")
            raise AssertionError(f"Navigation to login failed: {e}")

    def test_fp_012_email_field_accepts_text(self, driver, base_url):
        """FP-012 [Unit]: Email field accepts and retains typed text."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            field = wait_visible(driver, By.ID, "reset-email")
            field.send_keys("user@skillsync.com")
            assert field.get_attribute("value") == "user@skillsync.com"
        except Exception as e:
            take_screenshot(driver, "test_fp_012")
            raise AssertionError(f"Email field value retention failed: {e}")

    def test_fp_013_page_has_one_input(self, driver, base_url):
        """FP-013 [Unit]: Forgot Password page has exactly 1 input field."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            wait_visible(driver, By.CSS_SELECTOR, ".container")
            inputs = driver.find_elements(By.CSS_SELECTOR, "input")
            assert len(inputs) == 1, f"Expected 1 input, found {len(inputs)}"
        except Exception as e:
            take_screenshot(driver, "test_fp_013")
            raise AssertionError(f"Input count wrong: {e}")
