package dropzone;

import dropzone.repository.entity.UploadDirectory;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UploadDirectoryService;
import dropzone.repository.service.UserLoginService;
import dropzone.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(UserLoginService userLoginService, UploadDirectoryService uploadDirectoryService,
                           StorageService storageService) {
        return (args) -> {
            UserLogin userLogin = new UserLogin();
            userLogin.setLogin("DropZoneCSC");
            userLogin.setToken("AQAAAAAhTokfAASbsJGzj3ck2kDIhsnFw4FtY_Q");

            UploadDirectory uploadDirectory = new UploadDirectory();
            uploadDirectory.setUniqueKey("1a2b3c4d5e");
            uploadDirectory.setDirectory("disk:/");

            userLogin.setUploadDirectory(uploadDirectory);
            uploadDirectory.setUserLogin(userLogin);

            userLoginService.save(userLogin);
            uploadDirectoryService.save(uploadDirectory);

            storageService.deleteAll();
            storageService.init();
        };
    }
}