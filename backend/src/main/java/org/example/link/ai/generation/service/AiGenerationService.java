package org.example.link.ai.generation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.link.ai.generation.dto.PostGenerationResponse;
import org.example.link.ai.generation.dto.RequestPostGenerationRequest;
import org.example.link.ai.generation.dto.TalentPostGenerationRequest;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.repository.CategoryRepository;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationService {
    private final ChatModel chatModel;
    private final CategoryRepository categoryRepository;
    private final JsonMapper objectMapper;

    public PostGenerationResponse generateRequest(
            RequestPostGenerationRequest request, MultipartFile image) {
        CategoryEntity category = findCategory(request.categoryId());
        String details = "요청글\n카테고리: %s\n최소 예산: %d원\n최대 예산: %d원\n희망 마감일: %s\n사용자 초안: %s"
                .formatted(category.getName(), request.budgetMin(), request.budgetMax(),
                        valueOrNone(request.dueDate()), request.content());
        return generate(details, image);
    }

    public PostGenerationResponse generateTalent(
            TalentPostGenerationRequest request, MultipartFile image) {
        CategoryEntity category = findCategory(request.categoryId());
        String details = "재능글\n카테고리: %s\n가격: %d원\n예상 작업기간: %d %s\n사용자 초안: %s"
                .formatted(category.getName(), request.price(), request.estimatedDuration(),
                        request.durationUnit(), request.content());
        return generate(details, image);
    }

    private PostGenerationResponse generate(String details, MultipartFile image) {
        String instruction = """
                다음 정보를 바탕으로 한국어 게시글을 전문적이고 구체적으로 작성해 주세요.
                사용자의 의도와 사실은 유지하고, 입력되지 않은 가격·기간·경력·성과를 임의로 만들지 마세요.
                이미지가 제공되면 이미지에서 확인되는 대상과 특징만 자연스럽게 반영하세요.
                결과는 반드시 JSON 하나만 반환하세요. 본문은 마크다운 코드 블록을 적극적으로 사용해서 작성해주세요.
                JSON 형식: {"title":"간결하고 명확한 제목","content":"구체적인 게시글 본문"}
                본문에는 목적, 작업 범위 또는 제공 범위, 필요한 조건과 진행 관련 안내를 포함하세요.

                입력 정보:
                """ + details;

        try {
            UserMessage.Builder message = UserMessage.builder().text(instruction);
            if (image != null && !image.isEmpty()) {
                validateImage(image);
                message.media(new Media(
                        MimeTypeUtils.parseMimeType(image.getContentType()),
                        new ByteArrayResource(image.getBytes()) {
                            @Override
                            public String getFilename() {
                                return image.getOriginalFilename();
                            }
                        }));
            }
            ChatResponse response = chatModel.call(new Prompt(message.build()));
            String result = response.getResult().getOutput().getText();
            return objectMapper.readValue(cleanJson(result), PostGenerationResponse.class);
        } catch (CustomException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            log.warn("AI 게시글 생성 실패", e);
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }
    }

    private CategoryEntity findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateImage(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }

    private String cleanJson(String result) {
        return result.replaceFirst("^```json\\s*", "")
                .replaceFirst("\\s*```$", "").trim();
    }

    private String valueOrNone(Object value) {
        return value == null ? "입력되지 않음" : value.toString();
    }
}
