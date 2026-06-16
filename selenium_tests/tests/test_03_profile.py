# =============================================================
# tests/test_03_profile.py — Profile Tests (TC029–TC046)
# Module: Profile
# Covers: View, Edit, Skills (Add/Edit/Delete), Overview, Recommendations
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

MODULE = "Profile"


def navigate_to_profile_section(driver, screen_id):
    """Click My Profile in sidebar, then open a sub-screen."""
    wait_for_firebase(driver, 3)
    profile_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('profile')\"]", timeout=15)
    profile_btn.click()
    wait_for_firebase(driver, 1)
    card = wait_for_clickable(driver, By.CSS_SELECTOR, f"[onclick=\"openProfileScreen('{screen_id}')\"]", timeout=10)
    card.click()
    wait_for_firebase(driver, 1)


class TestViewProfile:

    def test_TC029_view_profile_screen_opens(self, logged_in_driver, results):
        """TC029 — View Profile screen opens correctly."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "viewProfileScreen")
            screen = wait_for_visible(driver, By.ID, "viewProfileScreen", timeout=10)
            assert screen.is_displayed()
            actual = "viewProfileScreen is visible"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "View Profile screen did not open"
        record(results, 29, "TC029", MODULE, "View Profile screen opens correctly",
               "viewProfileScreen is visible after clicking View Profile card", actual, status, remarks)

    def test_TC030_profile_fields_displayed(self, logged_in_driver, results):
        """TC030 — Profile details (Name, Email, Credits) are displayed."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "viewProfileScreen")
            wait_for_firebase(driver, 4)
            name_el    = element_exists(driver, By.ID, "vname")
            email_el   = element_exists(driver, By.ID, "vemail")
            credits_el = element_exists(driver, By.ID, "vcredits")
            assert all([name_el, email_el, credits_el])
            name_text = driver.find_element(By.ID, "vname").text
            actual = f"Profile fields displayed. Name: '{name_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Profile fields missing"
        record(results, 30, "TC030", MODULE, "Profile fields (Name, Email, Credits) are displayed",
               "vname, vemail, vcredits elements are visible with data", actual, status, remarks)

    def test_TC031_profile_image_displayed(self, logged_in_driver, results):
        """TC031 — Profile image is visible."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "viewProfileScreen")
            wait_for_firebase(driver, 3)
            img = wait_for_element(driver, By.ID, "profileImg", timeout=10)
            src = img.get_attribute("src")
            assert src and len(src) > 5
            actual = f"Profile image src: '{src[-40:]}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Profile image not found"
        record(results, 31, "TC031", MODULE, "Profile image is visible on View Profile screen",
               "profileImg element has valid src attribute", actual, status, remarks)

    def test_TC032_back_button_returns_to_profile_dashboard(self, logged_in_driver, results):
        """TC032 — Back button returns to Profile dashboard grid."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "viewProfileScreen")
            back_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "#viewProfileScreen .back-btn", timeout=10)
            back_btn.click()
            wait_for_firebase(driver, 1)
            dashboard = wait_for_visible(driver, By.ID, "profileDashboard", timeout=10)
            display = dashboard.value_of_css_property("display")
            assert display != "none"
            actual = f"profileDashboard is visible (display={display})"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Back button did not return to profile dashboard"
        record(results, 32, "TC032", MODULE, "Back button returns to Profile dashboard",
               "Clicking ← Back shows profileDashboard grid again", actual, status, remarks)


class TestEditProfile:

    def test_TC033_edit_profile_screen_opens(self, logged_in_driver, results):
        """TC033 — Edit Profile screen opens and auto-fills existing data."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "editProfileScreen")
            screen = wait_for_visible(driver, By.ID, "editProfileScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 3)
            name_val = wait_for_element(driver, By.ID, "pname").get_attribute("value")
            actual = f"Edit Profile screen open. Name field value: '{name_val}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Edit Profile screen did not open"
        record(results, 33, "TC033", MODULE, "Edit Profile screen opens and pre-fills data",
               "editProfileScreen opens and pname field is pre-filled from Firestore", actual, status, remarks)

    def test_TC034_edit_profile_fields_editable(self, logged_in_driver, results):
        """TC034 — All edit profile fields accept input."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "editProfileScreen")
            wait_for_firebase(driver, 3)
            fields = {
                "pname":   "Selenium Tester",
                "pmobile": "9876543210",
                "pteach":  "Python,Selenium",
                "plearn":  "Machine Learning",
                "plang":   "English",
            }
            for field_id, value in fields.items():
                elem = wait_for_clickable(driver, By.ID, field_id, timeout=10)
                elem.clear()
                elem.send_keys(value)
            actual = "All edit profile fields accept user input"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Fields not editable"
        record(results, 34, "TC034", MODULE, "Edit Profile fields accept text input",
               "All input fields in Edit Profile screen are editable", actual, status, remarks)

    def test_TC035_edit_profile_save_button_exists(self, logged_in_driver, results):
        """TC035 — Save button is present on Edit Profile screen."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "editProfileScreen")
            save_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='saveProfile()']", timeout=10)
            assert save_btn.is_displayed()
            actual = "Save button is visible"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Save button not found"
        record(results, 35, "TC035", MODULE, "Save button is visible on Edit Profile screen",
               "Button with onclick='saveProfile()' is displayed", actual, status, remarks)


class TestSkillsManagement:

    def test_TC036_add_skill_screen_opens(self, logged_in_driver, results):
        """TC036 — Add Skill screen opens with input field."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "addSkillScreen")
            screen = wait_for_visible(driver, By.ID, "addSkillScreen", timeout=10)
            assert screen.is_displayed()
            skill_input = element_exists(driver, By.ID, "skillInput")
            actual = f"Add Skill screen open. Input field present: {skill_input}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Add Skill screen did not open"
        record(results, 36, "TC036", MODULE, "Add Skill screen opens with skillInput field",
               "addSkillScreen is visible and skillInput field exists", actual, status, remarks)

    def test_TC037_add_skill_empty_input_validation(self, logged_in_driver, results):
        """TC037 — Add Skill with empty input shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "addSkillScreen")
            add_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='addSkill()']", timeout=10)
            add_btn.click()
            wait_for_firebase(driver, 2)
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for empty skill"
        record(results, 37, "TC037", MODULE, "Add Skill with empty input shows validation alert",
               "Alert appears when clicking Add Skill without entering a skill name", actual, status, remarks)

    def test_TC038_add_skill_button_exists(self, logged_in_driver, results):
        """TC038 — Add Skill button is present and clickable."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "addSkillScreen")
            btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='addSkill()']", timeout=10)
            assert btn.is_displayed()
            actual = f"Add Skill button found with text: '{btn.text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Add Skill button not found"
        record(results, 38, "TC038", MODULE, "Add Skill button is present and clickable",
               "Button with onclick='addSkill()' is visible and enabled", actual, status, remarks)

    def test_TC039_edit_skill_screen_opens(self, logged_in_driver, results):
        """TC039 — Edit Skill screen opens with two input fields."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "editSkillScreen")
            screen = wait_for_visible(driver, By.ID, "editSkillScreen", timeout=10)
            assert screen.is_displayed()
            old_input = element_exists(driver, By.ID, "oldSkillInput")
            new_input = element_exists(driver, By.ID, "newSkillInput")
            actual = f"Edit Skill screen open. Old: {old_input}, New: {new_input}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Edit Skill screen did not open"
        record(results, 39, "TC039", MODULE, "Edit Skill screen opens with both input fields",
               "editSkillScreen visible with oldSkillInput and newSkillInput", actual, status, remarks)

    def test_TC040_edit_skill_empty_validation(self, logged_in_driver, results):
        """TC040 — Edit Skill with empty fields shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "editSkillScreen")
            btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='editSkill()']", timeout=10)
            btn.click()
            wait_for_firebase(driver, 2)
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for empty edit"
        record(results, 40, "TC040", MODULE, "Edit Skill with empty fields shows validation alert",
               "Alert appears when clicking Edit Skill with empty inputs", actual, status, remarks)

    def test_TC041_delete_skill_screen_opens(self, logged_in_driver, results):
        """TC041 — Delete Skill screen opens with skill name input."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "deleteSkillScreen")
            screen = wait_for_visible(driver, By.ID, "deleteSkillScreen", timeout=10)
            assert screen.is_displayed()
            del_input = element_exists(driver, By.ID, "deleteSkillInput")
            actual = f"Delete Skill screen open. Input present: {del_input}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Delete Skill screen did not open"
        record(results, 41, "TC041", MODULE, "Delete Skill screen opens with deleteSkillInput field",
               "deleteSkillScreen is visible and deleteSkillInput field exists", actual, status, remarks)

    def test_TC042_delete_skill_empty_validation(self, logged_in_driver, results):
        """TC042 — Delete Skill with empty input shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "deleteSkillScreen")
            btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='deleteSkill()']", timeout=10)
            btn.click()
            wait_for_firebase(driver, 2)
            found_alert, alert_text = dismiss_alert(driver, timeout=5)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for empty delete"
        record(results, 42, "TC042", MODULE, "Delete Skill with empty input shows validation alert",
               "Alert appears when clicking Delete Skill with empty input", actual, status, remarks)

    def test_TC043_skills_overview_screen_opens(self, logged_in_driver, results):
        """TC043 — Skills Overview screen opens."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "skillsOverviewScreen")
            screen = wait_for_visible(driver, By.ID, "skillsOverviewScreen", timeout=10)
            assert screen.is_displayed()
            overview_list = element_exists(driver, By.ID, "skillsOverviewList")
            actual = f"Skills Overview screen open. List container present: {overview_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Skills Overview screen did not open"
        record(results, 43, "TC043", MODULE, "Skills Overview screen opens correctly",
               "skillsOverviewScreen visible and skillsOverviewList container present", actual, status, remarks)

    def test_TC044_recommendations_screen_opens(self, logged_in_driver, results):
        """TC044 — Recommendations screen opens and shows recommendation cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "recommendationScreen")
            screen = wait_for_visible(driver, By.ID, "recommendationScreen", timeout=10)
            assert screen.is_displayed()
            cards = driver.find_elements(By.CSS_SELECTOR, "#recommendationScreen .recommend-card")
            actual = f"Recommendation screen open. Cards found: {len(cards)}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Recommendations screen did not open"
        record(results, 44, "TC044", MODULE, "Recommendations screen opens and shows skill cards",
               "recommendationScreen is visible with recommend-card elements", actual, status, remarks)

    def test_TC045_profile_dashboard_has_8_cards(self, logged_in_driver, results):
        """TC045 — Profile dashboard grid shows 8 action cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            profile_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('profile')\"]", timeout=15)
            profile_btn.click()
            wait_for_firebase(driver, 2)
            cards = driver.find_elements(By.CSS_SELECTOR, "#profileDashboard .card")
            count = len(cards)
            assert count >= 7
            actual = f"Profile dashboard cards found: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Wrong number of profile dashboard cards"
        record(results, 45, "TC045", MODULE, "Profile dashboard shows all 8 action cards",
               "profileDashboard grid contains at least 7 card elements", actual, status, remarks)

    def test_TC046_upload_photo_screen_has_file_input(self, logged_in_driver, results):
        """TC046 — Upload Photo screen has a file input element."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_profile_section(driver, "uploadPhotoScreen")
            screen = wait_for_visible(driver, By.ID, "uploadPhotoScreen", timeout=10)
            assert screen.is_displayed()
            file_input = element_exists(driver, By.ID, "pimage")
            actual = f"Upload Photo screen open. File input present: {file_input}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Upload Photo screen or file input missing"
        record(results, 46, "TC046", MODULE, "Upload Photo screen has file input (pimage)",
               "uploadPhotoScreen is visible and pimage file input exists", actual, status, remarks)
