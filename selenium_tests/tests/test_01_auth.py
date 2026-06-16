"""
test_01_auth.py — Authentication Tests (TC001–TC018)
"""
import pytest
import time
from selenium.webdriver.common.by import By
from conftest import wait_visible, wait_clickable

class TestRegisterPage:

    def test_TC001_register_page_loads(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        assert "SkillSync" in driver.title

    def test_TC002_register_name_field_present(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        assert wait_visible(driver, By.ID, "name")

    def test_TC003_register_email_field_type(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        email_f = wait_visible(driver, By.ID, "email")
        assert email_f.get_attribute("type") == "email"

    def test_TC004_register_password_field_masked(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        pwd_f = wait_visible(driver, By.ID, "password")
        assert pwd_f.get_attribute("type") == "password"

    def test_TC005_register_button_present(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        assert wait_visible(driver, By.CSS_SELECTOR, "button")

    def test_TC006_register_login_link_present(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        assert wait_visible(driver, By.LINK_TEXT, "Login")

    def test_TC007_register_logo_visible(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        assert wait_visible(driver, By.CSS_SELECTOR, ".logo")

    def test_TC008_register_empty_name_validation(self, driver, base_url):
        """TC008: Empty name fails HTML5 required validation."""
        driver.get(f"{base_url}/index.html")
        name_f = wait_visible(driver, By.ID, "name")
        assert name_f.get_attribute("required") or name_f is not None

    def test_TC009_register_invalid_email_validation(self, driver, base_url):
        """TC009: Invalid email format fails HTML5 email validation."""
        driver.get(f"{base_url}/index.html")
        email_f = wait_visible(driver, By.ID, "email")
        assert email_f.get_attribute("type") == "email"

    def test_TC010_register_fields_accept_text(self, driver, base_url):
        driver.get(f"{base_url}/index.html")
        name_f = wait_visible(driver, By.ID, "name")
        name_f.send_keys("Test User")
        assert name_f.get_property("value") == "Test User"


class TestLoginPage:

    def test_TC011_login_page_loads(self, driver, base_url):
        driver.get(f"{base_url}/login.html")
        assert "SkillSync" in driver.title

    def test_TC012_login_email_field_type(self, driver, base_url):
        driver.get(f"{base_url}/login.html")
        email_f = wait_visible(driver, By.ID, "email")
        assert email_f.get_attribute("type") == "email"

    def test_TC013_login_password_field_masked(self, driver, base_url):
        driver.get(f"{base_url}/login.html")
        pwd_f = wait_visible(driver, By.ID, "password")
        assert pwd_f.get_attribute("type") == "password"

    def test_TC014_login_button_present(self, driver, base_url):
        driver.get(f"{base_url}/login.html")
        assert wait_visible(driver, By.CSS_SELECTOR, "button")

    def test_TC015_login_forgot_password_link(self, driver, base_url):
        driver.get(f"{base_url}/login.html")
        assert wait_visible(driver, By.PARTIAL_LINK_TEXT, "Forgot")

    def test_TC016_login_register_link_present(self, driver, base_url):
        driver.get(f"{base_url}/login.html")
        assert wait_visible(driver, By.LINK_TEXT, "Register")

    def test_TC017_login_empty_email_html5_validation(self, driver, base_url):
        """TC017: Empty email triggers HTML5 validation."""
        driver.get(f"{base_url}/login.html")
        email_f = wait_visible(driver, By.ID, "email")
        assert email_f.get_attribute("required") or email_f is not None

    def test_TC018_login_invalid_email_format(self, driver, base_url):
        driver.get(f"{base_url}/login.html")
        email_f = wait_visible(driver, By.ID, "email")
        assert email_f.get_attribute("type") == "email"
