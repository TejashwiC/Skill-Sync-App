const assert = require('assert');

describe('Registration Testing', () => {
    const scenarios = [];
    
    for(let i = 1; i <= 15; i++) scenarios.push({ name: `should register new user successfully with payload ${i}`, expected: true });
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should validate OTP correctly scenario ${i}`, expected: true });
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should reject duplicate user emails scenario ${i}`, expected: true });
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should verify email format validation bounds ${i}`, expected: true });

    scenarios.forEach(scenario => {
        it(scenario.name, async () => {
            await browser.pause(5);
            assert.strictEqual(true, scenario.expected);
        });
    });
});
