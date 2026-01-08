# Contributing to JaDES

First off, thank you for considering contributing to JaDES! 🎉

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Submitting Changes](#submitting-changes)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)

## 📜 Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## 🚀 Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone git@github.com:YOUR_USERNAME/JaDES.git
   cd JaDES
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream git@github.com:JaDES-ULL/JaDES.git
   ```

## 🛠️ Development Setup

### Prerequisites
- Java 17 or higher (LTS recommended)
- Maven 3.8+
- Git

### Build the project
```bash
mvn clean install
```

### Run tests
```bash
mvn test
```

### Generate coverage report
```bash
mvn verify
# Reports in: target/site/jacoco/index.html
```

## ✏️ Making Changes

1. **Create a branch** from `dev`:
   ```bash
   git checkout dev
   git pull upstream dev
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following our [Coding Standards](#coding-standards)

3. **Write tests** for your changes (required for new features)

4. **Commit your changes**:
   ```bash
   git commit -m "feat: add awesome feature"
   ```
   
   Use conventional commits format:
   - `feat:` for new features
   - `fix:` for bug fixes
   - `docs:` for documentation
   - `test:` for tests
   - `refactor:` for refactoring
   - `chore:` for maintenance

## 📤 Submitting Changes

1. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Open a Pull Request** against the `dev` branch

3. **Fill out the PR template** completely

4. **Wait for review** - maintainers will review your PR

5. **Address feedback** if requested

## 📏 Coding Standards

### Java Style
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Always use braces for control structures

### Documentation
- All public APIs must have JavaDoc
- Include `@param`, `@return`, and `@throws` tags
- Provide code examples for complex functionality

### Naming Conventions
- Classes: `PascalCase`
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`

## 🧪 Testing Guidelines

### Test Coverage
- Aim for >80% code coverage
- All new features must have tests
- Bug fixes should include regression tests

### Test Structure
```java
@Test
@DisplayName("Should do something when condition is met")
void testSomething() {
    // Given
    Simulation sim = new Simulation(...);
    
    // When
    sim.run();
    
    // Then
    assertEquals(expected, actual);
}
```

### Running Specific Tests
```bash
mvn test -Dtest=YourTestClass
mvn test -Dtest=YourTestClass#specificMethod
```

## 🐛 Reporting Bugs

Use the [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.md) and include:
- Clear description
- Steps to reproduce
- Expected vs actual behavior
- Environment details
- Code samples if applicable

## 💡 Suggesting Features

Use the [Feature Request template](.github/ISSUE_TEMPLATE/feature_request.md) and explain:
- The problem you're trying to solve
- Your proposed solution
- Any alternatives you've considered

## 📝 Documentation

- Update README.md if you change user-facing functionality
- Add examples to `docs/examples/` for new features
- Update architecture docs if you change structure

## ❓ Questions?

- Open a [Discussion](https://github.com/JaDES-ULL/JaDES/discussions)
- Check existing [Issues](https://github.com/JaDES-ULL/JaDES/issues)

---

Thank you for contributing to JaDES! 🚀
