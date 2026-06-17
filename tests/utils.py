import os
import datetime
import logging
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

logger = logging.getLogger("SkillSync-E2E")
TESTS_DIR      = os.path.dirname(os.path.abspath(__file__))
SCREENSHOTS_DIR = os.path.join(TESTS_DIR, "screenshots")
os.makedirs(SCREENSHOTS_DIR, exist_ok=True)

def wait_for(driver, by, value, timeout=15):
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located((by, value))
    )

def wait_visible(driver, by, value, timeout=15):
    return WebDriverWait(driver, timeout).until(
        EC.visibility_of_element_located((by, value))
    )

def wait_clickable(driver, by, value, timeout=15):
    return WebDriverWait(driver, timeout).until(
        EC.element_to_be_clickable((by, value))
    )

def dismiss_alert_if_present(driver, timeout=4):
    try:
        WebDriverWait(driver, timeout).until(EC.alert_is_present())
        alert = driver.switch_to.alert
        text  = alert.text
        alert.accept()
        logger.info(f"Alert dismissed: {text[:80]}")
        return text
    except Exception:
        return None

def take_screenshot(driver, test_name):
    """Save a screenshot to the screenshots directory."""
    fname = f"{test_name}_{datetime.datetime.now().strftime('%H%M%S')}.png"
    path  = os.path.join(SCREENSHOTS_DIR, fname)
    try:
        driver.save_screenshot(path)
        logger.info(f"Screenshot saved: {path}")
    except Exception as e:
        logger.warning(f"Could not save screenshot: {e}")
    return path
