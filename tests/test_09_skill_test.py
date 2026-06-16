"""
test_09_skill_test.py — Skill Test Section Tests (18 tests)
Module: Skill Test | Types: Functional, Validation, Unit
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.SkillTest")


def _nav_skill_test(driver):
    wait_clickable(driver, By.XPATH, "//button[contains(text(),'Skill Test')]").click()
    time.sleep(1.5)


class TestSkillTest:

    def test_tst_001_section_loads(self, logged_in_driver):
        """TST-001 [Functional]: Skill Test section loads when clicked."""
        try:
            _nav_skill_test(logged_in_driver)
            assert wait_visible(logged_in_driver, By.ID, "testSection").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_001")
            raise AssertionError(f"Skill Test section not loaded: {e}")

    def test_tst_002_heading_text(self, logged_in_driver):
        """TST-002 [Unit]: Skill Test section heading contains 'Test' or 'Skill'."""
        try:
            _nav_skill_test(logged_in_driver)
            heading = wait_visible(
                logged_in_driver, By.XPATH, "//div[@id='testSection']//h2"
            )
            assert "Test" in heading.text or "Skill" in heading.text
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_002")
            raise AssertionError(f"Skill Test heading incorrect: {e}")

    def test_tst_003_cards_count(self, logged_in_driver):
        """TST-003 [Unit]: Skill Test dashboard has at least 4 sub-section cards."""
        try:
            _nav_skill_test(logged_in_driver)
            cards = wait_visible(logged_in_driver, By.ID, "testDashboard").find_elements(
                By.CSS_SELECTOR, ".card"
            )
            assert len(cards) >= 4, f"Expected ≥4 cards, got {len(cards)}"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_003")
            raise AssertionError(f"Skill Test cards count wrong: {e}")

    def test_tst_004_create_test_screen(self, logged_in_driver):
        """TST-004 [Functional]: Create Test screen opens."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Create Test']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "createTestScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_004")
            raise AssertionError(f"Create Test screen not opened: {e}")

    def test_tst_005_create_test_title_field(self, logged_in_driver):
        """TST-005 [Unit]: Test Title input is present on Create Test screen."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Create Test']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "testTitle").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_005")
            raise AssertionError(f"testTitle field missing: {e}")

    def test_tst_006_create_test_skill_field(self, logged_in_driver):
        """TST-006 [Unit]: Skill field is present on Create Test screen."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Create Test']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "testSkill").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_006")
            raise AssertionError(f"testSkill field missing: {e}")

    def test_tst_007_create_test_credits_number(self, logged_in_driver):
        """TST-007 [Unit]: Credits field has type=number."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Create Test']/..").click()
            time.sleep(2)
            field = wait_visible(logged_in_driver, By.ID, "testCredits")
            assert field.get_attribute("type") == "number"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_007")
            raise AssertionError(f"testCredits type wrong: {e}")

    def test_tst_008_empty_title_alert(self, logged_in_driver):
        """TST-008 [Validation]: Creating test without title shows alert."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Create Test']/..").click()
            time.sleep(2)
            logged_in_driver.find_element(By.ID, "testTitle").clear()
            logged_in_driver.find_element(By.ID, "testSkill").send_keys("Python")
            logged_in_driver.find_element(By.ID, "testCredits").send_keys("5")
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='createTestScreen']//button[contains(text(),'Create Test')]"
            ).click()
            time.sleep(3)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=6)
            assert alert is not None and "title" in alert.lower()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_008")
            raise AssertionError(f"Empty title alert not shown: {e}")

    def test_tst_009_empty_skill_alert(self, logged_in_driver):
        """TST-009 [Validation]: Creating test without skill shows alert."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Create Test']/..").click()
            time.sleep(2)
            logged_in_driver.find_element(By.ID, "testTitle").send_keys("Python Basics")
            logged_in_driver.find_element(By.ID, "testSkill").clear()
            logged_in_driver.find_element(By.ID, "testCredits").send_keys("5")
            wait_clickable(
                logged_in_driver,
                By.XPATH, "//div[@id='createTestScreen']//button[contains(text(),'Create Test')]"
            ).click()
            time.sleep(3)
            alert = dismiss_alert_if_present(logged_in_driver, timeout=6)
            assert alert is not None and "skill" in alert.lower()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_009")
            raise AssertionError(f"Empty skill alert not shown: {e}")

    def test_tst_010_add_questions_screen(self, logged_in_driver):
        """TST-010 [Functional]: Add Questions screen opens."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Add Questions']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "addQuestionScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_010")
            raise AssertionError(f"Add Questions screen not opened: {e}")

    def test_tst_011_question_and_options_fields(self, logged_in_driver):
        """TST-011 [Unit]: Question + 4 option fields are all visible."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Add Questions']/..").click()
            time.sleep(2)
            for fid in ["question", "option1", "option2", "option3", "option4"]:
                assert logged_in_driver.find_element(By.ID, fid).is_displayed(), f"#{fid} hidden"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_011")
            raise AssertionError(f"Question/option fields missing: {e}")

    def test_tst_012_correct_answer_dropdown(self, logged_in_driver):
        """TST-012 [Unit]: Correct Answer dropdown is present."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Add Questions']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "correctAnswer").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_012")
            raise AssertionError(f"Correct Answer dropdown missing: {e}")

    def test_tst_013_option_populates_dropdown(self, logged_in_driver):
        """TST-013 [Functional]: Typing in option1 updates correct answer dropdown."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Add Questions']/..").click()
            time.sleep(2)
            opt1 = wait_visible(logged_in_driver, By.ID, "option1")
            opt1.clear()
            opt1.send_keys("Python")
            logged_in_driver.execute_script(
                "arguments[0].dispatchEvent(new Event('input'));", opt1
            )
            time.sleep(0.8)
            opt_a = logged_in_driver.find_element(By.ID, "optA")
            assert "Python" in opt_a.text or opt_a.get_attribute("value") == "Python"
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_013")
            raise AssertionError(f"Option dropdown not populated: {e}")

    def test_tst_014_my_questions_screen(self, logged_in_driver):
        """TST-014 [Functional]: My Questions screen opens."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='My Questions']/..").click()
            time.sleep(2)
            assert wait_visible(logged_in_driver, By.ID, "myQuestionsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_014")
            raise AssertionError(f"My Questions screen not opened: {e}")

    def test_tst_015_attend_test_screen(self, logged_in_driver):
        """TST-015 [Functional]: Attend Test screen opens."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Attend Test']/..").click()
            time.sleep(3)
            assert wait_visible(logged_in_driver, By.ID, "attendTestScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_015")
            raise AssertionError(f"Attend Test screen not opened: {e}")

    def test_tst_016_available_tests_container(self, logged_in_driver):
        """TST-016 [Unit]: Available tests container is present on Attend Test screen."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Attend Test']/..").click()
            time.sleep(3)
            el = logged_in_driver.find_element(By.ID, "availableTests")
            assert el is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_016")
            raise AssertionError(f"Available tests container missing: {e}")

    def test_tst_017_my_results_screen(self, logged_in_driver):
        """TST-017 [Functional]: My Results screen opens."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='My Results']/..").click()
            time.sleep(3)
            assert wait_visible(logged_in_driver, By.ID, "resultsScreen").is_displayed()
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_017")
            raise AssertionError(f"My Results screen not opened: {e}")

    def test_tst_018_leaderboard_screen(self, logged_in_driver):
        """TST-018 [Functional]: Leaderboard screen opens with container."""
        try:
            _nav_skill_test(logged_in_driver)
            wait_clickable(logged_in_driver, By.XPATH, "//h3[text()='Leaderboard']/..").click()
            time.sleep(3)
            assert wait_visible(logged_in_driver, By.ID, "leaderboardScreen").is_displayed()
            assert logged_in_driver.find_element(By.ID, "leaderboardContainer") is not None
        except Exception as e:
            take_screenshot(logged_in_driver, "test_tst_018")
            raise AssertionError(f"Leaderboard screen issue: {e}")
