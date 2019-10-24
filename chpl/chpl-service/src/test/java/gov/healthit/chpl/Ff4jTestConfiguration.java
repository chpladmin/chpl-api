package gov.healthit.chpl;

import org.ff4j.FF4j;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("Ff4jMock")
@Configuration
public class Ff4jTestConfiguration {

    @Bean
    @Primary
    public FF4j ff4j() {
        return Mockito.mock(FF4j.class);
    }
}
