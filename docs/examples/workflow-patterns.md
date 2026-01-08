# Example: Workflow Patterns Implementation

## Scenario
Demonstrates how JaDES implements workflow patterns (WFP) from van der Aalst's taxonomy.

## WFP-01: Sequence

Execute activities in order: A → B → C

```java
public class SequencePattern {
    public static void main(String[] args) {
        Simulation sim = new Simulation(0, "Sequence", TimeUnit.MINUTE, 0, 100);
        
        ElementType elementType = new ElementType(sim, "Element");
        
        // Three sequential activities
        DelayFlow activityA = new DelayFlow(sim, "Activity A", new ConstantFunction(5.0));
        DelayFlow activityB = new DelayFlow(sim, "Activity B", new ConstantFunction(3.0));
        DelayFlow activityC = new DelayFlow(sim, "Activity C", new ConstantFunction(4.0));
        
        // Chain them
        activityA.link(activityB);
        activityB.link(activityC);
        
        // Create element at t=0
        new SingleFlow(sim, "Start", activityA).addElement(elementType);
        
        sim.addInfoReceiver(new StdInfoListener());
        sim.run();
        
        // Expected: A(5) → B(3) → C(4) = 12 minutes total
    }
}
```

## WFP-02: Parallel Split

Execute B and C in parallel after A

```java
public class ParallelSplitPattern {
    public static void main(String[] args) {
        Simulation sim = new Simulation(0, "Parallel Split", TimeUnit.MINUTE, 0, 100);
        
        ElementType elementType = new ElementType(sim, "Element");
        
        DelayFlow activityA = new DelayFlow(sim, "Activity A", new ConstantFunction(5.0));
        
        // Fork: creates 2 branches
        ForkFlow fork = new ForkFlow(sim, "Fork");
        activityA.link(fork);
        
        // Branch 1: Activity B
        DelayFlow activityB = new DelayFlow(sim, "Activity B", new ConstantFunction(3.0));
        fork.link(activityB);
        
        // Branch 2: Activity C
        DelayFlow activityC = new DelayFlow(sim, "Activity C", new ConstantFunction(4.0));
        fork.link(activityC);
        
        new SingleFlow(sim, "Start", activityA).addElement(elementType);
        
        sim.addInfoReceiver(new StdInfoListener());
        sim.run();
        
        // Expected: A(5), then B(3) and C(4) execute concurrently
    }
}
```

## WFP-03: Synchronization

Wait for both branches before continuing

```java
public class SynchronizationPattern {
    public static void main(String[] args) {
        Simulation sim = new Simulation(0, "Synchronization", TimeUnit.MINUTE, 0, 100);
        
        ElementType elementType = new ElementType(sim, "Element");
        
        // Fork into 2 branches
        ForkFlow fork = new ForkFlow(sim, "Fork");
        
        DelayFlow branchA = new DelayFlow(sim, "Branch A", new ConstantFunction(5.0));
        DelayFlow branchB = new DelayFlow(sim, "Branch B", new ConstantFunction(8.0));
        
        fork.link(branchA);
        fork.link(branchB);
        
        // Join: waits for BOTH branches
        JoinFlow join = new JoinFlow(sim, "Join");
        branchA.link(join);
        branchB.link(join);
        
        // Continue after sync
        DelayFlow activityC = new DelayFlow(sim, "Activity C", new ConstantFunction(2.0));
        join.link(activityC);
        
        new SingleFlow(sim, "Start", fork).addElement(elementType);
        
        sim.addInfoReceiver(new StdInfoListener());
        sim.run();
        
        // Expected: Fork → A(5) & B(8) → Join waits 8 → C(2) = 10 minutes total
    }
}
```

## WFP-04: Exclusive Choice

Choose one path based on condition

```java
public class ExclusiveChoicePattern {
    public static void main(String[] args) {
        Simulation sim = new Simulation(0, "Exclusive Choice", TimeUnit.MINUTE, 0, 100);
        
        ElementType vipType = new ElementType(sim, "VIP");
        ElementType regularType = new ElementType(sim, "Regular");
        
        // Conditional flow: VIP → fast lane, Regular → normal lane
        ConditionalFlow choice = new ConditionalFlow(sim, "Choose Lane") {
            @Override
            public boolean condition(Element elem) {
                return elem.getType().equals(vipType);
            }
        };
        
        // Branch 1: VIP path (fast)
        DelayFlow vipService = new DelayFlow(sim, "VIP Service", new ConstantFunction(2.0));
        choice.link(vipService);
        
        // Branch 2: Regular path (slower)
        DelayFlow regularService = new DelayFlow(sim, "Regular Service", new ConstantFunction(5.0));
        choice.linkElse(regularService);
        
        // Create elements
        new SingleFlow(sim, "VIP Start", choice).addElement(vipType);
        new SingleFlow(sim, "Regular Start", choice).addElement(regularType);
        
        sim.addInfoReceiver(new StdInfoListener());
        sim.run();
        
        // Expected: VIP takes 2 min, Regular takes 5 min
    }
}
```

## WFP-05: Simple Merge

Multiple paths converge (no synchronization)

```java
public class SimpleMergePattern {
    public static void main(String[] args) {
        Simulation sim = new Simulation(0, "Simple Merge", TimeUnit.MINUTE, 0, 100);
        
        ElementType type1 = new ElementType(sim, "Type1");
        ElementType type2 = new ElementType(sim, "Type2");
        
        // Two different paths
        DelayFlow pathA = new DelayFlow(sim, "Path A", new ConstantFunction(3.0));
        DelayFlow pathB = new DelayFlow(sim, "Path B", new ConstantFunction(5.0));
        
        // Merge point (no waiting)
        MergeFlow merge = new MergeFlow(sim, "Merge");
        pathA.link(merge);
        pathB.link(merge);
        
        // Shared activity after merge
        DelayFlow shared = new DelayFlow(sim, "Shared Activity", new ConstantFunction(2.0));
        merge.link(shared);
        
        // Elements take different paths
        new SingleFlow(sim, "Start A", pathA).addElement(type1);
        new SingleFlow(sim, "Start B", pathB).addElement(type2);
        
        sim.addInfoReceiver(new StdInfoListener());
        sim.run();
        
        // Expected: Type1 takes A(3)+shared(2)=5, Type2 takes B(5)+shared(2)=7
    }
}
```

## Advanced Pattern: Deferred Choice

Decision made at runtime by external event

```java
public class DeferredChoicePattern {
    public static void main(String[] args) {
        Simulation sim = new Simulation(0, "Deferred Choice", TimeUnit.MINUTE, 0, 100);
        
        ElementType orderType = new ElementType(sim, "Order");
        
        // Create activity with multiple completion paths
        ResourceType paymentType = new ResourceType(sim, "Payment Method");
        Resource creditCard = new Resource(sim, "Credit Card", paymentType);
        Resource cash = new Resource(sim, "Cash", paymentType);
        
        // WorkGroup allows "any" resource from type
        WorkGroup paymentWG = new WorkGroup(sim, paymentType, 1);
        
        ActivityManager payment = new ActivityManager(sim, "Payment", false);
        payment.addWorkGroup(0, paymentWG);
        
        RequestResourcesFlow request = new RequestResourcesFlow(sim, "Request Payment", 0);
        request.addWorkGroup(paymentWG);
        
        // Process based on which resource was seized
        StructuredFlow process = new StructuredFlow(sim, "Process Payment") {
            @Override
            public void request(Element elem) {
                super.request(elem);
                Resource seized = elem.getCaughtResources().get(0);
                if (seized.equals(creditCard)) {
                    System.out.println("Processing credit card...");
                } else {
                    System.out.println("Processing cash...");
                }
            }
        };
        
        request.link(process);
        
        ReleaseResourcesFlow release = new ReleaseResourcesFlow(sim, "Release", 0);
        release.addWorkGroup(paymentWG);
        process.link(release);
        
        payment.addPredecessor(request);
        
        // Create multiple orders
        for (int i = 0; i < 5; i++) {
            new SingleFlow(sim, "Order " + i, request).addElement(orderType);
        }
        
        sim.addInfoReceiver(new StdInfoListener());
        sim.run();
        
        // Expected: Orders compete for available payment method
    }
}
```

## Pattern Summary

| Pattern | JaDES Implementation | Key Classes |
|---------|---------------------|-------------|
| WFP-01 Sequence | `flow1.link(flow2)` | Flow chaining |
| WFP-02 Parallel Split | `ForkFlow` | ForkFlow |
| WFP-03 Synchronization | `JoinFlow` | JoinFlow |
| WFP-04 Exclusive Choice | `ConditionalFlow` | ConditionalFlow |
| WFP-05 Simple Merge | `MergeFlow` | MergeFlow |
| WFP-06 Multi-Choice | Multiple `ConditionalFlow` | ConditionalFlow |
| WFP-16 Deferred Choice | Resource competition | WorkGroup |
| WFP-17 Interleaved Parallel | Resource serialization | WorkGroup |

## Next Steps
- [Custom Flow Implementation](custom-flows.md)
- [Event Listeners](event-listeners.md)
- [Resource Scheduling](resource-scheduling.md)
