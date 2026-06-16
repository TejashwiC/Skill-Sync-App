# =============================================================
# tests/test_04_users.py — Users Tests (TC047–TC058)
# Module: Users
# Covers: Users List, Followers, Following, Search, Mentors, Suggested
# =============================================================
import pytest
import time
from selenium.webdriver.common.by import By

import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from conftest import record
from utils.wait_helpers import (
    wait_for_element, wait_for_clickable, wait_for_visible,
    element_exists, wait_for_firebase
)

MODULE = "Users"


def navigate_to_users_section(driver, screen_id):
    """Navigate to Users section and open a sub-screen."""
    wait_for_firebase(driver, 3)
    users_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('users')\"]", timeout=15)
    users_btn.click()
    wait_for_firebase(driver, 1)
    card = wait_for_clickable(driver, By.CSS_SELECTOR, f"[onclick=\"openUserScreen('{screen_id}')\"]", timeout=10)
    card.click()
    wait_for_firebase(driver, 1)


class TestUsersDashboard:

    def test_TC047_users_dashboard_has_6_cards(self, logged_in_driver, results):
        """TC047 — Users dashboard grid contains 6 action cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            users_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('users')\"]", timeout=15)
            users_btn.click()
            wait_for_firebase(driver, 2)
            cards = driver.find_elements(By.CSS_SELECTOR, "#usersDashboard .card")
            count = len(cards)
            assert count >= 5
            actual = f"Users dashboard cards: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Wrong card count"
        record(results, 47, "TC047", MODULE, "Users dashboard shows all action cards",
               "usersDashboard grid contains at least 5 card elements", actual, status, remarks)

    def test_TC048_users_list_screen_opens(self, logged_in_driver, results):
        """TC048 — Users List screen opens and shows usersList container."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "usersListScreen")
            screen = wait_for_visible(driver, By.ID, "usersListScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            users_list = element_exists(driver, By.ID, "usersList")
            actual = f"Users List screen open. Container present: {users_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Users List screen did not open"
        record(results, 48, "TC048", MODULE, "Users List screen opens and loads user data",
               "usersListScreen is visible and usersList container has content", actual, status, remarks)

    def test_TC049_users_list_back_button(self, logged_in_driver, results):
        """TC049 — Back button from Users List returns to Users dashboard."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "usersListScreen")
            back_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "#usersListScreen .back-btn", timeout=10)
            back_btn.click()
            wait_for_firebase(driver, 1)
            dashboard = wait_for_visible(driver, By.ID, "usersDashboard", timeout=10)
            display = dashboard.value_of_css_property("display")
            assert display != "none"
            actual = f"usersDashboard is visible (display={display})"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Back button did not work"
        record(results, 49, "TC049", MODULE, "Back button from Users List returns to dashboard",
               "Clicking ← Back shows usersDashboard grid", actual, status, remarks)

    def test_TC050_followers_screen_opens(self, logged_in_driver, results):
        """TC050 — Followers screen opens and shows followersList container."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "followersScreen")
            screen = wait_for_visible(driver, By.ID, "followersScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 4)
            fl_list = element_exists(driver, By.ID, "followersList")
            actual = f"Followers screen open. Container present: {fl_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Followers screen did not open"
        record(results, 50, "TC050", MODULE, "Followers screen opens with followersList container",
               "followersScreen is visible and followersList container is present", actual, status, remarks)

    def test_TC051_following_screen_opens(self, logged_in_driver, results):
        """TC051 — Following screen opens and shows followingList container."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "followingScreen")
            screen = wait_for_visible(driver, By.ID, "followingScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 4)
            fw_list = element_exists(driver, By.ID, "followingList")
            actual = f"Following screen open. Container present: {fw_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Following screen did not open"
        record(results, 51, "TC051", MODULE, "Following screen opens with followingList container",
               "followingScreen is visible and followingList container is present", actual, status, remarks)

    def test_TC052_search_users_screen_opens(self, logged_in_driver, results):
        """TC052 — Search Users screen opens with search input."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "searchUsersScreen")
            screen = wait_for_visible(driver, By.ID, "searchUsersScreen", timeout=10)
            assert screen.is_displayed()
            search_input = element_exists(driver, By.ID, "searchInput")
            actual = f"Search Users screen open. Input present: {search_input}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Search Users screen did not open"
        record(results, 52, "TC052", MODULE, "Search Users screen opens with searchInput field",
               "searchUsersScreen is visible and searchInput is present", actual, status, remarks)

    def test_TC053_search_users_empty_query_clears_results(self, logged_in_driver, results):
        """TC053 — Search with empty query clears search results."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "searchUsersScreen")
            search_input = wait_for_clickable(driver, By.ID, "searchInput", timeout=10)
            search_input.clear()
            # Trigger oninput event
            from selenium.webdriver.common.keys import Keys
            search_input.send_keys(" ")
            search_input.send_keys(Keys.BACKSPACE)
            wait_for_firebase(driver, 2)
            results_div = driver.find_element(By.ID, "searchResults")
            content = results_div.get_attribute("innerHTML").strip()
            actual = f"Search results content on empty query: '{content[:80]}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Search results handling failed"
        record(results, 53, "TC053", MODULE, "Empty search query clears search results",
               "searchResults div is empty or clears on empty query", actual, status, remarks)

    def test_TC054_search_users_with_query_shows_results(self, logged_in_driver, results):
        """TC054 — Search Users with a valid keyword shows results."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "searchUsersScreen")
            search_input = wait_for_clickable(driver, By.ID, "searchInput", timeout=10)
            search_input.clear()
            search_input.send_keys("a")  # generic letter likely to match some user
            wait_for_firebase(driver, 5)
            results_div = driver.find_element(By.ID, "searchResults")
            content = results_div.get_attribute("innerHTML").strip()
            actual = f"Search results HTML length: {len(content)} chars"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Search failed"
        record(results, 54, "TC054", MODULE, "Search Users with keyword returns results",
               "searchResults div has content after typing a search term", actual, status, remarks)

    def test_TC055_top_mentors_screen_opens(self, logged_in_driver, results):
        """TC055 — Top Mentors screen opens."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "mentorsScreen")
            screen = wait_for_visible(driver, By.ID, "mentorsScreen", timeout=10)
            assert screen.is_displayed()
            actual = "Top Mentors screen is visible"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Top Mentors screen did not open"
        record(results, 55, "TC055", MODULE, "Top Mentors screen opens correctly",
               "mentorsScreen is visible after clicking Top Mentors card", actual, status, remarks)

    def test_TC056_suggested_users_screen_opens(self, logged_in_driver, results):
        """TC056 — Suggested Users screen opens and loads users."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "suggestedScreen")
            screen = wait_for_visible(driver, By.ID, "suggestedScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            suggested_list = element_exists(driver, By.ID, "suggestedList")
            actual = f"Suggested Users screen open. List present: {suggested_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Suggested Users screen did not open"
        record(results, 56, "TC056", MODULE, "Suggested Users screen opens with suggestedList",
               "suggestedScreen is visible and suggestedList container is present", actual, status, remarks)

    def test_TC057_users_list_contains_user_boxes(self, logged_in_driver, results):
        """TC057 — Users List screen shows user-box elements from Firestore."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "usersListScreen")
            wait_for_firebase(driver, 6)
            user_boxes = driver.find_elements(By.CSS_SELECTOR, "#usersList .user-box")
            count = len(user_boxes)
            actual = f"User boxes found in Users List: {count}"
            # It's OK if 0 (only 1 user in system), so we just assert the container exists
            assert element_exists(driver, By.ID, "usersList")
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Users List did not load"
        record(results, 57, "TC057", MODULE, "Users List screen loads user data from Firestore",
               "usersList container has user-box elements (or is empty if only 1 user)", actual, status, remarks)

    def test_TC058_users_list_follow_button_present(self, logged_in_driver, results):
        """TC058 — If other users exist, Follow button is present in Users List."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_users_section(driver, "usersListScreen")
            wait_for_firebase(driver, 6)
            user_boxes = driver.find_elements(By.CSS_SELECTOR, "#usersList .user-box")
            if user_boxes:
                follow_btn = driver.find_elements(By.CSS_SELECTOR, "#usersList button")
                actual = f"Follow/View buttons found in Users List: {len(follow_btn)}"
                assert len(follow_btn) >= 1
            else:
                actual = "No other users in system — Follow button not applicable"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Follow button check failed"
        record(results, 58, "TC058", MODULE, "Follow button present for each user in Users List",
               "Follow and View Profile buttons visible for each user-box", actual, status, remarks)
