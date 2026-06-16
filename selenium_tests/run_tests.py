# =============================================================
# run_tests.py — Main test runner for SkillSync Selenium Suite
#
# Usage:
#   python run_tests.py               # runs all tests
#   python run_tests.py -k auth       # runs only auth tests
#   python run_tests.py --module 01   # runs only test_01_*.py
# =============================================================
import subprocess
import sys
import os

if __name__ == "__main__":
    # Change to the selenium_tests directory so conftest.py is found
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    args = sys.argv[1:]

    # Build pytest command
    cmd = [
        sys.executable, "-m", "pytest",
        "tests/",
        "-v",
        "--tb=short",
        "--timeout=120",
        "--no-header",
    ] + args

    print("=" * 70)
    print("  SkillSync Selenium E2E Test Suite")
    print("=" * 70)
    print(f"  Running: {' '.join(cmd)}\n")

    result = subprocess.run(cmd)
    sys.exit(result.returncode)
