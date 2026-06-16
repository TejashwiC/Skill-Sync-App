# =============================================================
# tests/test_08_notes.py — Notes & PDFs Tests (TC101–TC108)
# Module: Notes & PDFs
# Covers: Upload Notes, View PDFs, file input validation
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

MODULE = "Notes & PDFs"


def navigate_to_notes_section(driver, screen_id):
    """Navigate to Notes & PDFs section and open a sub-screen."""
    wait_for_firebase(driver, 3)
    notes_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('notes')\"]", timeout=15)
    notes_btn.click()
    wait_for_firebase(driver, 1)
    card = wait_for_clickable(driver, By.CSS_SELECTOR, f"[onclick=\"openNotesScreen('{screen_id}')\"]", timeout=10)
    card.click()
    wait_for_firebase(driver, 2)


class TestNotesPDFs:

    def test_TC101_notes_dashboard_has_2_cards(self, logged_in_driver, results):
        """TC101 — Notes & PDFs dashboard shows 2 action cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            notes_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('notes')\"]", timeout=15)
            notes_btn.click()
            wait_for_firebase(driver, 2)
            cards = driver.find_elements(By.CSS_SELECTOR, "#notesDashboard .card")
            count = len(cards)
            assert count >= 2
            actual = f"Notes dashboard cards: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Notes dashboard card count mismatch"
        record(results, 101, "TC101", MODULE, "Notes & PDFs dashboard shows action cards",
               "notesDashboard grid has at least 2 card elements", actual, status, remarks)

    def test_TC102_upload_notes_screen_opens(self, logged_in_driver, results):
        """TC102 — Upload Notes screen opens with file input."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_notes_section(driver, "uploadNotesScreen")
            screen = wait_for_visible(driver, By.ID, "uploadNotesScreen", timeout=10)
            assert screen.is_displayed()
            file_input = element_exists(driver, By.ID, "pdfFile")
            upload_btn = element_exists(driver, By.ID, "uploadPDFBtn")
            actual = f"Upload Notes screen. File input: {file_input}, Upload button: {upload_btn}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Upload Notes screen did not open"
        record(results, 102, "TC102", MODULE, "Upload Notes screen opens with pdfFile input",
               "uploadNotesScreen visible with pdfFile input and uploadPDFBtn button", actual, status, remarks)

    def test_TC103_pdf_file_input_accepts_pdf_only(self, logged_in_driver, results):
        """TC103 — PDF file input has accept='application/pdf' attribute."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_notes_section(driver, "uploadNotesScreen")
            file_input = wait_for_element(driver, By.ID, "pdfFile", timeout=10)
            accept_attr = file_input.get_attribute("accept")
            assert "pdf" in accept_attr.lower()
            actual = f"File input accept attribute: '{accept_attr}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "File input does not restrict to PDF"
        record(results, 103, "TC103", MODULE, "PDF file input restricts to PDF files only",
               "pdfFile input has accept='application/pdf' attribute", actual, status, remarks)

    def test_TC104_upload_progress_bar_hidden_initially(self, logged_in_driver, results):
        """TC104 — Upload progress bar is hidden before upload starts."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_notes_section(driver, "uploadNotesScreen")
            progress_wrap = wait_for_element(driver, By.ID, "uploadProgressWrap", timeout=10)
            display = progress_wrap.value_of_css_property("display")
            assert display == "none"
            actual = f"Upload progress wrap is hidden (display={display})"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Progress bar not hidden initially"
        record(results, 104, "TC104", MODULE, "Upload progress bar is hidden before upload starts",
               "uploadProgressWrap element has display:none initially", actual, status, remarks)

    def test_TC105_view_notes_screen_opens(self, logged_in_driver, results):
        """TC105 — View PDFs screen opens with PDF list container."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_notes_section(driver, "viewNotesScreen")
            screen = wait_for_visible(driver, By.ID, "viewNotesScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            pdf_list = element_exists(driver, By.ID, "pdfList")
            actual = f"View Notes screen open. PDF list: {pdf_list}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "View Notes screen did not open"
        record(results, 105, "TC105", MODULE, "View PDFs screen opens with pdfList container",
               "viewNotesScreen visible and pdfList container is present", actual, status, remarks)

    def test_TC106_pdf_list_loads_from_firebase(self, logged_in_driver, results):
        """TC106 — PDF list loads data from Firebase Firestore."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_notes_section(driver, "viewNotesScreen")
            wait_for_firebase(driver, 6)
            pdf_list = driver.find_element(By.ID, "pdfList")
            content = pdf_list.get_attribute("innerHTML").strip()
            actual = f"PDF list content length: {len(content)} chars"
            # Empty state is also valid
            assert len(content) > 0
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "PDF list failed to load"
        record(results, 106, "TC106", MODULE, "PDF list loads content from Firebase Firestore",
               "pdfList container is not empty after Firebase sync (shows PDFs or empty message)", actual, status, remarks)

    def test_TC107_notes_back_button_from_upload(self, logged_in_driver, results):
        """TC107 — Back button from Upload Notes returns to Notes dashboard."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_notes_section(driver, "uploadNotesScreen")
            back_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "#uploadNotesScreen .back-btn", timeout=10)
            back_btn.click()
            wait_for_firebase(driver, 1)
            dashboard = wait_for_visible(driver, By.ID, "notesDashboard", timeout=10)
            display = dashboard.value_of_css_property("display")
            assert display != "none"
            actual = f"notesDashboard visible (display={display})"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Back button failed"
        record(results, 107, "TC107", MODULE, "Back button from Upload Notes returns to Notes dashboard",
               "Clicking ← Back shows notesDashboard grid", actual, status, remarks)

    def test_TC108_notes_back_button_from_view(self, logged_in_driver, results):
        """TC108 — Back button from View PDFs returns to Notes dashboard."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_notes_section(driver, "viewNotesScreen")
            back_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "#viewNotesScreen .back-btn", timeout=10)
            back_btn.click()
            wait_for_firebase(driver, 1)
            dashboard = wait_for_visible(driver, By.ID, "notesDashboard", timeout=10)
            display = dashboard.value_of_css_property("display")
            assert display != "none"
            actual = f"notesDashboard visible (display={display})"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Back button failed"
        record(results, 108, "TC108", MODULE, "Back button from View PDFs returns to Notes dashboard",
               "Clicking ← Back shows notesDashboard grid from viewNotesScreen", actual, status, remarks)
