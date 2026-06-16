"""
test_04_users.py — Users Tests (TC047–TC058)
Uses element_in_source() — fast page-source check before Firebase redirect.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestUsers:

    def test_TC047_users_dashboard_exists(self, driver, base_url):
        """TC047: #usersDashboard exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "usersDashboard")

    def test_TC048_users_list_screen_exists(self, driver, base_url):
        """TC048: #usersListScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "usersListScreen")

    def test_TC049_users_list_container_exists(self, driver, base_url):
        """TC049: #usersList container exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "usersList")

    def test_TC050_followers_screen_exists(self, driver, base_url):
        """TC050: #followersScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "followersScreen")

    def test_TC051_followers_list_exists(self, driver, base_url):
        """TC051: #followersList exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "followersList")

    def test_TC052_following_screen_exists(self, driver, base_url):
        """TC052: #followingScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "followingScreen")

    def test_TC053_following_list_exists(self, driver, base_url):
        """TC053: #followingList exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "followingList")

    def test_TC054_search_users_screen_exists(self, driver, base_url):
        """TC054: #searchUsersScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "searchUsersScreen")

    def test_TC055_search_input_exists(self, driver, base_url):
        """TC055: #searchInput field exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "searchInput")

    def test_TC056_search_results_container_exists(self, driver, base_url):
        """TC056: #searchResults container exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "searchResults")

    def test_TC057_mentors_screen_exists(self, driver, base_url):
        """TC057: #mentorsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "mentorsScreen")

    def test_TC058_suggested_screen_exists(self, driver, base_url):
        """TC058: #suggestedScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "suggestedScreen")
