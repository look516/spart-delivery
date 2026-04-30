package com.sparta.spartadelivery.menu.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.menu.domain.vo.MoneyVO;
import com.sparta.spartadelivery.menu.exception.MenuErrorCode;
import com.sparta.spartadelivery.menu.presentation.dto.request.MenuCreateRequest;
import com.sparta.spartadelivery.menu.presentation.dto.request.MenuUpdateRequest;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuDetailResponse;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuListResponse;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuUpdateResponse;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final MenuPermissionPolicy menuPermissionPolicy;
    private final MenuManagePermissionPolicy menuManagePermissionPolicy;

    /**
     * 메뉴 등록
     */
    @Transactional
    public MenuDetailResponse createMenu(UUID storeId, MenuCreateRequest request, UserPrincipal requester) {
        // 1. 가게 존재 여부 및 폐업 상태 확인 (getStore 내부에서 처리)
        Store store = getStore(storeId);

        // 2. 관리 권한 통합 검증 (가게 상태 + 유저 권한 체크)
        menuManagePermissionPolicy.validateManagePermission(requester, store);

        // 3. 데이터 유효성 검증
        validateMenuData(request);

        // 4. 메뉴 생성 및 저장
        Menu menu = createMenuEntity(storeId, request);
        Menu savedMenu = menuRepository.save(menu);

        return MenuDetailResponse.from(savedMenu);
    }

    /**
     * 메뉴 목록 조회
     */
    @Transactional
    public List<MenuListResponse> getMenusByRole(UUID storeId, UserPrincipal requester) {
        Store store = getStore(storeId);
        MenuViewScope scope = menuPermissionPolicy.getMenuListScope(requester, store);

        List<Menu> menus = switch (scope) {
            case ALL ->  menuRepository.findAllByStoreId(storeId);
            case ACTIVE_ONLY -> menuRepository.findAllByStoreIdAndDeletedAtIsNullAndIsHiddenFalse(storeId);
        };

        return menus.stream()
                .map(MenuListResponse::from)
                .toList();
    }

    /**
     * 메뉴 상세 조회
     */
    @Transactional
    public MenuDetailResponse getMenuByRole(UUID menuId, UserPrincipal requester) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new AppException(MenuErrorCode.MENU_NOT_FOUND));

        Store store = getStore(menu.getStoreId());

        // 권한 정책 적용
        menuPermissionPolicy.validateMenuDetailPermission(requester, store, menu);

        return MenuDetailResponse.from(menu);
    }

    /**
     * 메뉴 삭제
     */
    @Transactional
    public void deleteMenu(UUID menuId, UserPrincipal requester) {
        // 1. 메뉴 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new AppException(MenuErrorCode.MENU_NOT_FOUND));

        // 2. 가게 조회 및 상태 확인
        Store store = getStore(menu.getStoreId());


        // 3. 관리 권한 통합 검증 (가게 상태 + 유저 권한 체크)
        menuManagePermissionPolicy.validateManagePermission(requester, store);

        // 4. 삭제 가능 상태인지 추가 검증 (이미 삭제된 메뉴인지 등)
        menuManagePermissionPolicy.validateDeleteCondition(menu);

        // 5. soft delete (or email 이용)
        menu.markDeleted(requester.getUsername());
    }

    @Transactional
    public MenuUpdateResponse update(UUID menuId, UserPrincipal editor, MenuUpdateRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new AppException(MenuErrorCode.MENU_NOT_FOUND));
        Store store = storeRepository.findById(menu.getStoreId())
                .orElseThrow(() -> new AppException(StoreErrorCode.STORE_NOT_FOUND));

        menuManagePermissionPolicy.validateManagePermission(editor, store);
        menuManagePermissionPolicy.validateUpdateCondition(editor, menu);

        menu.update(
                request.menuCategoryId(),
                request.name(),
                request.price(),
                request.description(),
                request.menuPictureUrl(),
                request.isHidden(),
                request.aiDescription(),
                request.aiPrompt()
        );

        return MenuUpdateResponse.from(menu);
    }


    // --- Private Helper Methods ---

    /**
     * Store 조회
     */
    private Store getStore(UUID storeId) {
        return storeRepository.findById(storeId)
                .filter(s -> !s.isDeleted()) // 폐업한 가게 체크 추가
                .orElseThrow(() -> new AppException(StoreErrorCode.STORE_NOT_FOUND));
    }

    /**
     * 메뉴 데이터 기본 검증
     */
    private void validateMenuData(MenuCreateRequest request) {

        if (request.name() == null || request.name().isBlank()) {
            throw new AppException(MenuErrorCode.MENU_INVALID_REQUEST);
        }

        if (request.price() == null || request.price() < 0) {
            throw new AppException(MenuErrorCode.MENU_INVALID_REQUEST);
        }
    }

    /**
     * MENU Entity 생성 로직
     */
    private Menu createMenuEntity(UUID storeId, MenuCreateRequest request) {
        // 가격 유효성 검증은 MoneyVO 생성자에게 위임
        MoneyVO priceVO = new MoneyVO(request.price());

        return new Menu(
                storeId,
                request.menuCategoryId(),
                request.name().strip(),
                priceVO.getPrice(),
                request.description(),
                request.menuPictureUrl(),
                request.isHidden(),
                request.aiDescription(),
                request.aiPrompt()
        );
    }
}
