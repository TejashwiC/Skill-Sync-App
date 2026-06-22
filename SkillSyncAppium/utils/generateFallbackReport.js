const path = require('path');
const fs = require('fs');
const xlsxReporter = require('./xlsxReporter');
const { generateHtml } = require('./generateHtmlReport');
const { appendToGithubSummary } = require('./generateSummary');

async function run() {
    console.log('WDIO execution failed or crashed early. Generating fallback reports...');
    
    const reportsDir = path.join(__dirname, '..', 'reports');
    if (!fs.existsSync(reportsDir)) {
        fs.mkdirSync(reportsDir, { recursive: true });
    }

    const xlsxPath = path.join(reportsDir, 'E2E_SkillSync_Appium_Test_Cases.xlsx');
    const htmlPath = path.join(reportsDir, 'execution-report.html');
    const tempJsonlPath = path.join(__dirname, '..', '.wdio-results.jsonl');

    // Initialize and record a single failed setup test
    xlsxReporter.startRun();
    xlsxReporter.recordTest(
        'Appium Runner Execution Hook / Appium Server Connect',
        'E2E',
        'Failed',
        500,
        'Appium test harness terminated prematurely or encountered a fatal configuration crash.'
    );

    // Save xlsx
    await xlsxReporter.generateReport(xlsxPath);

    // Save local temp JSONL to generate html and GHA summary
    const testList = xlsxReporter.getTestsData();
    const jsonlData = testList.map(t => JSON.stringify(t)).join('\n') + '\n';
    fs.writeFileSync(tempJsonlPath, jsonlData);

    // Generate HTML
    generateHtml(testList, htmlPath);

    // Generate GHA summary if inside GHA
    appendToGithubSummary(tempJsonlPath);

    console.log('Fallback reports successfully written.');
}

run().catch(console.error);
