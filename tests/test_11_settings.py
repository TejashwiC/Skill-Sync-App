"""
test_11_settings.py — Settings Section Tests (14 tests)
Module: Settings | Types: Functional, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Settings")


def _nav_settings(driver):
    wait_clickable(driver, By.XPATH, "//button[contains(text(),'Settings')]").click()
    time.sleep(1.5)


class TestSettings:

    def test_set_001_section_loads(self, logged_in_driver):
        """SET-001 [Functional]: Settings section loads when clicked."""
        try:
            _nav_settings(logged_in_driver)
            assert wait_visible(logged_in_driver, By.ID, "settings").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_001")
            raise AssertionError(f"Settings section not loaded: {e}")

    def test_set_002_heading_text(self, logged_in_driver):
        """SET-002 [Unit]: Settings section shows 'Settings' in heading."""
        try:
            _nav_settings(logged_in_driver)
            heading = wait_visible(logged_in_driver, By.XPATH, "//div[@id='settings']//h2")
            assert "Settings" in heading.text
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_002")
            raise AssertionError(f"Settings heading incorrect: {e}")

    def test_set_003_cards_count(self, logged_in_driver):
        """SET-003 [Unit]: Settings dashboard has at least 4 sub-section cards."""
        try:
            _nav_settings(logged_in_driver)
            cards = wait_visible(logged_in_driver, By.ID, "settingsDashboard").find_elements(
                By.CSS_SELECTOR, ".card"
            )
            assert len(cards) >= 4, f"Expected ≥4 cards, got {len(cards)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_003")
            raise AssertionError(f"Settings cards count wrong: {e}")

    def test_set_004_account_settings_opens(self, logged_in_driver):
        """SET-004 [Functional]: Account Settings screen opens."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Account Settings']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "accountSettingsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_004")
            raise AssertionError(f"Account Settings not opened: {e}")

    def test_set_005_account_settings_inputs(self, logged_in_driver):
        """SET-005 [Unit]: Account Settings has at least 2 input fields."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Account Settings']/..").click()
            time.sleep(1.5)
            inputs = logged_in_driver.find_elements(
                By.CSS_SELECTOR, "#accountSettingsScreen input"
            )
            assert len(inputs) >= 2, f"Expected ≥2 inputs, got {len(inputs)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_005")
            raise AssertionError(f"Account Settings inputs missing: {e}")

    def test_set_006_notification_settings_opens(self, logged_in_driver):
        """SET-006 [Functional]: Notification Settings screen opens."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Notifications']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "notificationSettingsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_006")
            raise AssertionError(f"Notification Settings not opened: {e}")

    def test_set_007_follow_notification_toggles(self, logged_in_driver):
        """SET-007 [Functional]: Follow notification ON/OFF buttons are present."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Notifications']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "followNotifON").is_displayed()
            assert logged_in_driver.find_element(By.ID, "followNotifOFF").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_007")
            raise AssertionError(f"Follow notification toggles missing: {e}")

    def test_set_008_sound_notification_toggles(self, logged_in_driver):
        """SET-008 [Functional]: Sound notification ON/OFF buttons are present."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Notifications']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "soundON").is_displayed()
            assert logged_in_driver.find_element(By.ID, "soundOFF").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_008")
            raise AssertionError(f"Sound notification toggles missing: {e}")

    def test_set_009_email_notification_toggles(self, logged_in_driver):
        """SET-009 [Functional]: Email notification ON/OFF buttons are present."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Notifications']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "emailON").is_displayed()
            assert logged_in_driver.find_element(By.ID, "emailOFF").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_009")
            raise AssertionError(f"Email notification toggles missing: {e}")

    def test_set_010_session_alert_toggles(self, logged_in_driver):
        """SET-010 [Functional]: Session notification ON/OFF buttons are present."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Notifications']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "sessionNotifON").is_displayed()
            assert logged_in_driver.find_element(By.ID, "sessionNotifOFF").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_010")
            raise AssertionError(f"Session alert toggles missing: {e}")

    def test_set_011_security_settings_opens(self, logged_in_driver):
        """SET-011 [Functional]: Security Settings screen opens."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Security']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "securitySettingsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_011")
            raise AssertionError(f"Security Settings not opened: {e}")

    def test_set_012_password_settings_opens(self, logged_in_driver):
        """SET-012 [Functional]: Password Settings screen opens."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Password']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "passwordSettingsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_012")
            raise AssertionError(f"Password Settings not opened: {e}")

    def test_set_013_password_three_fields(self, logged_in_driver):
        """SET-013 [Unit]: Password Settings has 3 password-type input fields."""
        try:
            _nav_settings(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Password']/..").click()
            time.sleep(1.5)
            fields = logged_in_driver.find_elements(
                By.CSS_SELECTOR, "#passwordSettingsScreen input[type='password']"
            )
            assert len(fields) >= 3, f"Expected ≥3 password fields, got {len(fields)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_013")
            raise AssertionError(f"Password fields count wrong: {e}")

    def test_set_014_all_back_buttons_work(self, logged_in_driver):
        """SET-014 [Functional]: Back buttons on Account/Security/Password screens work."""
        screens = [
            ("Account Settings", "accountSettingsScreen"),
            ("Security",         "securitySettingsScreen"),
            ("Password",         "passwordSettingsScreen"),
        ]
        try:
            for card_text, screen_id in screens:
                _nav_settings(logged_in_driver)
                wait_clickable(
                    logged_in_driver, By.XPATH, "//h3[text()='" + card_text + "']/..").click()
                time.sleep(1.5)
                wait_clickable(
                    logged_in_driver, By.CSS_SELECTOR, f"#{screen_id} .back-btn"
                ).click()
                time.sleep(1)
                assert wait_visible(logged_in_driver, By.ID, "settingsDashboard").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_set_014")
            raise AssertionError(f"Settings back button failed: {e}")
