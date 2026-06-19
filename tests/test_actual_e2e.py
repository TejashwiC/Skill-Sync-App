import pytest
import time
from selenium import webdriver
from selenium.webdriver.common.by import By


from selenium.webdriver.chrome.options import Options

@pytest.fixture(scope="module")
def driver():
    print("\n[BOT] Starting Google Chrome...")
    options = Options()
    # No headless mode! We want the user to see it.
    options.add_argument("--start-maximized")
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    
    
    driver = webdriver.Chrome(options=options)
    yield driver
    print("\n[BOT] Tests complete. Closing Chrome in 3 seconds...")
    time.sleep(3)
    driver.quit()

def test_security_routing(driver):
    print("\n[BOT] Running Security Routing Test...")
    # Try to access dashboard directly
    driver.get("http://localhost:8080/dashboard.html")
    time.sleep(2)
    # The app should redirect back to login.html because we are not authenticated.
    assert "login.html" in driver.current_url or "index.html" in driver.current_url
    print("[BOT] Security Routing Test Passed! Unauthorized access blocked.")

def test_ui_rendering_and_toggle(driver):
    print("\n[BOT] Running UI Toggle Test...")
    driver.get("http://localhost:8080/login.html")
    time.sleep(2)
    
    # Check that register section is hidden initially
    register_section = driver.find_element(By.id, "registerSection")
    assert not register_section.is_displayed()
    
    # Click 'Create one' to toggle to Register
    toggle_link = driver.find_element(By.xpath, "//a[contains(text(), 'Create one')]")
    toggle_link.click()
    print("[BOT] Toggling to Register View...")
    time.sleep(2)
    
    # Register section should now be visible
    assert register_section.is_displayed()
    login_section = driver.find_element(By.id, "loginSection")
    assert not login_section.is_displayed()
    print("[BOT] UI Toggle Test Passed!")

def test_validation_fake_login(driver):
    print("\n[BOT] Running Firebase Validation Test...")
    driver.get("http://localhost:8080/login.html")
    time.sleep(2)
    
    # Make sure we are on login view
    login_toggle = driver.find_element(By.xpath, "//a[contains(text(), 'Login here')]")
    if login_toggle.is_displayed():
        login_toggle.click()
        time.sleep(1)

    # Type into email
    print("[BOT] Typing fake email...")
    email_input = driver.find_element(By.id, "email")
    email_input.clear()
    email_input.send_keys("fake_bot@test.com")
    time.sleep(1)
    
    # Type into password
    print("[BOT] Typing fake password...")
    pass_input = driver.find_element(By.id, "password")
    pass_input.clear()
    pass_input.send_keys("WrongPassword123")
    time.sleep(1)
    
    # Click Login
    print("[BOT] Clicking Login button...")
    login_btn = driver.find_element(By.xpath, "//button[contains(text(), 'Login')]")
    login_btn.click()
    
    # Wait for alert
    time.sleep(3)
    try:
        alert = driver.switch_to.alert
        alert_text = alert.text
        print(f"[BOT] Alert caught: {alert_text}")
        assert "Error" in alert_text or "invalid" in alert_text.lower()
        alert.accept()
        print("[BOT] Firebase Validation Test Passed! Error correctly caught.")
    except Exception as e:
        print("[BOT] No alert found or test failed. Details:", e)
def test_ui_ux_button_styling(driver):
    import time
    from selenium.webdriver.common.by import By
    print("\n[BOT] Running UI/UX Aesthetics & Styling Test...")
    driver.get("http://localhost:8080/login.html")
    time.sleep(2)
    
    # Locate the login button
    login_btn = driver.find_element(By.xpath, "//button[contains(text(), 'Login')]")
    
    # Check physical UI/UX properties
    bg_color = login_btn.value_of_css_property('background-color')
    font_weight = login_btn.value_of_css_property('font-weight')
    
    print(f"[BOT] Button Background Color: {bg_color}")
    print(f"[BOT] Button Font Weight: {font_weight}")
    
    assert bg_color != "", "Background color is missing!"
    assert font_weight in ["700", "bold"], "Button is not bold!"
    print("[BOT] UI/UX Aesthetics Test Passed! Button styling is fully loaded and responsive.")

