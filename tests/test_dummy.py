import pytest

@pytest.fixture
def driver():
    return None

def test_something(driver):
    driver.get("http://localhost")
