"""
test_08_session.py — Session Section Tests (21 tests)
Module: Session | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Session")


def _nav_session(driver):
    wait_clickable(driver, By.XPATH, "//button[contains(text(),'Session')]").click()
    time.sleep(1.5)


class TestSession:

    def test_sess_001_section_loads(self, logged_in_driver):
        """SESS-001 [Functional]: Session section loads when clicked."""
        try:
            _nav_session(logged_in_driver)
            assert wait_visible(logged_in_driver, By.ID, "session").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_001")
            raise AssertionError(f"Session section not loaded: {e}")

    def test_sess_002_heading_text(self, logged_in_driver):
        """SESS-002 [Unit]: Session section heading contains 'Session'."""
        try:
            _nav_session(logged_in_driver)
            heading = wait_visible(logged_in_driver, By.XPATH, "//div[@id='session']//h2")
            assert "Session" in heading.text
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_002")
            raise AssertionError(f"Session heading incorrect: {e}")

    def test_sess_003_cards_count(self, logged_in_driver):
        """SESS-003 [Unit]: Session dashboard has at least 5 sub-section cards."""
        try:
            _nav_session(logged_in_driver)
            cards = wait_visible(logged_in_driver, By.ID, "sessionDashboard").find_elements(
                By.CSS_SELECTOR, ".card"
            )
            assert len(cards) >= 5, f"Expected ≥5 cards, got {len(cards)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_003")
            raise AssertionError(f"Session cards count wrong: {e}")

    def test_sess_004_start_session_screen(self, logged_in_driver):
        """SESS-004 [Functional]: Start Session screen opens."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "startSessionScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_004")
            raise AssertionError(f"Start Session screen not opened: {e}")

    def test_sess_005_session_name_field(self, logged_in_driver):
        """SESS-005 [Unit]: Session name input field is present."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "sessionName").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_005")
            raise AssertionError(f"Session name field missing: {e}")

    def test_sess_006_platform_dropdown(self, logged_in_driver):
        """SESS-006 [Unit]: Platform dropdown has at least 5 options."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            options = Select(
                wait_visible(logged_in_driver, By.ID, "sessionPlatform")
            ).options
            assert len(options) >= 5, f"Expected ≥5 options, got {len(options)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_006")
            raise AssertionError(f"Platform dropdown issue: {e}")

    def test_sess_007_meeting_link_url_field(self, logged_in_driver):
        """SESS-007 [Unit]: Meeting link field has type=url."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            field = wait_visible(logged_in_driver, By.ID, "sessionLink")
            assert field.get_attribute("type") == "url"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_007")
            raise AssertionError(f"Meeting link field type wrong: {e}")

    def test_sess_008_empty_name_alert(self, logged_in_driver):
        """SESS-008 [Validation]: Starting session without name shows alert."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            logged_in_driver.find_element(By.ID, "sessionName").clear()
            logged_in_driver.find_element(By.ID, "sessionLink").send_keys("https://zoom.us/j/123")
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='startSessionScreen']//button[contains(text(),'Start Session')]"
            ).click()
            time.sleep(3)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=6)
            assert alert is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_008")
            raise AssertionError(f"Empty name alert not shown: {e}")

    def test_sess_009_empty_platform_alert(self, logged_in_driver):
        """SESS-009 [Validation]: Starting session without platform shows alert."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            logged_in_driver.find_element(By.ID, "sessionName").send_keys("Test Session")
            logged_in_driver.find_element(By.ID, "sessionLink").send_keys("https://zoom.us/j/123")
            # Leave platform as default (empty)
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='startSessionScreen']//button[contains(text(),'Start Session')]"
            ).click()
            time.sleep(3)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=6)
            assert alert is not None and "platform" in alert.lower()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_009")
            raise AssertionError(f"Empty platform alert not shown: {e}")

    def test_sess_010_invalid_link_alert(self, logged_in_driver):
        """SESS-010 [Validation]: Non-http meeting link triggers alert."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            logged_in_driver.find_element(By.ID, "sessionName").send_keys("Link Test")
            Select(logged_in_driver.find_element(By.ID, "sessionPlatform")).select_by_value("zoom")
            logged_in_driver.find_element(By.ID, "sessionLink").send_keys("not-a-valid-url")
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='startSessionScreen']//button[contains(text(),'Start Session')]"
            ).click()
            time.sleep(3)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=6)
            assert alert is not None and ("http" in alert.lower() or "link" in alert.lower())
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_010")
            raise AssertionError(f"Invalid link alert not shown: {e}")

    def test_sess_011_platform_links_present(self, logged_in_driver):
        """SESS-011 [Unit]: Zoom and Google Meet quick-start links are present."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Start Session']/..").click()
            time.sleep(2)
            zoom = logged_in_driver.find_element(By.XPATH, "//a[contains(@href,'zoom.us')]")
            meet = logged_in_driver.find_element(By.XPATH, "//a[contains(@href,'meet.google.com')]")
            assert zoom.is_displayed() and meet.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_011")
            raise AssertionError(f"Platform links missing: {e}")

    def test_sess_012_join_session_screen(self, logged_in_driver):
        """SESS-012 [Functional]: Join Session screen opens."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Join Session']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "joinSessionScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_012")
            raise AssertionError(f"Join Session screen not opened: {e}")

    def test_sess_013_join_code_maxlength(self, logged_in_driver):
        """SESS-013 [Unit]: Join code input has maxlength=6."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Join Session']/..").click()
            time.sleep(2)
            field = wait_visible(logged_in_driver, By.ID, "joinCodeInput")
            assert field.get_attribute("maxlength") == "6"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_013")
            raise AssertionError(f"Join code maxlength wrong: {e}")

    def test_sess_014_short_code_alert(self, logged_in_driver):
        """SESS-014 [Validation]: Code shorter than 6 chars shows alert."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Join Session']/..").click()
            time.sleep(2)
            wait_visible(logged_in_driver, By.ID, "joinCodeInput").send_keys("ABC")
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='joinSessionScreen']//button[contains(text(),'Join')]"
            ).click()
            time.sleep(3)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=6)
            assert alert is not None and "code" in alert.lower()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_014")
            raise AssertionError(f"Short code alert not shown: {e}")

    def test_sess_015_invalid_code_shows_result(self, logged_in_driver):
        """SESS-015 [Validation]: Valid-length invalid code shows error result."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Join Session']/..").click()
            time.sleep(2)
            field = wait_visible(logged_in_driver, By.ID, "joinCodeInput")
            field.clear()
            field.send_keys("XXXXXX")
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='joinSessionScreen']//button[contains(text(),'Join')]"
            ).click()
            time.sleep(5)
            result = logged_in_driver.find_element(By.ID, "joinResult")
            assert result.is_displayed() and len(result.get_attribute("innerHTML")) > 0
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_015")
            raise AssertionError(f"Invalid code result not shown: {e}")

    def test_sess_016_live_sessions_screen(self, logged_in_driver):
        """SESS-016 [Functional]: Live Sessions screen opens."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Live Sessions']/..").click()
            time.sleep(3)
            assert wait_visible(logged_in_driver, By.ID, "liveSessionScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_016")
            raise AssertionError(f"Live Sessions screen not opened: {e}")

    def test_sess_017_session_history_screen(self, logged_in_driver):
        """SESS-017 [Functional]: Session History screen opens."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Session History']/..").click()
            time.sleep(3)
            assert wait_visible(logged_in_driver, By.ID, "historySessionScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_017")
            raise AssertionError(f"Session History not opened: {e}")

    def test_sess_018_feedback_screen_fields(self, logged_in_driver):
        """SESS-018 [Functional]: Feedback screen opens with textarea."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Feedback']/..").click()
            time.sleep(2)
            screen = wait_visible(logged_in_driver, By.ID, "feedbackSessionScreen")
            assert screen.is_displayed()
            assert wait_visible(logged_in_driver, By.ID, "feedbackText").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_018")
            raise AssertionError(f"Feedback screen issue: {e}")

    def test_sess_019_feedback_empty_alert(self, logged_in_driver):
        """SESS-019 [Validation]: Submitting empty feedback shows alert."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Feedback']/..").click()
            time.sleep(2)
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='feedbackSessionScreen']//button[contains(text(),'Submit')]"
            ).click()
            time.sleep(3)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=5)
            assert alert is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_019")
            raise AssertionError(f"Empty feedback alert not shown: {e}")

    def test_sess_020_ratings_screen_stars(self, logged_in_driver):
        """SESS-020 [Functional]: Ratings screen opens with star elements."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Ratings']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "ratingsSessionScreen").is_displayed()
            assert logged_in_driver.find_element(By.ID, "star1").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_020")
            raise AssertionError(f"Ratings screen/stars issue: {e}")

    def test_sess_021_star_rating_clickable(self, logged_in_driver):
        """SESS-021 [Functional]: Star rating can be clicked without error."""
        try:
            _nav_session(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Ratings']/..").click()
            time.sleep(2)
            star3 = wait_clickable(logged_in_driver, By.ID, "star3")
            star3.click()
            time.sleep(0.5)
            # No exception means interaction succeeded
            assert wait_visible(logged_in_driver, By.ID, "ratingsSessionScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_sess_021")
            raise AssertionError(f"Star rating click failed: {e}")
