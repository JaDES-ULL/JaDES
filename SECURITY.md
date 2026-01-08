# Security Policy

## 🔒 Supported Versions

We release patches for security vulnerabilities in the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.x     | :white_check_mark: |
| < 1.0   | :x:                |

## 🐛 Reporting a Vulnerability

We take the security of JaDES seriously. If you discover a security vulnerability, please follow these steps:

### ⚠️ DO NOT
- **Do NOT** open a public GitHub issue
- **Do NOT** discuss the vulnerability publicly

### ✅ DO
1. **Email us privately** at: [security contact - add your email]
2. **Include the following information**:
   - Type of vulnerability
   - Full paths of source files related to the vulnerability
   - Location of the affected source code (tag/branch/commit/URL)
   - Step-by-step instructions to reproduce the issue
   - Proof-of-concept or exploit code (if possible)
   - Impact of the vulnerability

### 📅 Response Timeline

- **Initial Response**: Within 48 hours
- **Triage**: Within 1 week
- **Fix & Release**: Depends on severity
  - Critical: Within 7 days
  - High: Within 14 days
  - Medium: Within 30 days
  - Low: Next release cycle

### 🏆 Recognition

We appreciate responsible disclosure and will:
- Credit you in the security advisory (unless you prefer to remain anonymous)
- Keep you updated on the fix progress
- Notify you when the vulnerability is patched

## 🔐 Security Best Practices

When using JaDES:

1. **Keep Dependencies Updated**
   ```bash
   mvn versions:display-dependency-updates
   ```

2. **Use Latest Stable Version**
   - Always use the latest stable release
   - Subscribe to security advisories

3. **Validate Inputs**
   - Never trust user input in simulation models
   - Validate all external data sources

4. **Monitor CVEs**
   - Check [GitHub Security Advisories](https://github.com/JaDES-ULL/JaDES/security/advisories)
   - Subscribe to Maven Central vulnerability notifications

## 📋 Known Security Considerations

### ClassPathHacker.java (DEPRECATED)
- **Status**: Marked for removal in 2.0
- **Issue**: Uses reflection that breaks Java 9+ module system
- **Mitigation**: Do not use this class in production

### OWL API Dependencies
- Heavy dependencies (~15MB)
- Keep OWL API updated for security patches
- Consider isolating in separate module if not needed

## 🔍 Security Scanning

We use:
- **SonarCloud**: Continuous code quality and security analysis
- **Dependabot**: Automated dependency updates
- **GitHub Security Advisories**: Vulnerability tracking

## 📞 Contact

For security concerns, contact:
- **Email**: [Add security contact email]
- **GPG Key**: [Add if available]

---

*Last Updated: January 2026*
