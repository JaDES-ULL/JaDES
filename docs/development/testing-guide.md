# JaDES Testing Guide

Comprehensive testing guidelines for the JaDES (Java Discrete Event Simulation) framework. This guide ensures consistent, maintainable, and effective tests across the codebase.

---

## 📋 Testing Philosophy

**Core principles:**
1. **Tests are documentation** - They show how the system works
2. **Fast feedback** - Unit tests run in milliseconds
3. **Isolation** - Each test is independent
4. **Coverage** - Aim for >80%, prioritize critical paths
5. **Clarity** - Test names explain behavior

---

## 🎯 Test Coverage Targets

| Component Type | Target Coverage | Priority |
|---------------|----------------|----------|
| Domain models | >90% | Critical |
| Flow implementations | >85% | Critical |
| Utilities | >80% | High |
| Engine internals | >70% | Medium |
| Integration tests | >60% | Medium |
| UI/CLI | >50% | Low |

### Measuring Coverage

```bash
# Run tests with coverage
mvn clean verify

# Open coverage report
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
start target\site\jacoco\index.html  # Windows
```

---

## 📁 Test Organization

### Directory Structure

```
src/test/java/
├── es/ull/
│   ├── model/              # Unit tests for domain models
│   │   ├── SimulationTest.java
│   │   ├── ElementTest.java
│   │   └── ResourceTest.java
│   ├── flow/               # Unit tests for flows
│   │   ├── ActivityFlowTest.java
│   │   └── ConditionalFlowTest.java
│   ├── WFP/                # Workflow pattern integration tests
│   │   ├── WFP01SimulationTest.java
│   │   └── WFP02SimulationTest.java
│   ├── performance/        # Performance benchmarks
│   └── integration/        # Cross-component integration tests
└── resources/              # Test resources (config files, data)
```

### Naming Conventions

| Test Type | Class Name | Method Name |
|-----------|-----------|-------------|
| Unit test | `ClassNameTest.java` | `shouldDoSomething_whenCondition()` |
| Integration test | `IntegrationTestName.java` | `shouldIntegrate_whenScenario()` |
| Workflow pattern | `WFP##SimulationTest.java` | `shouldImplementPattern_whenConditions()` |

**Examples:**
```java
// ✅ GOOD: Descriptive test class and methods
public class SimulationTest {
    
    @Test
    void shouldStartSimulation_whenValidTimeRange() { }
    
    @Test
    void shouldThrowException_whenEndTimeBeforeStartTime() { }
    
    @Test
    void shouldAddElement_whenSimulationNotStarted() { }
}

// ❌ BAD: Vague names
public class TestSimulation {
    @Test
    void test1() { }
    
    @Test
    void testSimulation() { }
}
```

---

## 🧪 Test Structure

### Given-When-Then (GWT) Pattern

Organize tests using **Arrange-Act-Assert** (AAA) or **Given-When-Then** (GWT):

```java
@Test
void shouldSeizeResource_whenResourceAvailable() {
    // Given (Arrange)
    Simulation sim = new Simulation(0, "Test", TimeUnit.MINUTE, 0, 100);
    ResourceType serverType = new ResourceType(sim, "Server");
    Resource server = new Resource(sim, "Server-1", serverType);
    ElementType customerType = new ElementType(sim, "Customer");
    
    WorkGroup wg = new WorkGroup(sim, new ResourceType[]{serverType}, new int[]{1});
    RequestResourcesFlow request = new RequestResourcesFlow(sim, "Request", 0);
    request.addWorkGroup(wg);
    
    Element customer = new Element(sim, customerType, request);
    
    // When (Act)
    sim.run();
    
    // Then (Assert)
    assertTrue(customer.getSeizedResources().contains(server));
    assertEquals(1, server.getSeizedCount());
}
```

### Test Anatomy

```java
@Test  // JUnit 5 annotation
@DisplayName("Should calculate total cost when multiple resources are used")  // Optional descriptive name
void shouldCalculateTotalCost_whenMultipleResourcesUsed() {
    // Given: Setup test data
    Simulation sim = createTestSimulation();
    Resource resource1 = createResource(sim, "R1", 10.0);  // Extract helper methods
    Resource resource2 = createResource(sim, "R2", 20.0);
    
    Element element = new Element(sim, elementType, flow);
    element.seize(resource1);
    element.seize(resource2);
    
    // When: Execute behavior under test
    double totalCost = element.calculateTotalResourceCost();
    
    // Then: Verify expectations
    assertEquals(30.0, totalCost, 0.001);  // Delta for floating point
    verify(resource1).calculateCost();  // If using mocks
    verify(resource2).calculateCost();
}
```

---

## 🎭 Test Fixtures and Setup

### @BeforeEach and @AfterEach

```java
public class WorkGroupTest {
    
    private Simulation simulation;
    private ResourceType serverType;
    private ResourceType operatorType;
    
    @BeforeEach
    void setUp() {
        // Common setup for all tests
        simulation = new Simulation(0, "Test", TimeUnit.MINUTE, 0, 100);
        serverType = new ResourceType(simulation, "Server");
        operatorType = new ResourceType(simulation, "Operator");
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup if needed (usually automatic with GC)
        simulation = null;
    }
    
    @Test
    void shouldCreateWorkGroup_whenValidResourceTypes() {
        // Given
        ResourceType[] types = {serverType, operatorType};
        int[] needed = {1, 2};
        
        // When
        WorkGroup wg = new WorkGroup(simulation, types, needed);
        
        // Then
        assertEquals(2, wg.getResourceTypes().length);
    }
}
```

### @BeforeAll and @AfterAll

Use for expensive one-time setup:

```java
public class OntologyTest {
    
    private static OWLOntologyWrapper ontology;
    
    @BeforeAll
    static void setUpOnce() throws OWLOntologyCreationException {
        // Load ontology once for all tests (expensive operation)
        ontology = new OWLOntologyWrapper();
        ontology.loadOntology("/test-ontology.owl");
    }
    
    @AfterAll
    static void tearDownOnce() {
        // Release resources
        ontology = null;
    }
    
    @Test
    void shouldFindClass_whenClassExists() {
        assertNotNull(ontology.getOwlClass("Disease"));
    }
}
```

---

## ✅ Assertions

### JUnit 5 Assertions

```java
import static org.junit.jupiter.api.Assertions.*;

// Basic assertions
assertEquals(expected, actual);
assertNotEquals(unexpected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(object);
assertNotNull(object);

// Collection assertions
assertArrayEquals(expectedArray, actualArray);
assertIterableEquals(expectedList, actualList);

// Exception assertions
Exception exception = assertThrows(IllegalArgumentException.class, () -> {
    new Simulation(0, "Test", TimeUnit.MINUTE, 100, 0);  // Invalid: end < start
});
assertEquals("End time must be greater than start time", exception.getMessage());

// Timeout assertions
assertTimeout(Duration.ofSeconds(1), () -> {
    simulation.run();  // Should complete within 1 second
});

// Grouped assertions (all execute even if some fail)
assertAll("Resource properties",
    () -> assertEquals("Server-1", resource.getName()),
    () -> assertEquals(serverType, resource.getType()),
    () -> assertTrue(resource.isAvailable())
);
```

### Floating Point Comparisons

```java
// ✅ GOOD: Use delta for floating point comparisons
assertEquals(10.0, simulation.getCurrentTime(), 0.001);  // Within 0.001 tolerance

// ❌ BAD: Direct equality
assertEquals(10.0, simulation.getCurrentTime());  // May fail due to rounding
```

### Custom Assertions

```java
// Extract complex assertions into helper methods
private void assertResourceSeized(Element element, Resource resource) {
    assertTrue(element.getSeizedResources().contains(resource),
        () -> String.format("Element %d should have seized resource %s",
            element.getId(), resource.getName()));
}

@Test
void shouldSeizeMultipleResources_whenAllAvailable() {
    // ... setup code ...
    
    assertResourceSeized(customer, server);
    assertResourceSeized(customer, operator);
}
```

---

## 🎯 Unit Testing Best Practices

### Test One Thing

```java
// ✅ GOOD: Single responsibility per test
@Test
void shouldIncrementCounter_whenElementCreated() {
    int initialCount = simulation.getElementCount();
    new Element(simulation, elementType, flow);
    assertEquals(initialCount + 1, simulation.getElementCount());
}

@Test
void shouldRegisterElement_whenElementCreated() {
    Element element = new Element(simulation, elementType, flow);
    assertTrue(simulation.getElementList().contains(element));
}

// ❌ BAD: Testing multiple concerns
@Test
void shouldCreateElementCorrectly() {
    int initialCount = simulation.getElementCount();
    Element element = new Element(simulation, elementType, flow);
    
    // Too many assertions, multiple concerns
    assertEquals(initialCount + 1, simulation.getElementCount());
    assertTrue(simulation.getElementList().contains(element));
    assertEquals(elementType, element.getType());
    assertNotNull(element.getId());
}
```

### Test Edge Cases

```java
@Test
void shouldHandleEmptyResourceList_whenNoResourcesDefined() {
    Simulation sim = new Simulation(0, "Test", TimeUnit.MINUTE, 0, 100);
    assertEquals(0, sim.getResourceList().size());
}

@Test
void shouldHandleZeroTimeRange_whenStartEqualsEnd() {
    assertThrows(IllegalArgumentException.class, () -> {
        new Simulation(0, "Test", TimeUnit.MINUTE, 50, 50);
    });
}

@Test
void shouldHandleMaxPriority_whenPriorityIsIntegerMax() {
    Element element = new Element(simulation, elementType, flow);
    element.setPriority(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, element.getPriority());
}
```

### Test Null Handling

```java
@Test
void shouldThrowNullPointerException_whenSimulationIsNull() {
    assertThrows(NullPointerException.class, () -> {
        new ResourceType(null, "Server");
    });
}

@Test
void shouldThrowNullPointerException_whenDescriptionIsNull() {
    assertThrows(NullPointerException.class, () -> {
        new ResourceType(simulation, null);
    });
}
```

---

## 🔀 Mocking and Test Doubles

### When to Mock

**DO mock:**
- External dependencies (databases, file systems, network)
- Expensive operations (ontology loading, large computations)
- Non-deterministic behavior (random number generators, current time)

**DON'T mock:**
- Value objects (TimeUnit, WorkGroup)
- Simple domain models
- Internal framework classes (creates brittle tests)

### Mockito Examples

```java
import static org.mockito.Mockito.*;

@Test
void shouldNotifyListener_whenElementCreated() {
    // Given
    Simulation sim = new Simulation(0, "Test", TimeUnit.MINUTE, 0, 100);
    ElementListener listener = mock(ElementListener.class);
    sim.addInfoReceiver(listener);
    
    // When
    Element element = new Element(sim, elementType, flow);
    
    // Then
    verify(listener).infoEmitted(any(ElementInfo.class));
}

@Test
void shouldUseCustomRandomGenerator_whenProvided() {
    // Given
    RandomNumberGenerator mockRandom = mock(RandomNumberGenerator.class);
    when(mockRandom.nextDouble()).thenReturn(0.5);
    
    RandomFunction function = new RandomFunction(mockRandom);
    
    // When
    double value = function.getValue(params);
    
    // Then
    assertEquals(0.5, value);
    verify(mockRandom).nextDouble();
}
```

### Test Doubles (Manual Mocks)

```java
// Simple test double for listener
private static class TestListener extends BasicListener {
    private final List<SimulationInfo> receivedInfo = new ArrayList<>();
    
    @Override
    public void infoEmitted(SimulationInfo info) {
        receivedInfo.add(info);
    }
    
    public int getInfoCount() {
        return receivedInfo.size();
    }
    
    public SimulationInfo getLastInfo() {
        return receivedInfo.isEmpty() ? null : receivedInfo.get(receivedInfo.size() - 1);
    }
}

@Test
void shouldEmitInfoOnStart_whenSimulationRuns() {
    // Given
    TestListener listener = new TestListener();
    simulation.addInfoReceiver(listener);
    
    // When
    simulation.run();
    
    // Then
    assertTrue(listener.getInfoCount() > 0);
    assertInstanceOf(SimulationStartStopInfo.class, listener.getLastInfo());
}
```

---

## 🔄 Integration Testing

### Integration Test Characteristics

- Test multiple components together
- Use real implementations (no mocks for framework classes)
- Verify end-to-end workflows
- May take longer (seconds instead of milliseconds)

### Example: Workflow Pattern Test

```java
/**
 * Integration test for WFP-01 (Sequence Pattern).
 * Tests complete simulation with resources, elements, and flows.
 */
public class WFP01IntegrationTest {
    
    @Test
    void shouldExecuteSequentialActivities_whenElementFollowsFlow() {
        // Given: Complete simulation setup
        Simulation sim = new Simulation(0, "WFP-01", TimeUnit.MINUTE, 0, 100);
        
        ResourceType cashierType = new ResourceType(sim, "Cashier");
        Resource cashier1 = new Resource(sim, "Cashier-1", cashierType);
        Resource cashier2 = new Resource(sim, "Cashier-2", cashierType);
        
        WorkGroup wg = new WorkGroup(sim, new ResourceType[]{cashierType}, new int[]{1});
        
        ActivityFlow verifyAccount = new ActivityFlow(sim, "Verify Account");
        verifyAccount.newWorkGroupAdder(wg).withDelay(5.0).add();
        
        ActivityFlow getCardDetails = new ActivityFlow(sim, "Get Card Details");
        getCardDetails.newWorkGroupAdder(wg).withDelay(3.0).add();
        
        verifyAccount.link(getCardDetails);
        
        ElementType customerType = new ElementType(sim, "Customer");
        new TimeDrivenElementGenerator(sim, 10, customerType, verifyAccount);
        
        // Track simulation events
        TestListener listener = new TestListener();
        sim.addInfoReceiver(listener);
        
        // When: Run simulation
        sim.run();
        
        // Then: Verify expected behavior
        List<ElementActionInfo> actions = listener.getActions();
        
        // Should have executed both activities in sequence
        assertTrue(actions.stream().anyMatch(a -> 
            a.getActivity().getDescription().equals("Verify Account")));
        assertTrue(actions.stream().anyMatch(a -> 
            a.getActivity().getDescription().equals("Get Card Details")));
        
        // Verify Account should always happen before Get Card Details
        for (Element elem : sim.getElementList()) {
            long verifyTime = getActivityTime(actions, elem, "Verify Account");
            long detailsTime = getActivityTime(actions, elem, "Get Card Details");
            assertTrue(verifyTime < detailsTime, 
                "Verify Account should happen before Get Card Details");
        }
    }
    
    private long getActivityTime(List<ElementActionInfo> actions, Element elem, String activityName) {
        return actions.stream()
            .filter(a -> a.getElement().equals(elem))
            .filter(a -> a.getActivity().getDescription().equals(activityName))
            .findFirst()
            .map(ElementActionInfo::getTs)
            .orElse(-1L);
    }
}
```

---

## ⚡ Performance Testing

### Benchmark Structure

```java
public class SimulationPerformanceTest {
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void shouldCompleteSimulation_withinTimeLimit() {
        Simulation sim = createLargeScaleSimulation(1000, 100, 10000);
        
        long startTime = System.nanoTime();
        sim.run();
        long endTime = System.nanoTime();
        
        double durationMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Simulation completed in %.2f ms%n", durationMs);
        
        // Should complete in reasonable time
        assertTrue(durationMs < 5000, "Simulation took too long: " + durationMs + " ms");
    }
    
    @Test
    void shouldScaleLinearly_withElementCount() {
        // Measure performance with different element counts
        Map<Integer, Double> results = new HashMap<>();
        
        for (int nElements : Arrays.asList(10, 100, 1000)) {
            Simulation sim = createSimulation(nElements);
            
            long start = System.nanoTime();
            sim.run();
            long end = System.nanoTime();
            
            double duration = (end - start) / 1_000_000.0;
            results.put(nElements, duration);
        }
        
        // Verify roughly linear scaling (10x elements ≈ 10x time, allow 2x tolerance)
        double ratio1000to100 = results.get(1000) / results.get(100);
        assertTrue(ratio1000to100 < 20, "Scaling worse than linear: " + ratio1000to100);
    }
}
```

---

## 🐛 Test-Driven Development (TDD)

### Red-Green-Refactor Cycle

1. **Red**: Write failing test
2. **Green**: Write minimal code to pass
3. **Refactor**: Improve code while keeping tests green

```java
// 1. RED: Write test first
@Test
void shouldCalculateAverageDuration_whenMultipleActivities() {
    ActivityFlow flow = new ActivityFlow(sim, "Test");
    flow.addDuration(5.0);
    flow.addDuration(10.0);
    flow.addDuration(15.0);
    
    assertEquals(10.0, flow.getAverageDuration(), 0.001);  // FAILS: method doesn't exist
}

// 2. GREEN: Implement minimal solution
public class ActivityFlow {
    private List<Double> durations = new ArrayList<>();
    
    public void addDuration(double duration) {
        durations.add(duration);
    }
    
    public double getAverageDuration() {
        return durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}

// 3. REFACTOR: Test passes, now improve code
// - Add null checks
// - Handle edge cases (empty list)
// - Extract constants
// - Improve naming
```

---

## 📊 Test Data Management

### Test Data Builders

```java
// Builder pattern for complex test objects
public class SimulationBuilder {
    private int id = 0;
    private String description = "Test Simulation";
    private TimeUnit timeUnit = TimeUnit.MINUTE;
    private long startTs = 0;
    private long endTs = 100;
    
    public SimulationBuilder withId(int id) {
        this.id = id;
        return this;
    }
    
    public SimulationBuilder withTimeRange(long start, long end) {
        this.startTs = start;
        this.endTs = end;
        return this;
    }
    
    public Simulation build() {
        return new Simulation(id, description, timeUnit, startTs, endTs);
    }
}

// Usage in tests
@Test
void shouldCreateSimulation_withCustomTimeRange() {
    Simulation sim = new SimulationBuilder()
        .withId(42)
        .withTimeRange(100, 500)
        .build();
    
    assertEquals(42, sim.getId());
    assertEquals(100, sim.getStartTs());
}
```

### Test Resources

Place test data files in `src/test/resources/`:

```
src/test/resources/
├── test-config.xml
├── sample-ontology.owl
└── test-data.xlsx
```

Load in tests:

```java
@Test
void shouldLoadOntology_whenFileExists() throws Exception {
    InputStream stream = getClass().getResourceAsStream("/sample-ontology.owl");
    assertNotNull(stream, "Test ontology file not found");
    
    OWLOntologyWrapper ontology = new OWLOntologyWrapper();
    ontology.load(stream);
    
    assertNotNull(ontology.getOwlClass("Disease"));
}
```

---

## ✅ Test Quality Checklist

Before committing tests, verify:

- [ ] Test name clearly describes behavior (`shouldDoX_whenY`)
- [ ] Uses Given-When-Then structure
- [ ] Tests one behavior per method
- [ ] No hardcoded magic values (use constants)
- [ ] Assertions have descriptive messages
- [ ] No `System.out.println()` (use logger or remove)
- [ ] No commented-out code
- [ ] No ignored/disabled tests without JIRA ticket
- [ ] Runs fast (<100ms for unit tests)
- [ ] Independent (can run in any order)
- [ ] Deterministic (no flakiness)

---

## 🚫 Common Anti-Patterns

### ❌ Testing Implementation Instead of Behavior

```java
// ❌ BAD: Tests internal state
@Test
void shouldSetPriorityField() {
    Element element = new Element(simulation, elementType, flow);
    element.setPriority(5);
    assertEquals(5, element.priority);  // Accesses private field
}

// ✅ GOOD: Tests observable behavior
@Test
void shouldCompareLess_whenPriorityIsLower() {
    Element elem1 = new Element(simulation, elementType, flow);
    elem1.setPriority(5);
    
    Element elem2 = new Element(simulation, elementType, flow);
    elem2.setPriority(10);
    
    assertTrue(elem1.compareTo(elem2) < 0);
}
```

### ❌ Test Interdependence

```java
// ❌ BAD: Tests depend on execution order
private static Element sharedElement;

@Test
void test1_createElement() {
    sharedElement = new Element(simulation, elementType, flow);
    assertNotNull(sharedElement);
}

@Test
void test2_useElement() {
    sharedElement.setPriority(5);  // FAILS if test1 doesn't run first
}

// ✅ GOOD: Each test is independent
@Test
void shouldCreateElement() {
    Element element = new Element(simulation, elementType, flow);
    assertNotNull(element);
}

@Test
void shouldSetPriority() {
    Element element = new Element(simulation, elementType, flow);
    element.setPriority(5);
    assertEquals(5, element.getPriority());
}
```

### ❌ Excessive Mocking

```java
// ❌ BAD: Mocking internal framework classes
@Test
void shouldRunSimulation() {
    Simulation sim = mock(Simulation.class);
    Element elem = mock(Element.class);
    Flow flow = mock(Flow.class);
    
    // This tests nothing useful - just mock interactions
    when(sim.getElement(0)).thenReturn(elem);
    verify(sim).getElement(0);
}

// ✅ GOOD: Use real objects for framework classes
@Test
void shouldRunSimulation() {
    Simulation sim = new Simulation(0, "Test", TimeUnit.MINUTE, 0, 100);
    // ... real setup ...
    sim.run();
    // ... real assertions ...
}
```

---

## 📚 References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Effective Unit Testing](https://www.manning.com/books/effective-unit-testing)
- [Growing Object-Oriented Software, Guided by Tests](http://www.growing-object-oriented-software.com/)
- [JaDES Coding Standards](coding-standards.md)
- [JaDES Architecture Overview](../architecture/overview.md)
