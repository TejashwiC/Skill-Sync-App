"""
test_09_settings.py — Settings Tests (TC109–TC116)
Uses element_in_source() — fast page-source check before Firebase redirect.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestSettings:

    def test_TC109_settings_section_exists(self, driver, base_url):
        """TC109: #settings section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "settings")

    def test_TC110_settings_dashboard_exists(self, driver, base_url):
        """TC110: #settingsDashboard exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "settingsDashboard")

    def test_TC111_account_settings_screen_exists(self, driver, base_url):
        """TC111: #accountSettingsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "accountSettingsScreen")

    def test_TC112_notification_settings_screen_exists(self, driver, base_url):
        """TC112: #notificationSettingsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "notificationSettingsScreen")

    def test_TC113_follow_notif_toggles_exist(self, driver, base_url):
        """TC113: Follow notification toggles (#followNotifON/#followNotifOFF) exist."""
        assert element_in_source(driver, base_url, "followNotifON") and element_in_source(driver, base_url, "followNotifOFF")

    def test_TC114_sound_notif_toggles_exist(self, driver, base_url):
        """TC114: Sound notification toggles (#soundON/#soundOFF) exist."""
        assert element_in_source(driver, base_url, "soundON") and element_in_source(driver, base_url, "soundOFF")

    def test_TC115_security_settings_screen_exists(self, driver, base_url):
        """TC115: #securitySettingsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "securitySettingsScreen")

    def test_TC116_password_settings_screen_exists(self, driver, base_url):
        """TC116: #passwordSettingsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "passwordSettingsScreen")
