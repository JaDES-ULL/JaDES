# Getting Started with JaDES

## 📋 Prerequisites

- **Java**: 17 or higher (LTS recommended)
- **Maven**: 3.8 or higher
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (optional)

## 🚀 Installation

### Option 1: Maven Dependency (Once Published)

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>es.ull.simulation</groupId>
    <artifactId>jades-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/JaDES-ULL/JaDES.git
cd JaDES

# Build and install locally
mvn clean install

# Skip tests if you just want to install
mvn clean install -DskipTests
```

## 📝 Your First Simulation

### Example: Simple Queue System

```java
import es.ull.simulation.model.*;
import es.ull.simulation.inforeceiver.StdInfoListener;

public class SimpleQueueExample {
    public static void main(String[] args) {
        // 1. Create simulation (0 to 100 time units)
        Simulation sim = new Simulation(0, "Queue Example", TimeUnit.MINUTE, 0, 100);
        
        // 2. Define resource type (e.g., "Server")
        ResourceType serverType = new ResourceType(sim, "Server Type");
        
        // 3. Create resource (1 server)
        Resource server = new Resource(sim, "Server 1", serverType);
        
        // 4. Define element type (e.g., "Customer")
        ElementType customerType = new ElementType(sim, "Customer Type");
        
        // 5. Create activity manager (service activity)
        ActivityManager serviceActivity = new ActivityManager(sim, "Service Activity", false);
        
        // 6. Define work group (requires 1 server)
        WorkGroup wg = new WorkGroup(sim, serverType, 1);
        serviceActivity.addWorkGroup(0, wg);
        
        // 7. Create flow (request → delay → release)
        RequestResourcesFlow requestFlow = new RequestResourcesFlow(sim, "Request Server", 0);
        requestFlow.addWorkGroup(wg);
        
        DelayFlow serviceFlow = new DelayFlow(sim, "Service Time", 
            new ConstantFunction(5.0)); // 5 minutes
        
        ReleaseResourcesFlow releaseFlow = new ReleaseResourcesFlow(sim, "Release Server", 0);
        releaseFlow.addWorkGroup(wg);
        
        // Chain flows
        requestFlow.link(serviceFlow);
        serviceFlow.link(releaseFlow);
        
        serviceActivity.addPredecessor(requestFlow);
        
        // 8. Generate customers every 10 minutes
        TimeDrivenElementGenerator generator = new TimeDrivenElementGenerator(
            sim, 10, customerType, requestFlow);
        
        // 9. Add listener to see what's happening
        sim.addInfoReceiver(new StdInfoListener());
        
        // 10. Run simulation
        sim.run();
        
        System.out.println("Simulation completed!");
    }
}
```

## 🔍 Understanding the Code

### 1. Simulation Object
```java
Simulation sim = new Simulation(id, name, timeUnit, startTime, endTime);
```
- **id**: Unique identifier (usually 0)
- **name**: Descriptive name
- **timeUnit**: SECOND, MINUTE, HOUR, DAY, etc.
- **startTime/endTime**: Simulation time bounds

### 2. Resources
```java
ResourceType type = new ResourceType(sim, "Type Name");
Resource resource = new Resource(sim, "Resource Name", type);
```
- Resources have limited capacity
- Elements compete for them
- Can represent: servers, machines, people, etc.

### 3. Elements
```java
ElementType type = new ElementType(sim, "Customer");
// Elements created by generators or manually
```
- Entities flowing through simulation
- Can be: customers, jobs, patients, etc.

### 4. Flows
Flows define Element behavior:
- **RequestResourcesFlow**: Seize resources
- **DelayFlow**: Wait for time period
- **ReleaseResourcesFlow**: Free resources
- **ConditionalFlow**: Branch based on condition
- Many more (see Workflow Patterns)

### 5. Generators
```java
TimeDrivenElementGenerator gen = new TimeDrivenElementGenerator(
    sim, interval, elementType, initialFlow);
```
Creates elements at regular intervals.

### 6. Listeners
```java
sim.addInfoReceiver(new StdInfoListener());
```
Receive notifications about simulation events.

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=YourTestClass

# Run with coverage
mvn verify
# See: target/site/jacoco/index.html
```

## 📊 Viewing Results

### Built-in Listeners
- **StdInfoListener**: Prints events to console
- **ProgressListener**: Shows progress bar
- **CpuTimeView**: Displays CPU time usage

### Custom Listener Example
```java
public class MyListener extends BasicListener {
    @Override
    public void infoEmitted(SimulationInfo info) {
        if (info instanceof ElementInfo) {
            ElementInfo eInfo = (ElementInfo) info;
            System.out.println("Element " + eInfo.getElement().getIdentifier() 
                + " event: " + eInfo.getType());
        }
    }
}

sim.addInfoReceiver(new MyListener());
```

## 🎯 Next Steps

1. Explore [Examples](../examples/)
2. Read [Architecture Overview](../architecture/overview.md)
3. Check [Advanced Usage Guide](advanced-usage.md)
4. Browse [API Documentation](../api/)

## ❓ Common Issues

### Java Version Error
```
error: release version 17 not supported
```
**Solution**: Install Java 17+ or update `JAVA_HOME`

### Build Fails
```bash
# Clean and rebuild
mvn clean install -U
```

### Tests Fail
```bash
# Skip tests temporarily
mvn install -DskipTests

# But investigate why they fail!
```

## 💬 Get Help

- [GitHub Issues](https://github.com/JaDES-ULL/JaDES/issues)
- [Discussions](https://github.com/JaDES-ULL/JaDES/discussions)
- Check [CONTRIBUTING.md](../../CONTRIBUTING.md)
