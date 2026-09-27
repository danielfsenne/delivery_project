package com.rota.auth.infrastructure.seed;

import com.rota.auth.domain.User;
import com.rota.auth.domain.UserRepository;
import com.rota.common.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria contas de demonstração em um banco vazio. A ordem é fixa porque o
 * restaurant-service referencia o dono dos restaurantes de exemplo pelo id 2.
 */
@Component
@ConditionalOnProperty(prefix = "rota.seed", name = "enabled", havingValue = "true")
public class DemoUsersSeeder implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "rota12345";
    private static final Logger log = LoggerFactory.getLogger(DemoUsersSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DemoUsersSeeder(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        users.save(new User("Cliente Demo", "cliente@rota.dev", hash, Role.CUSTOMER, null));
        users.save(new User("Restaurante Demo", "restaurante@rota.dev", hash, Role.RESTAURANT, null));
        users.save(new User("Entregador Demo", "entregador@rota.dev", hash, Role.DRIVER, null));
        users.save(new User("Admin", "admin@rota.dev", hash, Role.ADMIN, null));
        log.info("Usuários de demonstração criados (senha: {})", DEMO_PASSWORD);
    }
}
