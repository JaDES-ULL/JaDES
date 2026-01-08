# Architecture Decision Records (ADRs)

This document tracks important architectural decisions made for the JaDES (Java Discrete Event Simulation) framework.

---

## ADR Template

When adding a new decision, copy this template:

```markdown
## ADR-###: [Short Title]

**Date**: YYYY-MM-DD  
**Status**: Proposed | Accepted | Deprecated | Superseded  
**Deciders**: [Names or roles]  
**Context**: [Background and problem description]

### Decision
[What we decided to do and why]

### Consequences
**Positive:**
- [Benefit 1]
- [Benefit 2]

**Negative:**
- [Tradeoff 1]
- [Tradeoff 2]

**Neutral:**
- [Impact 1]

### Alternatives Considered
1. **[Alternative 1]**: [Why rejected]
2. **[Alternative 2]**: [Why rejected]

### References
- [Link to discussion]
- [Related code]
```

---

## ADR-001: Multi-Module Maven Structure

**Date**: 2026-01-08  
**Status**: Accepted  
**Deciders**: JaDES maintainers  
**Context**: JaDES was split into two separate repositories (JaDES-core and utils-library), causing:
- Duplicate CI/CD configurations
- Difficult version synchronization
- Fragmented issue tracking
- Complex dependency management

### Decision
Merge both repositories into a single **multi-module Maven project**:
```
JaDES/
├── pom.xml                 (parent)
├── jades-core/
│   └── pom.xml
└── jades-utils/
    └── pom.xml
```

**Rationale:**
- Industry standard (Apache Commons, Spring Framework, Jackson)
- Atomic commits across modules
- Unified CI/CD pipeline
- Simplified release process
- Single source of truth for issues/PRs

### Consequences
**Positive:**
- Single CI/CD workflow (faster builds, no duplication)
- Coordinated releases (no version mismatch between core and utils)
- Easier contribution (one clone, one PR)
- Unified issue tracking
- Consistent tooling (PMD, Checkstyle, SonarCloud)

**Negative:**
- Larger repository size (~3500 objects)
- Longer initial clone time
- Build failure in one module blocks release of other

**Neutral:**
- Users still get separate JARs (`jades-core.jar`, `jades-utils.jar`)
- No API changes required

### Alternatives Considered
1. **Keep separate repos**: Rejected due to maintenance burden and coordination overhead
2. **Git submodules**: Rejected due to complexity and poor developer experience
3. **Gradle multi-project**: Rejected to maintain Maven ecosystem compatibility

### References
- [Repository Fusion PR #1](#)
- [Multi-module discussion](#)
- [Apache Commons as reference](https://github.com/apache/commons-parent)

---

## ADR-002: Java 17 LTS as Target

**Date**: 2026-01-08  
**Status**: Accepted  
**Deciders**: JaDES maintainers  
**Context**: Original repos had Java version inconsistency:
- JaDES-core: Java 17
- utils-library: Java 25 (non-LTS, not widely available)

### Decision
Standardize on **Java 17 LTS** as the minimum supported version.

**Rationale:**
- LTS (Long-Term Support) until September 2029
- Widely available (Oracle, OpenJDK, Temurin, Corretto)
- Modern features (sealed classes, pattern matching, records, text blocks)
- Sufficient for simulation domain needs
- GitHub Actions native support

### Consequences
**Positive:**
- Predictable support lifecycle
- Wider user adoption (Java 17+ = 80% of Java users)
- Stable API (no experimental features)
- Better IDE support

**Negative:**
- Cannot use Java 21+ features (virtual threads, structured concurrency)
- Requires Java 17+ runtime (drops Java 8/11 users)

**Neutral:**
- Can upgrade to Java 21 LTS in future without breaking changes

### Alternatives Considered
1. **Java 21 LTS**: Rejected as too new (released 2023, not yet mainstream in enterprise)
2. **Java 11 LTS**: Rejected as too old (misses sealed classes, records, pattern matching)
3. **Java 25**: Rejected as non-LTS, experimental features, limited availability

### References
- [Java Release Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)
- [JEP 409: Sealed Classes](https://openjdk.org/jeps/409)

---

## ADR-003: Deprecate Factory Package

**Date**: 2026-01-08  
**Status**: Accepted  
**Deciders**: JaDES maintainers  
**Context**: The `es.ull.simulation.factory` package was designed for:
- Centralized object creation
- Simplified API (fewer constructor parameters)
- Future extensibility (plugins, custom implementations)

**Reality check:**
- Only 1 actual usage found (`BarrelShipping` example)
- 53 test files use factory, but only for convenience (not necessity)
- Adds complexity without clear benefit
- Direct constructors are clearer and type-safe

### Decision
**Deprecate** the factory package:
1. Mark `@Deprecated` in JaDES 1.x
2. Update documentation to recommend direct constructors
3. Remove in JaDES 2.0

**Recommended migration:**
```java
// ❌ OLD: Factory pattern
SimulationFactory factory = new SimulationFactory(0, "Example");
ResourceType type = factory.getResourceTypeInstance("Server");
Resource res = factory.getResourceInstance("Server-1");

// ✅ NEW: Direct construction
Simulation sim = new Simulation(0, "Example", TimeUnit.MINUTE, 0, 100);
ResourceType type = new ResourceType(sim, "Server");
Resource res = new Resource(sim, "Server-1", type);
```

### Consequences
**Positive:**
- Clearer code (explicit constructors)
- Reduced API surface (fewer classes to learn)
- Better IDE support (constructor parameter hints)
- Easier testing (no factory setup)

**Negative:**
- Breaking change in 2.0 (migration guide provided)
- Slightly more verbose (but more explicit)

**Neutral:**
- Existing code continues working in 1.x (deprecated warnings only)

### Alternatives Considered
1. **Keep factory indefinitely**: Rejected due to maintenance burden with no clear benefit
2. **Remove immediately**: Rejected to allow gradual migration
3. **Make factory optional module**: Rejected as over-engineering

### References
- [Factory usage analysis](../architecture/overview.md#known-architectural-issues)
- [Effective Java: Prefer constructors to factories](https://www.oreilly.com/library/view/effective-java/9780134686097/)

---

## ADR-004: Self-Registration Pattern for Simulation Objects

**Date**: 2026-01-08  
**Status**: Accepted  
**Deciders**: JaDES maintainers  
**Context**: Simulation objects (Element, Resource, ActivityManager) need to register with their parent `Simulation` instance. Two approaches exist:

**Option 1: Manual registration**
```java
Simulation sim = new Simulation(...);
Element elem = new Element(sim, type, flow);
sim.add(elem);  // Easy to forget!
```

**Option 2: Self-registration**
```java
Simulation sim = new Simulation(...);
Element elem = new Element(sim, type, flow);  // Auto-registers
```

### Decision
Use **self-registration in constructors** for all simulation objects.

**Rationale:**
- Objects have tight lifecycle coupling with `Simulation`
- Reduces boilerplate and error-prone manual registration
- Consistent with existing codebase patterns
- Matches domain model (elements "belong to" simulation)

### Consequences
**Positive:**
- Cannot forget to register objects
- Cleaner API (fewer method calls)
- More reliable (no partially-initialized objects)

**Negative:**
- Constructors have side effects (violates pure functional principles)
- Harder to create objects for testing (always need `Simulation` instance)
- Cannot create "orphan" objects

**Neutral:**
- Standard pattern in simulation frameworks (MASON, Repast)
- Well-documented in Javadoc

### Alternatives Considered
1. **Manual registration**: Rejected as error-prone and verbose
2. **Builder pattern**: Rejected as over-engineering for simple domain objects
3. **Factory with registration**: Rejected (see ADR-003)

### References
- [Self-registration discussion](../development/coding-standards.md#self-registration-pattern)
- [Simulation.java implementation](../../jades-core/src/main/java/es/ull/simulation/model/Simulation.java)

---

## ADR-005: JaCoCo Coverage Enabled by Default

**Date**: 2026-01-08  
**Status**: Accepted  
**Deciders**: JaDES maintainers  
**Context**: Code coverage was previously:
- Only available via Maven profile (`-Pcoverage`)
- Inconsistent between core and utils
- Easy to skip (developers forget to activate profile)

**Current test coverage:**
- jades-core: ~2% (1 real @Test out of 54 files)
- jades-utils: 15.5% (9 tests for 58 classes)

### Decision
Enable **JaCoCo by default** in parent POM:
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Runs automatically with `mvn verify` (no profile needed).

### Consequences
**Positive:**
- Always-available coverage metrics
- CI/CD reports to Codecov automatically
- Developers see coverage immediately
- Encourages test writing (visible gap)

**Negative:**
- Slightly slower builds (~5% overhead)
- Larger `target/` directory (jacoco.exec file)

**Neutral:**
- Can disable with `-Djacoco.skip=true` if needed
- Report visible at `target/site/jacoco/index.html`

### Alternatives Considered
1. **Keep profile-only**: Rejected as developers consistently forget to activate
2. **CI-only coverage**: Rejected as too late (feedback after commit)
3. **Separate coverage module**: Rejected as over-engineering

### References
- [JaCoCo documentation](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [Coverage report location](../../target/site/jacoco/index.html)

---

## ADR-006: Conventional Commits for Version Control

**Date**: 2026-01-08  
**Status**: Accepted  
**Deciders**: JaDES maintainers  
**Context**: Inconsistent commit messages make:
- Changelog generation difficult
- Code review slower
- Git history unclear
- Semantic versioning error-prone

### Decision
Adopt **[Conventional Commits](https://www.conventionalcommits.org/)** specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `test`: Adding/updating tests
- `refactor`: Code restructuring (no behavior change)
- `perf`: Performance improvement
- `chore`: Build/tool changes

**Examples:**
```
feat(core): add support for parallel execution

Add SimulationEngine.runParallel() method to execute multiple
replications concurrently using ForkJoinPool.

Closes #42
```

```
fix(utils): correct Excel date parsing

Previously, dates before 1900 were incorrectly parsed.
Now uses POI DateUtil properly.

Fixes #89
```

### Consequences
**Positive:**
- Automated changelog generation
- Clear git history
- Easier code review (type indicates impact)
- Better semantic versioning (MAJOR.MINOR.PATCH auto-determined)
- GitHub automation (auto-label PRs)

**Negative:**
- Learning curve for contributors
- Stricter commit discipline required

**Neutral:**
- CI enforces format (commitlint)
- Can squash commits on merge (only final message matters)

### Alternatives Considered
1. **Free-form commits**: Rejected as unmaintainable at scale
2. **GitHub squash-only**: Rejected as loses granular history
3. **Custom format**: Rejected as non-standard (harder for new contributors)

### References
- [Conventional Commits spec](https://www.conventionalcommits.org/)
- [CONTRIBUTING.md guidelines](../../CONTRIBUTING.md#commit-messages)

---

## ADR-007: Google Java Style Guide with Adaptations

**Date**: 2026-01-08  
**Status**: Accepted  
**Deciders**: JaDES maintainers  
**Context**: Need consistent code formatting across contributors. Options:
- Oracle Java style (outdated, 80 char lines)
- Google Java Style (modern, widely adopted)
- Twitter Java Style (similar to Google)
- Custom style (maintenance burden)

### Decision
Adopt **[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)** with these adaptations:

**Deviations from Google:**
- Line length: 120 chars (Google: 100)
- Indentation: 4 spaces (Google: 2)
- Imports: No wildcards except test static imports

**Rationale:**
- 120 chars: Better for modern displays, reduces line breaks
- 4 spaces: Standard in simulation/scientific Java code, better readability
- Google base: Comprehensive, well-documented, IntelliJ/Eclipse support

### Consequences
**Positive:**
- Automated formatting (google-java-format plugin)
- IDE support (IntelliJ, Eclipse, VS Code)
- Clear documentation (style guide maintained by Google)
- Consistent diffs (no formatting fights in PRs)

**Negative:**
- Existing code needs reformatting (large initial diff)
- Some contributors prefer 2-space indent

**Neutral:**
- `.editorconfig` enforces rules automatically
- CI checks formatting (Checkstyle)

### Alternatives Considered
1. **Oracle style**: Rejected as outdated (80 chars too restrictive)
2. **Custom style**: Rejected as maintenance burden
3. **No standard**: Rejected as inconsistent codebase

### References
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [JaDES Coding Standards](coding-standards.md)
- [.editorconfig](../../.editorconfig)

---

## ADR-008: Planned Modularization of jades-utils (Future)

**Date**: 2026-01-08  
**Status**: Proposed  
**Deciders**: JaDES maintainers  
**Context**: `jades-utils` is monolithic with heterogeneous responsibilities:
- Functions (13 classes): Time-based calculations
- I/O (7 classes): Excel, XML, properties
- Ontology (4 classes): OWL/RDF, HermiT reasoning
- Concurrency (3 classes): Thread utilities
- Cycles (7 classes): Graph algorithms
- Simkit (19 classes): Random variates (duplicated from dependency)

**Problems:**
- Users import 25MB of dependencies for one function
- No optional dependencies (Excel/OWL always required)
- SRP violation (utils do "everything")
- Hard to test (tight coupling)

### Decision
**In JaDES 2.0**, split into focused modules:

```
jades-utils/          (aggregator, depends on all below)
├── jades-functions/  (~50KB, zero deps)
├── jades-io/         (~2MB, Apache POI optional)
├── jades-ontology/   (~15MB, OWL API optional)
├── jades-concurrent/ (~20KB, zero deps)
└── jades-algorithms/ (~30KB, zero deps)
```

**Migration path:**
```xml
<!-- OLD: Monolithic (JaDES 1.x) -->
<dependency>
  <groupId>es.ull.simulation</groupId>
  <artifactId>jades-utils</artifactId>
</dependency>

<!-- NEW: Selective (JaDES 2.x) -->
<dependency>
  <groupId>es.ull.simulation</groupId>
  <artifactId>jades-functions</artifactId>  <!-- Lightweight -->
</dependency>

<!-- Only if needed: -->
<dependency>
  <groupId>es.ull.simulation</groupId>
  <artifactId>jades-io</artifactId>  <!-- Brings POI -->
</dependency>
```

### Consequences
**Positive:**
- Lightweight core (`jades-functions` only 50KB)
- Optional heavy dependencies (POI, OWL)
- Better separation of concerns
- Easier testing (isolated modules)
- Clear dependency graph

**Negative:**
- More Maven artifacts to maintain
- Breaking change (requires migration)
- More complex release process

**Neutral:**
- Backward compatibility JAR (`jades-utils` depends on all)
- Gradual migration supported

### Alternatives Considered
1. **Keep monolithic**: Rejected as dependency bloat worsens over time
2. **Separate repos**: Rejected (see ADR-001)
3. **Optional dependencies in one JAR**: Rejected as Maven doesn't support well

### References
- [Architecture issues](../architecture/overview.md#known-architectural-issues)
- [Apache Commons modularization](https://commons.apache.org/) (similar pattern)

---

## Index of Decisions

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| ADR-001 | Multi-Module Maven Structure | Accepted | 2026-01-08 |
| ADR-002 | Java 17 LTS as Target | Accepted | 2026-01-08 |
| ADR-003 | Deprecate Factory Package | Accepted | 2026-01-08 |
| ADR-004 | Self-Registration Pattern | Accepted | 2026-01-08 |
| ADR-005 | JaCoCo Coverage by Default | Accepted | 2026-01-08 |
| ADR-006 | Conventional Commits | Accepted | 2026-01-08 |
| ADR-007 | Google Java Style Guide | Accepted | 2026-01-08 |
| ADR-008 | Modularize jades-utils | Proposed | 2026-01-08 |

---

## Contributing New ADRs

To propose a new architectural decision:

1. Copy ADR template above
2. Number sequentially (ADR-009, ADR-010, etc.)
3. Fill in all sections (especially "Alternatives Considered")
4. Create PR with `docs:` prefix
5. Get review from 2+ maintainers
6. Update index table above

**Guidelines:**
- Keep decisions immutable (don't edit accepted ADRs)
- Supersede old decisions with new ADR (link both)
- Mark deprecated with ~~strikethrough~~ in index
- Include concrete code examples
- Link to relevant issues/discussions
