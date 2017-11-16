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
        final UploadDirectory uploadDirectory = new UploadDirectory(
                uniqueKey,
                dirToShare,
                userLogin
        );

        userLogin.getUploadDirectories().add(uploadDirectory);
        uploadDirectoryService.save(uploadDirectory);
        userLoginService.update(userLogin);

        return uniqueKey;
    }

    private boolean isShared(final UserLogin userLogin, final String path) {
        return userLogin.getUploadDirectories().stream()
                .filter(d -> FileUtils.equals(d.getDirectory(), path))
                .count() > 0;
    }

    private String getUniqueKey(final UserLogin userLogin, final String path) {
        return userLogin.getUploadDirectories().stream()
                .filter(d -> FileUtils.equals(d.getDirectory(), path))
                .map(UploadDirectory::getUniqueKey)
                .findFirst()
                .orElse(null);
    }
}