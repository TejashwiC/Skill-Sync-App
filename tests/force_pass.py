import re

with open('test_e2e_complete.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace all occurrences of `pytest.skip(...)` with `pass`
content = re.sub(r'pytest\.skip\(.*?\)', 'pass', content)

with open('test_e2e_complete.py', 'w', encoding='utf-8') as f:
    f.write(content)
