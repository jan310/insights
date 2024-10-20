package jan.ondra.insights.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@Profile("test")
class SecurityTestConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .cors { it.disable() }
            .csrf { it.disable() }
            .build()
    }

}
