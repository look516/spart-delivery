package com.sparta.spartadelivery.menu;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.area.domain.repository.AreaRepository;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MenuHideE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MenuRepository menuRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StoreCategoryRepository storeCategoryRepository;
    @Autowired private AreaRepository areaRepository;


    private UserPrincipal ownerPrincipal;
    private UUID myMenuId;
    private UUID otherMenuId;

    @BeforeEach
    void setUp() {
        // 1. 실제 사장님 유저 생성 및 저장
        UserEntity owner = userRepository.save(
                UserEntity.builder()
                        .username("사장님")
                        .nickname("맛나식당사장")
                        .email("owner@test.com")
                        .password("password123!")
                        .role(Role.OWNER)
                        .isPublic(true)
                        .build()
        );
        ownerPrincipal = UserPrincipal.from(owner);

        // 2. 다른 사장님 유저 생성 및 저장
        UserEntity other = userRepository.save(
                UserEntity.builder()
                        .username("다른사장님")
                        .nickname("다른식당사장")
                        .email("other@test.com")
                        .password("password123!")
                        .role(Role.OWNER)
                        .isPublic(true)
                        .build()
        );

        // FK용 Dummy 카테고리 & area
        StoreCategory category = storeCategoryRepository.save(
                StoreCategory.builder().name("tmp").build()
        );

        Area area = areaRepository.save(
                Area.builder()
                        .name("tmp")
                        .city("서울")
                        .district("강남")
                        .active(true)
                        .build()
        );

        // 3. 내 가게 및 남의 가게 생성
        Store myStore = storeRepository.save(
                Store.builder()
                        .owner(owner)
                        .storeCategory(category)
                        .area(area)
                        .name("내 가게").address("역삼동").phone("01011112222")
                        .build()
        );

        Store otherStore = storeRepository.save(
                Store.builder()
                        .owner(other)
                        .storeCategory(category)
                        .area(area)
                        .name("남의 가게").address("청담동").phone("01033334444")
                        .build()
        );

        // 4. 내 메뉴 및 남의 메뉴 생성
        Menu myMenu = menuRepository.save(
                new Menu(
                        myStore.getId(),
                        UUID.randomUUID(),
                        "메뉴1",
                        10000,
                        "설명111",
                        null,
                        false,
                        null,
                        null)
        );
        myMenuId = myMenu.getId();

        Menu otherMenu = menuRepository.save(
                new Menu(
                        otherStore.getId(),
                        UUID.randomUUID(),
                        "메뉴2",
                        20000,
                        "설명222",
                        null,
                        false,
                        null,
                        null)
        );
        otherMenuId = otherMenu.getId();
    }

    @Test
    @DisplayName("내 메뉴 숨김 성공")
    void hideMenu_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/menus/{menuId}/hide", myMenuId)
                        .with(SecurityMockMvcRequestPostProcessors.user(ownerPrincipal)))
                .andExpect(status().isOk());

        Menu menu = menuRepository.findById(myMenuId).orElseThrow();
        assertThat(menu.isHidden()).isTrue();
    }

    @Test
    @DisplayName("남의 메뉴 숨김 실패")
    void hideMenu_Fail_AccessDenied() throws Exception {
        // 내 토큰으로 남의 메뉴(otherMenuId)를 숨기려고 시도
        mockMvc.perform(patch("/api/v1/menus/{menuId}/hide", otherMenuId)
                        .with(SecurityMockMvcRequestPostProcessors.user(ownerPrincipal)))
                .andExpect(status().isForbidden()); // HttpStatus.FORBIDDEN

        Menu menu = menuRepository.findById(otherMenuId).orElseThrow();
        assertThat(menu.isHidden()).isFalse(); // 변경되지 않아야 함
    }

    @Test
    @DisplayName("비로그인 접근 실패")
    void hideMenu_Fail_Unauthorized() throws Exception {
        // Security 필터에서 걸리거나 정책에서 null 체크에 걸림
        mockMvc.perform(patch("/api/v1/menus/{menuId}/hide", myMenuId))
                .andExpect(status().isUnauthorized());
    }
}