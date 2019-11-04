package gov.healthit.chpl;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import gov.healthit.chpl.validation.listing.ListingValidatorFactory;

@Profile("ListingValidatorMock")
@Configuration
public class ListingValidatorFactoryConfiguration {
    @Bean
    @Primary
    public ListingValidatorFactory listingValidatorFactory() {
        return Mockito.mock(ListingValidatorFactory.class);
    }
}
