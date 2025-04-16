package gov.healthit.chpl.sharedstore.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.sharedstore.SharedStore;
import gov.healthit.chpl.sharedstore.SharedStoreDAO;
import gov.healthit.chpl.sharedstore.SharedStoreProvider;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SharedUserStoreProvider extends SharedStoreProvider<String, User> {
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public SharedUserStoreProvider(SharedStoreDAO sharedStoreDAO) {
        super(sharedStoreDAO);
    }

    @Override
    public User get(String key, Supplier<User> s) {
        return super.get(key, s);
    }

    public List<User> getAll() {
        Date start = new Date();
        System.out.println("Getting all users from Shared Store");
        List<User> allUsers = new ArrayList<User>();
        List<SharedStore> results = sharedStoreDAO.getAll(getDomain());
        if (CollectionUtils.isEmpty(results)) {
            return allUsers;
        }
        results.stream()
            .filter(result -> result != null && !isExpired(result))
            .forEach(result -> addToUsers(result, allUsers));
        Date end = new Date();
        System.out.println("Got and converted all user JSON in " + (end.getTime() - start.getTime()) + "ms. " + allUsers.size() + " users found.");
        return allUsers;
    }

    private void addToUsers(SharedStore sharedStoreResult, List<User> allUsers) {
        try {
            allUsers.add(getFromJson(sharedStoreResult.getValue()));
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not create object from JSON: {} {}", getDomain(),
                    sharedStoreResult.getValue().substring(0, Math.min(sharedStoreResult.getValue().length(), MAX_JSON_LENGTH)), e);
        }
    }

    public void putAll(List<User> users) {
        users.stream()
            .forEach(user -> put(user.getCognitoId().toString(), user));
    }

    @Override
    protected String getDomain() {
        return User.class.getName();
    }

    @Override
    protected Class<User> getClazz() {
        return User.class;
    }

    @Override
    protected User getFromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, User.class);
    }

    @Override
    protected Integer getTimeToLive() {
        return SharedUserStoreProvider.UNLIMITED;
    }
}
