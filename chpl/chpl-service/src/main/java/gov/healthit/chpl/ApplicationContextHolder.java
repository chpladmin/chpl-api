package gov.healthit.chpl;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import gov.healthit.chpl.scheduler.JobAspect;

public class ApplicationContextHolder extends SpringBeanJobFactory implements ApplicationContextAware {
    private static ApplicationContext context;
    private transient AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        beanFactory = ctx.getAutowireCapableBeanFactory();
        context = ctx;
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);

        AspectJProxyFactory pFactory = new AspectJProxyFactory(job);
        pFactory.addAspect(new JobAspect());

        return pFactory.getProxy();
    }

    public static ApplicationContext getContext() {
        return context;
    }
  }