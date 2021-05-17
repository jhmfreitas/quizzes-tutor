package pt.ulisboa.tecnico.socialsoftware.auth.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.auth.services.FenixEduInterface;
import pt.ulisboa.tecnico.socialsoftware.auth.services.AuthUserService;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.auth.AuthDto;
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.TutorException;

import static pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage.INVALID_LOGIN_CREDENTIALS;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthUserService authUserService;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${oauth.consumer.key}")
    private String oauthConsumerKey;

    @Value("${oauth.consumer.secret}")
    private String oauthConsumerSecret;

    @Value("${callback.url}")
    private String callbackUrl;

    @GetMapping("/auth/fenix")
    public AuthDto fenixAuth(@RequestParam String code) {
        FenixEduInterface fenix = new FenixEduInterface(baseUrl, oauthConsumerKey, oauthConsumerSecret, callbackUrl);
        fenix.authenticate(code);
        return this.authUserService.fenixAuth(fenix);
    }

    @GetMapping("/auth/external")
    public AuthDto externalUserAuth(@RequestParam String email, @RequestParam String password) {
        try {
            return authUserService.externalUserAuth(email, password);
        } catch (TutorException e) {
            throw new TutorException(INVALID_LOGIN_CREDENTIALS);
        }
    }

    @GetMapping("/auth/demo/student")
    public AuthDto demoStudentAuth(@RequestParam Boolean createNew) {
        logger.info("Received demoStudentAuth request with createNew: " + createNew);
        return this.authUserService.demoStudentAuth(createNew);
    }

    @GetMapping("/auth/demo/teacher")
    public AuthDto demoTeacherAuth() {
        return this.authUserService.demoTeacherAuth();
    }

    @GetMapping("/auth/demo/admin")
    public AuthDto demoAdminAuth() {
        return this.authUserService.demoAdminAuth();
    }

}