# JaDES Coding Standards

This document defines the coding standards for the JaDES (Java Discrete Event Simulation) project. All contributors must follow these guidelines to maintain code quality and consistency.

## 📚 Base Style Guide

JaDES follows the **[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)** with specific adaptations for simulation domain modeling.

**Key requirements:**
- Java 17 LTS (language features, APIs)
- 4 spaces for indentation (NO tabs)
- 120 characters maximum line length
- UTF-8 file encoding
- LF line endings (Unix style)

Use `.editorconfig` in the root directory to automatically configure your IDE.

---

## 📦 Package Organization

### Structure

```
es.ull.simulation
├── model              # Core domain model (Simulation, Element, Resource)
│   ├── engine         # Execution engine (internal, not public API)
│   ├── flow           # Workflow patterns (ActivityFlow, ConditionalFlow, etc.)
│   └── location       # Spatial modeling (Location, MoveFlow)
├── info               # Event notification system (listeners, info objects)
├── condition          # Conditional logic for flows
├── factory            # [DEPRECATED] Object creation (use constructors)
└── utils              # Internal utilities (not public API)
```

### Naming Rules

| Type | Convention | Example |
|------|-----------|---------|
| Package | `lowercase` | `es.ull.simulation.model` |
| Class | `PascalCase` | `SimulationEngine` |
| Interface | `PascalCase` (no "I" prefix) | `Prioritizable` |
| Abstract Class | `PascalCase` (with "Abstract" prefix) | `AbstractEngineObject` |
| Enum | `PascalCase` | `TimeUnit` |
| Enum constants | `UPPER_SNAKE_CASE` | `WAIT_FOR_SIGNAL` |
| Method | `camelCase` | `getResourceList()` |
| Variable | `camelCase` | `activityManager` |
| Constant | `UPPER_SNAKE_CASE` | `DEFAULT_PRIORITY` |
| Generic type | Single uppercase letter | `T`, `E`, `K`, `V` |

---

## 🏗️ Class Design

### Class Structure Order

Organize class members in this order:

```java
public class ExampleClass {
    // 1. Static constants
    private static final int DEFAULT_VALUE = 0;
    
    // 2. Static variables
    private static int nextId = 0;
    
    // 3. Instance variables (by visibility)
    private final Simulation simulation;
    protected String description;
    
    // 4. Constructors
    public ExampleClass(Simulation sim) {
        this.simulation = sim;
    }
    
    // 5. Static methods
    public static ExampleClass createDefault() {
        // ...
    }
    
    // 6. Public methods
    public void publicMethod() {
        // ...
    }
    
    // 7. Protected methods
    protected void protectedMethod() {
        // ...
    }
    
    // 8. Private methods
    private void privateMethod() {
        // ...
    }
    
    // 9. Inner classes/enums
    public enum State {
        ACTIVE, INACTIVE
    }
}
```

### Constructor Guidelines

#### Self-Registration Pattern

JaDES uses **self-registration** where objects register themselves with their parent during construction:

```java
// ✅ GOOD: Element auto-registers with Simulation
public Element(Simulation simulation, ElementType type, IInitializerFlow initialFlow) {
    super(simulation, simulation.getElementList().size(), "ELEM");
    this.elementType = type;
    simulation.add(this);  // Self-registration
}

// ❌ BAD: Requiring manual registration
Element elem = new Element(simulation, type, flow);
simulation.add(elem);  // Redundant, error-prone
```

**Rationale**: Simulation objects have tight lifecycle coupling with `Simulation`. Auto-registration prevents forgotten registrations.

#### Validation in Constructors

```java
public WorkGroup(Simulation sim, ResourceType[] resourceTypes, int[] needed) {
    // Validate immediately
    Objects.requireNonNull(sim, "Simulation cannot be null");
    Objects.requireNonNull(resourceTypes, "Resource types cannot be null");
    
    if (resourceTypes.length != needed.length) {
        throw new IllegalArgumentException(
            "Resource types and needed arrays must have same length");
    }
    
    // ... continue construction
}
```

---

## 📝 Naming Conventions

### Methods

| Method Type | Pattern | Example |
|------------|---------|---------|
| Getter | `get<Property>()` | `getSimulation()` |
| Boolean getter | `is<Property>()` | `isExclusive()` |
| Setter | `set<Property>()` | `setPriority()` |
| Factory | `create<Type>()` | `createDefaultFlow()` |
| Builder | `new<Type>Adder()` | `newWorkGroupAdder()` |
| Callback | `on<Event>()` | `onResourceSeized()` |
| Template | `before<Action>()`, `after<Action>()` | `beforeRequest()`, `afterFinalize()` |

### Variables

```java
// ✅ GOOD: Descriptive, domain-specific
private final ActivityManager activityManager;
private final List<RequestResourcesFlow> activityList;
private final WorkGroup transportWorkGroup;

// ❌ BAD: Generic, abbreviations
private final ActivityManager am;  // Too short
private final List<RequestResourcesFlow> flows;  // Too generic
private final WorkGroup wg;  // Unclear abbreviation
```

**Exception**: Well-known abbreviations in simulation domain are acceptable:
- `wg` for `WorkGroup` (in local scope only)
- `rt` for `ResourceType` (in local scope only)
- `ei` for `ElementInstance` (in internal engine code only)

### Constants

```java
// ✅ GOOD: Descriptive constants
public static final int DEFAULT_PRIORITY = 0;
public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTE;
private static final Logger LOGGER = LogManager.getLogger(Simulation.class);

// ❌ BAD: Magic numbers in code
if (priority == 0) { ... }  // Use DEFAULT_PRIORITY instead
```

---

## 📖 Documentation

### Javadoc Requirements

**MANDATORY** for:
- All public classes
- All public methods
- All public constructors
- All protected methods (API extension points)

**OPTIONAL** but recommended for:
- Complex private methods (>20 lines)
- Non-obvious algorithms
- Engine internals (for maintainers)

### Javadoc Structure

```java
/**
 * Brief one-line summary (ends with period).
 * 
 * Detailed description explaining purpose, behavior, and usage.
 * Can span multiple paragraphs.
 * 
 * <p>Use {@code <p>} for paragraph breaks.
 * 
 * <p><b>Example usage:</b>
 * <pre>{@code
 * Simulation sim = new Simulation(0, "Example", TimeUnit.MINUTE, 0, 100);
 * ResourceType type = new ResourceType(sim, "Server");
 * }</pre>
 * 
 * @param paramName description (starts lowercase, no period)
 * @param anotherParam description
 * @return description of return value (starts lowercase, no period)
 * @throws ExceptionType when this exception is thrown
 * @see RelatedClass
 * @since 1.0
 */
public ReturnType methodName(ParamType paramName, ParamType anotherParam) {
    // ...
}
```

### Documentation Examples from JaDES

#### Good: Complete Domain Documentation

```java
/**
 * An entity capable of following a {@link IFlow workflow}. Elements have a 
 * {@link ElementType type} and interact with {@link Resource resources} by means of
 * {@link es.ull.simulation.model.flow.IResourceHandlerFlow resource handler flows}.
 * Elements can also move from a {@link Location} to another.
 * 
 * <p>Elements are the active entities in a simulation. They execute flows, seize 
 * resources, and generate events. Each element belongs to exactly one simulation 
 * and has a unique identifier within that simulation.
 * 
 * <p><b>Example:</b>
 * <pre>{@code
 * ElementType customerType = new ElementType(sim, "Customer");
 * Element customer = new Element(sim, customerType, initialFlow);
 * }</pre>
 * 
 * @author Iván Castilla Rodríguez
 * @see ElementType
 * @see IFlow
 * @see Resource
 */
public class Element extends VariableStoreSimulationObject 
        implements Prioritizable, IEventSource, IMovable {
    // ...
}
```

#### Good: Constructor with Constraints

```java
/**
 * Creates a simulation with specified time bounds and units.
 * 
 * @param id simulation identifier (typically 0 for single simulations)
 * @param description human-readable simulation name
 * @param unit time unit for simulation clock (SECOND, MINUTE, HOUR, DAY, etc.)
 * @param startTs start time (must be >= 0)
 * @param endTs end time (must be > startTs)
 * @throws IllegalArgumentException if endTs <= startTs or startTs < 0
 */
public Simulation(int id, String description, TimeUnit unit, long startTs, long endTs) {
    // ...
}
```

#### Good: Method with Side Effects

```java
/**
 * Adds an activity manager to the simulation.
 * 
 * <p>Activity managers are automatically added from their constructor, so this 
 * method is typically only called internally. Manual calls are only needed when 
 * reconstructing simulation state from external sources.
 * 
 * @param am the activity manager to add (must not be null)
 * @throws IllegalStateException if simulation has already started
 */
public void add(ActivityManager am) {
    // ...
}
```

### Package-level Documentation

Create `package-info.java` in each major package:

```java
/**
 * Core domain model for discrete event simulation.
 * 
 * <p>This package contains the fundamental building blocks:
 * <ul>
 *   <li>{@link Simulation} - Main simulation controller
 *   <li>{@link Element} - Active entities following workflows
 *   <li>{@link Resource} - Limited capacity entities
 *   <li>{@link ActivityManager} - Activity coordination
 * </ul>
 * 
 * <p><b>Usage:</b> Start by creating a {@link Simulation}, then add 
 * {@link ResourceType}s, {@link Resource}s, {@link ElementType}s, and 
 * define workflows using {@link es.ull.simulation.model.flow.IFlow flows}.
 * 
 * @see es.ull.simulation.model.flow
 * @see es.ull.simulation.info
 */
package es.ull.simulation.model;
```

---

## 🔍 Code Quality

### Imports

```java
// ✅ GOOD: Organized imports
import java.util.ArrayList;
import java.util.List;

import es.ull.simulation.info.SimulationInfo;
import es.ull.simulation.model.Element;
import es.ull.simulation.model.Resource;

// ❌ BAD: Wildcard imports (except for testing)
import java.util.*;
import es.ull.simulation.model.*;
```

**Exception**: Wildcard imports allowed in test classes for static imports:
```java
import static org.junit.jupiter.api.Assertions.*;
```

### Variable Declarations

```java
// ✅ GOOD: One variable per declaration, initialized
private final Simulation simulation;
private String description = "";
private int priority = DEFAULT_PRIORITY;

// ❌ BAD: Multiple variables, uninitialized primitives
private String description, name;
private int priority;  // Relies on default initialization
```

### Conditional Statements

```java
// ✅ GOOD: Always use braces, even for single statements
if (condition) {
    doSomething();
}

// ❌ BAD: No braces
if (condition)
    doSomething();
```

### String Formatting

```java
// ✅ GOOD: String.format for complex formatting
String message = String.format("Element %d at time %.2f", 
    element.getId(), simulation.getTs());

// ✅ GOOD: Simple concatenation for 2-3 parts
String description = elementType.getName() + " " + element.getId();

// ❌ BAD: Complex concatenation
String message = "Element " + element.getId() + " at time " + 
    simulation.getTs() + " in state " + state;  // Use String.format
```

---

## ⚡ Performance Considerations

### Collections

```java
// ✅ GOOD: Specify initial capacity when size known
List<Element> elements = new ArrayList<>(expectedSize);

// ✅ GOOD: Use appropriate collection type
Map<Integer, Resource> resourceMap = new TreeMap<>();  // Sorted keys
ArrayDeque<Event> eventQueue = new ArrayDeque<>();     // Fast FIFO

// ❌ BAD: Generic ArrayList for everything
List<Event> eventQueue = new ArrayList<>();  // Use ArrayDeque for queue
```

### Object Creation

```java
// ✅ GOOD: Reuse objects in tight loops
StringBuilder sb = new StringBuilder();
for (Element elem : elements) {
    sb.setLength(0);  // Reset instead of creating new
    sb.append(elem.getDescription());
    // ...
}

// ❌ BAD: Create new objects in loop
for (Element elem : elements) {
    String desc = new String(elem.getDescription());  // Unnecessary copy
}
```

### Logging

```java
// ✅ GOOD: Guard expensive operations
if (logger.isDebugEnabled()) {
    logger.debug("Complex state: {}", buildComplexDebugString());
}

// ❌ BAD: Always compute, even if not logged
logger.debug("Complex state: " + buildComplexDebugString());
```

---

## 🚨 Error Handling

### Null Handling

**Use `@Nullable` and `@NonNull` annotations** (JSR-305):

```java
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Finds resource by identifier.
 * 
 * @param id resource identifier
 * @return the resource, or null if not found
 */
@Nullable
public Resource findResourceById(int id) {
    return resourceMap.get(id);
}

/**
 * Adds a resource to simulation.
 * 
 * @param resource the resource to add (must not be null)
 */
public void add(@Nonnull Resource resource) {
    Objects.requireNonNull(resource, "Resource cannot be null");
    resourceList.add(resource);
}
```

### Exception Guidelines

| Situation | Exception Type | Example |
|-----------|---------------|---------|
| Null parameter | `NullPointerException` | `Objects.requireNonNull()` |
| Invalid argument | `IllegalArgumentException` | Invalid time range |
| Invalid state | `IllegalStateException` | Simulation already running |
| Unsupported operation | `UnsupportedOperationException` | Deprecated factory methods |

```java
// ✅ GOOD: Descriptive exception messages
if (endTs <= startTs) {
    throw new IllegalArgumentException(
        String.format("End time (%d) must be greater than start time (%d)", 
            endTs, startTs));
}

// ❌ BAD: Generic message
if (endTs <= startTs) {
    throw new IllegalArgumentException("Invalid time range");
}
```

### Exception Handling

```java
// ✅ GOOD: Handle specific exceptions
try {
    parseSimulationConfig(file);
} catch (IOException e) {
    logger.error("Failed to read config file: {}", file, e);
    throw new SimulationConfigException("Cannot load config", e);
} catch (SAXException e) {
    logger.error("Invalid XML in config file: {}", file, e);
    throw new SimulationConfigException("Malformed config", e);
}

// ❌ BAD: Catch generic Exception
try {
    parseSimulationConfig(file);
} catch (Exception e) {
    logger.error("Error", e);  // Too vague
}
```

---

## 🧩 Design Patterns in JaDES

### Builder Pattern for Complex Configuration

```java
// ✅ GOOD: Fluent builder API
Resource resource = new Resource(sim, "Server-1", serverType);
resource.newTimeTableOrCancelEntriesAdder(serverType)
    .withDuration(cycle, 100)
    .addTimeTableEntry();

// ❌ BAD: Multiple setter calls
resource.addTimeTableEntry(serverType, cycle, 100);
```

### Template Method Pattern

```java
// ✅ GOOD: Override specific hooks
public class CustomDelayFlow extends DelayFlow {
    @Override
    public boolean beforeRequest(ElementInstance ei) {
        // Custom pre-processing
        return super.beforeRequest(ei);
    }
    
    @Override
    public void afterFinalize(ElementInstance ei) {
        // Custom post-processing
        super.afterFinalize(ei);
    }
}
```

### Factory Pattern (Deprecated)

**DO NOT use `SimulationFactory` for new code.** Use direct constructors:

```java
// ✅ GOOD: Direct construction
Simulation sim = new Simulation(0, "Example", TimeUnit.MINUTE, 0, 100);
ResourceType type = new ResourceType(sim, "Server");
Resource resource = new Resource(sim, "Server-1", type);

// ❌ DEPRECATED: Factory pattern
SimulationFactory factory = new SimulationFactory(0, "Example");
ResourceType type = factory.getResourceTypeInstance("Server");
```

**Rationale**: Factory adds complexity without benefit. Direct constructors are clearer and type-safe.

---

## 🔧 Logging

### Logger Declaration

```java
// ✅ GOOD: Private static final logger per class
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Simulation {
    private static final Logger LOGGER = LogManager.getLogger(Simulation.class);
}
```

### Log Levels

| Level | Usage | Example |
|-------|-------|---------|
| `TRACE` | Detailed execution flow (loop iterations, state changes) | "Element 5 entering flow RequestResourcesFlow" |
| `DEBUG` | Diagnostic information for developers | "WorkGroup requires 2 servers, 3 available" |
| `INFO` | General informational messages | "Simulation started at t=0" |
| `WARN` | Potential problems, recoverable errors | "Resource unavailable, element queued" |
| `ERROR` | Error events that still allow app to continue | "Failed to parse config, using defaults" |
| `FATAL` | Severe errors causing shutdown | "Out of memory, terminating simulation" |

### Logging Examples

```java
// ✅ GOOD: Use parameterized logging
LOGGER.info("Simulation '{}' started at t={}", description, startTs);
LOGGER.debug("Element {} seized resource {}", elem.getId(), resource.getId());

// ✅ GOOD: Guard expensive operations
if (LOGGER.isDebugEnabled()) {
    LOGGER.debug("Full simulation state: {}", buildFullStateDebugString());
}

// ❌ BAD: String concatenation in log call
LOGGER.info("Simulation " + description + " started");  // Creates string even if INFO disabled
```

---

## 🧪 Testing Standards

See [testing-guide.md](testing-guide.md) for comprehensive testing guidelines.

**Quick reference:**

```java
// ✅ GOOD: Descriptive test names
@Test
void shouldSeizeResource_whenResourceAvailable() {
    // ...
}

@Test
void shouldThrowException_whenEndTimeBeforeStartTime() {
    // ...
}
```

---

## ✅ Checklist Before Committing

- [ ] Code compiles without warnings (`mvn clean compile`)
- [ ] All tests pass (`mvn test`)
- [ ] Code coverage >80% for modified classes (`mvn verify`)
- [ ] Javadoc for all public classes/methods
- [ ] No `System.out.println()` (use logger)
- [ ] No TODOs or FIXMEs (create GitHub issue instead)
- [ ] No commented-out code (remove or explain)
- [ ] Imports organized (no wildcards except tests)
- [ ] No magic numbers (use constants)
- [ ] Exception messages are descriptive

---

## 📚 References

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Effective Java (3rd Edition)](https://www.oreilly.com/library/view/effective-java/9780134686097/)
- [Clean Code](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)
- [JaDES Architecture Overview](../architecture/overview.md)
- [JaDES Testing Guide](testing-guide.md)
