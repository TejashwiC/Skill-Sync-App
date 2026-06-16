"""
test_05_profile.py — Profile Section Tests (16 tests)
Module: Profile | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Profile")


def _nav_profile(driver):
    wait_clickable(driver, By.XPATH, "//button[contains(text(),'My Profile')]").click()
    time.sleep(1)


def _open_sub(driver, card_title):
    driver.find_element(By.XPATH, "//h3[text()='" + card_title + "']/..").click()
    time.sleep(1.5)


class TestProfile:

    def test_prof_001_section_loads(self, logged_in_driver):
        """PROF-001 [Functional]: Profile section is visible after navigation."""
        try:
            _nav_profile(logged_in_driver)
            assert wait_visible(logged_in_driver, By.ID, "profile").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_001")
            raise AssertionError(f"Profile section not visible: {e}")

    def test_prof_002_dashboard_grid_cards(self, logged_in_driver):
        """PROF-002 [Unit]: Profile dashboard has at least 6 sub-section cards."""
        try:
            _nav_profile(logged_in_driver)
            cards = wait_visible(logged_in_driver, By.ID, "profileDashboard").find_elements(
                By.CSS_SELECTOR, ".card"
            )
            assert len(cards) >= 6, f"Expected ≥6 cards, got {len(cards)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_002")
            raise AssertionError(f"Profile cards issue: {e}")

    def test_prof_003_view_profile_opens(self, logged_in_driver):
        """PROF-003 [Functional]: View Profile sub-screen opens."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='View Profile']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "viewProfileScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_003")
            raise AssertionError(f"View Profile not opened: {e}")

    def test_prof_004_view_profile_fields(self, logged_in_driver):
        """PROF-004 [Unit]: View Profile shows Name, Email, Credits elements."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='View Profile']/..").click()
            time.sleep(2)
            for fid in ["vname", "vemail", "vcredits"]:
                el = logged_in_driver.find_element(By.ID, fid)
                assert el is not None, f"#{fid} missing"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_004")
            raise AssertionError(f"View Profile fields missing: {e}")

    def test_prof_005_view_profile_back_btn(self, logged_in_driver):
        """PROF-005 [Functional]: Back button on View Profile returns to dashboard."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='View Profile']/..").click()
            time.sleep(1.5)
            wait_clickable(logged_in_driver, By.CSS_SELECTOR, "#viewProfileScreen .back-btn").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "profileDashboard").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_005")
            raise AssertionError(f"Back button failed: {e}")

    def test_prof_006_edit_profile_opens(self, logged_in_driver):
        """PROF-006 [Functional]: Edit Profile sub-screen opens."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Edit Profile']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "editProfileScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_006")
            raise AssertionError(f"Edit Profile not opened: {e}")

    def test_prof_007_edit_profile_all_fields(self, logged_in_driver):
        """PROF-007 [Unit]: Edit Profile has all 6 input fields."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Edit Profile']/..").click()
            time.sleep(1.5)
            for fid in ["pname", "pemail", "pmobile", "pteach", "plearn", "plang"]:
                assert logged_in_driver.find_element(By.ID, fid).is_displayed(), f"#{fid} hidden"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_007")
            raise AssertionError(f"Edit Profile fields missing: {e}")

    def test_prof_008_save_button_present(self, logged_in_driver):
        """PROF-008 [Unit]: Save button is present in Edit Profile."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Edit Profile']/..").click()
            time.sleep(1.5)
            btn = logged_in_driver.find_element(
                By.XPATH, "//div[@id='editProfileScreen']//button[contains(text(),'Save')]"
            )
            assert btn.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_008")
            raise AssertionError(f"Save button missing: {e}")

    def test_prof_009_add_skill_screen_opens(self, logged_in_driver):
        """PROF-009 [Functional]: Add Skill screen opens."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Add Skill']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "addSkillScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_009")
            raise AssertionError(f"Add Skill screen not opened: {e}")

    def test_prof_010_add_skill_input_present(self, logged_in_driver):
        """PROF-010 [Unit]: Skill input field is visible on Add Skill screen."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Add Skill']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "skillInput").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_010")
            raise AssertionError(f"skillInput not visible: {e}")

    def test_prof_011_add_skill_empty_validation(self, logged_in_driver):
        """PROF-011 [Validation]: Adding empty skill shows an alert."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Add Skill']/..").click()
            time.sleep(1.5)
            logged_in_driver.find_element(By.ID, "skillInput").clear()
            wait_clickable(
                logged_in_driver,
                By.XPATH,
                "//div[@id='addSkillScreen']//button[contains(text(),'Add Skill')]"
            ).click()
            time.sleep(2)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=5)
            assert alert is not None and len(alert) > 0
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_011")
            raise AssertionError(f"Empty skill alert not shown: {e}")

    def test_prof_012_edit_skill_screen_opens(self, logged_in_driver):
        """PROF-012 [Functional]: Edit Skill screen opens with two inputs."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Edit Skill']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "editSkillScreen").is_displayed()
            assert logged_in_driver.find_element(By.ID, "oldSkillInput").is_displayed()
            assert logged_in_driver.find_element(By.ID, "newSkillInput").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_012")
            raise AssertionError(f"Edit Skill screen issue: {e}")

    def test_prof_013_delete_skill_screen_opens(self, logged_in_driver):
        """PROF-013 [Functional]: Delete Skill screen opens."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Delete Skill']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "deleteSkillScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_013")
            raise AssertionError(f"Delete Skill screen not opened: {e}")

    def test_prof_014_skills_overview_opens(self, logged_in_driver):
        """PROF-014 [Functional]: Skills Overview screen opens."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Skills Overview']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "skillsOverviewScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_014")
            raise AssertionError(f"Skills Overview not opened: {e}")

    def test_prof_015_recommendations_opens(self, logged_in_driver):
        """PROF-015 [Functional]: Recommendations screen opens with content cards."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Recommendations']/..").click()
            time.sleep(1.5)
            screen = wait_visible(logged_in_driver, By.ID, "recommendationScreen")
            assert screen.is_displayed()
            cards = screen.find_elements(By.CSS_SELECTOR, ".recommend-card")
            assert len(cards) > 0
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_015")
            raise AssertionError(f"Recommendations issue: {e}")

    def test_prof_016_upload_photo_screen_opens(self, logged_in_driver):
        """PROF-016 [Functional]: Upload Photo screen opens with file input."""
        try:
            _nav_profile(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Upload Photo']/..").click()
            time.sleep(1.5)
            screen = wait_visible(logged_in_driver, By.ID, "uploadPhotoScreen")
            assert screen.is_displayed()
            file_input = logged_in_driver.find_element(By.ID, "pimage")
            assert file_input is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_prof_016")
            raise AssertionError(f"Upload Photo screen issue: {e}")
