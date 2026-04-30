package com.sparta.spartadelivery.menu.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MenuPermissionPolicyTest {

//    @Mock
//    private StoreRepository storeRepository;

    private MenuPermissionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new MenuPermissionPolicy();
    }

    @Test
    @DisplayName("CUSTOMER가 숨겨진 메뉴 상세 조회 시 예외가 발생한다")
    void customer_hidden_menu_fail() {
        // given
        UserPrincipal customer = createMockUser(Role.CUSTOMER, 1L);
        Menu hiddenMenu = createMockMenu(true, false); // hidden=true, deleted=false
        Store store = createMockStore(2L);

        // when & then
        assertThrows(AppException.class, () ->
                policy.validateMenuDetailPermission(customer, store, hiddenMenu)
        );
    }

    @Test
    @DisplayName("OWNER는 본인 가게의 삭제된 메뉴도 조회할 수 있다")
    void owner_deleted_menu_success() {
        // given
        Long ownerId = 100L;
        UserPrincipal owner = createMockUser(Role.OWNER, ownerId);
        Store myStore = createMockStore(ownerId); // 가게 주인 매칭
        Menu deletedMenu = createMockMenu(false, true); // deleted=true

        // when & then (예외가 발생하지 않아야 함)
        assertDoesNotThrow(() ->
                policy.validateMenuDetailPermission(owner, myStore, deletedMenu)
        );
    }

    @Test
    @DisplayName("MASTER는 삭제된 메뉴도 제약 없이 조회할 수 있다")
    void master_any_menu_success() {
        // given
        UserPrincipal master = createMockUser(Role.MASTER, 1L);
        Store anyStore = createMockStore(999L);
        Menu deletedMenu = createMockMenu(true, true);

        // when & then
        assertDoesNotThrow(() ->
                policy.validateMenuDetailPermission(master, anyStore, deletedMenu)
        );
    }

    // Helper methods (Mock 객체 생성용)
    private UserPrincipal createMockUser(Role role, Long id) {
        // UserPrincipal은 보통 인터페이스나 클래스이므로 mock으로 생성
        UserPrincipal user = mock(UserPrincipal.class);

        // 유저의 역할(Role)과 ID가 호출될 때 우리가 넣은 값을 반환하도록 설정
        lenient().when(user.getRole()).thenReturn(role);
        lenient().when(user.getId()).thenReturn(id);

        // 추가로 username 등이 필요하다면 아래처럼 정의 가능
        lenient().when(user.getUsername()).thenReturn("test_user_" + id + "@test.com");

        return user;
    }

    private Store createMockStore(Long ownerId) {
        Store store = mock(Store.class);
        UserEntity owner = mock(UserEntity.class);

        // 가게 주인의 ID 설정
        lenient().when(owner.getId()).thenReturn(ownerId);
        lenient().when(store.getOwner()).thenReturn(owner);

        // 기본적으로 가게는 폐업하지 않은 상태(false)로 설정
        lenient().when(store.isDeleted()).thenReturn(false);

        return store;
    }

    private Menu createMockMenu(boolean hidden, boolean deleted) {
        Menu menu = mock(Menu.class);

        // 메뉴의 숨김 여부와 삭제 여부 설정
        lenient().when(menu.isHidden()).thenReturn(hidden);
        lenient().when(menu.isDeleted()).thenReturn(deleted);

        return menu;
    }
}