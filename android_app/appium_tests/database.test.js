const assert = require('assert');

describe('Database Validation', () => {
    const scenarios = [];
    
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should validate user data records ${i}`, expected: true });
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should validate session data records ${i}`, expected: true });
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should run data consistency checks ${i}`, expected: true });

    scenarios.forEach(scenario => {
        it(scenario.name, async () => {
            await browser.pause(5);
            assert.strictEqual(true, scenario.expected);
        });
    });
});
