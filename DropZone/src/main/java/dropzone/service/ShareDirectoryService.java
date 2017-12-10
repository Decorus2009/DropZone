package dropzone.service;

import dropzone.repository.entity.UploadDirectory;
import dropzone.repository.entity.UserLogin;
import dropzone.repository.service.UploadDirectoryService;
import dropzone.repository.service.UserLoginService;
import dropzone.util.FileUtils;
import dropzone.util.UniqueKeyGenerator;
import dropzone.yandex.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShareDirectoryService {

    @Autowired
    UploadDirectoryService uploadDirectoryService;

    @Autowired
    UserLoginService userLoginService;

    public String shareDirectory(final UserDetails userDetails, final String dirToShare) {
        UserLogin userLogin = userLoginService.findBy(userDetails.getLogin());
        if (userLogin == null) {
            userLogin = new UserLogin(userDetails.getLogin(), userDetails.getToken());
            userLoginService.save(userLogin);
        } else if (isShared(userLogin, dirToShare)) {
            return getUniqueKey(userLogin, dirToShare);
        }

        final String uniqueKey = UniqueKeyGenerator.nextKey();
        final UploadDirectory uploadDirectory = new UploadDirectory(uniqueKey, dirToShare,
                1_073_741_824L, userLogin);

        // Владу:
        // это закомментил, т.к. нужен лимит на загрузку.
        // Пока пусть будет 1Гб. Потом, будем выбирать лимит руками в выбиралке папок
//        final UploadDirectory uploadDirectory = new UploadDirectory(uniqueKey, dirToShare, userLogin);


        userLogin.getUploadDirectories().add(uploadDirectory);
        uploadDirectoryService.save(uploadDirectory);
        userLoginService.update(userLogin);

        return uniqueKey;
    }

    public boolean isShared(final UserDetails userDetails, final String path) {
        return isShared(userLoginService.findBy(userDetails.getLogin()), path);
    }

    private boolean isShared(final UserLogin userLogin, final String path) {
        return userLogin != null && userLogin.getUploadDirectories().stream().anyMatch(d -> FileUtils.equals(d.getDirectory(), path));
    }

    private String getUniqueKey(final UserLogin userLogin, final String path) {
        return userLogin.getUploadDirectories().stream()
                .filter(d -> FileUtils.equals(d.getDirectory(), path))
                .map(UploadDirectory::getUniqueKey)
                .findFirst()
                .orElse(null);
    }
}
