"""
test_04_dashboard_home.py — Dashboard Home Tests (20 tests)
Module: Dashboard/Home | Types: Functional, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Home")


def _go_home(driver):
    try:
        btn = driver.find_element(By.XPATH, "//button[contains(text(),'Home')]")
        btn.click()
        time.sleep(0.8)
    except Exception:
        pass


class TestDashboardHome:

    def test_home_001_dashboard_loads(self, logged_in_driver, base_url):
        """HOME-001 [Functional]: Dashboard page loads after login."""
        try:
            assert "dashboard.html" in logged_in_driver.current_url
            logger.info("Dashboard confirmed loaded")
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_001")
            raise AssertionError(f"Dashboard not loaded: {e}")

    def test_home_002_sidebar_present(self, logged_in_driver):
        """HOME-002 [Unit]: Sidebar element is visible."""
        try:
            sidebar = wait_visible(logged_in_driver, By.CSS_SELECTOR, ".sidebar")
            assert sidebar.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_002")
            raise AssertionError(f"Sidebar not visible: {e}")

    def test_home_003_logo_in_sidebar(self, logged_in_driver):
        """HOME-003 [Unit]: Logo image is in the sidebar."""
        try:
            logo = wait_visible(logged_in_driver, By.CSS_SELECTOR, ".sidebar .logo")
            assert logo.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_003")
            raise AssertionError(f"Sidebar logo missing: {e}")

    def test_home_004_welcome_text_shown(self, logged_in_driver):
        """HOME-004 [Unit]: Welcome text element is present and non-empty."""
        try:
            _go_home(logged_in_driver)
            welcome = wait_visible(logged_in_driver, By.ID, "welcomeText")
            assert len(welcome.text.strip()) > 0
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_004")
            raise AssertionError(f"Welcome text issue: {e}")

    def test_home_005_home_section_visible(self, logged_in_driver):
        """HOME-005 [Functional]: Home section is the default visible section."""
        try:
            _go_home(logged_in_driver)
            section = wait_visible(logged_in_driver, By.ID, "home")
            assert section.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_005")
            raise AssertionError(f"Home section not displayed: {e}")

    def test_home_006_skills_stat_card(self, logged_in_driver):
        """HOME-006 [Unit]: Total Skills stat card is visible."""
        try:
            _go_home(logged_in_driver)
            card = wait_visible(logged_in_driver, By.ID, "homeSkills")
            assert card.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_006")
            raise AssertionError(f"Skills card missing: {e}")

    def test_home_007_credits_stat_card(self, logged_in_driver):
        """HOME-007 [Unit]: Credits stat card is visible."""
        try:
            _go_home(logged_in_driver)
            card = wait_visible(logged_in_driver, By.ID, "homeCredits")
            assert card.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_007")
            raise AssertionError(f"Credits card missing: {e}")

    def test_home_008_sessions_stat_card(self, logged_in_driver):
        """HOME-008 [Unit]: Sessions stat card is visible."""
        try:
            _go_home(logged_in_driver)
            card = wait_visible(logged_in_driver, By.ID, "homeSessions")
            assert card.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_008")
            raise AssertionError(f"Sessions card missing: {e}")

    def test_home_009_tests_created_card(self, logged_in_driver):
        """HOME-009 [Unit]: Tests Created stat card is visible."""
        try:
            _go_home(logged_in_driver)
            card = wait_visible(logged_in_driver, By.ID, "homeTests")
            assert card.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_009")
            raise AssertionError(f"Tests Created card missing: {e}")

    def test_home_010_tests_completed_card(self, logged_in_driver):
        """HOME-010 [Unit]: Tests Completed stat card is visible."""
        try:
            _go_home(logged_in_driver)
            card = wait_visible(logged_in_driver, By.ID, "homeTestsCompleted")
            assert card.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_010")
            raise AssertionError(f"Tests Completed card missing: {e}")

    def test_home_011_dashboard_grid_rendered(self, logged_in_driver):
        """HOME-011 [Unit]: Dashboard grid layout is rendered in home section."""
        try:
            _go_home(logged_in_driver)
            grid = wait_visible(logged_in_driver, By.CSS_SELECTOR, "#home .dashboard-grid")
            assert grid.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_011")
            raise AssertionError(f"Dashboard grid not rendered: {e}")

    def test_home_012_nav_profile(self, logged_in_driver):
        """HOME-012 [Functional]: Profile sidebar button shows profile section."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'My Profile')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "profile").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_012")
            raise AssertionError(f"Profile nav failed: {e}")

    def test_home_013_nav_users(self, logged_in_driver):
        """HOME-013 [Functional]: Users sidebar button shows users section."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Users')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "users").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_013")
            raise AssertionError(f"Users nav failed: {e}")

    def test_home_014_nav_chat(self, logged_in_driver):
        """HOME-014 [Functional]: Chat sidebar button shows chat section."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Chat')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "chat").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_014")
            raise AssertionError(f"Chat nav failed: {e}")

    def test_home_015_nav_session(self, logged_in_driver):
        """HOME-015 [Functional]: Session sidebar button shows session section."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Session')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "session").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_015")
            raise AssertionError(f"Session nav failed: {e}")

    def test_home_016_nav_skill_test(self, logged_in_driver):
        """HOME-016 [Functional]: Skill Test sidebar button shows test section."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Skill Test')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "testSection").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_016")
            raise AssertionError(f"Skill Test nav failed: {e}")

    def test_home_017_nav_notes(self, logged_in_driver):
        """HOME-017 [Functional]: Notes & PDFs sidebar button shows notes section."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Notes')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "notes").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_017")
            raise AssertionError(f"Notes nav failed: {e}")

    def test_home_018_nav_settings(self, logged_in_driver):
        """HOME-018 [Functional]: Settings sidebar button shows settings section."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Settings')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "settings").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_018")
            raise AssertionError(f"Settings nav failed: {e}")

    def test_home_019_nav_home_returns(self, logged_in_driver):
        """HOME-019 [Functional]: Home button returns to home section from elsewhere."""
        try:
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Settings')]").click()
            time.sleep(0.5)
            wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Home')]").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "home").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_019")
            raise AssertionError(f"Home return nav failed: {e}")

    def test_home_020_logout_button_present(self, logged_in_driver):
        """HOME-020 [Unit]: Logout button is present in the sidebar."""
        try:
            btn = wait_visible(logged_in_driver, By.XPATH, "//button[contains(text(),'Logout')]")
            assert btn.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_home_020")
            raise AssertionError(f"Logout button missing: {e}")
