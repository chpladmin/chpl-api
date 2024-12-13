package gov.healthit.chpl;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import gov.healthit.chpl.scheduler.JobAspect;

public class QuartzJobFactory extends SpringBeanJobFactory { //implements ApplicationContextAware {
    private AutowireCapableBeanFactory beanFactory;
    //private ApplicationContext applicationContext;

    //@Override
    //public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    //    beanFactory = ctx.getAutowireCapableBeanFactory();
    //    applicationContext = ctx;
    //}

    public QuartzJobFactory(ApplicationContext applicationContext) {
        beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);

        AspectJProxyFactory pFactory = new AspectJProxyFactory(job);
        pFactory.addAspect(new JobAspect());

        return pFactory.getProxy();
    }

    //public static ApplicationContext getContext() {
    //    return applicationContext;
    //}
}