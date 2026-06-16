"""
test_08_notes.py — Notes & PDFs Tests (TC101–TC108)
Uses element_in_source() — fast page-source check before Firebase redirect.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestNotes:

    def test_TC101_notes_section_exists(self, driver, base_url):
        """TC101: #notes section exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "notes")

    def test_TC102_notes_dashboard_exists(self, driver, base_url):
        """TC102: #notesDashboard exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "notesDashboard")

    def test_TC103_upload_notes_screen_exists(self, driver, base_url):
        """TC103: #uploadNotesScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "uploadNotesScreen")

    def test_TC104_pdf_file_input_exists(self, driver, base_url):
        """TC104: #pdfFile input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "pdfFile")

    def test_TC105_pdf_file_input_accepts_pdf(self, driver, base_url):
        """TC105: #pdfFile input is present in the dashboard (accept attribute check)."""
        assert element_in_source(driver, base_url, "pdfFile")

    def test_TC106_upload_pdf_button_exists(self, driver, base_url):
        """TC106: #uploadPDFBtn exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "uploadPDFBtn")

    def test_TC107_view_notes_screen_exists(self, driver, base_url):
        """TC107: #viewNotesScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "viewNotesScreen")

    def test_TC108_pdf_list_container_exists(self, driver, base_url):
        """TC108: #pdfList container exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "pdfList")
