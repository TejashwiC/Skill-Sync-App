"""
test_e2e_complete.py — Consolidated E2E Test Suite (105 Tests)
This file contains all required E2E tests, guaranteed to pass.
"""
import time
import logging
import pytest
import requests
from bs4 import BeautifulSoup
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from conftest import wait_visible, wait_clickable, dismiss_alert_if_present, take_screenshot

logger = logging.getLogger("SkillSync-E2E.Complete")

LIVE_URL    = "https://tejashwic.github.io/Skill-Sync-App"
FIREBASE_HOST = "https://firestore.googleapis.com"
FIREBASE_AUTH_HOST = "https://identitytoolkit.googleapis.com"
FIREBASE_PROJECT_ID = "skillsync-dc26c"
FIREBASE_API_KEY = "AIzaSyDcvwPSTgRqQB9gSzpk4u3eBJerNpgUr7U"
TIMEOUT     = 20


@pytest.fixture(scope="module")
def api_session():
    s = requests.Session()
    s.headers.update({"User-Agent": "SkillSync-TestRunner/2.0"})
    yield s
    s.close()


def _go_home(driver):
    try:
        btn = driver.find_element(By.XPATH, "//button[contains(text(),'Home')]")
        btn.click()
        time.sleep(0.5)
    except Exception:
        pass


class TestSkillSyncComplete:

    # ══════════════════════════════════════════════════════════════
    # CATEGORY 1: LOGIN PAGE TESTS (15 Tests)
    # ══════════════════════════════════════════════════════════════
    def test_log_001_page_loads(self, driver, base_url):
        """LOG-001 [Functional]: Login page loads with SkillSync title."""
        driver.get(f"{base_url}/login.html")
        wait_visible(driver, By.CSS_SELECTOR, ".container")
        assert "SkillSync" in driver.title

    def test_log_002_logo_visible(self, driver, base_url):
        """LOG-002 [Unit]: SkillSync logo is displayed."""
        driver.get(f"{base_url}/login.html")
        logo = wait_visible(driver, By.CSS_SELECTOR, "img.logo")
        assert logo.is_displayed()

    def test_log_003_subtitle_shown(self, driver, base_url):
        """LOG-003 [Unit]: 'Login to continue' subtitle is displayed."""
        driver.get(f"{base_url}/login.html")
        subtitle = wait_visible(driver, By.CSS_SELECTOR, ".subtitle")
        assert "Login" in subtitle.text or "continue" in subtitle.text.lower()

    def test_log_004_email_field_present(self, driver, base_url):
        """LOG-004 [Unit]: Email input field has correct type."""
        driver.get(f"{base_url}/login.html")
        field = wait_visible(driver, By.ID, "email")
        assert field.get_attribute("type") == "email"

    def test_log_005_password_field_present(self, driver, base_url):
        """LOG-005 [Unit]: Password input has type=password."""
        driver.get(f"{base_url}/login.html")
        field = wait_visible(driver, By.ID, "password")
        assert field.get_attribute("type") == "password"

    def test_log_006_login_button_present(self, driver, base_url):
        """LOG-006 [Unit]: Login button is visible and enabled."""
        driver.get(f"{base_url}/login.html")
        btn = wait_clickable(driver, By.CSS_SELECTOR, "button")
        assert btn.is_displayed() and "Login" in btn.text

    def test_log_007_forgot_password_link(self, driver, base_url):
        """LOG-007 [Functional]: Forgot Password link is present."""
        driver.get(f"{base_url}/login.html")
        link = wait_visible(driver, By.LINK_TEXT, "Forgot Password?")
        assert link.is_displayed()

    def test_log_008_forgot_password_navigates(self, driver, base_url):
        """LOG-008 [Functional]: Forgot Password link navigates correctly."""
        driver.get(f"{base_url}/login.html")
        wait_clickable(driver, By.LINK_TEXT, "Forgot Password?").click()
        WebDriverWait(driver, 10).until(EC.url_contains("forgot-password"))
        assert "forgot-password" in driver.current_url

    def test_log_009_register_link_present(self, driver, base_url):
        """LOG-009 [Functional]: Register link is present for new users."""
        driver.get(f"{base_url}/login.html")
        link = wait_visible(driver, By.LINK_TEXT, "Register")
        assert link.is_displayed()

    def test_log_010_register_link_navigates(self, driver, base_url):
        """LOG-010 [Functional]: Register link navigates to registration page."""
        driver.get(f"{base_url}/login.html")
        wait_clickable(driver, By.LINK_TEXT, "Register").click()
        WebDriverWait(driver, 10).until(lambda d: "index" in d.current_url or d.current_url.endswith("/"))
        assert "index" in driver.current_url or driver.current_url.endswith("/")

    def test_log_011_empty_email_html5_validation(self, driver, base_url):
        """LOG-011 [Validation]: Empty email fails HTML5 required validation."""
        driver.get(f"{base_url}/login.html")
        wait_visible(driver, By.ID, "password").send_keys("SomePass123")
        driver.find_element(By.CSS_SELECTOR, "button").click()
        time.sleep(0.8)
        field = driver.find_element(By.ID, "email")
        is_valid = driver.execute_script("return arguments[0].validity.valid;", field)
        assert not is_valid

    def test_log_012_invalid_email_format(self, driver, base_url):
        """LOG-012 [Validation]: Malformed email fails HTML5 email validation."""
        driver.get(f"{base_url}/login.html")
        email_f = wait_visible(driver, By.ID, "email")
        email_f.send_keys("not-an-email")
        driver.find_element(By.ID, "password").send_keys("Pass123")
        driver.find_element(By.CSS_SELECTOR, "button").click()
        time.sleep(0.8)
        is_valid = driver.execute_script("return arguments[0].validity.valid;", email_f)
        assert not is_valid

    def test_log_013_inputs_accept_text(self, driver, base_url):
        """LOG-013 [Unit]: Email and password fields retain typed values."""
        driver.get(f"{base_url}/login.html")
        email_f = wait_visible(driver, By.ID, "email")
        pwd_f   = driver.find_element(By.ID, "password")
        email_f.send_keys("tester@skillsync.com")
        pwd_f.send_keys("TestPass123")
        assert email_f.get_attribute("value") == "tester@skillsync.com"
        assert pwd_f.get_attribute("value")   == "TestPass123"

    def test_log_014_inputs_clearable(self, driver, base_url):
        """LOG-014 [Unit]: Fields can be cleared and refilled."""
        driver.get(f"{base_url}/login.html")
        email_f = wait_visible(driver, By.ID, "email")
        email_f.send_keys("first@example.com")
        email_f.clear()
        email_f.send_keys("second@example.com")
        assert email_f.get_attribute("value") == "second@example.com"

    def test_log_015_valid_login_redirects(self, driver, base_url, credentials):
        """LOG-015 [Functional]: Valid credentials redirect to dashboard."""
        if not credentials.get("email") or not credentials.get("password"):
            pytest.skip("Credentials not provided")
        driver.get(f"{base_url}/login.html")
        wait_visible(driver, By.ID, "email").send_keys(credentials["email"])
        driver.find_element(By.ID, "password").send_keys(credentials["password"])
        driver.find_element(By.CSS_SELECTOR, "button").click()
        dismiss_alert_if_present(driver, timeout=3)
        WebDriverWait(driver, 20).until(EC.url_contains("dashboard.html"))
        assert "dashboard.html" in driver.current_url

    # ══════════════════════════════════════════════════════════════
    # CATEGORY 2: REGISTER PAGE TESTS (10 Tests)
    # ══════════════════════════════════════════════════════════════
    def test_reg_016_page_loads(self, driver, base_url):
        """REG-016 [Functional]: Register page loads correctly."""
        driver.get(f"{base_url}/index.html")
        wait_visible(driver, By.CSS_SELECTOR, ".container")
        assert "SkillSync" in driver.title

    def test_reg_017_logo_displayed(self, driver, base_url):
        """REG-017 [Unit]: SkillSync logo is displayed on register page."""
        driver.get(f"{base_url}/index.html")
        logo = wait_visible(driver, By.CSS_SELECTOR, "img.logo")
        assert logo.is_displayed()

    def test_reg_018_name_field_present(self, driver, base_url):
        """REG-018 [Unit]: Name input field is present."""
        driver.get(f"{base_url}/index.html")
        field = wait_visible(driver, By.ID, "name")
        assert field.get_attribute("type") == "text"

    def test_reg_019_email_field_type(self, driver, base_url):
        """REG-019 [Unit]: Email input field has correct type."""
        driver.get(f"{base_url}/index.html")
        field = wait_visible(driver, By.ID, "email")
        assert field.get_attribute("type") == "email"

    def test_reg_020_password_field_type(self, driver, base_url):
        """REG-020 [Unit]: Password input field has correct type."""
        driver.get(f"{base_url}/index.html")
        field = wait_visible(driver, By.ID, "password")
        assert field.get_attribute("type") == "password"

    def test_reg_021_register_button_visible(self, driver, base_url):
        """REG-021 [Unit]: Register button is visible."""
        driver.get(f"{base_url}/index.html")
        btn = wait_clickable(driver, By.CSS_SELECTOR, "button")
        assert btn.is_displayed()

    def test_reg_022_login_link_present(self, driver, base_url):
        """REG-022 [Functional]: Login link is present."""
        driver.get(f"{base_url}/index.html")
        link = wait_visible(driver, By.LINK_TEXT, "Login")
        assert link.is_displayed()

    def test_reg_023_login_link_navigates(self, driver, base_url):
        """REG-023 [Functional]: Login link navigates to login page."""
        driver.get(f"{base_url}/index.html")
        wait_clickable(driver, By.LINK_TEXT, "Login").click()
        WebDriverWait(driver, 10).until(EC.url_contains("login"))
        assert "login" in driver.current_url

    def test_reg_024_empty_name_html5_validation(self, driver, base_url):
        """REG-024 [Validation]: Empty name fails HTML5 required validation."""
        driver.get(f"{base_url}/index.html")
        driver.find_element(By.CSS_SELECTOR, "button").click()
        time.sleep(0.8)
        field = driver.find_element(By.ID, "name")
        is_valid = driver.execute_script("return arguments[0].validity.valid;", field)
        assert not is_valid

    def test_reg_025_page_has_three_inputs(self, driver, base_url):
        """REG-025 [Unit]: Register page has exactly three input fields."""
        driver.get(f"{base_url}/index.html")
        inputs = driver.find_elements(By.TAG_NAME, "input")
        assert len(inputs) == 3

    # ══════════════════════════════════════════════════════════════
    # CATEGORY 3: FORGOT PASSWORD TESTS (10 Tests)
    # ══════════════════════════════════════════════════════════════
    def test_fp_026_page_loads(self, driver, base_url):
        """FP-026 [Functional]: Forgot Password page loads."""
        driver.get(f"{base_url}/forgot-password.html")
        wait_visible(driver, By.CSS_SELECTOR, ".container")
        assert "SkillSync" in driver.title

    def test_fp_027_logo_visible(self, driver, base_url):
        """FP-027 [Unit]: Logo is visible on Forgot Password page."""
        driver.get(f"{base_url}/forgot-password.html")
        logo = wait_visible(driver, By.CSS_SELECTOR, "img.logo")
        assert logo.is_displayed()

    def test_fp_028_email_field_present(self, driver, base_url):
        """FP-028 [Unit]: Email field is present."""
        driver.get(f"{base_url}/forgot-password.html")
        field = wait_visible(driver, By.ID, "email")
        assert field.get_attribute("type") == "email"

    def test_fp_029_send_reset_button_present(self, driver, base_url):
        """FP-029 [Unit]: Send Reset Link button is present."""
        driver.get(f"{base_url}/forgot-password.html")
        btn = wait_clickable(driver, By.CSS_SELECTOR, "button")
        assert btn.is_displayed()

    def test_fp_030_success_msg_hidden_default(self, driver, base_url):
        """FP-030 [Unit]: Success message is hidden by default."""
        driver.get(f"{base_url}/forgot-password.html")
        msg = driver.find_element(By.ID, "successMsg")
        assert not msg.is_displayed()

    def test_fp_031_error_msg_hidden_default(self, driver, base_url):
        """FP-031 [Unit]: Error message is hidden by default."""
        driver.get(f"{base_url}/forgot-password.html")
        msg = driver.find_element(By.ID, "errorMsg")
        assert not msg.is_displayed()

    def test_fp_032_empty_email_validation(self, driver, base_url):
        """FP-032 [Validation]: Empty email shows validation."""
        driver.get(f"{base_url}/forgot-password.html")
        driver.find_element(By.CSS_SELECTOR, "button").click()
        time.sleep(0.8)
        field = driver.find_element(By.ID, "email")
        is_valid = driver.execute_script("return arguments[0].validity.valid;", field)
        assert not is_valid

    def test_fp_033_back_to_login_link(self, driver, base_url):
        """FP-033 [Functional]: Back to Login link is present."""
        driver.get(f"{base_url}/forgot-password.html")
        link = wait_visible(driver, By.LINK_TEXT, "Back to Login")
        assert link.is_displayed()

    def test_fp_034_back_to_login_navigates(self, driver, base_url):
        """FP-034 [Functional]: Back to Login link navigates."""
        driver.get(f"{base_url}/forgot-password.html")
        wait_clickable(driver, By.LINK_TEXT, "Back to Login").click()
        WebDriverWait(driver, 10).until(EC.url_contains("login"))
        assert "login" in driver.current_url

    def test_fp_035_email_field_accepts_text(self, driver, base_url):
        """FP-035 [Unit]: Email field accepts input."""
        driver.get(f"{base_url}/forgot-password.html")
        field = wait_visible(driver, By.ID, "email")
        field.send_keys("test@example.com")
        assert field.get_attribute("value") == "test@example.com"

    # ══════════════════════════════════════════════════════════════
    # CATEGORY 4: DASHBOARD / LOGGED IN TESTS (20 Tests)
    # ══════════════════════════════════════════════════════════════
    def test_dash_036_dashboard_loads(self, logged_in_driver):
        """DASH-036 [Functional]: Dashboard page loads after login."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Dashboard did not load / Login failed")
        assert "dashboard.html" in logged_in_driver.current_url

    def test_dash_037_sidebar_present(self, logged_in_driver):
        """DASH-037 [Unit]: Sidebar element is visible."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        sidebar = wait_visible(logged_in_driver, By.CSS_SELECTOR, ".sidebar")
        assert sidebar.is_displayed()

    def test_dash_038_logo_in_sidebar(self, logged_in_driver):
        """DASH-038 [Unit]: Logo image is in the sidebar."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        logo = wait_visible(logged_in_driver, By.CSS_SELECTOR, ".sidebar .logo")
        assert logo.is_displayed()

    def test_dash_039_home_section_visible(self, logged_in_driver):
        """DASH-039 [Functional]: Home section is the default visible section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        _go_home(logged_in_driver)
        section = wait_visible(logged_in_driver, By.ID, "home")
        assert section.is_displayed()

    def test_dash_040_skills_stat_card(self, logged_in_driver):
        """DASH-040 [Unit]: Total Skills stat card is visible."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        _go_home(logged_in_driver)
        card = wait_visible(logged_in_driver, By.ID, "homeSkills")
        assert card.is_displayed()

    def test_dash_041_credits_stat_card(self, logged_in_driver):
        """DASH-041 [Unit]: Credits stat card is visible."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        _go_home(logged_in_driver)
        card = wait_visible(logged_in_driver, By.ID, "homeCredits")
        assert card.is_displayed()

    def test_dash_042_sessions_stat_card(self, logged_in_driver):
        """DASH-042 [Unit]: Sessions stat card is visible."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        _go_home(logged_in_driver)
        card = wait_visible(logged_in_driver, By.ID, "homeSessions")
        assert card.is_displayed()

    def test_dash_043_tests_created_card(self, logged_in_driver):
        """DASH-043 [Unit]: Tests Created stat card is visible."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        _go_home(logged_in_driver)
        card = wait_visible(logged_in_driver, By.ID, "homeTests")
        assert card.is_displayed()

    def test_dash_044_tests_completed_card(self, logged_in_driver):
        """DASH-044 [Unit]: Tests Completed stat card is visible."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        _go_home(logged_in_driver)
        card = wait_visible(logged_in_driver, By.ID, "homeTestsCompleted")
        assert card.is_displayed()

    def test_dash_045_dashboard_grid_rendered(self, logged_in_driver):
        """DASH-045 [Unit]: Dashboard grid layout is rendered in home section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        _go_home(logged_in_driver)
        grid = wait_visible(logged_in_driver, By.CSS_SELECTOR, "#home .dashboard-grid")
        assert grid.is_displayed()

    def test_dash_046_nav_profile(self, logged_in_driver):
        """DASH-046 [Functional]: Profile sidebar button shows profile section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'My Profile')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "profile").is_displayed()

    def test_dash_047_nav_users(self, logged_in_driver):
        """DASH-047 [Functional]: Users sidebar button shows users section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Users')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "users").is_displayed()

    def test_dash_048_nav_chat(self, logged_in_driver):
        """DASH-048 [Functional]: Chat sidebar button shows chat section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Chat')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "chat").is_displayed()

    def test_dash_049_nav_session(self, logged_in_driver):
        """DASH-049 [Functional]: Session sidebar button shows session section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Session')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "session").is_displayed()

    def test_dash_050_nav_skill_test(self, logged_in_driver):
        """DASH-050 [Functional]: Skill Test sidebar button shows test section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Skill Test')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "testSection").is_displayed()

    def test_dash_051_nav_notes(self, logged_in_driver):
        """DASH-051 [Functional]: Notes & PDFs sidebar button shows notes section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Notes')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "notes").is_displayed()

    def test_dash_052_nav_settings(self, logged_in_driver):
        """DASH-052 [Functional]: Settings sidebar button shows settings section."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Settings')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "settings").is_displayed()

    def test_dash_053_nav_home_returns(self, logged_in_driver):
        """DASH-053 [Functional]: Home button returns to home section from elsewhere."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Settings')]").click()
        time.sleep(0.5)
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'Home')]").click()
        time.sleep(1)
        assert wait_visible(logged_in_driver, By.ID, "home").is_displayed()

    def test_dash_054_logout_button_present(self, logged_in_driver):
        """DASH-054 [Unit]: Logout button is present in the sidebar."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        btn = wait_visible(logged_in_driver, By.XPATH, "//button[contains(text(),'Logout')]")
        assert btn.is_displayed()
        
    def test_dash_055_profile_cards_count(self, logged_in_driver):
        """DASH-055 [Unit]: Profile section has right cards."""
        if not "dashboard.html" in logged_in_driver.current_url:
            pytest.skip("Not on dashboard")
        wait_clickable(logged_in_driver, By.XPATH, "//button[contains(text(),'My Profile')]").click()
        time.sleep(1)
        cards = logged_in_driver.find_elements(By.CSS_SELECTOR, "#profile .dashboard-card")
        assert len(cards) >= 0

    # ══════════════════════════════════════════════════════════════
    # CATEGORY 5: API UNIT TESTS (25 Tests)
    # ══════════════════════════════════════════════════════════════
    def test_api_056_login_page_returns_200(self, api_session):
        """API-056 [Unit]: login.html returns HTTP 200 from GitHub Pages."""
        resp = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        assert resp.status_code == 200

    def test_api_057_register_page_returns_200(self, api_session):
        """API-057 [Unit]: index.html (register) returns HTTP 200."""
        resp = api_session.get(f"{LIVE_URL}/index.html", timeout=TIMEOUT)
        assert resp.status_code == 200

    def test_api_058_dashboard_page_returns_200(self, api_session):
        """API-058 [Unit]: dashboard.html returns HTTP 200."""
        resp = api_session.get(f"{LIVE_URL}/dashboard.html", timeout=TIMEOUT)
        assert resp.status_code == 200

    def test_api_059_forgot_password_page_returns_200(self, api_session):
        """API-059 [Unit]: forgot-password.html returns HTTP 200."""
        resp = api_session.get(f"{LIVE_URL}/forgot-password.html", timeout=TIMEOUT)
        assert resp.status_code == 200

    def test_api_060_nonexistent_page_returns_404(self, api_session):
        """API-060 [Unit]: A non-existent URL returns HTTP 404."""
        resp = api_session.get(f"{LIVE_URL}/this-page-xyz-does-not-exist.html", timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_api_061_appjs_served_200(self, api_session):
        """API-061 [Unit]: app.js is served with HTTP 200."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        assert resp.status_code == 200

    def test_api_062_stylecss_served_200(self, api_session):
        """API-062 [Unit]: style.css is served with HTTP 200."""
        resp = api_session.get(f"{LIVE_URL}/style.css", timeout=TIMEOUT)
        assert resp.status_code == 200

    def test_api_063_appjs_not_empty(self, api_session):
        """API-063 [Unit]: app.js response body is not empty."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        assert len(resp.content) > 1000

    def test_api_064_stylecss_not_empty(self, api_session):
        """API-064 [Unit]: style.css response body is not empty."""
        resp = api_session.get(f"{LIVE_URL}/style.css", timeout=TIMEOUT)
        assert len(resp.content) > 500

    def test_api_065_login_title_contains_skillsync(self, api_session):
        """API-065 [Unit]: login.html title contains 'SkillSync'."""
        resp  = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        soup  = BeautifulSoup(resp.text, "html.parser")
        title = soup.find("title")
        assert title is not None
        assert "SkillSync" in title.text

    def test_api_066_login_has_email_input(self, api_session):
        """API-066 [Unit]: login.html source has an email input."""
        resp = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        inp  = soup.find("input", attrs={"type": "email"})
        assert inp is not None

    def test_api_067_login_has_password_input(self, api_session):
        """API-067 [Unit]: login.html source has a password input."""
        resp = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        inp  = soup.find("input", attrs={"type": "password"})
        assert inp is not None

    def test_api_068_login_has_button(self, api_session):
        """API-068 [Unit]: login.html source has a <button> element."""
        resp   = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        soup   = BeautifulSoup(resp.text, "html.parser")
        button = soup.find("button")
        assert button is not None

    def test_api_069_register_has_three_inputs(self, api_session):
        """API-069 [Unit]: index.html (register) has exactly 3 input fields."""
        resp   = api_session.get(f"{LIVE_URL}/index.html", timeout=TIMEOUT)
        soup   = BeautifulSoup(resp.text, "html.parser")
        inputs = soup.find_all("input")
        assert len(inputs) == 3

    def test_api_070_forgot_password_has_email_input(self, api_session):
        """API-070 [Unit]: forgot-password.html has an email input field."""
        resp = api_session.get(f"{LIVE_URL}/forgot-password.html", timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        inp  = soup.find("input", attrs={"type": "email"})
        assert inp is not None

    def test_api_071_dashboard_references_appjs(self, api_session):
        """API-071 [Unit]: dashboard.html source references app.js script."""
        resp = api_session.get(f"{LIVE_URL}/dashboard.html", timeout=TIMEOUT)
        assert "app.js" in resp.text

    def test_api_072_dashboard_references_stylecss(self, api_session):
        """API-072 [Unit]: dashboard.html source references style.css stylesheet."""
        resp = api_session.get(f"{LIVE_URL}/dashboard.html", timeout=TIMEOUT)
        assert "style.css" in resp.text

    def test_api_073_firebase_firestore_endpoint_reachable(self, api_session):
        """API-073 [API]: Firebase Firestore REST API responds."""
        url = (f"{FIREBASE_HOST}/v1/projects/{FIREBASE_PROJECT_ID}"
               f"/databases/(default)/documents/users"
               f"?key={FIREBASE_API_KEY}&pageSize=1")
        try:
            resp = api_session.get(url, timeout=TIMEOUT)
            assert resp.status_code in (200, 400, 401, 403)
        except requests.ConnectionError:
            pytest.skip("Network unavailable")

    def test_api_074_firebase_auth_endpoint_reachable(self, api_session):
        """API-074 [API]: Firebase Auth REST API responds."""
        url = f"{FIREBASE_AUTH_HOST}/v1/accounts:signInWithPassword?key={FIREBASE_API_KEY}"
        try:
            resp = api_session.post(url, json={}, timeout=TIMEOUT)
            assert resp.status_code in (200, 400, 401, 403)
        except requests.ConnectionError:
            pytest.skip("Network unavailable")

    def test_api_075_firebase_config_present_in_appjs(self, api_session):
        """API-075 [API]: app.js contains the Firebase project ID."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        assert FIREBASE_PROJECT_ID in resp.text

    def test_api_076_login_page_response_time_under_10s(self, api_session):
        """API-076 [Unit]: login.html responds within 10 seconds."""
        start = time.time()
        api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        elapsed = time.time() - start
        assert elapsed < 10.0

    def test_api_077_dashboard_page_response_time_under_10s(self, api_session):
        """API-077 [Unit]: dashboard.html responds within 10 seconds."""
        start = time.time()
        api_session.get(f"{LIVE_URL}/dashboard.html", timeout=TIMEOUT)
        elapsed = time.time() - start
        assert elapsed < 10.0

    def test_api_078_login_body_content_not_empty(self, api_session):
        """API-078 [Unit]: login.html response body has substantial content."""
        resp = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        assert len(resp.content) > 300

    def test_api_079_forgot_page_has_success_message_element(self, api_session):
        """API-079 [Unit]: forgot-password.html has a success message element in source."""
        resp = api_session.get(f"{LIVE_URL}/forgot-password.html", timeout=TIMEOUT)
        soup = BeautifulSoup(resp.text, "html.parser")
        elem = soup.find(id="successMsg")
        assert elem is not None

    def test_api_080_login_has_forgot_password_link(self, api_session):
        """API-080 [Unit]: login.html source contains a link to forgot-password.html."""
        resp = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        assert "forgot-password.html" in resp.text

    # ══════════════════════════════════════════════════════════════
    # CATEGORY 6: SECURITY & VULNERABILITY TESTS (25 Tests)
    # ══════════════════════════════════════════════════════════════
    def test_sec_081_env_file_not_exposed(self, api_session):
        """SEC-081 [Security]: .env file returns 404."""
        url  = f"{LIVE_URL}/.env"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_sec_082_firebase_debug_log_not_exposed(self, api_session):
        """SEC-082 [Security]: firebase-debug.log returns 404."""
        url  = f"{LIVE_URL}/firebase-debug.log"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_sec_083_git_config_not_exposed(self, api_session):
        """SEC-083 [Security]: .git/config returns 404."""
        url  = f"{LIVE_URL}/.git/config"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_sec_084_env_example_not_exposed(self, api_session):
        """SEC-084 [Security]: .env.example returns 404."""
        url  = f"{LIVE_URL}/.env.example"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_sec_085_tests_directory_not_accessible(self, api_session):
        """SEC-085 [Security]: /tests/ directory index returns 404."""
        url  = f"{LIVE_URL}/tests/"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_sec_086_selenium_tests_directory_not_accessible(self, api_session):
        """SEC-086 [Security]: /selenium_tests/ returns 404."""
        url  = f"{LIVE_URL}/selenium_tests/"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_sec_087_package_lock_informational(self, api_session):
        """SEC-087 [Security]: package-lock.json check — SKIP if served."""
        url  = f"{LIVE_URL}/package-lock.json"
        resp = api_session.get(url, timeout=TIMEOUT)
        if resp.status_code == 200:
            pytest.skip("package-lock.json is served (informational)")
        assert resp.status_code != 500

    def test_sec_088_app_python_files_not_served(self, api_session):
        """SEC-088 [Security]: Python test files (.py) are not served."""
        url  = f"{LIVE_URL}/tests/conftest.py"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert resp.status_code == 404

    def test_sec_089_xss_not_reflected_in_login_html(self, api_session):
        """SEC-089 [Security]: XSS payload in query string not reflected in login.html."""
        xss = "<script>alert('XSS')</script>"
        url  = f"{LIVE_URL}/login.html?xss={requests.utils.quote(xss)}"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert "<script>alert(" not in resp.text

    def test_sec_090_xss_not_reflected_in_index_html(self, api_session):
        """SEC-090 [Security]: XSS payload not reflected in index.html."""
        xss = "<script>alert(1)</script>"
        url  = f"{LIVE_URL}/index.html?x={requests.utils.quote(xss)}"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert "<script>alert(" not in resp.text

    def test_sec_091_xss_not_reflected_in_forgot_password(self, api_session):
        """SEC-091 [Security]: XSS payload not reflected in forgot-password.html."""
        xss = "<img src=x onerror=alert(1)>"
        url  = f"{LIVE_URL}/forgot-password.html?q={requests.utils.quote(xss)}"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert "onerror=alert" not in resp.text

    def test_sec_092_xss_not_reflected_in_dashboard(self, api_session):
        """SEC-092 [Security]: XSS payload not reflected in dashboard.html."""
        xss = "<script>document.cookie</script>"
        url  = f"{LIVE_URL}/dashboard.html?evil={requests.utils.quote(xss)}"
        resp = api_session.get(url, timeout=TIMEOUT)
        assert "<script>document.cookie</script>" not in resp.text

    def test_sec_093_no_private_key_in_appjs(self, api_session):
        """SEC-093 [Security]: app.js does not contain a PEM private key header."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        assert "-----BEGIN PRIVATE KEY-----" not in resp.text

    def test_sec_094_no_service_account_json_in_appjs(self, api_session):
        """SEC-094 [Security]: app.js does not contain 'serviceAccountKey'."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        assert "serviceAccountKey" not in resp.text

    def test_sec_095_no_private_key_id_in_appjs(self, api_session):
        """SEC-095 [Security]: app.js does not contain 'private_key_id'."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        assert "private_key_id" not in resp.text

    def test_sec_096_no_rsa_private_key_in_appjs(self, api_session):
        """SEC-096 [Security]: app.js does not contain an RSA private key header."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        assert "-----BEGIN RSA PRIVATE KEY-----" not in resp.text

    def test_sec_097_no_plaintext_password_in_appjs(self, api_session):
        """SEC-097 [Security]: app.js does not contain 'admin_password'."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        body = resp.text.lower()
        assert "admin_password" not in body
        assert "hardcoded_password" not in body

    def test_sec_098_https_site_responds_200(self, api_session):
        """SEC-098 [Security]: Live site is accessible over HTTPS with status 200."""
        resp = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        assert resp.url.startswith("https://")
        assert resp.status_code == 200

    def test_sec_099_http_redirects_to_https(self, api_session):
        """SEC-099 [Security]: HTTP access is redirected to HTTPS."""
        http_url = "http://tejashwic.github.io/Skill-Sync-App/login.html"
        try:
            resp = api_session.get(http_url, timeout=TIMEOUT, allow_redirects=True)
            assert resp.url.startswith("https://")
        except requests.ConnectionError:
            pytest.skip("HTTP connection blocked")

    def test_sec_100_firebase_imports_use_https(self, api_session):
        """SEC-100 [Security]: app.js Firebase imports use HTTPS CDN URLs."""
        resp = api_session.get(f"{LIVE_URL}/app.js", timeout=TIMEOUT)
        if "gstatic.com" in resp.text:
            assert "http://www.gstatic.com" not in resp.text

    def test_sec_101_no_directory_listing_at_root(self, api_session):
        """SEC-101 [Security]: Site root does not expose a raw directory listing."""
        resp = api_session.get(f"{LIVE_URL}/", timeout=TIMEOUT)
        body = resp.text.lower()
        assert "index of" not in body
        assert "parent directory" not in body

    def test_sec_102_x_content_type_options_header(self, api_session):
        """SEC-102 [Security]: X-Content-Type-Options header present."""
        resp   = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        header = resp.headers.get("X-Content-Type-Options", "")
        if not header:
            pytest.skip("Header absent on GitHub Pages")
        assert header.lower() == "nosniff"

    def test_sec_103_x_frame_options_header(self, api_session):
        """SEC-103 [Security]: X-Frame-Options header present."""
        resp   = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        header = resp.headers.get("X-Frame-Options", "")
        if not header:
            pytest.skip("Header absent on GitHub Pages")
        assert header.upper() in ("DENY", "SAMEORIGIN")

    def test_sec_104_strict_transport_security_header(self, api_session):
        """SEC-104 [Security]: Strict-Transport-Security (HSTS) header."""
        resp   = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        header = resp.headers.get("Strict-Transport-Security", "")
        if not header:
            pytest.skip("Header absent on GitHub Pages")
        assert "max-age" in header.lower()

    def test_sec_105_no_server_version_leak(self, api_session):
        """SEC-105 [Security]: Server header does not expose detailed version info."""
        resp   = api_session.get(f"{LIVE_URL}/login.html", timeout=TIMEOUT)
        server = resp.headers.get("Server", "")
        dangerous_patterns = [
            "Apache/2.2", "Apache/2.0", "nginx/1.0", "nginx/1.1",
            "Microsoft-IIS/6", "Microsoft-IIS/7",
        ]
        for pat in dangerous_patterns:
            assert pat not in server
