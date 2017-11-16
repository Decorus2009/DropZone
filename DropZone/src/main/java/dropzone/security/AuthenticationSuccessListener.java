package dropzone.security;

import dropzone.util.DropzoneLog;
import dropzone.yandex.YandexDisk;
import dropzone.yandex.service.YandexDiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    @Qualifier("oauth2ClientContext")
    @Autowired
    private OAuth2ClientContext oauth2Context;

    @Autowired
    YandexDiskService yandexDiskService;

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        DropzoneLog.info("User " + event.getAuthentication().getName() + " logged in");
        yandexDiskService.addDisc(
                new YandexDisk(event.getAuthentication().getName(), oauth2Context.getAccessToken().getValue()));
    }
}

