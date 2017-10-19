package dropzone.controller;

import dropzone.util.UniqueKeyGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UniqueKeyGeneratorController {

    @GetMapping("/")
    public String index(Model model) {
        String uniqueKey =  UniqueKeyGenerator.nextKey();
        model.addAttribute("uniqueKey", uniqueKey);
        return "generate";
    }
}
