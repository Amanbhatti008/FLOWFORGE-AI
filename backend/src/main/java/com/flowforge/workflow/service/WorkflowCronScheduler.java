package com.flowforge.workflow.service;

import com.flowforge.workflow.domain.Workflow;
import com.flowforge.workflow.dto.TriggerWorkflowRequest;
import com.flowforge.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowCronScheduler {

    private final WorkflowRepository workflowRepository;
    private final WorkflowTriggerService workflowTriggerService;
    private final RedissonClient redissonClient;

    @Scheduled(fixedDelay = 60000) // Run every minute
    @Transactional
    public void processScheduledWorkflows() {
        RLock lock = redissonClient.getLock("workflow-cron-scheduler-lock");
        try {
            if (lock.tryLock()) {
                Instant now = Instant.now();
                List<Workflow> dueWorkflows = workflowRepository.findByNextRunAtBefore(now);
                
                for (Workflow workflow : dueWorkflows) {
                    log.info("Triggering scheduled workflow: {}", workflow.getId());
                    try {
                        TriggerWorkflowRequest request = new TriggerWorkflowRequest();
                        // Trigger as the user who created it
                        workflowTriggerService.triggerWorkflow(workflow.getId(), request, workflow.getCreatedBy().getEmail());
                        
                        // Calculate next run time
                        if (workflow.getCronExpression() != null) {
                            CronExpression cron = CronExpression.parse(workflow.getCronExpression());
                            ZonedDateTime next = cron.next(ZonedDateTime.now(ZoneOffset.UTC));
                            if (next != null) {
                                workflow.setNextRunAt(next.toInstant());
                                workflowRepository.save(workflow);
                            } else {
                                workflow.setNextRunAt(null);
                                workflowRepository.save(workflow);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to trigger scheduled workflow {}", workflow.getId(), e);
                    }
                }
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
