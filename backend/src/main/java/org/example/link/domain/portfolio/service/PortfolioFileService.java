package org.example.link.domain.portfolio.service;

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

@Service
@RequiredArgsConstructor
public class PortfolioFileService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final StorageService storageService;

    @Transactional
    public PortfolioFileResponse uploadFile(
            Long userId,
            Long portfolioId,
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

        return PortfolioFileResponse.from(savedFile);
    }

    @Transactional
    public void deleteFile(
            Long userId,
            Long portfolioId,
            Long fileId
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
    }

    @Transactional
    public void changeThumbnail(
            Long userId,
            Long portfolioId,
            Long fileId
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
    }

    private PortfolioEntity findPortfolio(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PORTFOLIO_NOT_FOUND
                        )
                );
    }

    private PortfolioFileEntity findPortfolioFile(Long fileId) {
        return portfolioFileRepository.findById(fileId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PORTFOLIO_FILE_NOT_FOUND
                        )
                );
    }

    private void validateOwner(
            PortfolioEntity portfolio,
            Long userId
    ) {
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new CustomException(
                    ErrorCode.PORTFOLIO_ACCESS_DENIED
            );
        }
    }

    private void validateFileBelongsToPortfolio(
            PortfolioFileEntity file,
            Long portfolioId
    ) {
        if (!file.getPortfolio().getId().equals(portfolioId)) {
            throw new CustomException(
                    ErrorCode.INVALID_PORTFOLIO_FILE
            );
        }
    }
}