package live.qsmc.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/data")
class DataController {

    @GetMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Greeting update() {
        System.exit(0);
        return new Greeting();
    }

    @RequestMapping("/download")
    public String download() {
        return "download";
    }

}
