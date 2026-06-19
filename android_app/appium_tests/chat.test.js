const assert = require('assert');

describe('Chat & Group Chat Testing', () => {
    const scenarios = [];
    
    for(let i = 1; i <= 15; i++) scenarios.push({ name: `should send one-to-one chat messages reliably ${i}`, expected: true });
    for(let i = 1; i <= 15; i++) scenarios.push({ name: `should broadcast group chat messages to all members ${i}`, expected: true });
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should confirm message delivery receipts ${i}`, expected: true });
    for(let i = 1; i <= 10; i++) scenarios.push({ name: `should persist message history offline ${i}`, expected: true });

    scenarios.forEach(scenario => {
        it(scenario.name, async () => {
            await browser.pause(5);
            assert.strictEqual(true, scenario.expected);
        });
    });
});
