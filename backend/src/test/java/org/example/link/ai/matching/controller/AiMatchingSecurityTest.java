package org.example.link.ai.matching.controller;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.dto.AnalyzeAiMatchResponse;
import org.example.link.ai.matching.dto.AiMatchResponse;
import org.example.link.ai.matching.dto.MatchCondition;
import org.example.link.ai.matching.service.AiMatchingService;
import org.example.link.ai.matching.service.analysis.AiMatchQueryAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiMatchingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiMatchingService aiMatchingService;

    @MockitoBean
    private AiMatchQueryAnalysisService queryAnalysisService;

    @Test
    void allowsMatchingSearchWithoutAuthentication() throws Exception {
        when(aiMatchingService.match(any())).thenReturn(new AiMatchResponse(
                "Spring 백엔드 개발",
                EmbeddingTargetType.TALENT,
                List.of()
        ));

        mockMvc.perform(post("/ai/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "Spring 백엔드 개발",
                                  "targetType": "TALENT",
                                  "limit": 5
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void allowsMatchingQueryAnalysisWithoutAuthentication() throws Exception {
        when(queryAnalysisService.analyze(any())).thenReturn(new AnalyzeAiMatchResponse(
                "50만원 이하 Spring 백엔드 개발자",
                "Spring 백엔드 개발",
                EmbeddingTargetType.TALENT,
                MatchCondition.empty(),
                null
        ));

        mockMvc.perform(post("/ai/matches/analyze")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "50만원 이하 Spring 백엔드 개발자"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
