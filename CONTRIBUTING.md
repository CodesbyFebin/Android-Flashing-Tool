# Contributing to Android Flashing Tool

We welcome contributions! This document provides guidelines for contributing to the Android Flashing Tool project.

## Code of Conduct

Be respectful, inclusive, and helpful. We're building a safe space for all contributors.

## Getting Started

### Prerequisites

- Python 3.10+
- Node.js (for build/packaging)
- Android SDK Platform-Tools (`adb` and `fastboot`)
- Git

### Development Setup

```bash
# Clone the repository
git clone https://github.com/CodesbyFebin/Android-Flashing-Tool.git
cd Android-Flashing-Tool

# Create virtual environment
python3 -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run tests
python -m unittest discover -s tests -v

# Start development server (inspection mode)
python3 runtime.py
```

Then open http://127.0.0.1:8765 in your browser.

## Development Workflow

### 1. Find or Create an Issue

- Check [existing issues](https://github.com/CodesbyFebin/Android-Flashing-Tool/issues) first
- For bugs: provide device model, bootloader version, and reproduction steps
- For features: explain the use case and expected behavior

### 2. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

Use descriptive branch names that reference the issue.

### 3. Make Your Changes

**Code Style:**
- Follow PEP 8 for Python code
- Use descriptive variable names
- Keep functions focused and testable
- Add docstrings for public functions
- Use type hints where helpful

**Key Files:**
- `runtime.py` - Python HTTP server and firmware/device operations
- `web/app.js` - Frontend API client and UI logic
- `web/style.css` - Styling and responsive design
- `tests/test_runtime.py` - Unit tests (critical for PRs)

**Testing:**
```bash
# Run all tests
python -m unittest discover -s tests -v

# Run specific test file
python -m unittest tests.test_runtime -v

# Run with coverage
python -m coverage run -m unittest discover -s tests -v
python -m coverage report
```

### 4. Commit Your Changes

Use clear, descriptive commit messages:

```bash
git add .
git commit -m "Fix: resolve device probing timeout in fastboot mode

- Increase timeout from 5s to 10s for slow devices
- Add retry logic for transient connection failures
- Update tests to verify retry behavior

Fixes #123"
```

**Commit Message Guidelines:**
- First line: concise summary (50 chars max)
- Blank line
- Detailed explanation of what and why
- Reference related issues with `Fixes #123` or `Related to #456`

### 5. Push and Open a Pull Request

```bash
git push -u origin feature/your-feature-name
```

Create a pull request against the `main` branch. Use the PR template and:

- ✅ Fill in all sections of the template
- ✅ Link to related issues
- ✅ Describe what changed and why
- ✅ Include test results or coverage info
- ✅ Verify CI passes (tests, lint checks)

### 6. Address Review Feedback

- Be open to feedback
- Make requested changes in new commits (don't force-push)
- Re-request review after making changes
- Update the PR description if scope changed

## Areas for Contribution

### High Priority
- 🛡️ Security audits and cryptographic verification
- 🧪 Test coverage (aiming for 90%+)
- 📖 Documentation improvements
- 🐛 Bug fixes

### Medium Priority
- 🎨 UI/UX enhancements
- ⚡ Performance optimization
- 🌐 International language support
- 📱 Mobile device support improvements

### Nice to Have
- 📊 Analytics dashboard (privacy-first, local-only)
- 🔔 Better error messages and recovery flows
- 🎯 Device-specific guides
- 🧬 Additional partition type support

## Submitting Security Issues

**Do not** open public issues for security vulnerabilities. Instead:

1. Email security concerns to the maintainer
2. Include reproduction steps if possible
3. Allow time for a fix before public disclosure

## Testing Guidelines

All PRs must include or update tests. Test coverage is critical for reliability:

### Writing Tests

```python
import unittest
from unittest.mock import patch
from pathlib import Path

class YourTestCase(unittest.TestCase):
    def setUp(self):
        # Setup test fixtures
        pass
    
    def tearDown(self):
        # Cleanup
        pass
    
    def test_specific_behavior(self):
        # Arrange
        # Act
        # Assert
        self.assertEqual(actual, expected)
```

### Testing Device Operations

- Mock `adb` and `fastboot` commands using `unittest.mock.patch`
- Do not require an actual connected device
- Test both success and failure paths
- Verify error messages are clear

### Running Tests Locally

```bash
# All tests
python -m unittest discover -s tests -v

# Specific test
python -m unittest tests.test_runtime.Gates.test_valid_signed_package -v

# With coverage
pip install coverage
coverage run -m unittest discover -s tests -v
coverage report -m
```

## Documentation

When adding features, update documentation:

- **README.md** - Update feature list and quick start if needed
- **Docstrings** - Add/update function documentation
- **API schema** - Update `openapi.json` if API changes
- **Design docs** - Update `design.md` for UI changes

## Performance Considerations

- Device probing should complete in <1 second
- Firmware inspection should complete in <5 seconds
- UI should remain responsive during long operations
- Minimize memory usage for large firmware files (>500MB)

## Release Process

Maintainers will handle releases. Version bumps follow [Semantic Versioning](https://semver.org):

- MAJOR: Breaking API changes or major features
- MINOR: New features (backward compatible)
- PATCH: Bug fixes

## Getting Help

- 💬 **Questions?** Open a discussion or issue
- 🐛 **Found a bug?** Open a bug report issue
- 💡 **Have an idea?** Start a discussion
- 📖 **Need docs?** Check README.md and design.md

## License

By contributing, you agree your code will be licensed under GPL-3.0. See [LICENSE](LICENSE) for details.

---

Thank you for contributing to Android Flashing Tool! 🚀

Your efforts help users maintain control over their devices with confidence and transparency.
