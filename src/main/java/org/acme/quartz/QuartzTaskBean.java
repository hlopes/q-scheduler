package org.acme.quartz;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import io.quarkus.runtime.StartupEvent;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@ApplicationScoped
public class QuartzTaskBean {

    private final Scheduler quartz;

    public QuartzTaskBean(Scheduler quartz) {
        this.quartz = quartz;
    }

    void onStart(@Observes StartupEvent ev) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(MyJob.class).withIdentity("myTrigger", "myGroup").build();

        Trigger trigger = TriggerBuilder.newTrigger()
                                        .withIdentity("myTrigger", "myGroup")
                                        .startNow()
                                        .withSchedule(
                                                SimpleScheduleBuilder.simpleSchedule()
                                                                     .withIntervalInSeconds(10)
                                                                     .repeatForever()
                                        )
                                        .build();

        quartz.scheduleJob(job, trigger);
    }

    @Transactional
    void performTask() {
        Task task = new Task();

        task.persist();
    }

    public static class MyJob implements Job {
        private final QuartzTaskBean quartzTaskBean;

        public MyJob(QuartzTaskBean quartzTaskBean) {
            this.quartzTaskBean = quartzTaskBean;
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            quartzTaskBean.performTask();
        }
    }
}
