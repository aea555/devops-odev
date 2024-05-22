package com.aea.project2z.controllers;

import com.aea.project2z.models.Person;
import com.aea.project2z.repositories.PersonRepository;
import com.aea.project2z.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;


@Controller
public class HomeController {
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    private final Environment environment;
    private final FileStorageService localFileStorageService;
    private final FileStorageService amazonS3FileStorageService;

    public HomeController(Environment environment, FileStorageService localFileStorageService, FileStorageService amazonS3FileStorageService) {
        this.environment = environment;
        this.localFileStorageService = localFileStorageService;
        this.amazonS3FileStorageService = amazonS3FileStorageService;
    }

    @GetMapping("/")
    public String getUsers(Model model){
        List<Person> ulist = personRepository.findAll();
        model.addAttribute("ulist", ulist);
        return "index";
    }

    @GetMapping("/add")
    public String addPage(Model model) {
        List<Person> ulist = personRepository.findAll();
        model.addAttribute("ulist", ulist);
        return "add";
    }

    @PostMapping("/add")
    public String addPerson(Person person) {
        personRepository.save(person);
        return "redirect:/";
    }

    @GetMapping("/delete")
    public String deletePage(Model model) {
        List<Person> ulist = personRepository.findAll();
        model.addAttribute("ulist", ulist);
        return "delete";
    }

    @PostMapping("/delete")
    public String deletePersons(@RequestParam("selectedIds") List<Integer> selectedIds) {
        personRepository.deleteAllByIdInBatch(selectedIds);
        return "redirect:/";
    }

    @GetMapping("/update")
    public String updatePage(Model model) {
        List<Person> ulist = personRepository.findAll();
        model.addAttribute("ulist", ulist);
        return "update";
    }

    @PostMapping("/update")
    public String updatePersons(@RequestParam("ids") List<Integer> ids,
                                @RequestParam("newNames") List<String> newNames,
                                @RequestParam("newAddresses") List<String> newAddresses,
                                @RequestParam("file") MultipartFile[] files) {
        String activeProfile = environment.getActiveProfiles()[0];
        FileStorageService fileStorageService = getFileStorageService(activeProfile);

        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            String newName = newNames.get(i);
            String newAddress = newAddresses.get(i);
            Person person = personRepository.findById(id).orElse(null);
            if (person != null) {
                person.setName(newName);
                person.setAddress(newAddress);
                if (files != null && files.length > 0 && !files[i].isEmpty()) {
                    try {
                        // Upload the file using the appropriate file storage service
                        String imgUrl = fileStorageService.storeFile(files[i]);
                        person.setImgUrl(imgUrl);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                personRepository.save(person);
            }
        }
        return "redirect:/";
    }

    private FileStorageService getFileStorageService(String activeProfile) {
        if ("prod".equals(activeProfile)) {
            return amazonS3FileStorageService;
        } else {
            return localFileStorageService;
        }
    }
}
