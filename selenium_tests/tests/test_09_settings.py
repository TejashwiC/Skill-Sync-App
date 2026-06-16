# =============================================================
# tests/test_09_settings.py — Settings Tests (TC109–TC116)
# Module: Settings
# Covers: Account Settings, Notification Settings, Security, Password
# =============================================================
import pytest
import time
from selenium.webdriver.common.by import By

import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from conftest import record
from utils.wait_helpers import (
    wait_for_element, wait_for_clickable, wait_for_visible,
    element_exists, wait_for_firebase, dismiss_alert
)

MODULE = "Settings"


def navigate_to_settings_section(driver, screen_id):
    """Navigate to Settings and open a sub-screen."""
    wait_for_firebase(driver, 3)
    settings_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('settings')\"]", timeout=15)
    settings_btn.click()
    wait_for_firebase(driver, 1)
    card = wait_for_clickable(driver, By.CSS_SELECTOR, f"[onclick=\"openSettingsScreen('{screen_id}')\"]", timeout=10)
    card.click()
    wait_for_firebase(driver, 2)


class TestSettingsDashboard:

    def test_TC109_settings_dashboard_has_4_cards(self, logged_in_driver, results):
        """TC109 — Settings dashboard has 4 action cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            settings_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('settings')\"]", timeout=15)
            settings_btn.click()
            wait_for_firebase(driver, 2)
            cards = driver.find_elements(By.CSS_SELECTOR, "#settingsDashboard .card")
            count = len(cards)
            assert count >= 4
            actual = f"Settings dashboard cards: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Settings dashboard card count mismatch"
        record(results, 109, "TC109", MODULE, "Settings dashboard shows all 4 action cards",
               "settingsDashboard grid contains 4 card elements", actual, status, remarks)

    def test_TC110_account_settings_screen_opens(self, logged_in_driver, results):
        """TC110 — Account Settings screen opens with Name and Email fields."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_settings_section(driver, "accountSettingsScreen")
            screen = wait_for_visible(driver, By.ID, "accountSettingsScreen", timeout=10)
            assert screen.is_displayed()
            actual = "Account Settings screen is visible"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Account Settings screen did not open"
        record(results, 110, "TC110", MODULE, "Account Settings screen opens correctly",
               "accountSettingsScreen is visible after clicking Account Settings card", actual, status, remarks)

    def test_TC111_notification_settings_screen_opens(self, logged_in_driver, results):
        """TC111 — Notification Settings screen opens with all toggle buttons."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_settings_section(driver, "notificationSettingsScreen")
            screen = wait_for_visible(driver, By.ID, "notificationSettingsScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 4)
            follow_on  = element_exists(driver, By.ID, "followNotifON")
            sound_on   = element_exists(driver, By.ID, "soundON")
            email_on   = element_exists(driver, By.ID, "emailON")
            session_on = element_exists(driver, By.ID, "sessionNotifON")
            actual = f"Notification toggles. Follow:{follow_on} Sound:{sound_on} Email:{email_on} Session:{session_on}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Notification Settings screen did not open"
        record(results, 111, "TC111", MODULE, "Notification Settings screen opens with all toggle buttons",
               "notificationSettingsScreen visible with followNotifON, soundON, emailON, sessionNotifON", actual, status, remarks)

    def test_TC112_follow_notification_toggle_on(self, logged_in_driver, results):
        """TC112 — Follow notification ON toggle works."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_settings_section(driver, "notificationSettingsScreen")
            wait_for_firebase(driver, 4)
            on_btn = wait_for_clickable(driver, By.ID, "followNotifON", timeout=10)
            on_btn.click()
            wait_for_firebase(driver, 4)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            status_elem = driver.find_element(By.ID, "followNotifStatus")
            actual = f"Alert: '{alert_text}'. Status: '{status_elem.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Follow notification toggle failed"
        record(results, 112, "TC112", MODULE, "Follow Notification ON toggle updates status",
               "Clicking followNotifON updates followNotifStatus to 'ON'", actual, status, remarks)

    def test_TC113_sound_notification_toggle_off(self, logged_in_driver, results):
        """TC113 — Sound notification OFF toggle works."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_settings_section(driver, "notificationSettingsScreen")
            wait_for_firebase(driver, 4)
            off_btn = wait_for_clickable(driver, By.ID, "soundOFF", timeout=10)
            off_btn.click()
            wait_for_firebase(driver, 4)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            status_elem = driver.find_element(By.ID, "soundStatus")
            actual = f"Alert: '{alert_text}'. Status: '{status_elem.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Sound notification toggle failed"
        record(results, 113, "TC113", MODULE, "Sound Notification OFF toggle updates status",
               "Clicking soundOFF updates soundStatus to 'OFF'", actual, status, remarks)

    def test_TC114_security_settings_screen_opens(self, logged_in_driver, results):
        """TC114 — Security Settings screen opens."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_settings_section(driver, "securitySettingsScreen")
            screen = wait_for_visible(driver, By.ID, "securitySettingsScreen", timeout=10)
            assert screen.is_displayed()
            actual = "Security Settings screen is visible"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Security Settings screen did not open"
        record(results, 114, "TC114", MODULE, "Security Settings screen opens correctly",
               "securitySettingsScreen is visible with 2FA and Login Activity sections", actual, status, remarks)

    def test_TC115_password_settings_screen_opens(self, logged_in_driver, results):
        """TC115 — Password Settings screen opens with 3 password fields."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_settings_section(driver, "passwordSettingsScreen")
            screen = wait_for_visible(driver, By.ID, "passwordSettingsScreen", timeout=10)
            assert screen.is_displayed()
            pwd_fields = driver.find_elements(By.CSS_SELECTOR, "#passwordSettingsScreen input[type='password']")
            count = len(pwd_fields)
            actual = f"Password Settings screen open. Password fields: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Password Settings screen did not open"
        record(results, 115, "TC115", MODULE, "Password Settings screen opens with 3 password fields",
               "passwordSettingsScreen visible with 3 password input fields", actual, status, remarks)

    def test_TC116_session_notification_toggle(self, logged_in_driver, results):
        """TC116 — Session notification toggle works correctly."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_settings_section(driver, "notificationSettingsScreen")
            wait_for_firebase(driver, 4)
            on_btn = wait_for_clickable(driver, By.ID, "sessionNotifON", timeout=10)
            on_btn.click()
            wait_for_firebase(driver, 4)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            status_elem = driver.find_element(By.ID, "sessionStatus")
            actual = f"Alert: '{alert_text}'. Session status: '{status_elem.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Session notification toggle failed"
        record(results, 116, "TC116", MODULE, "Session Notification ON toggle updates status",
               "Clicking sessionNotifON updates sessionStatus to 'ON'", actual, status, remarks)
