package javamid.auth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClientSetupService {

  @Autowired
  private RegisteredClientRepository clientRepository;   // этот репозиторий создан спрингом


  @PostConstruct
  public void setupClients() {
    if (clientRepository.findByClientId("exgen") == null) {
      RegisteredClient exgenClient = RegisteredClient.withId(UUID.randomUUID().toString())
              .clientId("exgen")
              .clientSecret("{bcrypt}$2a$12$GtWkz.bCYkyW91pvVAZ6I.LIz.OQ6prtpBOArgObN1Qo/V7TFrZS.")   // https://bcrypt-generator.com/ exgenPassword
              .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
              .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
              .scope("write")
              .build();

      clientRepository.save(exgenClient);
      System.out.println("Created exgen client");
    } else {
      System.out.println("exgen client already exists");
    }
  }


}