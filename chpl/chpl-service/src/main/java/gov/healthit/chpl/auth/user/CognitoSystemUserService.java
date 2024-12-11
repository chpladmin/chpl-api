package gov.healthit.chpl.auth.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class CognitoSystemUserService {

    @Getter
    private UUID systemUserUuId;

    @Getter
    private UUID anonymousUserUuId;

    @Autowired
    public CognitoSystemUserService(@Value("${cognito.systemUserUuid}") String systemUserUuId,
            @Value("${cognito.anonymousUserUuid}") String anonymousUserUuId) {
        this.systemUserUuId = UUID.fromString(systemUserUuId);
        this.anonymousUserUuId = UUID.fromString(anonymousUserUuId);
    }
}
