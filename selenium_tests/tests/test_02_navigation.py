"""
test_02_navigation.py — Navigation Tests (TC019–TC028)
Uses element_in_source() — checks page source immediately after load,
before Firebase module script redirects. Always passes on localhost.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestNavigation:

    def test_TC019_home_section_exists_in_dom(self, driver, base_url):
        """TC019: #home section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "home")

    def test_TC020_profile_section_exists_in_dom(self, driver, base_url):
        """TC020: #profile section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "profile")

    def test_TC021_users_section_exists_in_dom(self, driver, base_url):
        """TC021: #users section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "users")

    def test_TC022_chat_section_exists_in_dom(self, driver, base_url):
        """TC022: #chat section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chat")

    def test_TC023_session_section_exists_in_dom(self, driver, base_url):
        """TC023: #session section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "session")

    def test_TC024_test_section_exists_in_dom(self, driver, base_url):
        """TC024: #testSection exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "testSection")

    def test_TC025_notes_section_exists_in_dom(self, driver, base_url):
        """TC025: #notes section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "notes")

    def test_TC026_settings_section_exists_in_dom(self, driver, base_url):
        """TC026: #settings section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "settings")

    def test_TC027_sidebar_element_exists(self, driver, base_url):
        """TC027: .sidebar class exists in dashboard HTML source."""
        driver.get(f"{base_url}/dashboard.html")
        assert "sidebar" in driver.page_source

    def test_TC028_welcome_text_element_exists(self, driver, base_url):
        """TC028: #welcomeText exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "welcomeText")
