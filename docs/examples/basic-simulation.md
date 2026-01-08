# Example: Basic Server Simulation

## Scenario
A single server processes customers arriving at fixed intervals. Each service takes 5 minutes.

## Complete Code

```java
package examples;

import es.ull.simulation.model.*;
import es.ull.simulation.inforeceiver.StdInfoListener;

/**
 * Demonstrates basic JaDES features:
 * - Resource creation
 * - Element generation
 * - Simple flow (Request → Delay → Release)
 */
public class BasicServerSimulation {
    
    public static void main(String[] args) {
        // Simulation runs from t=0 to t=100 minutes
        Simulation sim = new Simulation(0, "Basic Server", TimeUnit.MINUTE, 0, 100);
        
        // Define resource
        ResourceType serverType = new ResourceType(sim, "Server Type");
        Resource server = new Resource(sim, "Server-1", serverType);
        
        // Define customer type
        ElementType customerType = new ElementType(sim, "Customer");
        
        // Create service activity
        ActivityManager service = new ActivityManager(sim, "Service", false);
        WorkGroup wg = new WorkGroup(sim, serverType, 1); // Need 1 server
        service.addWorkGroup(0, wg);
        
        // Define flow chain
        RequestResourcesFlow request = new RequestResourcesFlow(sim, "Request", 0);
        request.addWorkGroup(wg);
        
        DelayFlow delay = new DelayFlow(sim, "Service", new ConstantFunction(5.0));
        
        ReleaseResourcesFlow release = new ReleaseResourcesFlow(sim, "Release", 0);
        release.addWorkGroup(wg);
        
        request.link(delay);
        delay.link(release);
        
        service.addPredecessor(request);
        
        // Generate 1 customer every 10 minutes
        new TimeDrivenElementGenerator(sim, 10, customerType, request);
        
        // Add console output
        sim.addInfoReceiver(new StdInfoListener());
        
        // Run
        sim.run();
    }
}
```

## Expected Output

```
[0.0] Element 0 (Customer) created
[0.0] Element 0 requests Server-1
[0.0] Element 0 seizes Server-1
[5.0] Element 0 releases Server-1
[5.0] Element 0 finished
[10.0] Element 1 created
[10.0] Element 1 seizes Server-1
[15.0] Element 1 releases Server-1
[15.0] Element 1 finished
...
```

## Key Concepts

### 1. Resource Management
```java
ResourceType type = new ResourceType(sim, "Server Type");
Resource resource = new Resource(sim, "Server-1", type);
```
- **ResourceType**: Category (e.g., "Doctor", "Machine")
- **Resource**: Specific instance (e.g., "Doctor Smith")

### 2. Work Groups
```java
WorkGroup wg = new WorkGroup(sim, serverType, 1);
```
Defines resource requirements:
- Which type needed
- How many units

### 3. Flow Chain
```java
request.link(delay);  // After request → do delay
delay.link(release);  // After delay → do release
```
Flows execute sequentially.

## Variations

### Multiple Servers
```java
Resource server1 = new Resource(sim, "Server-1", serverType);
Resource server2 = new Resource(sim, "Server-2", serverType);
// Now 2 customers can be served simultaneously
```

### Variable Service Time
```java
// Uniform distribution: 3-7 minutes
RandomFunction serviceTime = new RandomNumberFunction(
    3.0, 7.0, RandomNumberFactory.getInstance());
DelayFlow delay = new DelayFlow(sim, "Service", serviceTime);
```

### Arrival Rate
```java
// Faster arrivals (every 6 minutes)
new TimeDrivenElementGenerator(sim, 6, customerType, request);

// Random arrivals (exponential distribution)
new TimeDrivenElementGenerator(sim, 
    new ExponentialFunction(10.0), customerType, request);
```

## Next Steps
- [Resource Pools](resource-pools.md)
- [Conditional Flows](conditional-flows.md)
- [Statistics Collection](statistics-collection.md)
