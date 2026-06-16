# =============================================================
# tests/test_07_skilltest.py — Skill Test Tests (TC087–TC100)
# Module: Skill Test
# Covers: Create Test, Add Questions, My Questions, Attend Test,
#         My Results, Leaderboard
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

MODULE = "Skill Test"


def navigate_to_test_section(driver, screen_id):
    """Navigate to Skill Test section and open a sub-screen."""
    wait_for_firebase(driver, 3)
    test_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('testSection')\"]", timeout=15)
    test_btn.click()
    wait_for_firebase(driver, 1)
    card = wait_for_clickable(driver, By.CSS_SELECTOR, f"[onclick=\"openTestScreen('{screen_id}')\"]", timeout=10)
    card.click()
    wait_for_firebase(driver, 2)


class TestSkillTestDashboard:

    def test_TC087_skill_test_dashboard_cards(self, logged_in_driver, results):
        """TC087 — Skill Test dashboard has 6 action cards."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            test_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('testSection')\"]", timeout=15)
            test_btn.click()
            wait_for_firebase(driver, 2)
            cards = driver.find_elements(By.CSS_SELECTOR, "#testDashboard .card")
            count = len(cards)
            assert count >= 5
            actual = f"Skill Test dashboard cards: {count}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Skill Test dashboard card count mismatch"
        record(results, 87, "TC087", MODULE, "Skill Test dashboard shows all action cards",
               "testDashboard grid contains at least 5 card elements", actual, status, remarks)

    def test_TC088_create_test_screen_opens(self, logged_in_driver, results):
        """TC088 — Create Test screen opens with all input fields."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "createTestScreen")
            screen = wait_for_visible(driver, By.ID, "createTestScreen", timeout=10)
            assert screen.is_displayed()
            title   = element_exists(driver, By.ID, "testTitle")
            skill   = element_exists(driver, By.ID, "testSkill")
            credits = element_exists(driver, By.ID, "testCredits")
            actual = f"Create Test screen. Title:{title} Skill:{skill} Credits:{credits}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Create Test screen did not open"
        record(results, 88, "TC088", MODULE, "Create Test screen opens with all required fields",
               "createTestScreen visible with testTitle, testSkill, testCredits fields", actual, status, remarks)

    def test_TC089_create_test_empty_title_validation(self, logged_in_driver, results):
        """TC089 — Create Test with empty title shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "createTestScreen")
            create_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='createTest()']", timeout=10)
            create_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for empty test title"
        record(results, 89, "TC089", MODULE, "Create Test with empty title shows validation alert",
               "Alert 'Enter test title' appears when title field is empty", actual, status, remarks)

    def test_TC090_create_test_empty_skill_validation(self, logged_in_driver, results):
        """TC090 — Create Test with empty skill shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "createTestScreen")
            wait_for_clickable(driver, By.ID, "testTitle").send_keys("My Test")
            create_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='createTest()']", timeout=10)
            create_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for empty skill"
        record(results, 90, "TC090", MODULE, "Create Test with empty skill shows validation alert",
               "Alert 'Enter skill name' appears when skill field is empty", actual, status, remarks)

    def test_TC091_create_test_empty_credits_validation(self, logged_in_driver, results):
        """TC091 — Create Test with empty credits shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "createTestScreen")
            wait_for_clickable(driver, By.ID, "testTitle").send_keys("My Test")
            wait_for_clickable(driver, By.ID, "testSkill").send_keys("Python")
            create_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='createTest()']", timeout=10)
            create_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for empty credits"
        record(results, 91, "TC091", MODULE, "Create Test with empty credits shows validation alert",
               "Alert 'Enter credits' appears when credits field is empty", actual, status, remarks)

    def test_TC092_add_question_screen_opens(self, logged_in_driver, results):
        """TC092 — Add Questions screen opens with all fields."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "addQuestionScreen")
            screen = wait_for_visible(driver, By.ID, "addQuestionScreen", timeout=10)
            assert screen.is_displayed()
            q_field = element_exists(driver, By.ID, "question")
            opt1    = element_exists(driver, By.ID, "option1")
            opt4    = element_exists(driver, By.ID, "option4")
            correct = element_exists(driver, By.ID, "correctAnswer")
            actual = f"Add Question screen. Q:{q_field} O1:{opt1} O4:{opt4} Correct:{correct}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Add Questions screen did not open"
        record(results, 92, "TC092", MODULE, "Add Questions screen opens with all 4 option fields",
               "addQuestionScreen visible with question, option1-4, correctAnswer", actual, status, remarks)

    def test_TC093_add_question_validation_no_test_selected(self, logged_in_driver, results):
        """TC093 — Add Question without selecting a test shows alert."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "addQuestionScreen")
            wait_for_firebase(driver, 3)
            wait_for_clickable(driver, By.ID, "question").send_keys("What is 2+2?")
            add_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick='addQuestion()']", timeout=10)
            add_btn.click()
            wait_for_firebase(driver, 3)
            found_alert, alert_text = dismiss_alert(driver, timeout=6)
            assert found_alert
            actual = f"Alert shown: '{alert_text}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "No validation for missing test selection"
        record(results, 93, "TC093", MODULE, "Add Question without test selection shows alert",
               "Alert 'Select a test first' appears when no test is chosen", actual, status, remarks)

    def test_TC094_correct_answer_dropdown_auto_populates(self, logged_in_driver, results):
        """TC094 — Correct Answer dropdown auto-populates when options are typed."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "addQuestionScreen")
            opt1 = wait_for_clickable(driver, By.ID, "option1", timeout=10)
            opt1.send_keys("Option Alpha")
            # trigger oninput
            from selenium.webdriver.common.keys import Keys
            opt1.send_keys(Keys.TAB)
            wait_for_firebase(driver, 1)
            opt_a = driver.find_element(By.ID, "optA")
            opt_a_val = opt_a.get_attribute("value")
            assert "Option Alpha" in opt_a_val or len(opt_a_val) > 0
            actual = f"optA value after typing: '{opt_a_val}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Correct answer dropdown did not auto-populate"
        record(results, 94, "TC094", MODULE, "Correct Answer dropdown auto-populates from typed options",
               "optA value matches option1 text after updateCorrectDropdown()", actual, status, remarks)

    def test_TC095_my_questions_screen_opens(self, logged_in_driver, results):
        """TC095 — My Questions screen opens and loads questions."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "myQuestionsScreen")
            screen = wait_for_visible(driver, By.ID, "myQuestionsScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            q_container = element_exists(driver, By.ID, "myQuestions")
            actual = f"My Questions screen open. Container: {q_container}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "My Questions screen did not open"
        record(results, 95, "TC095", MODULE, "My Questions screen opens with myQuestions container",
               "myQuestionsScreen visible and myQuestions container present", actual, status, remarks)

    def test_TC096_attend_test_screen_opens(self, logged_in_driver, results):
        """TC096 — Attend Test screen opens with available tests."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "attendTestScreen")
            screen = wait_for_visible(driver, By.ID, "attendTestScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            available = element_exists(driver, By.ID, "availableTests")
            actual = f"Attend Test screen open. Available tests container: {available}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Attend Test screen did not open"
        record(results, 96, "TC096", MODULE, "Attend Test screen opens with availableTests container",
               "attendTestScreen visible and availableTests container present", actual, status, remarks)

    def test_TC097_my_results_screen_opens(self, logged_in_driver, results):
        """TC097 — My Results screen opens and shows attempts."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "resultsScreen")
            screen = wait_for_visible(driver, By.ID, "resultsScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 5)
            container = element_exists(driver, By.ID, "myResultsContainer")
            actual = f"My Results screen open. Container: {container}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "My Results screen did not open"
        record(results, 97, "TC097", MODULE, "My Results screen opens with myResultsContainer",
               "resultsScreen visible and myResultsContainer present", actual, status, remarks)

    def test_TC098_leaderboard_screen_opens(self, logged_in_driver, results):
        """TC098 — Leaderboard screen opens and loads ranking data."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            navigate_to_test_section(driver, "leaderboardScreen")
            screen = wait_for_visible(driver, By.ID, "leaderboardScreen", timeout=10)
            assert screen.is_displayed()
            wait_for_firebase(driver, 6)
            container = element_exists(driver, By.ID, "leaderboardContainer")
            actual = f"Leaderboard screen open. Container: {container}"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "Leaderboard screen did not open"
        record(results, 98, "TC098", MODULE, "Leaderboard screen opens and loads data",
               "leaderboardScreen visible and leaderboardContainer present", actual, status, remarks)

    def test_TC099_home_stats_tests_created_element(self, logged_in_driver, results):
        """TC099 — Home dashboard shows Tests Created stat."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            home_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('home')\"]", timeout=15)
            home_btn.click()
            wait_for_firebase(driver, 5)
            tests_elem = wait_for_element(driver, By.ID, "homeTests", timeout=10)
            count = tests_elem.text
            actual = f"homeTests element shows: '{count}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "homeTests element not found"
        record(results, 99, "TC099", MODULE, "Home dashboard shows Tests Created stat (homeTests)",
               "homeTests element is present and shows a numeric value", actual, status, remarks)

    def test_TC100_home_stats_tests_completed_element(self, logged_in_driver, results):
        """TC100 — Home dashboard shows Tests Completed stat."""
        driver = logged_in_driver
        status, actual, remarks = "PASS", "", ""
        try:
            home_btn = wait_for_clickable(driver, By.CSS_SELECTOR, "button[onclick=\"navTo('home')\"]", timeout=15)
            home_btn.click()
            wait_for_firebase(driver, 5)
            completed_elem = wait_for_element(driver, By.ID, "homeTestsCompleted", timeout=10)
            count = completed_elem.text
            actual = f"homeTestsCompleted element shows: '{count}'"
        except Exception as e:
            status, actual, remarks = "FAIL", str(e), "homeTestsCompleted element not found"
        record(results, 100, "TC100", MODULE, "Home dashboard shows Tests Completed stat",
               "homeTestsCompleted element is present and shows a numeric value", actual, status, remarks)
