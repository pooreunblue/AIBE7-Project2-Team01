package org.example.link.domain.talent.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.talent.service.TalentPostService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/talents")
public class TalentPostController {
    private final TalentPostService talentPostService;
}
