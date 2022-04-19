package gov.healthit.chpl.sharedstorage;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class SharedDataProvider<K, V> {

    private SharedDataDAO sharedDataDAO;
    private ObjectMapper mapper;

    @Autowired
    public SharedDataProvider(SharedDataDAO sharedDataDAO) {
        this.sharedDataDAO = sharedDataDAO;
        this.mapper = new ObjectMapper();
    }


    public abstract String getType();
    public abstract Class<V> getClazz();
    public abstract V getFromJson(String json);

    public void clean() {

    }

    public void clear() {

    }

    public boolean containsKey(K key) {
        return sharedDataDAO.get(getType(), key.toString()) != null;
    }

    public V get(K key, Supplier<V> s) {
        SharedData data = sharedDataDAO.get(getType(), key.toString());
        V obj;
        if (data != null) {
            obj = getFromJson(data.getValue());
            LOGGER.info("Retreived from shared data: {} {}", getClazz().getName(), key.toString());
        } else {
            obj = s.get();
            put(key, obj);
            LOGGER.info("Retreived from supplier: {} {}", getClazz().getName(), key.toString());
        }
        return obj;
    }

    private void put(K key, V value) {
        if (containsKey(key)) {
            remove(key);
        }
        try {
            sharedDataDAO.add(SharedData.builder()
                    .type(getType())
                    .key(key.toString())
                    .value(mapper.writeValueAsString(value))
                    .build());
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void remove(K key) {
        sharedDataDAO.remove(getType(), key.toString());
    }
}
