package org.example.link.domain.portfolio.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.service.StorageService;
import org.example.link.common.storage.type.FileType;
import org.example.link.domain.portfolio.dto.PortfolioFileResponse;
import org.example.link.domain.portfolio.entity.PortfolioEntity;
import org.example.link.domain.portfolio.entity.PortfolioFileEntity;
import org.example.link.domain.portfolio.repository.PortfolioFileRepository;
import org.example.link.domain.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioFileService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final StorageService storageService;

    @Transactional
    public PortfolioFileResponse uploadFile(
            UUID userId,
            UUID portfolioId,
            MultipartFile file
    ) {
        PortfolioEntity portfolio =
                portfolioRepository.findById(portfolioId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PORTFOLIO_NOT_FOUND
                                )
                        );

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new CustomException(
                    ErrorCode.PORTFOLIO_ACCESS_DENIED
            );
        }

        StoredFile storedFile =
                storageService.upload(
                        file,
                        "portfolios/"
                                + userId
                                + "/"
                                + portfolioId,
                        FileType.PORTFOLIO
                );

        PortfolioFileEntity portfolioFile =
                PortfolioFileEntity.create(
                        portfolio,
                        storedFile.originalFileName(),
                        storedFile.path(),
                        storedFile.url(),
                        storedFile.contentType(),
                        storedFile.fileSize()
                );

        PortfolioFileEntity savedFile =
                portfolioFileRepository.save(portfolioFile);

        setDefaultThumbnailIfAbsent(portfolioId);

        return PortfolioFileResponse.from(savedFile);
    }

    @Transactional(readOnly = true)
    public List<PortfolioFileResponse> getFiles(
            UUID userId,
            UUID portfolioId
    ) {
        PortfolioEntity portfolio =
                findPortfolio(portfolioId);

        validateOwner(
                portfolio,
                userId
        );

        return portfolioFileRepository
                .findAllByPortfolioId(portfolioId)
                .stream()
                .map(PortfolioFileResponse::from)
                .toList();
    }

    @Transactional
    public void deleteFile(
            UUID userId,
            UUID portfolioId,
            UUID fileId
    ) {

        PortfolioEntity portfolio =
                findPortfolio(portfolioId);

        validateOwner(
                portfolio,
                userId
        );

        PortfolioFileEntity file =
                findPortfolioFile(fileId);

        validateFileBelongsToPortfolio(
                file,
                portfolioId
        );

        storageService.delete(
                file.getStoragePath()
        );

        portfolioFileRepository.delete(file);
        portfolioFileRepository.flush();
        setDefaultThumbnailIfAbsent(portfolioId);
    }

    @Transactional

    public PortfolioFileResponse updateFile(
            UUID userId,
            UUID portfolioId,
            UUID fileId,
            MultipartFile newFile
    ){
        PortfolioEntity portfolio =
                findPortfolio(portfolioId);

        validateOwner(
                portfolio,
                userId
        );

        PortfolioFileEntity file =
                findPortfolioFile(fileId);

        validateFileBelongsToPortfolio(
                file,
                portfolioId);

        StoredFile storedFile =
                storageService.upload(
                        newFile,
                        "portfolios/"
                                + userId
                                + "/"
                                + portfolioId,
                        FileType.PORTFOLIO
                );

        storageService.delete(
                file.getStoragePath()
        );

        file.updateFile(
                storedFile.originalFileName(),
                storedFile.path(),
                storedFile.url(),
                storedFile.contentType(),
                storedFile.fileSize()
        );

        if (!isImageFile(file)) {
            file.unsetThumbnail();
        }
        setDefaultThumbnailIfAbsent(portfolioId);

        return PortfolioFileResponse.from(file);
    }

    @Transactional
    public PortfolioFileResponse changeThumbnail(
            UUID userId,
            UUID portfolioId,
            UUID fileId
    ){
        PortfolioEntity portfolio =
                findPortfolio(portfolioId);
        validateOwner(
                portfolio,
                userId
        );
        PortfolioFileEntity file =
                findPortfolioFile(fileId);
        validateFileBelongsToPortfolio(
                file,
                portfolioId
        );
        if (!file.getContentType().startsWith("image/")) {
            throw new CustomException(
                    ErrorCode.INVALID_THUMBNAIL
            );
        }
        portfolioFileRepository
                .findByPortfolioIdAndThumbnailTrue(portfolioId)
                .ifPresent(
                        PortfolioFileEntity::unsetThumbnail
                );
        file.setThumbnail();

        return PortfolioFileResponse.from(file);
    }

    private PortfolioEntity findPortfolio(UUID portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PORTFOLIO_NOT_FOUND
                        )
                );
    }

    private PortfolioFileEntity findPortfolioFile(UUID fileId) {
        return portfolioFileRepository.findById(fileId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PORTFOLIO_FILE_NOT_FOUND
                        )
                );
    }

    private void validateOwner(
            PortfolioEntity portfolio,
            UUID userId
    ) {
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new CustomException(
                    ErrorCode.PORTFOLIO_ACCESS_DENIED
            );
        }
    }

    private void validateFileBelongsToPortfolio(
            PortfolioFileEntity file,
            UUID portfolioId
    ) {
        if (!file.getPortfolio().getId().equals(portfolioId)) {
            throw new CustomException(
                    ErrorCode.INVALID_PORTFOLIO_FILE
            );
        }
    }

    private void setDefaultThumbnailIfAbsent(UUID portfolioId) {
        if (portfolioFileRepository
                .findByPortfolioIdAndThumbnailTrue(portfolioId)
                .isPresent()) {
            return;
        }

        portfolioFileRepository
                .findAllByPortfolioIdOrderByIdAsc(portfolioId)
                .stream()
                .filter(this::isImageFile)
                .findFirst()
                .ifPresent(PortfolioFileEntity::setThumbnail);
    }

    private boolean isImageFile(PortfolioFileEntity file) {
        return file.getContentType() != null
                && file.getContentType().startsWith("image/");
    }
}
