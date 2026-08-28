package org.example.link.domain.request.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.service.StorageService;
import org.example.link.common.storage.type.FileType;
import org.example.link.domain.request.dto.RequestPostFileResponse;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.entity.RequestPostFileEntity;
import org.example.link.domain.request.repository.RequestPostFileRepository;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestPostFileService {
    private final RequestPostRepository requestPostRepository;
    private final RequestPostFileRepository requestPostFileRepository;
    private final StorageService storageService;

    @Transactional
    public RequestPostFileResponse uploadFile(CustomUserDetails user, UUID postId, MultipartFile file) {
        RequestPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        StoredFile stored = storageService.upload(file, "requests/" + user.getUserId() + "/" + postId, FileType.PORTFOLIO);
        RequestPostFileEntity saved = requestPostFileRepository.save(RequestPostFileEntity.create(
                post, stored.originalFileName(), stored.path(), stored.url(), stored.contentType(), stored.fileSize()));
        setDefaultThumbnailIfAbsent(postId);
        return RequestPostFileResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RequestPostFileResponse> getFiles(CustomUserDetails user, UUID postId) {
        RequestPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        return requestPostFileRepository.findAllByRequestPostId(postId).stream()
                .map(RequestPostFileResponse::from).toList();
    }

    @Transactional
    public void deleteFile(CustomUserDetails user, UUID postId, UUID fileId) {
        RequestPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        RequestPostFileEntity file = findFile(fileId);
        validateBelongsToPost(file, postId);
        storageService.delete(file.getStoragePath());
        requestPostFileRepository.delete(file);
        requestPostFileRepository.flush();
        setDefaultThumbnailIfAbsent(postId);
    }

    @Transactional
    public RequestPostFileResponse updateFile(CustomUserDetails user, UUID postId, UUID fileId, MultipartFile newFile) {
        RequestPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        RequestPostFileEntity file = findFile(fileId);
        validateBelongsToPost(file, postId);
        StoredFile stored = storageService.upload(newFile, "requests/" + user.getUserId() + "/" + postId, FileType.PORTFOLIO);
        storageService.delete(file.getStoragePath());
        file.updateFile(stored.originalFileName(), stored.path(), stored.url(), stored.contentType(), stored.fileSize());
        if (!isImageFile(file)) {
            file.unsetThumbnail();
        }
        setDefaultThumbnailIfAbsent(postId);
        return RequestPostFileResponse.from(file);
    }

    @Transactional
    public RequestPostFileResponse changeThumbnail(CustomUserDetails user, UUID postId, UUID fileId) {
        RequestPostEntity post = findPost(postId);
        validateOwner(post, user.getUserId());
        RequestPostFileEntity file = findFile(fileId);
        validateBelongsToPost(file, postId);
        if (!isImageFile(file)) {
            throw new CustomException(ErrorCode.INVALID_THUMBNAIL);
        }

        requestPostFileRepository.findByRequestPostIdAndThumbnailTrue(postId)
                .ifPresent(RequestPostFileEntity::unsetThumbnail);
        file.setThumbnail();

        return RequestPostFileResponse.from(file);
    }

    private RequestPostEntity findPost(UUID postId) {
        return requestPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private RequestPostFileEntity findFile(UUID fileId) {
        return requestPostFileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateOwner(RequestPostEntity post, UUID userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    private void validateBelongsToPost(RequestPostFileEntity file, UUID postId) {
        if (!file.getRequestPost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.INVALID_POST_FILE);
        }
    }

    private void setDefaultThumbnailIfAbsent(UUID postId) {
        if (requestPostFileRepository.findByRequestPostIdAndThumbnailTrue(postId).isPresent()) {
            return;
        }

        requestPostFileRepository.findAllByRequestPostIdOrderByIdAsc(postId)
                .stream()
                .filter(this::isImageFile)
                .findFirst()
                .ifPresent(RequestPostFileEntity::setThumbnail);
    }

    private boolean isImageFile(RequestPostFileEntity file) {
        return file.getContentType() != null && file.getContentType().startsWith("image/");
    }
}
