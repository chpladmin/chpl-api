package gov.healthit.chpl.sharedstore.user;

import java.util.ArrayList;
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

    @Override
    public void put(String key, User value) {
        //NOTE about value != null check:
        //In lower envs, we sometimes load users that we don't have access to (if they have done things in prod)
        //and until we figure that out, those users end up as null values in the shared store
        //which causes things to break. So we will not put any null values in the shared store
        //
        //NOTE about value.getAccountEnabled() check
        //We expect the User Shared Store to always contain only the active users for the current environment.
        //When loading activity, we "get" users by cognito ID per activity loaded. Most of those users will already
        //be in the shared store since we always keep it fully loaded with all active users. However, some activity inevitably
        //will have users that have been disabled at some point. We do not want those disabled users requested for
        //activity display to end up in the shared store. So here, we check that any user is enabled before
        //we "put" it.
        if (value != null && value.getAccountEnabled()) {
            super.put(key, value);
        }
    }

    public List<User> getAll() {
        List<User> allUsers = new ArrayList<User>();
        List<SharedStore> results = sharedStoreDAO.getAll(getDomain());
        if (CollectionUtils.isEmpty(results)) {
            return allUsers;
        }
        results.stream()
            .filter(result -> result != null && !isExpired(result))
            .forEach(result -> addToUsers(result, allUsers));
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
