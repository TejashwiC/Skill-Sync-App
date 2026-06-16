# =============================================================
# config.py — SkillSync Selenium Test Configuration
# =============================================================

# ── Live URLs ──────────────────────────────────────────────
BASE_URL           = "https://tejashwic.github.io/Skill-Sync-App"
REGISTER_URL       = f"{BASE_URL}/index.html"
LOGIN_URL          = f"{BASE_URL}/login.html"
DASHBOARD_URL      = f"{BASE_URL}/dashboard.html"
FORGOT_PWD_URL     = f"{BASE_URL}/forgot-password.html"

# ── Test Credentials ──────────────────────────────────────
# Primary test account (must already exist in Firebase)
TEST_EMAIL         = "seleniumtest@skillsync.com"
TEST_PASSWORD      = "Test@123456"
TEST_NAME          = "Selenium Test User"

# Secondary account (for social / follow / chat tests)
TEST_EMAIL_2       = "seleniumtest2@skillsync.com"
TEST_PASSWORD_2    = "Test@123456"
TEST_NAME_2        = "Selenium Test User 2"

# Account that does NOT exist (for negative tests)
INVALID_EMAIL      = "doesnotexist_99999@skillsync.com"
INVALID_PASSWORD   = "wrongpassword"

# ── Timeouts ──────────────────────────────────────────────
PAGE_LOAD_TIMEOUT  = 30   # seconds
ELEMENT_TIMEOUT    = 15   # seconds
FIREBASE_TIMEOUT   = 20   # seconds — Firebase ops can be slow in CI

# ── Browser settings ──────────────────────────────────────
HEADLESS           = True   # Set to False for local debugging

# ── Report ────────────────────────────────────────────────
REPORT_PREFIX      = "E2E_Test_Report"
REPORT_DIR         = "."   # written to repo root so GH Actions can upload it
