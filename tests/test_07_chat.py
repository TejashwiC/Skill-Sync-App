"""
test_07_chat.py — Chat Section Tests (17 tests)
Module: Chat | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Chat")


def _nav_chat(driver):
    wait_clickable(driver, By.XPATH, "//button[contains(text(),'Chat')]").click()
    time.sleep(1.5)


class TestChat:

    def test_chat_001_section_loads(self, logged_in_driver):
        """CHAT-001 [Functional]: Chat section loads when clicked."""
        try:
            _nav_chat(logged_in_driver)
            assert wait_visible(logged_in_driver, By.ID, "chat").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_001")
            raise AssertionError(f"Chat section not loaded: {e}")

    def test_chat_002_dashboard_heading(self, logged_in_driver):
        """CHAT-002 [Unit]: Chat section shows Chat heading."""
        try:
            _nav_chat(logged_in_driver)
            heading = wait_visible(logged_in_driver, By.XPATH, "//div[@id='chat']//h2")
            assert "Chat" in heading.text
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_002")
            raise AssertionError(f"Chat heading not found: {e}")

    def test_chat_003_cards_count(self, logged_in_driver):
        """CHAT-003 [Unit]: Chat dashboard has at least 4 sub-section cards."""
        try:
            _nav_chat(logged_in_driver)
            cards = wait_visible(logged_in_driver, By.ID, "chatDashboard").find_elements(
                By.CSS_SELECTOR, ".card"
            )
            assert len(cards) >= 4
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_003")
            raise AssertionError(f"Chat cards count wrong: {e}")

    def test_chat_004_main_chat_opens(self, logged_in_driver):
        """CHAT-004 [Functional]: Chat main screen opens."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "chatMainScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_004")
            raise AssertionError(f"Chat main screen not opened: {e}")

    def test_chat_005_message_input_present(self, logged_in_driver):
        """CHAT-005 [Unit]: Message input field is visible."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "messageInput").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_005")
            raise AssertionError(f"Message input not visible: {e}")

    def test_chat_006_send_button_present(self, logged_in_driver):
        """CHAT-006 [Unit]: Send button is visible in chat screen."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat']/..").click()
            time.sleep(2)
            btn = logged_in_driver.find_element(
                By.XPATH, "//div[@id='chatMainScreen']//button[contains(text(),'Send')]"
            )
            assert btn.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_006")
            raise AssertionError(f"Send button missing: {e}")

    def test_chat_007_chat_user_list_present(self, logged_in_driver):
        """CHAT-007 [Unit]: Chat user list container is present."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat']/..").click()
            time.sleep(2)
            el = logged_in_driver.find_element(By.ID, "chatUserList")
            assert el is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_007")
            raise AssertionError(f"Chat user list missing: {e}")

    def test_chat_008_chat_box_present(self, logged_in_driver):
        """CHAT-008 [Unit]: Chat box display area is present."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat']/..").click()
            time.sleep(2)
            el = logged_in_driver.find_element(By.ID, "chatBox")
            assert el is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_008")
            raise AssertionError(f"Chat box missing: {e}")

    def test_chat_009_send_empty_no_crash(self, logged_in_driver):
        """CHAT-009 [Validation]: Sending empty message does not crash the app."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat']/..").click()
            time.sleep(2)
            msg_input = wait_visible(logged_in_driver, By.ID, "messageInput")
            msg_input.clear()
            logged_in_driver.find_element(
                By.XPATH, "//div[@id='chatMainScreen']//button[contains(text(),'Send')]"
            ).click()
            time.sleep(2)
            dismiss_alert_if_present(logged_in_driver, timeout=3)
            # App should still be responsive
            assert wait_visible(logged_in_driver, By.ID, "chatMainScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_009")
            raise AssertionError(f"Send empty message caused crash: {e}")

    def test_chat_010_chat_requests_opens(self, logged_in_driver):
        """CHAT-010 [Functional]: Chat Requests screen opens."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat Requests']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "chatRequestScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_010")
            raise AssertionError(f"Chat Requests screen not opened: {e}")

    def test_chat_011_inbox_opens(self, logged_in_driver):
        """CHAT-011 [Functional]: Inbox screen opens."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Inbox']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "inboxScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_011")
            raise AssertionError(f"Inbox screen not opened: {e}")

    def test_chat_012_inbox_container_present(self, logged_in_driver):
        """CHAT-012 [Unit]: Inbox container element is present."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Inbox']/..").click()
            time.sleep(3)
            el = logged_in_driver.find_element(By.ID, "inboxContainer")
            assert el is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_012")
            raise AssertionError(f"Inbox container missing: {e}")

    def test_chat_013_chat_settings_opens(self, logged_in_driver):
        """CHAT-013 [Functional]: Chat Settings screen opens."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat Settings']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "chatSettingsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_013")
            raise AssertionError(f"Chat Settings not opened: {e}")

    def test_chat_014_notification_toggles_present(self, logged_in_driver):
        """CHAT-014 [Functional]: Notification ON/OFF toggles are present."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Chat Settings']/..").click()
            time.sleep(2)
            on_btn  = logged_in_driver.find_element(
                By.XPATH, "//button[contains(@onclick,'setChatNotification(true)')]"
            )
            off_btn = logged_in_driver.find_element(
                By.XPATH, "//button[contains(@onclick,'setChatNotification(false)')]"
            )
            assert on_btn.is_displayed() and off_btn.is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_014")
            raise AssertionError(f"Notification toggles missing: {e}")

    def test_chat_015_blocked_users_opens(self, logged_in_driver):
        """CHAT-015 [Functional]: Blocked Users screen opens."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Blocked Users']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "blockedUsersScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_015")
            raise AssertionError(f"Blocked Users screen not opened: {e}")

    def test_chat_016_blocked_users_list_present(self, logged_in_driver):
        """CHAT-016 [Unit]: Blocked users list container is present."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Blocked Users']/..").click()
            time.sleep(2)
            el = logged_in_driver.find_element(By.ID, "blockedUsersList")
            assert el is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_016")
            raise AssertionError(f"Blocked users list missing: {e}")

    def test_chat_017_group_chat_opens(self, logged_in_driver):
        """CHAT-017 [Functional]: Group Chats screen opens."""
        try:
            _nav_chat(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Group Chats']/..").click()
            time.sleep(1.5)
            assert wait_visible(logged_in_driver, By.ID, "groupChatScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_chat_017")
            raise AssertionError(f"Group Chats screen not opened: {e}")
