package com.sparta.spartadelivery.menu.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.exception.MenuErrorCode;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class MenuManagePermissionPolicy {

    /**
     * 메뉴 관리(등록, 수정, 삭제) 공통 권한 및 상태 검증
     */
    public void validateManagePermission(UserPrincipal requester, Store store) {
        // 1. 공통: 폐업한 가게는 어떤 관리 작업도 불가
        if (store.isDeleted()) {
            throw new AppException(StoreErrorCode.STORE_NOT_FOUND);
        }

        // 2. 공통: 로그인 및 기본 권한 체크 (CUSTOMER 차단)
        if (requester == null || requester.getRole() == Role.CUSTOMER) {
            throw new AppException(MenuErrorCode.MENU_ACCESS_DENIED);
        }

        // 3. 역할별 상세 체크
        Role role = requester.getRole();

        // MASTER는 무조건 통과
        if (role == Role.MASTER) return;

        // MANAGER는 메뉴 관리 권한이 없음 (복구만 가능하거나 정책에 따라 다름)
        if (role == Role.MANAGER) {
            throw new AppException(MenuErrorCode.MENU_ACCESS_DENIED);
        }

        // OWNER는 본인 가게만 가능
        if (role == Role.OWNER) {
            if (!store.getOwner().getId().equals(requester.getId())) {
                throw new AppException(MenuErrorCode.MENU_ACCESS_DENIED);
            }
        }
    }

    /**
     * 삭제 전용 추가 검증 (이미 삭제된 경우 등)
     */
    public void validateDeleteCondition(Menu menu) {
        if (menu.isDeleted()) {
            throw new AppException(MenuErrorCode.MENU_ALREADY_DELETED);
        }
    }

    public void validateUpdateCondition(UserPrincipal editor, Menu menu) {
        if (menu.isDeleted()) {
            throw new AppException(MenuErrorCode.MENU_UPDATE_ACCESS_DENIED);
        }
    }
}
