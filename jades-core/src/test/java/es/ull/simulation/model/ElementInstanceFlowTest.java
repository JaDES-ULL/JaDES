package es.ull.simulation.model;

import es.ull.simulation.model.flow.IFlow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ElementInstanceFlow}.
 * Tests flow state management without requiring full simulation execution.
 * 
 * This test demonstrates the benefit of Phase 2's extraction:
 * ElementInstanceFlow can be tested in isolation without Element/Simulation.
 * 
 * @author Phase 6 - Option A (Better ROI Classes)
 */
class ElementInstanceFlowTest {
    
    @Test
    void shouldReturnInitialFlow() {
        // Given
        IFlow initialFlow = mock(IFlow.class);
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, initialFlow);
        
        // When & Then
        assertSame(initialFlow, flowManager.getInitialFlow());
    }
    
    @Test
    void shouldStartWithNullCurrentFlow() {
        // Given
        IFlow initialFlow = mock(IFlow.class);
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, initialFlow);
        
        // When & Then
        assertNull(flowManager.getCurrentFlow());
    }
    
    @Test
    void shouldStartWithNullLastFlow() {
        // Given
        IFlow initialFlow = mock(IFlow.class);
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, initialFlow);
        
        // When & Then
        assertNull(flowManager.getLastFlow());
    }
    
    @Test
    void shouldSetAndGetCurrentFlow() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow newFlow = mock(IFlow.class);
        
        // When
        flowManager.setCurrentFlow(newFlow);
        
        // Then
        assertSame(newFlow, flowManager.getCurrentFlow());
    }
    
    @Test
    void shouldSaveLastFlowWhenSettingNewCurrent() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow flow1 = mock(IFlow.class);
        IFlow flow2 = mock(IFlow.class);
        
        // When
        flowManager.setCurrentFlow(flow1);
        flowManager.setCurrentFlow(flow2);
        
        // Then
        assertSame(flow2, flowManager.getCurrentFlow());
        assertSame(flow1, flowManager.getLastFlow());
    }
    
    @Test
    void shouldNotUpdateLastFlowWhenSettingNull() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow flow1 = mock(IFlow.class);
        
        // When
        flowManager.setCurrentFlow(flow1);
        IFlow lastBeforeNull = flowManager.getLastFlow();
        flowManager.setCurrentFlow(null);
        
        // Then
        assertNull(flowManager.getCurrentFlow());
        assertSame(lastBeforeNull, flowManager.getLastFlow());
    }
    
    @Test
    void shouldTrackFlowSequence() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow flow1 = mock(IFlow.class);
        IFlow flow2 = mock(IFlow.class);
        IFlow flow3 = mock(IFlow.class);
        
        // When & Then
        flowManager.setCurrentFlow(flow1);
        assertSame(flow1, flowManager.getCurrentFlow());
        assertNull(flowManager.getLastFlow());
        
        flowManager.setCurrentFlow(flow2);
        assertSame(flow2, flowManager.getCurrentFlow());
        assertSame(flow1, flowManager.getLastFlow());
        
        flowManager.setCurrentFlow(flow3);
        assertSame(flow3, flowManager.getCurrentFlow());
        assertSame(flow2, flowManager.getLastFlow());
    }
    
    @Test
    void shouldHandleFlowReentry() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow flowA = mock(IFlow.class);
        IFlow flowB = mock(IFlow.class);
        
        // When: A -> B -> A (loop)
        flowManager.setCurrentFlow(flowA);
        flowManager.setCurrentFlow(flowB);
        flowManager.setCurrentFlow(flowA);
        
        // Then
        assertSame(flowA, flowManager.getCurrentFlow());
        assertSame(flowB, flowManager.getLastFlow());
    }
    
    @Test
    void shouldReportExecutingFlow() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        
        // When: no flow
        assertFalse(flowManager.isExecutingFlow());
        
        // When: flow set
        flowManager.setCurrentFlow(mock(IFlow.class));
        assertTrue(flowManager.isExecutingFlow());
        
        // When: flow cleared
        flowManager.clearCurrentFlow();
        assertFalse(flowManager.isExecutingFlow());
    }
    
    @Test
    void shouldClearCurrentFlowAndSaveToLast() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow flow = mock(IFlow.class);
        flowManager.setCurrentFlow(flow);
        
        // When
        flowManager.clearCurrentFlow();
        
        // Then
        assertNull(flowManager.getCurrentFlow());
        assertSame(flow, flowManager.getLastFlow());
    }
    
    @Test
    void shouldAllowNullInitialFlow() {
        // Given & When
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, null);
        
        // Then
        assertNull(flowManager.getInitialFlow());
    }
    
    @Test
    void shouldHandleSameFlowSetTwice() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow sameFlow = mock(IFlow.class);
        
        // When
        flowManager.setCurrentFlow(sameFlow);
        flowManager.setCurrentFlow(sameFlow);
        
        // Then
        assertSame(sameFlow, flowManager.getCurrentFlow());
        assertSame(sameFlow, flowManager.getLastFlow());
    }
    
    @Test
    void shouldMaintainInitialFlowUnchanged() {
        // Given
        IFlow initialFlow = mock(IFlow.class);
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, initialFlow);
        
        // When: change current flow multiple times
        flowManager.setCurrentFlow(mock(IFlow.class));
        flowManager.setCurrentFlow(mock(IFlow.class));
        flowManager.setCurrentFlow(mock(IFlow.class));
        
        // Then: initial flow remains unchanged
        assertSame(initialFlow, flowManager.getInitialFlow());
    }
    
    @Test
    void shouldSetLastFlowDirectly() {
        // Given
        ElementInstanceFlow flowManager = new ElementInstanceFlow(null, mock(IFlow.class));
        IFlow lastFlow = mock(IFlow.class);
        
        // When
        flowManager.setLastFlow(lastFlow);
        
        // Then
        assertSame(lastFlow, flowManager.getLastFlow());
    }
}
