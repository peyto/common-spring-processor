package org.peyto.common.processor.core;

import org.peyto.common.processor.ProcessorContext;
import org.peyto.common.processor.ProcessorTimeProvider;
import org.peyto.common.processor.core.schedule.ProcessorScheduler;

import java.util.concurrent.atomic.AtomicLong;

public class ProcessorContextImpl implements InternalProcessorContext {

    private final long threadId;
    private final ProcessorScheduler scheduler;

    private final ProcessorTimeProvider processorTimeProvider;
    private final AtomicLong currentCycleTimeMillis = new AtomicLong();
    private final AtomicLong currentCycleNumber = new AtomicLong(0);

    private final long processorEndTimeMillis;

    public ProcessorContextImpl(long threadId, ProcessorScheduler scheduler, ProcessorTimeProvider processorTimeProvider, long processorEndTimeMillis) {
        this.threadId = threadId;
        this.scheduler = scheduler;
        this.processorTimeProvider = processorTimeProvider;
        this.processorEndTimeMillis = processorEndTimeMillis;
    }

    @Override
    public long calculateNextCycleNumber() {
        return currentCycleNumber.incrementAndGet();
    }

    @Override
    public long calculateNextCycleTime() {
        currentCycleTimeMillis.set(processorTimeProvider.getMillis());
        return currentCycleTimeMillis.get();
    }

    @Override
    public long getCycleNumber() {
        return currentCycleNumber.get();
    }

    @Override
    public long getCycleTimeMillis() {
        return currentCycleTimeMillis.get();
    }

    @Override
    public long processorEndTimeMillis() {
        return processorEndTimeMillis;
    }

    @Override
    public void scheduleWakeup(long timeMillis) {
        scheduler.schedule(threadId, timeMillis);
    }

    @Override
    @Deprecated
    public void cancelAllScheduledWakeups() {
        scheduler.cancelAllScheduled(threadId);
    }
}
