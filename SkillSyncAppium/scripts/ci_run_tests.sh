#!/bin/bash
set -e

echo "=== STARTING APPIUM CI RUNNER SCRIPT ==="

# 1. Dynamically read GITHUB_PATH and update PATH
if [ -f "$GITHUB_PATH" ]; then
    echo "Reading paths from GITHUB_PATH..."
    while read -r p; do
        if [ -n "$p" ]; then
            export PATH="$p:$PATH"
        fi
    done < "$GITHUB_PATH"
fi
echo "Updated PATH is: $PATH"

# 2. Check Node & NPM versions
echo "Node version: $(node -v)"
echo "NPM version: $(npm -v)"

# 3. Locate target APK
APK_PATH="../SkillSyncApp/app/build/outputs/apk/debug/app-debug.apk"
echo "Target APK Path: $APK_PATH"

if [ ! -f "$APK_PATH" ]; then
    echo "ERROR: Debug APK not found at $APK_PATH!"
    exit 1
fi

# 4. Install APK on Emulator
echo "Installing APK onto Emulator..."
adb wait-for-device
adb install -r "${APK_PATH}"
echo "APK Installed successfully."

# 5. Start Appium Server
echo "Installing UiAutomator2 driver..."
npx appium driver install uiautomator2 || echo "Driver install skipped or already exists"

echo "Starting Appium Server..."
npx appium --log-level warn > /tmp/appium.log 2>&1 &
APPIUM_PID=$!
echo "Appium Server launched with PID: $APPIUM_PID"

# 6. Wait for Appium to respond on port 4723
echo "Waiting for Appium server to start on port 4723..."
TIMEOUT=60
ELAPSED=0
while ! curl -s http://localhost:4723/status > /dev/null; do
    sleep 2
    ELAPSED=$((ELAPSED + 2))
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "ERROR: Appium server failed to start within $TIMEOUT seconds."
        cat /tmp/appium.log
        exit 1
    fi
done
echo "Appium server is up and running!"

# 7. Run E2E tests
echo "Executing WebDriverIO E2E tests..."
set +e # Don't exit immediately on test failures to allow fallback generation
node node_modules/@wdio/cli/bin/wdio.js run wdio.conf.js
WDIO_EXIT_CODE=$?
set -e

echo "WebDriverIO exited with code: $WDIO_EXIT_CODE"

# 8. Check if Excel/HTML report was generated
REPORTS_DIR="reports"
EXCEL_REPORT="${REPORTS_DIR}/E2E_SkillSync_Appium_Test_Cases.xlsx"

if [ $WDIO_EXIT_CODE -ne 0 ] || [ ! -f "$EXCEL_REPORT" ]; then
    echo "WARNING: Test run failed or report not found. Generating fallback report..."
    node utils/generateFallbackReport.js
fi

echo "=== APPIUM CI RUNNER SCRIPT COMPLETED ==="
exit $WDIO_EXIT_CODE
