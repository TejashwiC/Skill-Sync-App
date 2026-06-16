"""
test_07_skilltest.py — Skill Test Tests (TC087–TC100)
Uses element_in_source() — fast page-source check before Firebase redirect.
"""
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from conftest import element_in_source


class TestSkillTest:

    def test_TC087_test_dashboard_exists(self, driver, base_url):
        """TC087: #testDashboard exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "testDashboard")

    def test_TC088_create_test_screen_exists(self, driver, base_url):
        """TC088: #createTestScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "createTestScreen")

    def test_TC089_test_title_field_exists(self, driver, base_url):
        """TC089: #testTitle input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "testTitle")

    def test_TC090_test_skill_field_exists(self, driver, base_url):
        """TC090: #testSkill input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "testSkill")

    def test_TC091_test_credits_field_exists(self, driver, base_url):
        """TC091: #testCredits input exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "testCredits")

    def test_TC092_my_tests_list_exists(self, driver, base_url):
        """TC092: #myTestsList exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "myTestsList")

    def test_TC093_add_question_screen_exists(self, driver, base_url):
        """TC093: #addQuestionScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "addQuestionScreen")

    def test_TC094_question_field_exists(self, driver, base_url):
        """TC094: #question textarea exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "question")

    def test_TC095_option_fields_exist(self, driver, base_url):
        """TC095: All 4 option inputs (#option1-#option4) exist in dashboard HTML."""
        assert all(element_in_source(driver, base_url, opt_id) for opt_id in ["option1", "option2", "option3", "option4"])

    def test_TC096_correct_answer_dropdown_exists(self, driver, base_url):
        """TC096: #correctAnswer dropdown exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "correctAnswer")

    def test_TC097_my_questions_screen_exists(self, driver, base_url):
        """TC097: #myQuestionsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "myQuestionsScreen")

    def test_TC098_attend_test_screen_exists(self, driver, base_url):
        """TC098: #attendTestScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "attendTestScreen")

    def test_TC099_results_screen_exists(self, driver, base_url):
        """TC099: #resultsScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "resultsScreen")

    def test_TC100_leaderboard_screen_exists(self, driver, base_url):
        """TC100: #leaderboardScreen exists in dashboard HTML."""
        assert element_in_source(driver, base_url, "leaderboardScreen")
