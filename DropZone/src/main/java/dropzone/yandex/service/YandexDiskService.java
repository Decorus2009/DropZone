package dropzone.yandex.service;

import dropzone.yandex.YandexDisk;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class YandexDiskService {

    private Map<String, YandexDisk> disks = new HashMap<>();

    public void addDisc(final YandexDisk yandexDisk) {
        disks.put(yandexDisk.getUserDetails().getLogin(), yandexDisk);
    }

    public YandexDisk getDisk(final String login) {
        return disks.get(login);
    }
}
