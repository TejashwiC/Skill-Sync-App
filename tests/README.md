# SkillSync — Selenium E2E Test Suite

A comprehensive end-to-end test suite for the **SkillSync** skill-sharing web application.
Built with **Python + Selenium + pytest**, auto-generates an Excel report on every run.

---

## 📁 Folder Structure

```
tests/
├── conftest.py                   # pytest fixtures & hooks
├── report_generator.py           # Excel report builder
├── requirements.txt              # Python dependencies
├── .env.example                  # Environment variable template
├── test_01_register.py           # Registration tests
├── test_02_login.py              # Login tests
├── test_03_forgot_password.py    # Forgot Password tests
├── test_04_dashboard_home.py     # Dashboard Home tests
├── test_05_profile.py            # Profile section tests
├── test_06_users.py              # Users section tests
├── test_07_chat.py               # Chat section tests
├── test_08_session.py            # Session section tests
├── test_09_skill_test.py         # Skill Test section tests
├── test_10_notes.py              # Notes & PDFs section tests
├── test_11_settings.py           # Settings section tests
└── reports/                      # Generated Excel reports (auto-created)
```

---

## 🧪 Test Coverage

| Module | Test File | Count | Types |
|--------|-----------|-------|-------|
| Register | test_01_register.py | 13 | Functional, Validation, Unit |
| Login | test_02_login.py | 15 | Functional, Validation, Unit |
| Forgot Password | test_03_forgot_password.py | 12 | Functional, Validation, Unit |
| Dashboard Home | test_04_dashboard_home.py | 20 | Functional, Unit |
| Profile | test_05_profile.py | 16 | Functional, Validation, Unit |
| Users | test_06_users.py | 13 | Functional, Validation, Unit |
| Chat | test_07_chat.py | 17 | Functional, Validation, Unit |
| Session | test_08_session.py | 21 | Functional, Validation, Unit |
| Skill Test | test_09_skill_test.py | 18 | Functional, Validation, Unit |
| Notes & PDFs | test_10_notes.py | 11 | Functional, Unit |
| Settings | test_11_settings.py | 18 | Functional, Unit |
| **TOTAL** | **11 files** | **174** | |

---

## ⚙️ Local Setup

### Prerequisites
- Python 3.9+
- Google Chrome installed
- Git

### 1. Install Dependencies

```bash
cd SkillSync/tests
pip install -r requirements.txt
```

### 2. Configure Environment Variables

```bash
# Copy the template
copy .env.example .env

# Edit .env and fill in your Firebase test credentials:
# TEST_EMAIL=your_test@email.com
# TEST_PASSWORD=your_test_password
# BASE_URL=http://localhost:8080
# HEADLESS=false   # set to false to see the browser
```

### 3. Start the App Server

Open a terminal and run from the `SkillSync/` directory:

```bash
cd SkillSync
python -m http.server 8080
```

### 4. Run the Tests

Open a **second** terminal:

```bash
cd SkillSync/tests
python -m pytest . -v
```

### 5. View the Report

The Excel report is auto-generated in `tests/reports/`:

```
tests/reports/E2E_Test_Report_SkillSync_2026-06-16T09-30-00.xlsx
```

---

## 📊 Excel Report Format

The generated report matches the reference format with 3 sheets:

| Sheet | Contents |
|-------|----------|
| 📊 Summary | App info, pass/fail counts, execution time, module breakdown |
| 📋 Test Results | Full test table with status, duration, error notes |
| 📈 Charts | Pie chart (pass/fail distribution) + Bar chart (by module) |

---

## 🤖 GitHub Actions CI/CD

Every push to `main`/`master` automatically:
1. ✅ Runs all Selenium tests
2. 📊 Generates an Excel report
3. 📎 Uploads the report as a **downloadable artifact** (30 days)
4. 💬 Posts a test summary comment on Pull Requests

### Setting Up GitHub Secrets

Go to your repo → **Settings → Secrets and variables → Actions → New repository secret**:

| Secret Name | Value |
|-------------|-------|
| `TEST_EMAIL` | Your primary Firebase test account email |
| `TEST_PASSWORD` | Your primary Firebase test account password |
| `TEST_EMAIL_2` | (Optional) Second test account email |
| `TEST_PASSWORD_2` | (Optional) Second test account password |

### Downloading the Report

1. Go to **GitHub → Actions tab**
2. Click on any workflow run
3. Scroll to **Artifacts** at the bottom
4. Click **E2E-Test-Report-SkillSync-{run_number}** to download the Excel file

---

## 🔧 Running Specific Tests

```bash
# Run only login tests
pytest test_02_login.py -v

# Run only validation tests
pytest -k "validation" -v

# Run a specific test
pytest test_05_profile.py::TestProfile::test_prof_011_add_skill_empty_validation -v

# Run with browser visible (non-headless)
HEADLESS=false pytest . -v

# Generate report from sample data (no browser needed)
python report_generator.py
```

---

## 🛠️ Troubleshooting

| Issue | Solution |
|-------|----------|
| `WebDriverException: Chrome not found` | Install Chrome or run `pip install webdriver-manager` |
| `ConnectionRefusedError` | Make sure `python -m http.server 8080` is running |
| Tests skip with "credentials not configured" | Add `TEST_EMAIL` and `TEST_PASSWORD` to `.env` |
| Firebase timeout errors | Increase `time.sleep()` values or check internet connection |
| Report not generated | Run `python report_generator.py` manually to test |
