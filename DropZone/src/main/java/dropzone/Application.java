package dropzone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // добавить лимит в базу, сколько еще можно загрузить
    // выбрать папку, генерится uniqueKey, и в базу
    // дефолтный лимит на папку пока - 1 Гб, проверять, что место есть

    // TODO в CommandLineRunner удалялись временные папки
//    @Bean
//    CommandLineRunner init(UserLoginService userLoginService, UploadDirectoryService uploadDirectoryService,
//                           StorageService storageService) {
//        return (args) -> {
//            UserLogin userLogin = new UserLogin();
//            userLogin.setLogin("DropZoneCSC");
//            userLogin.setToken("");
//
//            UploadDirectory uploadDirectory = new UploadDirectory();
//            uploadDirectory.setUniqueKey("1a2b3c4d5e");
//            uploadDirectory.setDirectory("disk:/");
//            uploadDirectory.setByteLimit(1_073_741_824L);
//
//            userLogin.addUploadDirectory(uploadDirectory);
//            uploadDirectory.setUserLogin(userLogin);
//
//            uploadDirectoryService.save(uploadDirectory);
//            userLoginService.save(userLogin);
//
//            storageService.deleteAll();
//            storageService.init();
//        };
//    }
}