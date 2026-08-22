package yu.spring.gyeongsanlog.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.place.domain.Favorite;
import yu.spring.gyeongsanlog.place.repository.FavoriteRepository;
import yu.spring.gyeongsanlog.place.repository.PlaceRepository;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    // 찜 상태면 취소, 아니면 찜.
    @Transactional
    public boolean toggleFavorite(Long userId, Long placeId) {
        if (favoriteRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            favoriteRepository.deleteByUserIdAndPlaceId(userId, placeId);
            return false;
        }

        if (!placeRepository.existsById(placeId)) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
        }

        Favorite favorite = Favorite.builder()
                .user(userRepository.getReferenceById(userId))
                .place(placeRepository.getReferenceById(placeId))
                .build();

        favoriteRepository.save(favorite);
        return true;
    }
}
