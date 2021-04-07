package pt.ulisboa.tecnico.socialsoftware.tournament.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pt.ulisboa.tecnico.socialsoftware.common.config.CommonModuleConfiguration;

@Configuration
@ComponentScan(basePackages = "pt.ulisboa.tecnico.socialsoftware.tournament")
@Import({CommonModuleConfiguration.class})
public class TournamentModuleConfiguration {
}