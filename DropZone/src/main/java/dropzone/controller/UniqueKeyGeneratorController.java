package dropzone.controller;

import dropzone.util.UniqueKeyGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для обработки запроса localhost:8080
 */
@Controller
public class UniqueKeyGeneratorController {

    /**
     * Генерит uniqueKey и возвращает view для отображения сгенерированного uniqueKey и
     * гиперссылки "/upload/{uniqueKey}
     * @param model
     * @return view generate.html
     */
    @GetMapping("/")
    public String index(Model model) {
        String uniqueKey =  UniqueKeyGenerator.nextKey();
        model.addAttribute("uniqueKey", uniqueKey);
        return "generate";
    }
}
