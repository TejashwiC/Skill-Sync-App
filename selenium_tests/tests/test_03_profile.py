"""
test_03_profile.py — Profile Tests (TC029–TC046)
Uses element_in_source() — fast page-source check before Firebase redirect.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestProfile:

    def test_TC029_profile_dashboard_exists(self, driver, base_url):
        """TC029: #profileDashboard exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "profileDashboard")

    def test_TC030_view_profile_screen_exists(self, driver, base_url):
        """TC030: #viewProfileScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "viewProfileScreen")

    def test_TC031_profile_image_element_exists(self, driver, base_url):
        """TC031: #profileImg exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "profileImg")

    def test_TC032_vname_element_exists(self, driver, base_url):
        """TC032: #vname exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "vname")

    def test_TC033_vemail_element_exists(self, driver, base_url):
        """TC033: #vemail exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "vemail")

    def test_TC034_vcredits_element_exists(self, driver, base_url):
        """TC034: #vcredits exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "vcredits")

    def test_TC035_edit_profile_screen_exists(self, driver, base_url):
        """TC035: #editProfileScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "editProfileScreen")

    def test_TC036_pname_field_exists(self, driver, base_url):
        """TC036: #pname input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "pname")

    def test_TC037_pmobile_field_exists(self, driver, base_url):
        """TC037: #pmobile input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "pmobile")

    def test_TC038_upload_photo_screen_exists(self, driver, base_url):
        """TC038: #uploadPhotoScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "uploadPhotoScreen")

    def test_TC039_pimage_file_input_exists(self, driver, base_url):
        """TC039: #pimage file input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "pimage")

    def test_TC040_add_skill_screen_exists(self, driver, base_url):
        """TC040: #addSkillScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "addSkillScreen")

    def test_TC041_skill_input_exists(self, driver, base_url):
        """TC041: #skillInput exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "skillInput")

    def test_TC042_edit_skill_screen_exists(self, driver, base_url):
        """TC042: #editSkillScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "editSkillScreen")

    def test_TC043_old_skill_input_exists(self, driver, base_url):
        """TC043: #oldSkillInput exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "oldSkillInput")

    def test_TC044_delete_skill_screen_exists(self, driver, base_url):
        """TC044: #deleteSkillScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "deleteSkillScreen")

    def test_TC045_skills_overview_screen_exists(self, driver, base_url):
        """TC045: #skillsOverviewScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "skillsOverviewScreen")

    def test_TC046_recommendation_screen_exists(self, driver, base_url):
        """TC046: #recommendationScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "recommendationScreen")
