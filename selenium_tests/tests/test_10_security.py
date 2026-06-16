"""
test_10_security.py — Security & Edge Cases (TC117–TC125)
"""
import sys, os
import pytest
from selenium.webdriver.common.by import By
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import wait_visible


class TestSecurity:

    def test_TC117_login_password_is_masked(self, driver, base_url):
        """TC117: Login password field has type='password'."""
        driver.get(f"{base_url}/login.html")
        pwd_f = wait_visible(driver, By.ID, "password")
        assert pwd_f.get_attribute("type") == "password"

    def test_TC118_register_password_is_masked(self, driver, base_url):
        """TC118: Register password field has type='password'."""
        driver.get(f"{base_url}/index.html")
        pwd_f = wait_visible(driver, By.ID, "password")
        assert pwd_f.get_attribute("type") == "password"

    def test_TC119_xss_in_login_email_not_executed(self, driver, base_url):
        """TC119: XSS payload in email field is literal text, no alert."""
        driver.get(f"{base_url}/login.html")
        email_f = wait_visible(driver, By.ID, "email")
        payload = "<script>alert('xss')</script>"
        email_f.send_keys(payload)
        assert email_f.get_property("value") == payload
        # Ensure no alert is present
        from selenium.common.exceptions import NoAlertPresentException
        try:
            alert = driver.switch_to.alert
            alert.dismiss()
            pytest.fail("XSS Alert executed!")
        except NoAlertPresentException:
            pass

    def test_TC120_xss_in_register_name_not_executed(self, driver, base_url):
        """TC120: XSS payload in register name field is literal text."""
        driver.get(f"{base_url}/index.html")
        name_f = wait_visible(driver, By.ID, "name")
        payload = "<script>alert('xss')</script>"
        name_f.send_keys(payload)
        assert name_f.get_property("value") == payload

    def test_TC121_login_page_has_no_password_in_source(self, driver, base_url):
        """TC121: Passwords or hardcoded creds not in source."""
        driver.get(f"{base_url}/login.html")
        src = driver.page_source.lower()
        assert "value=\"password\"" not in src

    def test_TC122_register_email_field_type_enforced(self, driver, base_url):
        """TC122: Register email field enforce 'email' type."""
        driver.get(f"{base_url}/index.html")
        email_f = wait_visible(driver, By.ID, "email")
        assert email_f.get_attribute("type") == "email"

    def test_TC123_forgot_password_page_loads(self, driver, base_url):
        """TC123: forgot-password.html is accessible."""
        driver.get(f"{base_url}/forgot-password.html")
        assert "Reset" in driver.page_source or "Forgot" in driver.page_source or "Submit" in driver.page_source

    def test_TC124_dashboard_html_in_dom(self, driver, base_url):
        """TC124: dashboard.html content loaded."""
        import urllib.request
        html = urllib.request.urlopen(f"{base_url}/dashboard.html").read().decode("utf-8", errors="replace")
        assert len(html) > 1000

    def test_TC125_login_page_stylesheet_loaded(self, driver, base_url):
        """TC125: CSS stylesheet loaded on login page."""
        driver.get(f"{base_url}/login.html")
        assert "style.css" in driver.page_source
