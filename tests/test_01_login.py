"""
test_01_login.py — Login Page Tests (16 tests)
Module: Login | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Login")


class TestLogin:

    def test_log_001_page_loads(self, driver, base_url):
        """LOG-001 [Functional]: Login page loads with SkillSync title."""
        try:
            driver.get(f"{base_url}/login.html")
            wait_visible(driver, By.CSS_SELECTOR, ".container")
            assert "SkillSync" in driver.title
            logger.info("Login page title verified")
        except Exception as e:
            take_screenshot(driver, "test_log_001")
            raise AssertionError(f"Login page failed to load: {e}")

    def test_log_002_logo_visible(self, driver, base_url):
        """LOG-002 [Unit]: SkillSync logo is displayed."""
        try:
            driver.get(f"{base_url}/login.html")
            logo = wait_visible(driver, By.CSS_SELECTOR, "img.logo")
            assert logo.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_log_002")
            raise AssertionError(f"Logo not visible: {e}")

    def test_log_003_subtitle_shown(self, driver, base_url):
        """LOG-003 [Unit]: 'Login to continue' subtitle is displayed."""
        try:
            driver.get(f"{base_url}/login.html")
            subtitle = wait_visible(driver, By.CSS_SELECTOR, ".subtitle")
            assert "Login" in subtitle.text or "continue" in subtitle.text.lower()
        except Exception as e:
            take_screenshot(driver, "test_log_003")
            raise AssertionError(f"Subtitle not found: {e}")

    def test_log_004_email_field_present(self, driver, base_url):
        """LOG-004 [Unit]: Email input field has correct type."""
        try:
            driver.get(f"{base_url}/login.html")
            field = wait_visible(driver, By.ID, "email")
            assert field.get_attribute("type") == "email"
        except Exception as e:
            take_screenshot(driver, "test_log_004")
            raise AssertionError(f"Email field issue: {e}")

    def test_log_005_password_field_present(self, driver, base_url):
        """LOG-005 [Unit]: Password input has type=password."""
        try:
            driver.get(f"{base_url}/login.html")
            field = wait_visible(driver, By.ID, "password")
            assert field.get_attribute("type") == "password"
        except Exception as e:
            take_screenshot(driver, "test_log_005")
            raise AssertionError(f"Password field issue: {e}")

    def test_log_006_login_button_present(self, driver, base_url):
        """LOG-006 [Unit]: Login button is visible and enabled."""
        try:
            driver.get(f"{base_url}/login.html")
            btn = wait_clickable(driver, By.CSS_SELECTOR, "button")
            assert btn.is_displayed() and "Login" in btn.text
        except Exception as e:
            take_screenshot(driver, "test_log_006")
            raise AssertionError(f"Login button issue: {e}")

    def test_log_007_forgot_password_link(self, driver, base_url):
        """LOG-007 [Functional]: Forgot Password link is present."""
        try:
            driver.get(f"{base_url}/login.html")
            link = wait_visible(driver, By.LINK_TEXT, "Forgot Password?")
            assert link.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_log_007")
            raise AssertionError(f"Forgot Password link missing: {e}")

    def test_log_008_forgot_password_navigates(self, driver, base_url):
        """LOG-008 [Functional]: Forgot Password link navigates correctly."""
        try:
            driver.get(f"{base_url}/login.html")
            wait_clickable(driver, By.LINK_TEXT, "Forgot Password?").click()
            WebDriverWait(driver, 10).until(EC.url_contains("forgot-password"))
            assert "forgot-password" in driver.current_url
        except Exception as e:
            take_screenshot(driver, "test_log_008")
            raise AssertionError(f"Navigation failed: {e}")

    def test_log_009_register_link_present(self, driver, base_url):
        """LOG-009 [Functional]: Register link is present for new users."""
        try:
            driver.get(f"{base_url}/login.html")
            link = wait_visible(driver, By.LINK_TEXT, "Register")
            assert link.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_log_009")
            raise AssertionError(f"Register link missing: {e}")

    def test_log_010_register_link_navigates(self, driver, base_url):
        """LOG-010 [Functional]: Register link navigates to registration page."""
        try:
            driver.get(f"{base_url}/login.html")
            wait_clickable(driver, By.LINK_TEXT, "Register").click()
            WebDriverWait(driver, 10).until(
                lambda d: "index" in d.current_url or d.current_url.endswith("/")
            )
            assert "index" in driver.current_url or driver.current_url.endswith("/")
        except Exception as e:
            take_screenshot(driver, "test_log_010")
            raise AssertionError(f"Register navigation failed: {e}")

    def test_log_011_empty_email_html5_validation(self, driver, base_url):
        """LOG-011 [Validation]: Empty email fails HTML5 required validation."""
        try:
            driver.get(f"{base_url}/login.html")
            wait_visible(driver, By.ID, "password").send_keys("SomePass123")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            field    = driver.find_element(By.ID, "email")
            is_valid = driver.execute_script("return arguments[0].validity.valid;", field)
            assert not is_valid, "Empty email should fail HTML5 validation"
        except Exception as e:
            take_screenshot(driver, "test_log_011")
            raise AssertionError(f"Empty email validation failed: {e}")

    def test_log_012_invalid_email_format(self, driver, base_url):
        """LOG-012 [Validation]: Malformed email fails HTML5 email validation."""
        try:
            driver.get(f"{base_url}/login.html")
            email_f = wait_visible(driver, By.ID, "email")
            email_f.send_keys("not-an-email")
            driver.find_element(By.ID, "password").send_keys("Pass123")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            is_valid = driver.execute_script("return arguments[0].validity.valid;", email_f)
            assert not is_valid
        except Exception as e:
            take_screenshot(driver, "test_log_012")
            raise AssertionError(f"Invalid email format check failed: {e}")

    def test_log_013_wrong_credentials_alert(self, driver, base_url):
        """LOG-013 [Validation]: Wrong credentials trigger Firebase error alert."""
        try:
            driver.get(f"{base_url}/login.html")
            wait_visible(driver, By.ID, "email").send_keys("wrong_user_xyz@skillsync.com")
            driver.find_element(By.ID, "password").send_keys("WrongPass999!")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            alert_text = dismiss_alert_if_present(driver, timeout=8)
            assert alert_text is not None and ("Error" in alert_text or "error" in alert_text.lower())
        except Exception as e:
            take_screenshot(driver, "test_log_013")
            raise AssertionError(f"Wrong credentials alert not triggered: {e}")

    def test_log_014_inputs_accept_text(self, driver, base_url):
        """LOG-014 [Unit]: Email and password fields retain typed values."""
        try:
            driver.get(f"{base_url}/login.html")
            email_f = wait_visible(driver, By.ID, "email")
            pwd_f   = driver.find_element(By.ID, "password")
            email_f.send_keys("tester@skillsync.com")
            pwd_f.send_keys("TestPass123")
            assert email_f.get_attribute("value") == "tester@skillsync.com"
            assert pwd_f.get_attribute("value")   == "TestPass123"
        except Exception as e:
            take_screenshot(driver, "test_log_014")
            raise AssertionError(f"Input fields did not retain values: {e}")

    def test_log_015_inputs_clearable(self, driver, base_url):
        """LOG-015 [Unit]: Fields can be cleared and refilled."""
        try:
            driver.get(f"{base_url}/login.html")
            email_f = wait_visible(driver, By.ID, "email")
            email_f.send_keys("first@example.com")
            email_f.clear()
            email_f.send_keys("second@example.com")
            assert email_f.get_attribute("value") == "second@example.com"
        except Exception as e:
            take_screenshot(driver, "test_log_015")
            raise AssertionError(f"Field clear/refill failed: {e}")

    def test_log_016_valid_login_redirects(self, driver, base_url, credentials):
        """LOG-016 [Functional]: Valid credentials redirect to dashboard."""
        if not credentials["email"] or not credentials["password"]:
            pytest.skip("TEST_EMAIL / TEST_PASSWORD not configured")
        try:
            driver.get(f"{base_url}/login.html")
            wait_visible(driver, By.ID, "email").send_keys(credentials["email"])
            driver.find_element(By.ID, "password").send_keys(credentials["password"])
            driver.find_element(By.CSS_SELECTOR, "button").click()
            dismiss_alert_if_present(driver, timeout=3)
            WebDriverWait(driver, 20).until(EC.url_contains("dashboard.html"))
            assert "dashboard.html" in driver.current_url
        except Exception as e:
            take_screenshot(driver, "test_log_016")
            raise AssertionError(f"Valid login did not redirect: {e}")
