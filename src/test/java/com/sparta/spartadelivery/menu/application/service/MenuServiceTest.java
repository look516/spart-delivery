package com.sparta.spartadelivery.menu.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.menu.exception.MenuErrorCode;
import com.sparta.spartadelivery.menu.presentation.dto.request.MenuCreateRequest;
import com.sparta.spartadelivery.menu.presentation.dto.request.MenuUpdateRequest;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuDetailResponse;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuUpdateResponse;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    // 추가해야 할 부분
    @Mock
    private MenuPermissionPolicy menuPermissionPolicy;

    @Mock
    private MenuManagePermissionPolicy menuManagePermissionPolicy;

    @InjectMocks
    private MenuService menuService;

    // -----------------------------
    // 메뉴 등록 실패 - 폐업한 가게
    // -----------------------------
    @Test
    @DisplayName("메뉴 등록 실패 - 삭제된(폐업) 가게인 경우")
    void createMenu_Fail_DeletedStore() {
        UUID storeId = UUID.randomUUID();

        UserPrincipal requester = principal(Role.OWNER);
        MenuCreateRequest request = createRequest();

        Store store = mock(Store.class);
        when(store.isDeleted()).thenReturn(true);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> menuService.createMenu(storeId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_NOT_FOUND);

        verify(menuRepository, never()).save(any());
    }

    // -----------------------------
    // 메뉴 상세 조회 실패 - 고객이 삭제된 메뉴 조회
    // -----------------------------
    @Test
    @DisplayName("메뉴 상세 조회 실패 - 고객이 삭제된 메뉴 조회 시")
    void getMenuByRole_Fail_DeletedMenu() {
        // given
        UUID menuId = UUID.randomUUID();
        UserPrincipal requester = principal(Role.CUSTOMER);

        Menu menu = mock(Menu.class);
        Store store = mock(Store.class);

        // (1) 메뉴 조회 Mocking
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(menu.getStoreId()).thenReturn(UUID.randomUUID());

        // (2) 가게 조회 Mocking
        when(storeRepository.findById(any())).thenReturn(Optional.of(mock(Store.class)));

        lenient().when(store.isDeleted()).thenReturn(false);

        // (3) Policy가 예외를 던지도록 설정
        doThrow(new AppException(MenuErrorCode.MENU_NOT_FOUND))
                .when(menuPermissionPolicy)
                .validateMenuDetailPermission(any(), any(), any());

        // when & then
        assertThatThrownBy(() -> menuService.getMenuByRole(menuId, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(MenuErrorCode.MENU_NOT_FOUND);
    }

    // -----------------------------
    // 메뉴 등록 성공 - OWNER 본인 가게
    // -----------------------------
    @Test
    @DisplayName("메뉴 등록 성공 - OWNER가 본인 가게에 등록")
    void createMenu_Success() {
        UUID storeId = UUID.randomUUID();
        Long ownerId = 1L;
        UserPrincipal requester = principal(ownerId, Role.OWNER);
        MenuCreateRequest request = createRequest();

        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(store.isDeleted()).thenReturn(false);

        doNothing().when(menuManagePermissionPolicy).validateManagePermission(any(), any());

        when(menuRepository.save(any(Menu.class)))
                .thenAnswer(invocation -> {
                    Menu menu = invocation.getArgument(0);
                    ReflectionTestUtils.setField(menu, "id", UUID.randomUUID());
                    return menu;
                });

        // when
        MenuDetailResponse response = menuService.createMenu(storeId, request, requester);

        // then
        assertThat(response).isNotNull();
        verify(menuRepository).save(any(Menu.class));
        verify(menuManagePermissionPolicy).validateManagePermission(eq(requester), eq(store));
    }

    @Test
    @DisplayName("가게 수정 실패 - 소유자(Owner)가 아닌 사용자가 수정을 시도할 경우")
    void updateStore_Fail_NotOwner() {
        // given
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        // 1. 요청자 설정 (ID 999L인 다른 사장님)
        UserPrincipal notOwner = principal(999L, Role.OWNER);

        // 2. DTO 생성 (record 생성자 이용)
        MenuUpdateRequest request = new MenuUpdateRequest(
                "수정메뉴", 15000, "설명", "url", false, UUID.randomUUID(), "AI설명", "AI프롬프트"
        );

        // 3. 도메인 객체 Mocking
        Menu menu = mock(Menu.class);
        Store store = mock(Store.class);

        // 4. Repository 행위 정의
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(menu.getStoreId()).thenReturn(storeId); // 메뉴가 속한 가게 ID 반환
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // 5. Policy에서 소유권 검증 실패(AppException)를 던지도록 설정
        // 서비스 로직에서 가장 먼저 호출되는 validateManagePermission을 타겟팅
        doThrow(new AppException(MenuErrorCode.MENU_UPDATE_ACCESS_DENIED))
                .when(menuManagePermissionPolicy).validateManagePermission(eq(notOwner), any(Store.class));

        // when & then
        assertThatThrownBy(() -> menuService.update(menuId, notOwner, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(MenuErrorCode.MENU_UPDATE_ACCESS_DENIED);

        // 검증: 예외가 발생했으므로 엔티티의 update 메서드는 절대 호출되지 않아야 함
        verify(menu, never()).update(any(), any(), any(), any(), any(), anyBoolean(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OWNER", "MANAGER", "MASTER"})
    @DisplayName("메뉴 수정 성공 - 권한 검증 통과된 경우")
    void updateMenu_Success_ByAuthorizedRoles(Role role) {
        // Given
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UserPrincipal editor = principal(1L, role);

        MenuUpdateRequest request = new MenuUpdateRequest(
                "수정된 메뉴", 25000, "설명", "url", false,
                categoryId, "AI설명", "AI프롬프트"
        );

        Menu menu = new Menu(
                storeId, categoryId,
                "기존이름", 20000, "설명", "url",
                false, "AI", "Prompt"
        );
        ReflectionTestUtils.setField(menu, "id", menuId);

        Store store = mock(Store.class);

        //when
        when(menuRepository.findById(menuId))
                .thenReturn(Optional.of(menu));

        when(storeRepository.findById(any()))
                .thenReturn(Optional.of(store));

        doNothing().when(menuManagePermissionPolicy)
                .validateManagePermission(any(), any());
        doNothing().when(menuManagePermissionPolicy)
                .validateUpdateCondition(any(), any());

        MenuUpdateResponse response =
                menuService.update(menuId, editor, request);

        //then
        assertThat(response.name()).isEqualTo("수정된 메뉴");
        assertThat(response.price()).isEqualTo(25000);
        verify(menuRepository).findById(menuId);
        verify(storeRepository).findById(any());
    }

    // -----------------------------
    // Helper Methods
    // -----------------------------
    private MenuCreateRequest createRequest() {
        return new MenuCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "후라이드 치킨",
                20000,
                "바삭함",
                null,
                false,
                null,
                null
        );
    }

    private UserPrincipal principal(Long id, Role role) {
        return UserPrincipal.builder()
                .id(id)
                .accountName("tester")
                .password("pw")
                .nickname("nick")
                .email("test@test.com")
                .role(role)
                .build();
    }

    private UserPrincipal principal(Role role) {
        return principal(1L, role);
    }
}