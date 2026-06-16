"""
test_06_session.py — Session Tests (TC073–TC086)
Uses element_in_source() — fast page-source check before Firebase redirect.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestSession:

    def test_TC073_session_dashboard_exists(self, driver, base_url):
        """TC073: #sessionDashboard exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "sessionDashboard")

    def test_TC074_start_session_screen_exists(self, driver, base_url):
        """TC074: #startSessionScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "startSessionScreen")

    def test_TC075_session_name_field_exists(self, driver, base_url):
        """TC075: #sessionName input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "sessionName")

    def test_TC076_session_skill_field_exists(self, driver, base_url):
        """TC076: #sessionSkill input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "sessionSkill")

    def test_TC077_session_platform_dropdown_exists(self, driver, base_url):
        """TC077: #sessionPlatform dropdown exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "sessionPlatform")

    def test_TC078_session_link_field_exists(self, driver, base_url):
        """TC078: #sessionLink input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "sessionLink")

    def test_TC079_session_code_box_exists(self, driver, base_url):
        """TC079: #sessionCodeBox exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "sessionCodeBox")

    def test_TC080_join_session_screen_exists(self, driver, base_url):
        """TC080: #joinSessionScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "joinSessionScreen")

    def test_TC081_join_code_input_exists(self, driver, base_url):
        """TC081: #joinCodeInput exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "joinCodeInput")

    def test_TC082_live_session_screen_exists(self, driver, base_url):
        """TC082: #liveSessionScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "liveSessionScreen")

    def test_TC083_history_session_screen_exists(self, driver, base_url):
        """TC083: #historySessionScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "historySessionScreen")

    def test_TC084_feedback_session_screen_exists(self, driver, base_url):
        """TC084: #feedbackSessionScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "feedbackSessionScreen")

    def test_TC085_ratings_session_screen_exists(self, driver, base_url):
        """TC085: #ratingsSessionScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "ratingsSessionScreen")

    def test_TC086_star_rating_elements_exist(self, driver, base_url):
        """TC086: All 5 star rating elements exist in dashboard HTML."""
        assert all(element_in_source(driver, base_url, star_id) for star_id in ["star1", "star2", "star3", "star4", "star5"])
