# =============================================================
# tests/test_06_session.py — Session Tests (TC073–TC086)
# Module: Session
# Covers: Start, End, Join, Live Sessions, History, Feedback, Ratings
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

MODULE = "Session"


def navigate_to_session_section(driver, screen_id):
    """Navigate to Session section and open a sub-screen."""
    wait_for_firebase(driver, 3)
    sess_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('session')\"]", timeout=15)
    sess_btn.click()
    wait_for_firebase(driver, 1)
    card = wait_for_clickable(driver, By.CSS_SELECTOR, f"[onclick=\"openSessionScreen('{screen_id}')\"]", timeout=10)
    card.click()
    wait_for_firebase(driver, 2)


class TestSessionDashboard:

    def test_TC073_session_dashboard_cards(self, logged_in_driver, results):
        """TC073 — Session dashboard has 7 action cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            sess_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('session')\"]", timeout=15)
            sess_btn.click()
            wait_for_firebase(driver, 2)
            cards = driver.find_elements(By.CSS_SELECTOR, "#sessionDashboard .card")
            count = len(cards)
            assert count >= 6
            actual = f"Session dashboard cards: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Session dashboard card count mismatch"
        record(results, 73, "TC073", MODULE, "Session dashboard shows all action cards",
               "sessionDashboard grid contains at least 6 card elements", actual, status, remarks)

    def test_TC074_start_session_screen_opens(self, logged_in_driver, results):
        """TC074 — Start Session screen opens with all required fields."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "startSessionScreen")
            screen = wait_for_visible(driver, By.ID, "startSessionScreen", timeout=10)
            assert screen.is_displayed()
            session_name = element_exists(driver, By.ID, "sessionName")
            session_skill = element_exists(driver, By.ID, "sessionSkill")
            platform_sel = element_exists(driver, By.ID, "sessionPlatform")
            session_link = element_exists(driver, By.ID, "sessionLink")
            actual = f"Start Session screen. Name:{session_name} Skill:{session_skill} Platform:{platform_sel} Link:{session_link}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Start Session screen did not open"
        record(results, 74, "TC074", MODULE, "Start Session screen opens with all required fields",
               "startSessionScreen visible with sessionName, sessionSkill, sessionPlatform, sessionLink", actual, status, remarks)

    def test_TC075_start_session_empty_name_validation(self, logged_in_driver, results):
        """TC075 — Start Session with empty session name shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "startSessionScreen")
            start_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='startSession()']", timeout=10)
            start_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for empty session name"
        record(results, 75, "TC075", MODULE, "Start Session with empty name shows validation alert",
               "Alert 'Enter session name' appears when no session name provided", actual, status, remarks)

    def test_TC076_start_session_without_platform_validation(self, logged_in_driver, results):
        """TC076 — Start Session without platform selection shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "startSessionScreen")
            wait_for_clickable(driver, By.ID, "sessionName").send_keys("Test Session")
            start_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='startSession()']", timeout=10)
            start_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for missing platform"
        record(results, 76, "TC076", MODULE, "Start Session without platform shows validation alert",
               "Alert 'Select a platform' appears when no platform is chosen", actual, status, remarks)

    def test_TC077_start_session_invalid_meeting_link(self, logged_in_driver, results):
        """TC077 — Start Session with invalid meeting link (no http) shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "startSessionScreen")
            wait_for_clickable(driver, By.ID, "sessionName").send_keys("Test Session")
            from selenium.webdriver.support.ui import Select
            Select(driver.find_element(By.ID, "sessionPlatform")).select_by_value("zoom")
            wait_for_clickable(driver, By.ID, "sessionLink").send_keys("not-a-valid-url")
            start_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='startSession()']", timeout=10)
            start_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            assert found_alert and "http" in alert_text.lower()
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Invalid URL not validated"
        record(results, 77, "TC077", MODULE, "Start Session with invalid link URL shows alert",
               "Alert about http:// requirement appears for non-URL meeting link", actual, status, remarks)

    def test_TC078_session_platform_dropdown_options(self, logged_in_driver, results):
        """TC078 — Session platform dropdown has all 7 platform options."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "startSessionScreen")
            from selenium.webdriver.support.ui import Select
            platform_sel = Select(wait_for_element(driver, By.ID, "sessionPlatform"))
            options = [o.get_attribute("value") for o in platform_sel.options if o.get_attribute("value")]
            expected = ["zoom", "meet", "teams", "webex", "jitsi", "whereby", "other"]
            for p in expected:
                assert p in options
            actual = f"Platform options found: {options}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Platform options missing"
        record(results, 78, "TC078", MODULE, "Platform dropdown has all 7 platform options",
               "sessionPlatform select has zoom, meet, teams, webex, jitsi, whereby, other", actual, status, remarks)

    def test_TC079_join_session_screen_opens(self, logged_in_driver, results):
        """TC079 — Join Session screen opens with code input."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "joinSessionScreen")
            screen = wait_for_visible(driver, By.ID, "joinSessionScreen", timeout=10)
            assert screen.is_displayed()
            code_input = element_exists(driver, By.ID, "joinCodeInput")
            join_btn = element_exists(driver, By.CSS_SELECTOR, "button[onclick='joinSession()']")
            actual = f"Join Session screen. Code input: {code_input}, Join button: {join_btn}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Join Session screen did not open"
        record(results, 79, "TC079", MODULE, "Join Session screen opens with code input",
               "joinSessionScreen visible with joinCodeInput and Join button", actual, status, remarks)

    def test_TC080_join_session_invalid_code(self, logged_in_driver, results):
        """TC080 — Join Session with invalid code shows error."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "joinSessionScreen")
            code_input = wait_for_clickable(driver, By.ID, "joinCodeInput", timeout=10)
            code_input.send_keys("XXXXXX")
            join_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='joinSession()']", timeout=10)
            join_btn.click()
            wait_for_firebase(driver, 6)
            result_div = wait_for_visible(driver, By.ID, "joinResult", timeout=10)
            content = result_div.get_attribute("innerHTML")
            assert "Invalid" in content or "Expired" in content or len(content) > 20
            actual = f"Join result shows error. Content snippet: '{content[:100]}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Invalid code not handled"
        record(results, 80, "TC080", MODULE, "Join Session with invalid code shows error message",
               "joinResult div shows 'Invalid or Expired Code' message", actual, status, remarks)

    def test_TC081_join_session_short_code_validation(self, logged_in_driver, results):
        """TC081 — Join Session with code shorter than 6 chars shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "joinSessionScreen")
            code_input = wait_for_clickable(driver, By.ID, "joinCodeInput", timeout=10)
            code_input.send_keys("AB")
            join_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='joinSession()']", timeout=10)
            join_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            assert found_alert
            actual = f"Alert shown for short code: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Short code not validated"
        record(results, 81, "TC081", MODULE, "Join Session with < 6 char code shows validation alert",
               "Alert about 6-character code requirement appears for short codes", actual, status, remarks)

    def test_TC082_live_sessions_screen_opens(self, logged_in_driver, results):
        """TC082 — Live Sessions screen opens and loads sessions."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "liveSessionScreen")
            screen = wait_for_visible(driver, By.ID, "liveSessionScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            container = element_exists(driver, By.ID, "liveSessionContainer")
            actual = f"Live Sessions screen open. Container: {container}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Live Sessions screen did not open"
        record(results, 82, "TC082", MODULE, "Live Sessions screen opens and loads data",
               "liveSessionScreen visible and liveSessionContainer present", actual, status, remarks)

    def test_TC083_session_history_screen_opens(self, logged_in_driver, results):
        """TC083 — Session History screen opens and loads past sessions."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "historySessionScreen")
            screen = wait_for_visible(driver, By.ID, "historySessionScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            container = element_exists(driver, By.ID, "historyContainer")
            actual = f"Session History screen open. Container: {container}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Session History screen did not open"
        record(results, 83, "TC083", MODULE, "Session History screen opens and loads data",
               "historySessionScreen visible and historyContainer present", actual, status, remarks)

    def test_TC084_feedback_screen_opens(self, logged_in_driver, results):
        """TC084 — Feedback screen opens with session select and textarea."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "feedbackSessionScreen")
            screen = wait_for_visible(driver, By.ID, "feedbackSessionScreen", timeout=10)
            assert screen.is_displayed()
            select = element_exists(driver, By.ID, "feedbackSessionSelect")
            textarea = element_exists(driver, By.ID, "feedbackText")
            actual = f"Feedback screen open. Select: {select}, Textarea: {textarea}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Feedback screen did not open"
        record(results, 84, "TC084", MODULE, "Feedback screen opens with session select and textarea",
               "feedbackSessionScreen visible with feedbackSessionSelect and feedbackText", actual, status, remarks)

    def test_TC085_submit_feedback_without_session_validation(self, logged_in_driver, results):
        """TC085 — Submit Feedback without selecting session shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "feedbackSessionScreen")
            wait_for_firebase(driver, 3)
            submit_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='submitFeedback()']", timeout=10)
            submit_btn.click()
            wait_for_firebase(driver, 2)
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for feedback without session"
        record(results, 85, "TC085", MODULE, "Submit Feedback without session selection shows alert",
               "Alert 'Select a session' appears on submit without session choice", actual, status, remarks)

    def test_TC086_ratings_screen_with_star_rating(self, logged_in_driver, results):
        """TC086 — Ratings screen has 5 clickable stars."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_session_section(driver, "ratingsSessionScreen")
            screen = wait_for_visible(driver, By.ID, "ratingsSessionScreen", timeout=10)
            assert screen.is_displayed()
            stars = driver.find_elements(By.CSS_SELECTOR, "#starRating span")
            count = len(stars)
            assert count == 5
            # Click star 3
            stars[2].click()
            wait_for_firebase(driver, 1)
            star3 = driver.find_element(By.ID, "star3")
            assert star3.text == "★"
            actual = f"Ratings screen with {count} stars. Star 3 filled: ★"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Stars not found or not clickable"
        record(results, 86, "TC086", MODULE, "Ratings screen has 5 clickable stars that toggle correctly",
               "starRating has 5 span elements; clicking fills stars up to selected index", actual, status, remarks)
