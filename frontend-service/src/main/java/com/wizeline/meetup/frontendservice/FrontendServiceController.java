package com.wizeline.meetup.frontendservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
public class FrontendServiceController {
    private static final Logger log = Logger.getLogger(FrontendServiceController.class.getName());

    @Autowired
    private GcpProjectIdProvider projectIdProvider;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MessagesBackendClient client;

    @Value("${default.image}")
    private String defaultImage;

    @Value(("${gcs.domain}"))
    private String gcsDomain;

    @GetMapping(value = "/hello")
    public String returnHelloMsg() {
        return "Hello !!!!";
    }

    @GetMapping("/")
    public String index(Model model) {

        Collection<Map> messages = client.getMessages("id,desc").getContent();
        model.addAttribute("messages", messages);
        return "index";
    }

    @PostMapping("/post")
    public String post(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam String name, @RequestParam String message, Model model)
            throws IOException {
        log.info("Begin handle submit form .......");
        model.addAttribute("name", name);

        String filename = null;
        if (file != null && !file.isEmpty()
                && file.getContentType().equals("image/jpeg")) {

            // Bucket ID is our Project ID
            String bucket = "gs://" + projectIdProvider.getProjectId();

            // Generate a random file name
            filename = UUID.randomUUID().toString() + ".jpg";
            WritableResource resource = (WritableResource)
                    context.getResource(bucket + "/" + filename);

            // Write the file to Cloud Storage using WritableResource
            try (OutputStream os = resource.getOutputStream()) {
                os.write(file.getBytes());
            }

            filename = gcsDomain +
                    projectIdProvider.getProjectId() + "/" + filename;
        }

        if (message != null && !message.trim().isEmpty()) {
            // Post the message to the backend service
            Map<String, String> payload = new HashMap<>();
            payload.put("name", name);
            payload.put("message", message);

            // Store the generated file name in the database
            filename = (filename == null) ? defaultImage : filename;
            payload.put("imageUri", filename);

            client.add(payload);
        }
        log.info("End handling submit form.");
        return "redirect:/";
    }
}
