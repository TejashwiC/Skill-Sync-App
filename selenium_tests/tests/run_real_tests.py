import subprocess
import time
import sys

def main():
    print("========================================")
    print("  Starting SkillSync Local Server...    ")
    print("========================================")
    
    # Start server at repo root (one level up from tests/)
    server = subprocess.Popen([sys.executable, "-m", "http.server", "8080"], cwd="..")
    time.sleep(2) # Give it time to start
    
    print("\n========================================")
    print("  Launching Real Selenium Bot...        ")
    print("========================================")
    
    try:
        subprocess.run([sys.executable, "-m", "pytest", "test_actual_e2e.py", "-s", "-v", "--tb=short", "-p", "no:warnings"])
    finally:
        print("\n========================================")
        print("  Shutting down server...               ")
        print("========================================")
        server.terminate()

if __name__ == "__main__":
    main()
