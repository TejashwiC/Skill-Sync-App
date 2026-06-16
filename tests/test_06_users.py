"""
test_06_users.py — Users Section Tests (14 tests)
Module: Users | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Users")


def _nav_users(driver):
    wait_clickable(driver, By.XPATH, "//button[contains(text(),'Users')]").click()
    time.sleep(1)


class TestUsers:

    def test_usr_001_section_loads(self, logged_in_driver):
        """USR-001 [Functional]: Users section loads when clicked."""
        try:
            _nav_users(logged_in_driver)
            assert wait_visible(logged_in_driver, By.ID, "users").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_001")
            raise AssertionError(f"Users section not loaded: {e}")

    def test_usr_002_dashboard_heading(self, logged_in_driver):
        """USR-002 [Unit]: Users section shows 'Users' in heading text."""
        try:
            _nav_users(logged_in_driver)
            heading = wait_visible(logged_in_driver, By.XPATH, "//div[@id='users']//h2")
            assert "Users" in heading.text
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_002")
            raise AssertionError(f"Users heading incorrect: {e}")

    def test_usr_003_cards_count(self, logged_in_driver):
        """USR-003 [Unit]: Users dashboard shows at least 4 sub-section cards."""
        try:
            _nav_users(logged_in_driver)
            cards = wait_visible(logged_in_driver, By.ID, "usersDashboard").find_elements(
                By.CSS_SELECTOR, ".card"
            )
            assert len(cards) >= 4, f"Expected ≥4 cards, got {len(cards)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_003")
            raise AssertionError(f"Users cards count wrong: {e}")

    def test_usr_004_users_list_opens(self, logged_in_driver):
        """USR-004 [Functional]: Users List screen opens."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Users List']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "usersListScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_004")
            raise AssertionError(f"Users List not opened: {e}")

    def test_usr_005_users_list_back_btn(self, logged_in_driver):
        """USR-005 [Functional]: Back button on Users List returns to dashboard."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Users List']/..").click()
            time.sleep(1.5)
            wait_clickable(logged_in_driver, By.CSS_SELECTOR, "#usersListScreen .back-btn").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "usersDashboard").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_005")
            raise AssertionError(f"Back from Users List failed: {e}")

    def test_usr_006_followers_screen_opens(self, logged_in_driver):
        """USR-006 [Functional]: Followers screen opens."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Followers']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "followersScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_006")
            raise AssertionError(f"Followers screen not opened: {e}")

    def test_usr_007_following_screen_opens(self, logged_in_driver):
        """USR-007 [Functional]: Following screen opens."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Following']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "followingScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_007")
            raise AssertionError(f"Following screen not opened: {e}")

    def test_usr_008_search_users_screen_opens(self, logged_in_driver):
        """USR-008 [Functional]: Search Users screen opens."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Search Users']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "searchUsersScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_008")
            raise AssertionError(f"Search Users screen not opened: {e}")

    def test_usr_009_search_input_present(self, logged_in_driver):
        """USR-009 [Unit]: Search input field is present."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Search Users']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "searchInput").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_009")
            raise AssertionError(f"Search input not visible: {e}")

    def test_usr_010_search_with_query_shows_results(self, logged_in_driver):
        """USR-010 [Functional]: Searching shows results container with content."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Search Users']/..").click()
            time.sleep(1.5)
            field = wait_visible(logged_in_driver, By.ID, "searchInput")
            field.send_keys("a")
            logged_in_driver.execute_script(
                "arguments[0].dispatchEvent(new Event('input'));", field
            )
            time.sleep(3)
            results = logged_in_driver.find_element(By.ID, "searchResults")
            assert results.get_attribute("innerHTML").strip() != ""
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_010")
            raise AssertionError(f"Search results not shown: {e}")

    def test_usr_011_search_clear_resets_results(self, logged_in_driver):
        """USR-011 [Validation]: Clearing search input clears results container."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Search Users']/..").click()
            time.sleep(1.5)
            field = wait_visible(logged_in_driver, By.ID, "searchInput")
            field.send_keys("test")
            time.sleep(1)
            field.clear()
            logged_in_driver.execute_script(
                "arguments[0].dispatchEvent(new Event('input'));", field
            )
            time.sleep(1)
            results = logged_in_driver.find_element(By.ID, "searchResults")
            assert results.get_attribute("innerHTML").strip() == ""
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_011")
            raise AssertionError(f"Search clear did not reset results: {e}")

    def test_usr_012_suggested_users_opens(self, logged_in_driver):
        """USR-012 [Functional]: Suggested Users screen opens."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Suggested Users']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "suggestedScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_012")
            raise AssertionError(f"Suggested Users screen not opened: {e}")

    def test_usr_013_top_mentors_screen_opens(self, logged_in_driver):
        """USR-013 [Functional]: Top Mentors screen opens."""
        try:
            _nav_users(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Top Mentors']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "mentorsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_013")
            raise AssertionError(f"Top Mentors screen not opened: {e}")

    def test_usr_014_users_section_heading_visible(self, logged_in_driver):
        """USR-014 [Unit]: Users dashboard grid has visible heading."""
        try:
            _nav_users(logged_in_driver)
            dashboard = wait_visible(logged_in_driver, By.ID, "usersDashboard")
            assert dashboard.is_displayed()
            heading = logged_in_driver.find_element(By.XPATH, "//div[@id='users']//h2")
            assert len(heading.text.strip()) > 0
        except Exception as e:
            take_screenshot(logged_in_driver, "test_usr_014")
            raise AssertionError(f"Users heading validation failed: {e}")
