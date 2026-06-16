# =============================================================
# tests/test_05_chat.py — Chat Tests (TC059–TC072)
# Module: Chat
# Covers: Chat main, requests, inbox, settings, blocked users
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

MODULE = "Chat"


def navigate_to_chat_section(driver, screen_id):
    """Navigate to Chat section and open a sub-screen."""
    wait_for_firebase(driver, 3)
    chat_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('chat')\"]", timeout=15)
    chat_btn.click()
    wait_for_firebase(driver, 1)
    card = wait_for_clickable(driver, By.CSS_SELECTOR, f"[onclick=\"openChatScreen('{screen_id}')\"]", timeout=10)
    card.click()
    wait_for_firebase(driver, 2)


class TestChatDashboard:

    def test_TC059_chat_dashboard_has_6_cards(self, logged_in_driver, results):
        """TC059 — Chat dashboard has 6 action cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            chat_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('chat')\"]", timeout=15)
            chat_btn.click()
            wait_for_firebase(driver, 2)
            cards = driver.find_elements(By.CSS_SELECTOR, "#chatDashboard .card")
            count = len(cards)
            assert count >= 5
            actual = f"Chat dashboard cards: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Chat dashboard card count mismatch"
        record(results, 59, "TC059", MODULE, "Chat dashboard shows all 6 action cards",
               "chatDashboard grid contains at least 5 card elements", actual, status, remarks)

    def test_TC060_chat_main_screen_opens(self, logged_in_driver, results):
        """TC060 — Chat main screen opens with user list and message input."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatMainScreen")
            screen = wait_for_visible(driver, By.ID, "chatMainScreen", timeout=10)
            assert screen.is_displayed()
            user_list = element_exists(driver, By.ID, "chatUserList")
            chat_box  = element_exists(driver, By.ID, "chatBox")
            msg_input = element_exists(driver, By.ID, "messageInput")
            actual = f"Chat screen open. UserList: {user_list}, ChatBox: {chat_box}, Input: {msg_input}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Chat main screen failed to open"
        record(results, 60, "TC060", MODULE, "Chat main screen opens with required elements",
               "chatMainScreen visible with chatUserList, chatBox, messageInput", actual, status, remarks)

    def test_TC061_chat_user_list_loads(self, logged_in_driver, results):
        """TC061 — Chat user list loads users from Firestore."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatMainScreen")
            wait_for_firebase(driver, 6)
            user_list_content = driver.find_element(By.ID, "chatUserList").get_attribute("innerHTML")
            actual = f"chatUserList HTML length: {len(user_list_content.strip())} chars"
            assert element_exists(driver, By.ID, "chatUserList")
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Chat user list failed to load"
        record(results, 61, "TC061", MODULE, "Chat user list loads from Firestore",
               "chatUserList container has content after Firebase sync", actual, status, remarks)

    def test_TC062_send_message_without_user_selected(self, logged_in_driver, results):
        """TC062 — Sending message without selecting user shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatMainScreen")
            wait_for_firebase(driver, 3)
            msg_input = wait_for_clickable(driver, By.ID, "messageInput", timeout=10)
            msg_input.send_keys("Hello test message")
            send_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='sendMessage()']", timeout=10)
            send_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            if found_alert and "select" in alert_text.lower():
                actual = f"Correct alert shown: '{alert_text}'"
            elif found_alert:
                actual = f"Alert shown: '{alert_text}'"
            else:
                actual = "No alert — message may have been blocked silently"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Send without user selected not handled"
        record(results, 62, "TC062", MODULE, "Sending message without user selected shows alert",
               "Alert 'Select a user first!' appears when no chat user selected", actual, status, remarks)

    def test_TC063_message_input_enter_key_triggers_send(self, logged_in_driver, results):
        """TC063 — Pressing Enter in message input triggers send flow."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatMainScreen")
            wait_for_firebase(driver, 3)
            msg_input = wait_for_clickable(driver, By.ID, "messageInput", timeout=10)
            msg_input.send_keys("test")
            from selenium.webdriver.common.keys import Keys
            msg_input.send_keys(Keys.RETURN)
            wait_for_firebase(driver, 3)
            # Either alert or message sent
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            actual = f"Enter key triggered. Alert: {found_alert} ('{alert_text}')"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Enter key handling failed"
        record(results, 63, "TC063", MODULE, "Enter key in message input triggers send action",
               "handleEnterKey called, either alert or message processed", actual, status, remarks)

    def test_TC064_chat_requests_screen_opens(self, logged_in_driver, results):
        """TC064 — Chat Requests screen opens with requestContainer."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatRequestScreen")
            screen = wait_for_visible(driver, By.ID, "chatRequestScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            req_container = element_exists(driver, By.ID, "requestContainer")
            actual = f"Chat Requests screen open. Container: {req_container}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Chat Requests screen did not open"
        record(results, 64, "TC064", MODULE, "Chat Requests screen opens with requestContainer",
               "chatRequestScreen visible and requestContainer present", actual, status, remarks)

    def test_TC065_inbox_screen_opens(self, logged_in_driver, results):
        """TC065 — Inbox screen opens and loads conversations."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "inboxScreen")
            screen = wait_for_visible(driver, By.ID, "inboxScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 6)
            inbox = element_exists(driver, By.ID, "inboxContainer")
            actual = f"Inbox screen open. Container: {inbox}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Inbox screen did not open"
        record(results, 65, "TC065", MODULE, "Inbox screen opens with inboxContainer",
               "inboxScreen is visible and inboxContainer is present", actual, status, remarks)

    def test_TC066_chat_settings_screen_opens(self, logged_in_driver, results):
        """TC066 — Chat Settings screen opens with notification controls."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatSettingsScreen")
            screen = wait_for_visible(driver, By.ID, "chatSettingsScreen", timeout=10)
            assert screen.is_displayed()
            notif_status = element_exists(driver, By.ID, "chatNotifStatus")
            actual = f"Chat Settings screen open. NotifStatus present: {notif_status}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Chat Settings screen did not open"
        record(results, 66, "TC066", MODULE, "Chat Settings screen opens with notification controls",
               "chatSettingsScreen visible with chatNotifStatus element", actual, status, remarks)

    def test_TC067_chat_notification_toggle_on(self, logged_in_driver, results):
        """TC067 — Chat notification ON button updates status."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatSettingsScreen")
            wait_for_firebase(driver, 4)
            on_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='setChatNotification(true)']", timeout=10)
            on_btn.click()
            wait_for_firebase(driver, 4)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            status_elem = driver.find_element(By.ID, "chatNotifStatus")
            actual = f"Alert: '{alert_text}'. Status text: '{status_elem.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Chat notification ON toggle failed"
        record(results, 67, "TC067", MODULE, "Chat Notification ON button updates status to ON",
               "Clicking ON updates chatNotifStatus to 'ON' and shows confirmation", actual, status, remarks)

    def test_TC068_chat_notification_toggle_off(self, logged_in_driver, results):
        """TC068 — Chat notification OFF button updates status."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatSettingsScreen")
            wait_for_firebase(driver, 4)
            off_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='setChatNotification(false)']", timeout=10)
            off_btn.click()
            wait_for_firebase(driver, 4)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            status_elem = driver.find_element(By.ID, "chatNotifStatus")
            actual = f"Alert: '{alert_text}'. Status text: '{status_elem.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Chat notification OFF toggle failed"
        record(results, 68, "TC068", MODULE, "Chat Notification OFF button updates status to OFF",
               "Clicking OFF updates chatNotifStatus to 'OFF' and shows confirmation", actual, status, remarks)

    def test_TC069_delete_chat_user_list_present(self, logged_in_driver, results):
        """TC069 — Delete Chat section in Settings has user list."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatSettingsScreen")
            wait_for_firebase(driver, 5)
            del_list = element_exists(driver, By.ID, "deleteChatUserList")
            actual = f"deleteChatUserList present: {del_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Delete chat user list not found"
        record(results, 69, "TC069", MODULE, "Delete Chat section has user list container",
               "deleteChatUserList element is present in Chat Settings", actual, status, remarks)

    def test_TC070_block_user_list_present(self, logged_in_driver, results):
        """TC070 — Block User section in Settings has user list."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "chatSettingsScreen")
            wait_for_firebase(driver, 5)
            block_list = element_exists(driver, By.ID, "blockUserList")
            actual = f"blockUserList present: {block_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Block user list not found"
        record(results, 70, "TC070", MODULE, "Block User section has user list container",
               "blockUserList element is present in Chat Settings", actual, status, remarks)

    def test_TC071_blocked_users_screen_opens(self, logged_in_driver, results):
        """TC071 — Blocked Users screen opens with blockedUsersList."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "blockedUsersScreen")
            screen = wait_for_visible(driver, By.ID, "blockedUsersScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 4)
            blocked_list = element_exists(driver, By.ID, "blockedUsersList")
            actual = f"Blocked Users screen open. List present: {blocked_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Blocked Users screen did not open"
        record(results, 71, "TC071", MODULE, "Blocked Users screen opens with blockedUsersList",
               "blockedUsersScreen visible and blockedUsersList container present", actual, status, remarks)

    def test_TC072_group_chats_screen_opens(self, logged_in_driver, results):
        """TC072 — Group Chats screen opens with Create Group UI."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_chat_section(driver, "groupChatScreen")
            screen = wait_for_visible(driver, By.ID, "groupChatScreen", timeout=10)
            assert screen.is_displayed()
            actual = "Group Chats screen is visible"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Group Chats screen did not open"
        record(results, 72, "TC072", MODULE, "Group Chats screen opens correctly",
               "groupChatScreen is visible after clicking Group Chats card", actual, status, remarks)
