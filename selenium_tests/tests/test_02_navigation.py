# =============================================================
# tests/test_02_navigation.py — Navigation Tests (TC019–TC028)
# Module: Navigation
# Covers: Sidebar links, section switching, hamburger menu
# =============================================================
import pytest
import time
from selenium.webdriver.common.by import By

import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from conftest import record
from utils.wait_helpers import (
    wait_for_element, wait_for_clickable, wait_for_visible,
    element_exists, wait_for_firebase, safe_get
)
from config import DASHBOARD_URL

MODULE = "Navigation"

SIDEBAR_SECTIONS = [
    ("home",        "TC019", 19, "Home section visible after clicking Home"),
    ("profile",     "TC020", 20, "Profile section visible after clicking My Profile"),
    ("users",       "TC021", 21, "Users section visible after clicking Users"),
    ("chat",        "TC022", 22, "Chat section visible after clicking Chat"),
    ("session",     "TC023", 23, "Session section visible after clicking Session"),
    ("testSection", "TC024", 24, "Skill Test section visible after clicking Skill Test"),
    ("notes",       "TC025", 25, "Notes section visible after clicking Notes & PDFs"),
    ("settings",    "TC026", 26, "Settings section visible after clicking Settings"),
]


class TestDashboardNavigation:

    @pytest.mark.parametrize("section_id,tc_id,sno,description", SIDEBAR_SECTIONS)
    def test_sidebar_navigation(self, logged_in_driver, results, section_id, tc_id, sno, description):
        """TC019–TC026 — Each sidebar link shows the correct section."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            wait_for_firebase(driver, 4)
            # Map section_id to sidebar button text / aria
            section_map = {
                "home":        "button[onclick=\"navTo('home')\"]",
                "profile":     "button[onclick=\"navTo('profile')\"]",
                "users":       "button[onclick=\"navTo('users')\"]",
                "chat":        "button[onclick=\"navTo('chat')\"]",
                "session":     "button[onclick=\"navTo('session')\"]",
                "testSection": "button[onclick=\"navTo('testSection')\"]",
                "notes":       "button[onclick=\"navTo('notes')\"]",
                "settings":    "button[onclick=\"navTo('settings')\"]",
            }
            btn_selector = section_map[section_id]
            nav_btn = wait_for_clickable(driver, By.CSS_SELECTOR, btn_selector, timeout=15)
            nav_btn.click()
            wait_for_firebase(driver, 1)
            section_elem = driver.find_element(By.ID, section_id)
            display = section_elem.value_of_css_property("display")
            assert display != "none"
            actual = f"Section '{section_id}' is displayed (display={display})"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), f"Section '{section_id}' not shown"
        record(results, sno, tc_id, MODULE, description,
               f"Section '{section_id}' becomes visible", actual, status, remarks)

    def test_TC027_logo_visible_in_sidebar(self, logged_in_driver, results):
        """TC027 — SkillSync logo is visible in the sidebar."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            wait_for_firebase(driver, 3)
            logo = wait_for_visible(driver, By.CSS_SELECTOR, ".sidebar .logo img", timeout=10)
            assert logo.is_displayed()
            src = logo.get_attribute("src")
            actual = f"Logo displayed, src: '{src[-30:]}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Logo not found in sidebar"
        record(results, 27, "TC027", MODULE, "SkillSync logo is visible in the sidebar",
               "Logo image is displayed in the sidebar", actual, status, remarks)

    def test_TC028_welcome_text_displayed(self, logged_in_driver, results):
        """TC028 — Welcome text is displayed in the top bar with user name."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            wait_for_firebase(driver, 5)
            welcome_elem = wait_for_element(driver, By.ID, "welcomeText", timeout=15)
            text = welcome_elem.text
            assert "Welcome" in text
            actual = f"Welcome text: '{text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Welcome text not found"
        record(results, 28, "TC028", MODULE, "Welcome text displayed in the top bar",
               "welcomeText element contains 'Welcome' and user name", actual, status, remarks)
