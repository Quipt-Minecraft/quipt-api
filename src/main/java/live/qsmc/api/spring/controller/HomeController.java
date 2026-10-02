package live.qsmc.api.spring.controller;

import live.qsmc.quipt.core.utils.net.ApiResponse;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    private final Resource indexHtml = new ClassPathResource("static/index.html");
    private final Resource docsHtml = new ClassPathResource("static/docs.html");
    private final DataController dataController;

    public HomeController(DataController dataController) {
        this.dataController = dataController;
    }

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> index() {
        if (!indexHtml.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtml);
    }

    @GetMapping(value = {"/docs", "/docs.html", "/docs/"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> docs() {
        if (!docsHtml.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(docsHtml);
    }

    @GetMapping(value = {"/status", "/uptime"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ApiResponse<JSONObject> status() {
        return dataController.status();
    }
}
