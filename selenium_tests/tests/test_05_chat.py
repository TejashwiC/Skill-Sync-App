"""
test_05_chat.py — Chat Tests (TC059–TC072)
Uses element_in_source() — fast page-source check before Firebase redirect.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestChat:

    def test_TC059_chat_dashboard_exists(self, driver, base_url):
        """TC059: #chatDashboard exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chatDashboard")

    def test_TC060_chat_main_screen_exists(self, driver, base_url):
        """TC060: #chatMainScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chatMainScreen")

    def test_TC061_chat_user_list_exists(self, driver, base_url):
        """TC061: #chatUserList exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chatUserList")

    def test_TC062_chat_box_exists(self, driver, base_url):
        """TC062: #chatBox exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chatBox")

    def test_TC063_message_input_exists(self, driver, base_url):
        """TC063: #messageInput exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "messageInput")

    def test_TC064_chat_request_screen_exists(self, driver, base_url):
        """TC064: #chatRequestScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chatRequestScreen")

    def test_TC065_request_container_exists(self, driver, base_url):
        """TC065: #requestContainer exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "requestContainer")

    def test_TC066_inbox_screen_exists(self, driver, base_url):
        """TC066: #inboxScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "inboxScreen")

    def test_TC067_inbox_container_exists(self, driver, base_url):
        """TC067: #inboxContainer exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "inboxContainer")

    def test_TC068_chat_settings_screen_exists(self, driver, base_url):
        """TC068: #chatSettingsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chatSettingsScreen")

    def test_TC069_chat_notif_status_exists(self, driver, base_url):
        """TC069: #chatNotifStatus exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "chatNotifStatus")

    def test_TC070_blocked_users_screen_exists(self, driver, base_url):
        """TC070: #blockedUsersScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "blockedUsersScreen")

    def test_TC071_blocked_users_list_exists(self, driver, base_url):
        """TC071: #blockedUsersList exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "blockedUsersList")

    def test_TC072_group_chat_screen_exists(self, driver, base_url):
        """TC072: #groupChatScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "groupChatScreen")
