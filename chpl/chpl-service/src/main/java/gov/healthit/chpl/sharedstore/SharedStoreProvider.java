package gov.healthit.chpl.sharedstore;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class SharedStoreProvider<K, V> {
    public static final Integer UNLIMITED = -1;
    public static final Integer MAX_JSON_LENGTH = 200;

    private SharedStoreDAO sharedStoreDAO;
    private ObjectMapper mapper;

    @Autowired
    public SharedStoreProvider(SharedStoreDAO sharedStoreDAO) {
        this.sharedStoreDAO = sharedStoreDAO;
        this.mapper = new ObjectMapper();
    }

    protected abstract String getDomain();
    protected abstract Class<V> getClazz();
    protected abstract V getFromJson(String json) throws JsonProcessingException;
    protected abstract Integer getTimeToLive();

    public boolean containsKey(K key) {
        return sharedStoreDAO.get(getDomain(), key.toString()) != null;
    }

    public V get(K key, Supplier<V> s) {
        SharedStore data = sharedStoreDAO.get(getDomain(), key.toString());
        V obj = null;
        if (data != null && !isExpired(data)) {
            try {
                LOGGER.info("Retreived from shared data: {} {}", getDomain(), key.toString());
                obj = getFromJson(data.getValue());
            } catch (JsonProcessingException e) {
                LOGGER.error("Could not create object from JSON: {} {}", getDomain(), data.getValue().substring(0, Math.min(data.getValue().length(), MAX_JSON_LENGTH)), e);
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
            sharedStoreDAO.add(SharedStore.builder()
                    .domain(getDomain())
                    .key(key.toString())
                    .value(mapper.writeValueAsString(value))
                    .build());
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not write object to JSON: {} {}", getDomain(), key.toString(), e);
        }
    }

    private void remove(K key) {
        sharedStoreDAO.remove(getDomain(), key.toString());
    }

    private boolean isExpired(SharedStore sharedData) {
        return getTimeToLive().equals(UNLIMITED)
                || sharedData.getPutDate().plusHours(getTimeToLive()).isBefore(LocalDateTime.now());
    }
}
