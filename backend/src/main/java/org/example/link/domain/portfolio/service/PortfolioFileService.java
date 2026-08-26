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
        // 파일 조회
        // 포트폴리오/소유권 검증
        // Storage 삭제
        // DB 삭제
    }
}