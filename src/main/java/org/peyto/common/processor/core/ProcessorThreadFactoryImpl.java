package org.peyto.common.processor.core;

import org.peyto.common.processor.ProcessorThreadFactory;
import org.peyto.common.processor.ProcessorProvider;
import org.peyto.common.processor.ProcessorThread;
import org.peyto.common.processor.ProcessorThreadListener;
import org.peyto.common.processor.ProcessorTimeProvider;
import org.peyto.common.processor.core.schedule.ProcessorScheduler;
import org.peyto.common.processor.simulation.ManualSimulationController;
import org.peyto.common.processor.simulation.ReplayData;
import org.peyto.common.processor.simulation.SimulationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProcessorThreadFactoryImpl implements ProcessorThreadFactory {

    private final ProcessorScheduler processorScheduler;
    private final ProcessorTimeProvider processorTimeProvider;

    @SuppressWarnings("rawtypes")
    private final Map<Class, ThreadGroup> threadGroups = new ConcurrentHashMap<>();

    private final ThreadGroup simulationThreadGroup = new ThreadGroup("simulation-processor");

    public ProcessorThreadFactoryImpl(ProcessorScheduler processorScheduler, ProcessorTimeProvider processorTimeProvider) {
        this.processorScheduler = processorScheduler;
        this.processorTimeProvider = processorTimeProvider;
    }

    @Override
    public <T> ProcessorThread createProcessorThread(ProcessorProvider<T> processorProvider, Long threadId, T configurationObject, ProcessorThreadListener threadStatusChangeListener, long processorEndTimeMillis) {
        return new ProcessorThreadImpl<>(
                processorProvider,
                threadGroups.computeIfAbsent(processorProvider.getClass(), pp -> new ThreadGroup(threadGroupName(pp))),
                threadId,
                configurationObject,
                processorTimeProvider,
                processorScheduler,
                threadStatusChangeListener,
                processorEndTimeMillis
        );
    }

    @Override
    public <T> ProcessorThread createSimulationReplay(ProcessorProvider<T> processorProvider, long threadId, T configurationObject, ProcessorThreadListener threadStatusChangeListener, ReplayData<T> replayData) {
        SimulationContext simulationContext = new SimulationContext(replayData.getCycleData());
        return new ProcessorThreadImpl<>(
                processorProvider,
                simulationThreadGroup,
                replayData.getThreadId(),
                replayData.getConfigurationObject(),
                simulationContext,
                processorScheduler,
                simulationContext,
                simulationContext.processorEndTimeMillis()
        );
    }

    @Override
    public <T> ProcessorThread createSimulationManual(ProcessorProvider<T> processorProvider, long threadId, T configurationObject, ProcessorThreadListener threadStatusChangeListener, ManualSimulationController manualSimulationController) {
        throw new RuntimeException("Manual Simulation is not implemented at the moment");
    }

    @SuppressWarnings("rawtypes")
    private static String threadGroupName(Class pp) {
        return pp.getSimpleName().toLowerCase().replace("processorprovider", "");
    }

}
