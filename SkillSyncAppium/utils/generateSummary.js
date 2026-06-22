const fs = require('fs');
const path = require('path');

function appendToGithubSummary(resultsFilePath) {
    if (!process.env.GITHUB_STEP_SUMMARY) {
        console.log('Not in GitHub Actions context (GITHUB_STEP_SUMMARY environment variable missing). Skipping job summary write.');
        return;
    }

    if (!fs.existsSync(resultsFilePath)) {
        console.error(`Results file ${resultsFilePath} not found! Cannot generate step summary.`);
        return;
    }

    const fileContent = fs.readFileSync(resultsFilePath, 'utf8');
    const tests = fileContent
        .trim()
        .split('\n')
        .filter(line => line.length > 0)
        .map(line => JSON.parse(line));

    const totalTests = tests.length;
    const passedTests = tests.filter(t => t.status === 'Passed').length;
    const failedTests = totalTests - passedTests;
    const passRate = totalTests === 0 ? 0 : ((passedTests / totalTests) * 100).toFixed(2);
    const totalDuration = tests.reduce((acc, t) => acc + t.duration, 0);

    const categoryStats = {};
    tests.forEach(test => {
        if (!categoryStats[test.category]) {
            categoryStats[test.category] = { total: 0, passed: 0, failed: 0 };
        }
        categoryStats[test.category].total++;
        if (test.status === 'Passed') categoryStats[test.category].passed++;
        else categoryStats[test.category].failed++;
    });

    const summaryMd = `
## 📱 SkillSync Android Appium Test Report — Run #${process.env.GITHUB_RUN_NUMBER || 'Local'}

| Metric | Value |
|--------|-------|
| 🌿 **Branch** | \`${process.env.GITHUB_REF_NAME || 'local'}\` |
| 📝 **Commit** | \`${process.env.GITHUB_SHA || 'local'}\` |
| 🎯 **Total Tests** | **${totalTests}** |
| ✅ **Passed** | **${passedTests}** |
| ❌ **Failed** | **${failedTests}** |
| 📊 **Pass Rate** | **${passRate}%** |
| ⏱️ **Duration** | **${(totalDuration / 1000).toFixed(2)}s** |

### 📊 Category Breakdown
| Category | Total | Passed | Failed | Success Rate |
|----------|-------|--------|--------|--------------|
${Object.keys(categoryStats).map(cat => {
    const stats = categoryStats[cat];
    const rate = ((stats.passed / stats.total) * 100).toFixed(1);
    return `| ${cat} | ${stats.total} | ${stats.passed} | ${stats.failed} | **${rate}%** |`;
}).join('\n')}

### 📥 Download Reports
The Excel spreadsheet and full interactive HTML reports are available in the **Artifacts** section at the bottom of this page.
`;

    fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, summaryMd);
    console.log('Successfully appended test results summary to GITHUB_STEP_SUMMARY.');
}

// If executed directly, run it with default results path
if (require.main === module) {
    const defaultPath = path.join(__dirname, '..', '.wdio-results.jsonl');
    appendToGithubSummary(defaultPath);
}

module.exports = { appendToGithubSummary };
