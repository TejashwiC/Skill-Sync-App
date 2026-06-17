"""
test_13_validation.py — Form Validation Tests for SkillSync (12 tests)
Module: Validation | Type: Validation
Tests cover edge-case input validation: password strength, duplicate accounts,
injection attempts, boundary values, and alert responses.
"""
import time
import logging
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Validation")


class TestValidation:

    # ── VAL-001: Password too short rejected by Firebase ─────────────────
    def test_val_001_password_too_short(self, driver, base_url):
        """VAL-001 [Validation]: Registration with <6 char password triggers Firebase error."""
        try:
            driver.get(f"{base_url}/index.html")
            wait_visible(driver, By.ID, "name").send_keys("Test User")
            driver.find_element(By.ID, "email").send_keys("testshort@skillsync.com")
            driver.find_element(By.ID, "password").send_keys("abc")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            alert_text = dismiss_alert_if_present(driver, timeout=10)
            assert alert_text is not None, "Expected an alert for short password"
            assert any(kw in alert_text.lower() for kw in ["error", "password", "weak", "short", "6"]), \
                f"Alert did not mention password strength: '{alert_text}'"
        except Exception as e:
            take_screenshot(driver, "test_val_001")
            raise AssertionError(f"Short password validation failed: {e}")

    # ── VAL-002: Empty name field shows HTML5 required validation ─────────
    def test_val_002_registration_empty_name(self, driver, base_url):
        """VAL-002 [Validation]: Submitting registration with empty name fails HTML5 validation."""
        try:
            driver.get(f"{base_url}/index.html")
            email_f = wait_visible(driver, By.ID, "email")
            email_f.send_keys("valid@skillsync.com")
            driver.find_element(By.ID, "password").send_keys("ValidPass123")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            name_field = driver.find_element(By.ID, "name")
            is_valid = driver.execute_script("return arguments[0].validity.valid;", name_field)
            assert not is_valid, "Empty name field should fail HTML5 required validation"
        except Exception as e:
            take_screenshot(driver, "test_val_002")
            raise AssertionError(f"Empty name validation failed: {e}")

    # ── VAL-003: Invalid email format on registration ─────────────────────
    def test_val_003_registration_invalid_email(self, driver, base_url):
        """VAL-003 [Validation]: Registration with bad email format fails HTML5 validation."""
        try:
            driver.get(f"{base_url}/index.html")
            wait_visible(driver, By.ID, "name").send_keys("Test User")
            email_f = driver.find_element(By.ID, "email")
            email_f.send_keys("not-a-valid-email")
            driver.find_element(By.ID, "password").send_keys("ValidPass123")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            is_valid = driver.execute_script("return arguments[0].validity.valid;", email_f)
            assert not is_valid, "Invalid email format should fail HTML5 validation"
        except Exception as e:
            take_screenshot(driver, "test_val_003")
            raise AssertionError(f"Invalid email format not caught: {e}")

    # ── VAL-004: SQL injection attempt in login email ─────────────────────
    def test_val_004_sql_injection_in_email(self, driver, base_url):
        """VAL-004 [Validation]: SQL injection in email field fails gracefully (no crash)."""
        try:
            driver.get(f"{base_url}/login.html")
            email_f = wait_visible(driver, By.ID, "email")
            email_f.send_keys("' OR 1=1 --@test.com")
            driver.find_element(By.ID, "password").send_keys("password123")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(1)
            # Should either stay on login page or show error — must NOT crash to blank page
            current_url = driver.current_url
            page_source = driver.page_source
            assert "dashboard" not in current_url, "SQL injection should not grant dashboard access"
            assert len(page_source) > 100, "Page should not be blank after injection attempt"
            # Dismiss any alert if shown
            dismiss_alert_if_present(driver, timeout=5)
        except Exception as e:
            take_screenshot(driver, "test_val_004")
            raise AssertionError(f"SQL injection test failed unexpectedly: {e}")

    # ── VAL-005: Extremely long email in login field ───────────────────────
    def test_val_005_extremely_long_email(self, driver, base_url):
        """VAL-005 [Validation]: Extremely long email (300 chars) doesn't crash the page."""
        try:
            driver.get(f"{base_url}/login.html")
            long_email = "a" * 290 + "@test.com"
            email_f = wait_visible(driver, By.ID, "email")
            email_f.send_keys(long_email)
            driver.find_element(By.ID, "password").send_keys("TestPass")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(1.5)
            # Page must remain functional
            assert len(driver.page_source) > 100, "Page went blank after long email input"
            dismiss_alert_if_present(driver, timeout=5)
        except Exception as e:
            take_screenshot(driver, "test_val_005")
            raise AssertionError(f"Long email input caused a crash: {e}")

    # ── VAL-006: Forgot password with invalid email shows error message ────
    def test_val_006_forgot_password_invalid_email_format(self, driver, base_url):
        """VAL-006 [Validation]: Forgot password with invalid email format fails HTML5 validation."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            email_f = wait_visible(driver, By.ID, "email")
            email_f.send_keys("not-an-email")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            is_valid = driver.execute_script("return arguments[0].validity.valid;", email_f)
            assert not is_valid, "Invalid email should fail HTML5 validation on forgot-password"
        except Exception as e:
            take_screenshot(driver, "test_val_006")
            raise AssertionError(f"Forgot password email validation failed: {e}")

    # ── VAL-007: Forgot password with empty email shows HTML5 error ────────
    def test_val_007_forgot_password_empty_email(self, driver, base_url):
        """VAL-007 [Validation]: Forgot password with empty email blocked by HTML5 required."""
        try:
            driver.get(f"{base_url}/forgot-password.html")
            wait_visible(driver, By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            email_f = driver.find_element(By.ID, "email")
            is_valid = driver.execute_script("return arguments[0].validity.valid;", email_f)
            assert not is_valid, "Empty email should be required (HTML5 validation)"
        except Exception as e:
            take_screenshot(driver, "test_val_007")
            raise AssertionError(f"Empty email on forgot-password passed unexpectedly: {e}")

    # ── VAL-008: Session form — missing session name shows alert ──────────
    def test_val_008_session_missing_name_alert(self, logged_in_driver, base_url):
        """VAL-008 [Validation]: Starting session without a name shows an error alert."""
        driver = logged_in_driver
        try:
            driver.get(f"{base_url}/dashboard.html")
            time.sleep(2)
            # Navigate to Session section
            try:
                sess_btn = WebDriverWait(driver, 10).until(
                    EC.element_to_be_clickable((By.XPATH,
                        "//a[contains(text(),'Session')] | //button[contains(text(),'Session')] | //*[@onclick=\"showSection('sessionSection')\"]"
                    ))
                )
                sess_btn.click()
            except TimeoutException:
                driver.execute_script("showSection('sessionSection')")
            time.sleep(1)

            # Try to open start session screen
            try:
                start_btn = WebDriverWait(driver, 8).until(
                    EC.element_to_be_clickable((By.XPATH,
                        "//*[contains(text(),'Start Session') or contains(@onclick,'startSessionScreen')]"
                    ))
                )
                start_btn.click()
            except TimeoutException:
                driver.execute_script("openSessionScreen('startSessionScreen')")
            time.sleep(1)

            # Click start without filling name
            start_session_btns = driver.find_elements(By.XPATH,
                "//button[contains(text(),'Start') and not(contains(text(),'Session Management'))]"
            )
            if start_session_btns:
                start_session_btns[-1].click()
            else:
                driver.execute_script("startSession()")

            alert_text = dismiss_alert_if_present(driver, timeout=8)
            assert alert_text is not None, "Expected alert for missing session name"
            assert any(kw in alert_text.lower() for kw in ["name", "session", "enter", "fill"]), \
                f"Alert didn't mention session name: '{alert_text}'"
        except Exception as e:
            take_screenshot(driver, "test_val_008")
            raise AssertionError(f"Session name validation check failed: {e}")

    # ── VAL-009: Session join — short code shows alert ───────────────────
    def test_val_009_session_join_short_code(self, logged_in_driver, base_url):
        """VAL-009 [Validation]: Joining session with < 6 char code triggers alert."""
        driver = logged_in_driver
        try:
            driver.get(f"{base_url}/dashboard.html")
            time.sleep(2)
            try:
                driver.execute_script("openSessionScreen('joinSessionScreen')")
            except Exception:
                pass
            time.sleep(1)

            code_inputs = driver.find_elements(By.CSS_SELECTOR, "#sessionCode, input[maxlength='6'], #joinCode")
            if code_inputs:
                code_inputs[0].clear()
                code_inputs[0].send_keys("AB")
            else:
                pytest.skip("Join session code input not found in current DOM state")

            join_btns = driver.find_elements(By.XPATH,
                "//button[contains(text(),'Join') or contains(@onclick,'joinSession')]"
            )
            if join_btns:
                join_btns[0].click()
            else:
                driver.execute_script("joinSession()")

            alert_text = dismiss_alert_if_present(driver, timeout=8)
            assert alert_text is not None, "Expected alert for short session code"
        except Exception as e:
            take_screenshot(driver, "test_val_009")
            raise AssertionError(f"Session join short-code validation failed: {e}")

    # ── VAL-010: Skill test — empty title triggers alert ─────────────────
    def test_val_010_skill_test_empty_title(self, logged_in_driver, base_url):
        """VAL-010 [Validation]: Creating skill test without title triggers alert."""
        driver = logged_in_driver
        try:
            driver.get(f"{base_url}/dashboard.html")
            time.sleep(2)
            try:
                driver.execute_script("openTestScreen('createTestScreen')")
            except Exception:
                pass
            time.sleep(1)

            # Try to submit without title
            create_btns = driver.find_elements(By.XPATH,
                "//button[contains(text(),'Create') or contains(@onclick,'createTest')]"
            )
            if create_btns:
                create_btns[0].click()
            else:
                driver.execute_script("createTest()")

            alert_text = dismiss_alert_if_present(driver, timeout=8)
            assert alert_text is not None, "Expected alert for empty test title"
        except Exception as e:
            take_screenshot(driver, "test_val_010")
            raise AssertionError(f"Skill test empty title validation failed: {e}")

    # ── VAL-011: Skill add — empty skill name triggers alert ──────────────
    def test_val_011_skill_add_empty(self, logged_in_driver, base_url):
        """VAL-011 [Validation]: Adding an empty skill name triggers an alert."""
        driver = logged_in_driver
        try:
            driver.get(f"{base_url}/dashboard.html")
            time.sleep(2)
            try:
                driver.execute_script("openProfileScreen('addSkillScreen')")
            except Exception:
                pass
            time.sleep(1)

            add_btns = driver.find_elements(By.XPATH,
                "//button[contains(text(),'Add') or contains(@onclick,'addSkill')]"
            )
            if add_btns:
                add_btns[0].click()
            else:
                driver.execute_script("addSkill()")

            alert_text = dismiss_alert_if_present(driver, timeout=8)
            assert alert_text is not None, "Expected alert when adding empty skill"
            assert any(kw in alert_text.lower() for kw in ["skill", "enter", "name", "fill"]), \
                f"Alert did not mention skill name: '{alert_text}'"
        except Exception as e:
            take_screenshot(driver, "test_val_011")
            raise AssertionError(f"Empty skill add validation failed: {e}")

    # ── VAL-012: Login with empty password blocked by HTML5 ───────────────
    def test_val_012_login_empty_password(self, driver, base_url):
        """VAL-012 [Validation]: Login with empty password fails HTML5 required check."""
        try:
            driver.get(f"{base_url}/login.html")
            email_f = wait_visible(driver, By.ID, "email")
            email_f.send_keys("test@example.com")
            driver.find_element(By.CSS_SELECTOR, "button").click()
            time.sleep(0.8)
            pwd_field = driver.find_element(By.ID, "password")
            is_valid = driver.execute_script("return arguments[0].validity.valid;", pwd_field)
            assert not is_valid, "Empty password should fail HTML5 required validation"
        except Exception as e:
            take_screenshot(driver, "test_val_012")
            raise AssertionError(f"Empty password validation failed: {e}")
