package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.HandlerInstantiator;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.jsontype.TypeIdResolver;
import tools.jackson.databind.jsontype.TypeResolverBuilder;

@Component
public class SpringBeanJsonHandlerInstantiator extends HandlerInstantiator {

    private ApplicationContext applicationContext;

    @Autowired
    public SpringBeanJsonHandlerInstantiator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public ValueDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
            Class<?> deserClass) {
        try {
            return (ValueDeserializer<?>) applicationContext.getBean(deserClass);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
    }

    @Override
    public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
            Class<?> keyDeserClass) {
        try {
            return (KeyDeserializer) applicationContext.getBean(keyDeserClass);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
    }

    @Override
    public ValueSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
        try {
            return (ValueSerializer<?>) applicationContext.getBean(serClass);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
    }

    @Override
    public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
            Class<?> builderClass) {
        try {
            return (TypeResolverBuilder<?>) applicationContext.getBean(builderClass);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
    }

    @Override
    public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
        try {
            return (TypeIdResolver) applicationContext.getBean(resolverClass);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
    }
}
