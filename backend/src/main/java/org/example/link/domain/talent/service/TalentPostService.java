package org.example.link.domain.talent.service;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TalentPostService {
    private final TalentPostRepository talentPostRepository;
}
