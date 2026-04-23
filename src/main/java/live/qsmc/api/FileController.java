package live.qsmc.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/files")
class FileController {

    @GetMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public Greeting upload() {
        return new Greeting();
    }

    @RequestMapping("/download")
    public String download() {
        return "download";
    }

}
