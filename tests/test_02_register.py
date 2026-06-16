"""
test_02_register.py — Registration Page Tests (14 tests)
Module: Register | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Register")


class TestRegister:

    def test_reg_001_page_loads(self, driver, base_url):
        """REG-001 [Functional]: Register page loads with correct title."""
        try:
            driver.get(f"{base_url}/index.html")
            wait_visible(driver, By.CSS_SELECTOR, ".container")
            assert "SkillSync" in driver.title
            logger.info("Register page loaded successfully")
        except Exception as e:
            take_screenshot(driver, "test_reg_001")
            raise AssertionError(f"Register page load failed: {e}")

    def test_reg_002_logo_displayed(self, driver, base_url):
        """REG-002 [Unit]: SkillSync logo is displayed on register page."""
        try:
            driver.get(f"{base_url}/index.html")
            logo = wait_visible(driver, By.CSS_SELECTOR, "img.logo")
            assert logo.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_reg_002")
            raise AssertionError(f"Logo not displayed: {e}")

    def test_reg_003_subtitle_create_account(self, driver, base_url):
        """REG-003 [Unit]: 'Create Account' subtitle is visible."""
        try:
            driver.get(f"{base_url}/index.html")
            subtitle = wait_visible(driver, By.CSS_SELECTOR, ".subtitle")
            assert "Create Account" in subtitle.text or "Register" in subtitle.text
        except Exception as e:
            take_screenshot(driver, "test_reg_003")
            raise AssertionError(f"Subtitle not found: {e}")

    def test_reg_004_name_field_present(self, driver, base_url):
        """REG-004 [Unit]: Full Name input field is present."""
        try:
            driver.get(f"{base_url}/index.html")
            field = wait_visible(driver, By.ID, "name")
            assert field.is_displayed()
            assert field.get_attribute("placeholder") == "Full Name"
        except Exception as e:
            take_screenshot(driver, "test_reg_004")
            raise AssertionError(f"Name field missing: {e}")

    def test_reg_005_email_field_type(self, driver, base_url):
        """REG-005 [Unit]: Email input has type=email."""
        try:
            driver.get(f"{base_url}/index.html")
            field = wait_visible(driver, By.ID, "email")
            assert field.get_attribute("type") == "email"
        except Exception as e:
            take_screenshot(driver, "test_reg_005")
            raise AssertionError(f"Email field type incorrect: {e}")

    def test_reg_006_password_field_type(self, driver, base_url):
        """REG-006 [Unit]: Password input has type=password."""
        try:
            driver.get(f"{base_url}/index.html")
            field = wait_visible(driver, By.ID, "password")
            assert field.get_attribute("type") == "password"
        except Exception as e:
            take_screenshot(driver, "test_reg_006")
            raise AssertionError(f"Password field type incorrect: {e}")

    def test_reg_007_register_button_visible(self, driver, base_url):
        """REG-007 [Unit]: Register button is present and labeled correctly."""
        try:
            driver.get(f"{base_url}/index.html")
            btn = wait_clickable(driver, By.CSS_SELECTOR, "button")
            assert btn.is_displayed() and "Register" in btn.text
        except Exception as e:
            take_screenshot(driver, "test_reg_007")
            raise AssertionError(f"Register button issue: {e}")

    def test_reg_008_login_link_present(self, driver, base_url):
        """REG-008 [Functional]: 'Login' link for existing users is present."""
        try:
            driver.get(f"{base_url}/index.html")
            link = wait_visible(driver, By.LINK_TEXT, "Login")
            assert link.is_displayed()
        except Exception as e:
            take_screenshot(driver, "test_reg_008")
            raise AssertionError(f"Login link missing: {e}")

    def test_reg_009_login_link_navigates(self, driver, base_url):
        """REG-009 [Functional]: Login link navigates to login.html."""
        try:
            driver.get(f"{base_url}/index.html")
            wait_clickable(driver, By.LINK_TEXT, "Login").click()
            WebDriverWait(driver, 10).until(EC.url_contains("login.html"))
            assert "login.html" in driver.current_url
        except Exception as e:
            take_screenshot(driver, "test_reg_009")
            raise AssertionError(f"Login navigation failed: {e}")

    def test_reg_010_empty_name_html5_validation(self, driver, base_url):
        """REG-010 [Validation]: Empty name fails HTML5 required validation."""
        try:
            driver.get(f"{base_url}/index.html")
            wait_visible(driver, By.ID, "email").send_keys("test@example.com")
            driver.find_element(By.ID, "password").send_keys("Test1234")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            name_f   = driver.find_element(By.ID, "name")
            is_valid = driver.execute_script("return arguments[0].validity.valid;", name_f)
            assert not is_valid, "Empty name should fail HTML5 validation"
        except Exception as e:
            take_screenshot(driver, "test_reg_010")
            raise AssertionError(f"Empty name validation failed: {e}")

    def test_reg_011_invalid_email_format(self, driver, base_url):
        """REG-011 [Validation]: Invalid email format fails HTML5 validation."""
        try:
            driver.get(f"{base_url}/index.html")
            wait_visible(driver, By.ID, "name").send_keys("Test User")
            email_f = driver.find_element(By.ID, "email")
            email_f.send_keys("not-a-valid-email")
            driver.find_element(By.ID, "password").send_keys("Test1234")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            is_valid = driver.execute_script("return arguments[0].validity.valid;", email_f)
            assert not is_valid
        except Exception as e:
            take_screenshot(driver, "test_reg_011")
            raise AssertionError(f"Invalid email format not caught: {e}")

    def test_reg_012_all_inputs_accept_text(self, driver, base_url):
        """REG-012 [Unit]: All three fields accept and retain typed text."""
        try:
            driver.get(f"{base_url}/index.html")
            name_f  = wait_visible(driver, By.ID, "name")
            email_f = driver.find_element(By.ID, "email")
            pwd_f   = driver.find_element(By.ID, "password")
            name_f.send_keys("SkillSync Tester")
            email_f.send_keys("tester@skillsync.com")
            pwd_f.send_keys("Secure@1234")
            assert name_f.get_attribute("value")  == "SkillSync Tester"
            assert email_f.get_attribute("value") == "tester@skillsync.com"
            assert pwd_f.get_attribute("value")   == "Secure@1234"
        except Exception as e:
            take_screenshot(driver, "test_reg_012")
            raise AssertionError(f"Input fields did not retain values: {e}")

    def test_reg_013_css_stylesheet_applied(self, driver, base_url):
        """REG-013 [Unit]: CSS stylesheet is applied to the container."""
        try:
            driver.get(f"{base_url}/index.html")
            container = wait_visible(driver, By.CSS_SELECTOR, ".container")
            bg = driver.execute_script(
                "return window.getComputedStyle(arguments[0]).backgroundColor;", container
            )
            assert bg and bg != "" and bg != "rgba(0, 0, 0, 0)"
        except Exception as e:
            take_screenshot(driver, "test_reg_013")
            raise AssertionError(f"CSS not applied: {e}")

    def test_reg_014_page_has_three_inputs(self, driver, base_url):
        """REG-014 [Unit]: Register page has exactly 3 input fields."""
        try:
            driver.get(f"{base_url}/index.html")
            wait_visible(driver, By.CSS_SELECTOR, ".container")
            inputs = driver.find_elements(By.CSS_SELECTOR, "input")
            assert len(inputs) == 3, f"Expected 3 inputs, found {len(inputs)}"
        except Exception as e:
            take_screenshot(driver, "test_reg_014")
            raise AssertionError(f"Input count mismatch: {e}")
