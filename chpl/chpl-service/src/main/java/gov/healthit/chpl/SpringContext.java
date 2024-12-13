package gov.healthit.chpl;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.scheduler.JobAspect;

@Component
public class SpringContext extends SpringBeanJobFactory implements ApplicationContextAware {

    private static ApplicationContext context;
    private AutowireCapableBeanFactory beanFactory;

    /**
     * Returns the Spring managed bean instance of the given class type if it exists. Returns null otherwise.
     *
     * @param beanClass
     * @return
     */
    public static <T extends Object> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {

        // store ApplicationContext reference to access required beans later on
        SpringContext.context = appContext;
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);

        AspectJProxyFactory pFactory = new AspectJProxyFactory(job);
        pFactory.addAspect(new JobAspect());

        return pFactory.getProxy();
    }

}
