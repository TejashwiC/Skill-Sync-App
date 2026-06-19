const fs = require('fs');
const path = require('path');
const ExcelJS = require('exceljs');

async function generateReports() {
    const summaryPath = path.join(__dirname, '..', 'artifacts', 'execution-summary.json');
    if (!fs.existsSync(summaryPath)) {
        console.error('execution-summary.json not found! Run tests first.');
        process.exit(1);
    }

    const rawData = fs.readFileSync(summaryPath, 'utf-8');
    const tests = JSON.parse(rawData);

    let totalPassed = 0;
    let totalFailed = 0;
    let totalDuration = 0;

    const categoryStats = {};

    tests.forEach(test => {
        if (test.status === 'Passed') totalPassed++;
        else totalFailed++;
        totalDuration += test.duration;

        if (!categoryStats[test.category]) {
            categoryStats[test.category] = { total: 0, passed: 0, failed: 0 };
        }
        categoryStats[test.category].total++;
        if (test.status === 'Passed') categoryStats[test.category].passed++;
        else categoryStats[test.category].failed++;
    });

    const totalTests = tests.length;
    const passRate = totalTests === 0 ? 0 : ((totalPassed / totalTests) * 100).toFixed(2);

    // 1. GENERATE EXCEL REPORT
    const workbook = new ExcelJS.Workbook();
    
    // Sheet 1: Summary
    const summarySheet = workbook.addWorksheet('Summary');
    summarySheet.columns = [
        { header: 'Metric', key: 'metric', width: 20 },
        { header: 'Value', key: 'value', width: 20 }
    ];
    summarySheet.addRow({ metric: 'Total Tests', value: totalTests });
    summarySheet.addRow({ metric: 'Passed', value: totalPassed });
    summarySheet.addRow({ metric: 'Failed', value: totalFailed });
    summarySheet.addRow({ metric: 'Skipped', value: 0 }); // Requested constraint
    summarySheet.addRow({ metric: 'Pass Rate (%)', value: passRate });
    summarySheet.addRow({ metric: 'Execution Time (ms)', value: totalDuration });

    // Sheet 2: Category Breakdown
    const catSheet = workbook.addWorksheet('Category Breakdown');
    catSheet.columns = [
        { header: 'Category', key: 'category', width: 30 },
        { header: 'Total', key: 'total', width: 15 },
        { header: 'Passed', key: 'passed', width: 15 },
        { header: 'Failed', key: 'failed', width: 15 }
    ];
    Object.keys(categoryStats).forEach(cat => {
        catSheet.addRow({
            category: cat,
            total: categoryStats[cat].total,
            passed: categoryStats[cat].passed,
            failed: categoryStats[cat].failed
        });
    });

    // Sheet 3: Detailed Results
    const detailsSheet = workbook.addWorksheet('Detailed Results');
    detailsSheet.columns = [
        { header: 'Test ID', key: 'id', width: 15 },
        { header: 'Test Name', key: 'name', width: 60 },
        { header: 'Category', key: 'category', width: 25 },
        { header: 'Status', key: 'status', width: 15 },
        { header: 'Duration (ms)', key: 'duration', width: 15 },
        { header: 'Timestamp', key: 'timestamp', width: 30 }
    ];
    tests.forEach(test => {
        detailsSheet.addRow(test);
    });

    const reportsDir = path.join(__dirname, '..', 'reports');
    if (!fs.existsSync(reportsDir)) fs.mkdirSync(reportsDir, { recursive: true });

    await workbook.xlsx.writeFile(path.join(reportsDir, 'E2E_SkillSync_Appium_Test_Cases.xlsx'));
    console.log('Excel report generated at reports/E2E_SkillSync_Appium_Test_Cases.xlsx');

    // 2. GENERATE HTML REPORT
    const htmlContent = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Android Appium Execution Report</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #121212; color: #ffffff; margin: 0; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        h1, h2 { border-bottom: 1px solid #333; padding-bottom: 10px; }
        .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 40px; }
        .card { background-color: #1e1e1e; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #333; }
        .card.pass { border-top: 4px solid #4CAF50; }
        .card.fail { border-top: 4px solid #F44336; }
        .card h3 { margin: 0 0 10px 0; color: #aaa; font-size: 14px; text-transform: uppercase; }
        .card p { margin: 0; font-size: 32px; font-weight: bold; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 40px; background-color: #1e1e1e; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #333; }
        th { background-color: #2c2c2c; }
        .status-Passed { color: #4CAF50; font-weight: bold; }
        .status-Failed { color: #F44336; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <h1>📱 Android Appium Test Report</h1>
        <p>Execution Time: ${new Date().toLocaleString()}</p>
        
        <div class="summary-grid">
            <div class="card">
                <h3>Total Tests</h3>
                <p>${totalTests}</p>
            </div>
            <div class="card pass">
                <h3>Passed</h3>
                <p>${totalPassed}</p>
            </div>
            <div class="card fail">
                <h3>Failed</h3>
                <p>${totalFailed}</p>
            </div>
            <div class="card">
                <h3>Pass Rate</h3>
                <p>${passRate}%</p>
            </div>
            <div class="card">
                <h3>Duration</h3>
                <p>${(totalDuration / 1000).toFixed(2)}s</p>
            </div>
        </div>

        <h2>Category Breakdown</h2>
        <table>
            <thead>
                <tr>
                    <th>Category</th>
                    <th>Total</th>
                    <th>Passed</th>
                    <th>Failed</th>
                    <th>Pass Rate</th>
                </tr>
            </thead>
            <tbody>
                ${Object.keys(categoryStats).map(cat => `
                <tr>
                    <td>${cat}</td>
                    <td>${categoryStats[cat].total}</td>
                    <td class="status-Passed">${categoryStats[cat].passed}</td>
                    <td class="status-Failed">${categoryStats[cat].failed}</td>
                    <td>${((categoryStats[cat].passed / categoryStats[cat].total) * 100).toFixed(1)}%</td>
                </tr>
                `).join('')}
            </tbody>
        </table>

        <h2>Detailed Results</h2>
        <table>
            <thead>
                <tr>
                    <th>Test ID</th>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Status</th>
                    <th>Duration (ms)</th>
                </tr>
            </thead>
            <tbody>
                ${tests.map(test => `
                <tr>
                    <td>${test.id}</td>
                    <td>${test.name}</td>
                    <td>${test.category}</td>
                    <td class="status-${test.status}">${test.status}</td>
                    <td>${test.duration}</td>
                </tr>
                `).join('')}
            </tbody>
        </table>
    </div>
</body>
</html>
    `;

    fs.writeFileSync(path.join(reportsDir, 'appium-report.html'), htmlContent);
    console.log('HTML report generated at reports/appium-report.html');
}

generateReports().catch(console.error);
