# =============================================================
# utils/wait_helpers.py — Explicit wait utilities
# =============================================================
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoAlertPresentException
import time
import sys, os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from config import ELEMENT_TIMEOUT


def wait_for_element(driver, by, value, timeout=ELEMENT_TIMEOUT):
    """Wait until element is present in DOM."""
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located((by, value))
    )


def wait_for_clickable(driver, by, value, timeout=ELEMENT_TIMEOUT):
    """Wait until element is clickable."""
    return WebDriverWait(driver, timeout).until(
        EC.element_to_be_clickable((by, value))
    )


def wait_for_visible(driver, by, value, timeout=ELEMENT_TIMEOUT):
    """Wait until element is visible."""
    return WebDriverWait(driver, timeout).until(
        EC.visibility_of_element_located((by, value))
    )


def wait_for_url_contains(driver, partial_url, timeout=ELEMENT_TIMEOUT):
    """Wait until current URL contains the expected substring."""
    return WebDriverWait(driver, timeout).until(
        EC.url_contains(partial_url)
    )


def wait_for_text_in_element(driver, by, value, text, timeout=ELEMENT_TIMEOUT):
    """Wait until a specific element contains the expected text."""
    return WebDriverWait(driver, timeout).until(
        EC.text_to_be_present_in_element((by, value), text)
    )


def dismiss_alert(driver, timeout=5):
    """Accept any open browser alert. Returns True if alert was found."""
    try:
        WebDriverWait(driver, timeout).until(EC.alert_is_present())
        alert = driver.switch_to.alert
        text = alert.text
        alert.accept()
        return True, text
    except (TimeoutException, NoAlertPresentException):
        return False, ""


def safe_get(driver, url, retries=2):
    """Navigate to URL with retry logic for flaky CI environments."""
    for attempt in range(retries + 1):
        try:
            driver.get(url)
            return True
        except Exception as e:
            if attempt == retries:
                raise
            time.sleep(2)


def element_exists(driver, by, value, timeout=5):
    """Return True if element is found within timeout, False otherwise."""
    try:
        WebDriverWait(driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )
        return True
    except TimeoutException:
        return False


def wait_for_firebase(driver, seconds=3):
    """Give Firebase async operations time to complete."""
    time.sleep(seconds)
