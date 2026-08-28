package org.example.link.domain.talent.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.service.StorageService;
import org.example.link.common.storage.type.FileType;
import org.example.link.domain.talent.dto.TalentPostFileResponse;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.entity.TalentPostFileEntity;
import org.example.link.domain.talent.repository.TalentPostFileRepository;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentPostFileService {
    private final TalentPostRepository talentPostRepository;
    private final TalentPostFileRepository talentPostFileRepository;
    private final StorageService storageService;

    @Transactional
    public TalentPostFileResponse uploadFile(CustomUserDetails user, UUID postId, MultipartFile file) {
        TalentPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        StoredFile stored = storageService.upload(file, "talents/" + user.getUserId() + "/" + postId, FileType.PORTFOLIO);
        TalentPostFileEntity saved = talentPostFileRepository.save(TalentPostFileEntity.create(
                post, stored.originalFileName(), stored.path(), stored.url(), stored.contentType(), stored.fileSize()));
        setDefaultThumbnailIfAbsent(postId);
        return TalentPostFileResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TalentPostFileResponse> getFiles(CustomUserDetails user, UUID postId) {
        TalentPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        return talentPostFileRepository.findAllByTalentPostId(postId).stream()
                .map(TalentPostFileResponse::from).toList();
    }

    @Transactional
    public void deleteFile(CustomUserDetails user, UUID postId, UUID fileId) {
        TalentPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        TalentPostFileEntity file = findFile(fileId);
        validateBelongsToPost(file, postId);
        storageService.delete(file.getStoragePath());
        talentPostFileRepository.delete(file);
        talentPostFileRepository.flush();
        setDefaultThumbnailIfAbsent(postId);
    }

    @Transactional
    public TalentPostFileResponse updateFile(CustomUserDetails user, UUID postId, UUID fileId, MultipartFile newFile) {
        TalentPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        TalentPostFileEntity file = findFile(fileId);
        validateBelongsToPost(file, postId);
        StoredFile stored = storageService.upload(newFile, "talents/" + user.getUserId() + "/" + postId, FileType.PORTFOLIO);
        storageService.delete(file.getStoragePath());
        file.updateFile(stored.originalFileName(), stored.path(), stored.url(), stored.contentType(), stored.fileSize());
        if (!isImageFile(file)) {
            file.unsetThumbnail();
        }
        setDefaultThumbnailIfAbsent(postId);
        return TalentPostFileResponse.from(file);
    }

    @Transactional
    public TalentPostFileResponse changeThumbnail(CustomUserDetails user, UUID postId, UUID fileId) {
        TalentPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        TalentPostFileEntity file = findFile(fileId);
        validateBelongsToPost(file, postId);
        if (!isImageFile(file)) {
            throw new CustomException(ErrorCode.INVALID_THUMBNAIL);
        }

        talentPostFileRepository.findByTalentPostIdAndThumbnailTrue(postId)
                .ifPresent(TalentPostFileEntity::unsetThumbnail);
        file.setThumbnail();

        return TalentPostFileResponse.from(file);
    }

    private TalentPostEntity findPost(UUID postId) {
        return talentPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private TalentPostFileEntity findFile(UUID fileId) {
        return talentPostFileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateOwner(TalentPostEntity post, UUID userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    private void validateBelongsToPost(TalentPostFileEntity file, UUID postId) {
        if (!file.getTalentPost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.INVALID_POST_FILE);
        }
    }

    private void setDefaultThumbnailIfAbsent(UUID postId) {
        if (talentPostFileRepository.findByTalentPostIdAndThumbnailTrue(postId).isPresent()) {
            return;
        }

        talentPostFileRepository.findAllByTalentPostIdOrderByIdAsc(postId)
                .stream()
                .filter(this::isImageFile)
                .findFirst()
                .ifPresent(TalentPostFileEntity::setThumbnail);
    }

    private boolean isImageFile(TalentPostFileEntity file) {
        return file.getContentType() != null && file.getContentType().startsWith("image/");
    }
}
