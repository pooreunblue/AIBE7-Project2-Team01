package org.example.link.domain.portfolio.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.portfolio.dto.CreatePortfolioRequest;
import org.example.link.domain.portfolio.dto.UpdatePortfolioRequest;
import org.example.link.domain.portfolio.dto.PortfolioResponse;
import org.example.link.domain.portfolio.entity.PortfolioEntity;
import org.example.link.domain.portfolio.repository.PortfolioRepository;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserService userService;

    //포트폴리오 생성
    @Transactional
    public PortfolioResponse createPortfolio(
            UUID userId,
            CreatePortfolioRequest request
    ) {
        UserEntity user = userService.getUserEntity(userId);

        PortfolioEntity portfolio = PortfolioEntity.create(
                user,
                request.title(),
                request.description()
        );

        PortfolioEntity savedPortfolio =
                portfolioRepository.save(portfolio);

        return PortfolioResponse.from(savedPortfolio);
    }

    // 내 포트폴리오 조회
    public List<PortfolioResponse> getMyPortfolios(UUID userId) {
        return getUserPortfolios(userId); // 재사용
    }

    //유저 지정 포트폴리오 조회
    public List<PortfolioResponse> getUserPortfolios(UUID userId) {
        return portfolioRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PortfolioResponse::from)
                .toList();
    }

    //포트폴리오 가져오기
    public PortfolioResponse getPortfolio(UUID portfolioId) {
        return PortfolioResponse.from(
                getPortfolioEntity(portfolioId)
        );
    }

    //수정
    @Transactional
    public PortfolioResponse updatePortfolio(
            UUID userId,
            UUID portfolioId,
            UpdatePortfolioRequest request
    ) {
        PortfolioEntity portfolio =
                getPortfolioEntity(portfolioId);

        validateOwner(portfolio, userId);

        portfolio.update(
                request.title(),
                request.description()
        );

        return PortfolioResponse.from(portfolio);
    }

    //삭제
    @Transactional
    public void deletePortfolio(
            UUID userId,
            UUID portfolioId
    ) {
        PortfolioEntity portfolio =
                getPortfolioEntity(portfolioId);

        validateOwner(portfolio, userId);

        portfolioRepository.delete(portfolio);
    }

    //검증 로직
    private PortfolioEntity getPortfolioEntity(UUID portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PORTFOLIO_NOT_FOUND
                        )
                );
    }

    //작성자 검증
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
}