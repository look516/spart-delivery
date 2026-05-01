package com.sparta.spartadelivery.menu.presentation.controller;

import com.sparta.spartadelivery.global.annotation.CheckMenuPermission;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.menu.application.service.MenuAction;
import com.sparta.spartadelivery.menu.application.service.MenuService;
import com.sparta.spartadelivery.menu.presentation.dto.request.MenuCreateRequest;
import com.sparta.spartadelivery.menu.presentation.dto.request.MenuUpdateRequest;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuDetailResponse;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuListResponse;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Menu", description = "메뉴 API")
public class MenuController {

    private final MenuService menuService;

    @Operation(
            summary = "메뉴 등록 API",
            description = """
                    새로운 메뉴를 등록합니다.

                    **처리 정책**
                    - OWNER: 본인 가게만 등록 가능
                    - MANAGER/MASTER: 모든 가게 등록 가능
                    """
    )
    @PostMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> createMenu(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MenuCreateRequest request
    ) {
        MenuDetailResponse response = menuService.createMenu(storeId, request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "메뉴가 성공적으로 등록되었습니다.", response));
    }

    @Operation(
            summary = "메뉴 목록 조회 API",
            description = """
                    특정 매장의 메뉴 목록을 조회합니다.

                    **권한별 조회 정책**
                    
                    - CUSTOMER/다른가게OWNER: 숨겨지지 않은 메뉴만 조회 가능
                    - OWNER: 숨겨진 메뉴를 포함하여 조회 가능, 삭제된 메뉴는 별도 페이지에서 조회 가능
                    - MANAGER/MASTER: 숨겨진, 삭제된 메뉴를 포함하여 조회 가능
                    """
    )
    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<List<MenuListResponse>>> getMenus(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<MenuListResponse> response = menuService.getMenusByRole(storeId, userPrincipal);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "메뉴 목록 조회 성공", response));
    }

    @Operation(
            summary = "메뉴 상세 조회 API",
            description = """
                    메뉴의 상세 정보를 조회합니다.

                    **권한별 조회 정책 (일반 메뉴 상세 조회 기준)**
                    
                    - CUSTOMER:
                      - 활성(Active) 메뉴만 조회 가능
                      - 숨김(Hidden), 삭제(Deleted) 메뉴는 이 API로 조회할 수 없음
                      - 단, 과거 주문내역에서의 메뉴 조회는 별도 API(Snapshot)에서 허용
            
                    - OWNER:
                      - 자기 가게의 경우: 활성, 숨김, 삭제 메뉴 모두 조회 가능
                      - 다른 가게의 경우: 활성(Active) 메뉴만 조회 가능
            
                    - MANAGER:
                      - 모든 가게의 활성, 숨김, 삭제 메뉴 조회 가능
            
                    - MASTER:
                      - 모든 가게의 활성, 숨김, 삭제 메뉴 조회 가능
                    """
    )
    @GetMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        MenuDetailResponse response = menuService.getMenuByRole(menuId, userPrincipal);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "메뉴 상세 조회 성공", response));
    }

    @Operation(
            summary = "메뉴 삭제 API",
            description = """
                    메뉴를 삭제(Soft Delete) 처리합니다.
                    - OWNER: 본인 가게 메뉴만 삭제 가능
                    - MASTER: 모든 메뉴 삭제 가능
                    - MANAGER/CUSTOMER: 삭제 불가
                    """
    )
    @DeleteMapping("/menus/{menuId}")
    @CheckMenuPermission(action = MenuAction.DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        menuService.deleteMenu(menuId, userPrincipal);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "메뉴가 성공적으로 삭제되었습니다.", null));
    }

    @Operation(
            summary = "메뉴 숨김 API",
            description = """
                    메뉴를 숨김 처리합니다.
                    - CUSTOMER: 수정 불가
                    - OWNER: 본인 가게 메뉴만 숨김 가능
                    - MANAGER/MASTER: 모든 메뉴 숨김 가능
                    """
    )
    @PatchMapping("/menus/{menuId}/hide")
    @CheckMenuPermission(action = MenuAction.HIDE)
    public ResponseEntity<ApiResponse<MenuDetailResponse>> hideMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        MenuDetailResponse response = menuService.hideMenu(menuId, userPrincipal);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "메뉴가 성공적으로 숨겨졌습니다.", response));
    }

    @Operation(
            summary = "메뉴 수정",
            description = """
                    메뉴의 기본 정보, 카테고리, 상태(숨김), AI 관련 정보를 수정합니다.
                    
                    - OWNER: 본인 가게의 메뉴만 수정 가능 (삭제된 메뉴는 수정 불가)
                    - MANAGER: 부적절한 정보 수정 가능
                    - MASTER: 모든 메뉴 수정 가능
                    """
    )
    @PutMapping("/menus/{menuId}")
    //@CheckMenuPermission(action = MenuAction.UPDATE)
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<ApiResponse<MenuUpdateResponse>> update(@PathVariable UUID menuId,
                                     @RequestBody MenuUpdateRequest request,
                                     @AuthenticationPrincipal UserPrincipal updateBy
    ) {
        MenuUpdateResponse response = menuService.updateMenu(menuId, updateBy, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "메뉴가 성공적으로 수정됐습니다.", response));
    }



}