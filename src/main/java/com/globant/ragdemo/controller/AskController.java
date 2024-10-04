package com.globant.ragdemo.controller;

import com.globant.ragdemo.service.IRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ask")
@CrossOrigin
@RequiredArgsConstructor
public class AskController {

    private final IRagService ragService;

    @GetMapping()
    public String query(@RequestParam(value = "q", defaultValue = "Tell me anything") String question) {
        return ragService.askLlm(question);
    }

}
