package gov.healthit.chpl.shareddata;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class SharedDataProvider<K, V> {
    public static final Integer UNLIMITED = -1;

    private SharedDataDAO sharedDataDAO;
    private ObjectMapper mapper;

    @Autowired
    public SharedDataProvider(SharedDataDAO sharedDataDAO) {
        this.sharedDataDAO = sharedDataDAO;
        this.mapper = new ObjectMapper();
    }

    protected abstract String getDomain();
    protected abstract Class<V> getClazz();
    protected abstract V getFromJson(String json) throws JsonProcessingException;
    protected abstract Integer getTimeToLive();

    public boolean containsKey(K key) {
        return sharedDataDAO.get(getDomain(), key.toString()) != null;
    }

    public V get(K key, Supplier<V> s) {
        SharedData data = sharedDataDAO.get(getDomain(), key.toString());
        V obj = null;
        if (data != null && !isExpired(data)) {
            try {
                LOGGER.info("Retreived from shared data: {} {}", getDomain(), key.toString());
                obj = getFromJson(data.getValue());
            } catch (JsonProcessingException e) {
                LOGGER.error("Could not create object from JSON: {} {}", getDomain(), data.getValue().substring(0, Math.min(data.getValue().length(), 200)), e);
            }

        } else {
            obj = s.get();
            put(key, obj);
            LOGGER.info("Retreived from supplier: {} {}", getDomain(), key.toString());
        }
        return obj;
    }

    private void put(K key, V value) {
        if (containsKey(key)) {
            remove(key);
        }
        try {
            sharedDataDAO.add(SharedData.builder()
                    .domain(getDomain())
                    .key(key.toString())
                    .value(mapper.writeValueAsString(value))
                    .build());
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not write object to JSON: {} {}", getDomain(), key.toString(), e);
        }
    }

    private void remove(K key) {
        sharedDataDAO.remove(getDomain(), key.toString());
    }

    private boolean isExpired(SharedData sharedData) {
        return getTimeToLive() == UNLIMITED
                || sharedData.getPutDate().plusHours(getTimeToLive()).isBefore(LocalDateTime.now());
    }
}
