package pt.ulisboa.tecnico.socialsoftware.auth.config;

import io.eventuate.tram.sagas.spring.orchestration.SagaOrchestratorConfiguration;
import io.eventuate.tram.spring.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pt.ulisboa.tecnico.socialsoftware.auth.sagas.createUserWithAuth.CreateUserWithAuthSaga;
import pt.ulisboa.tecnico.socialsoftware.auth.services.AuthUserService;

/**
 * The configuration class to instantiate and wire the domain service class.
 */
@Configuration
@Import({SagaOrchestratorConfiguration.class, TramEventsPublisherConfiguration.class})
public class AuthServiceConfiguration {

    /*@Bean
    public AuthUserService authUserService() {return new AuthUserService();}*/

    @Bean
    public CreateUserWithAuthSaga createOrderSaga() {
        return new CreateUserWithAuthSaga();
    }
}