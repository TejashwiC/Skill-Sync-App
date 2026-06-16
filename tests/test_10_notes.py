"""
test_10_notes.py — Notes & PDFs Section Tests (11 tests)
Module: Notes | Types: Functional, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Notes")


def _nav_notes(driver):
    wait_clickable(driver, By.XPATH, "//button[contains(text(),'Notes')]").click()
    time.sleep(1.5)


class TestNotes:

    def test_notes_001_section_loads(self, logged_in_driver):
        """NOTES-001 [Functional]: Notes & PDFs section loads when clicked."""
        try:
            _nav_notes(logged_in_driver)
            assert wait_visible(logged_in_driver, By.ID, "notes").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_001")
            raise AssertionError(f"Notes section not loaded: {e}")

    def test_notes_002_heading_text(self, logged_in_driver):
        """NOTES-002 [Unit]: Notes section shows correct heading text."""
        try:
            _nav_notes(logged_in_driver)
            heading = wait_visible(logged_in_driver, By.XPATH, "//div[@id='notes']//h2")
            assert "Notes" in heading.text or "PDF" in heading.text
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_002")
            raise AssertionError(f"Notes heading incorrect: {e}")

    def test_notes_003_dashboard_cards(self, logged_in_driver):
        """NOTES-003 [Unit]: Notes dashboard shows at least 2 sub-section cards."""
        try:
            _nav_notes(logged_in_driver)
            cards = wait_visible(logged_in_driver, By.ID, "notesDashboard").find_elements(
                By.CSS_SELECTOR, ".card"
            )
            assert len(cards) >= 2, f"Expected ≥2 cards, got {len(cards)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_003")
            raise AssertionError(f"Notes cards count wrong: {e}")

    def test_notes_004_upload_notes_screen(self, logged_in_driver):
        """NOTES-004 [Functional]: Upload Notes screen opens."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Upload Notes']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "uploadNotesScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_004")
            raise AssertionError(f"Upload Notes screen not opened: {e}")

    def test_notes_005_pdf_file_input_accept(self, logged_in_driver):
        """NOTES-005 [Unit]: File input accepts application/pdf only."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Upload Notes']/..").click()
            time.sleep(2)
            file_input = logged_in_driver.find_element(By.ID, "pdfFile")
            assert "pdf" in (file_input.get_attribute("accept") or "").lower()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_005")
            raise AssertionError(f"PDF file input accept type wrong: {e}")

    def test_notes_006_upload_button_present(self, logged_in_driver):
        """NOTES-006 [Unit]: Upload PDF button is visible."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Upload Notes']/..").click()
            time.sleep(2)
            btn = wait_visible(logged_in_driver, By.ID, "uploadPDFBtn")
            assert btn.is_displayed() and "Upload" in btn.text
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_006")
            raise AssertionError(f"Upload button missing: {e}")

    def test_notes_007_progress_bar_hidden_default(self, logged_in_driver):
        """NOTES-007 [Unit]: Upload progress bar is hidden by default."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Upload Notes']/..").click()
            time.sleep(2)
            progress = logged_in_driver.find_element(By.ID, "uploadProgressWrap")
            assert not progress.is_displayed(), "Progress bar should be hidden initially"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_007")
            raise AssertionError(f"Progress bar visible initially: {e}")

    def test_notes_008_view_notes_screen(self, logged_in_driver):
        """NOTES-008 [Functional]: Uploaded PDFs screen opens."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Uploaded PDFs']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "viewNotesScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_008")
            raise AssertionError(f"Uploaded PDFs screen not opened: {e}")

    def test_notes_009_pdf_list_container(self, logged_in_driver):
        """NOTES-009 [Unit]: PDF list container is present in Uploaded PDFs screen."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Uploaded PDFs']/..").click()
            time.sleep(3)
            el = logged_in_driver.find_element(By.ID, "pdfList")
            assert el is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_009")
            raise AssertionError(f"PDF list container missing: {e}")

    def test_notes_010_view_notes_back_btn(self, logged_in_driver):
        """NOTES-010 [Functional]: Back button on Uploaded PDFs returns to dashboard."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Uploaded PDFs']/..").click()
            time.sleep(2)
            wait_clickable(logged_in_driver, By.CSS_SELECTOR, "#viewNotesScreen .back-btn").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "notesDashboard").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_010")
            raise AssertionError(f"Back from Uploaded PDFs failed: {e}")

    def test_notes_011_upload_notes_back_btn(self, logged_in_driver):
        """NOTES-011 [Functional]: Back button on Upload Notes returns to dashboard."""
        try:
            _nav_notes(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Upload Notes']/..").click()
            time.sleep(2)
            wait_clickable(logged_in_driver, By.CSS_SELECTOR, "#uploadNotesScreen .back-btn").click()
            time.sleep(1)
            assert wait_visible(logged_in_driver, By.ID, "notesDashboard").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_notes_011")
            raise AssertionError(f"Back from Upload Notes failed: {e}")
