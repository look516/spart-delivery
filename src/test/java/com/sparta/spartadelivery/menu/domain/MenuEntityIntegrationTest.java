package com.sparta.spartadelivery.menu.domain;

import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.entity.MenuCategory;
import com.sparta.spartadelivery.menu.domain.entity.MenuTag;
import com.sparta.spartadelivery.menu.domain.entity.OptionGroupSpec;
import com.sparta.spartadelivery.menu.domain.entity.OptionSpec;
import com.sparta.spartadelivery.menu.domain.entity.Tag;
import com.sparta.spartadelivery.menu.domain.repository.MenuCategoryRepository;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.menu.domain.repository.MenuTagRepository;
import com.sparta.spartadelivery.menu.domain.repository.OptionGroupSpecRepository;
import com.sparta.spartadelivery.menu.domain.repository.OptionSpecRepository;
import com.sparta.spartadelivery.menu.domain.repository.TagRepository;
import com.sparta.spartadelivery.menu.domain.vo.MenuTagVO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
//@ActiveProfiles("test-h2")
// test-h2 yaml 파일 적용 [data가 들어가는지 확인하려면 Transactional이 없어야 한다]
// 추후 Tag unique 중복 오류 해결
class MenuEntityIntegrationTest {
    @Autowired
    private MenuRepository menuRepo;

    @Autowired
    private MenuCategoryRepository menuCategoryRepo;

    @Autowired
    private TagRepository tagRepo;

    @Autowired
    private MenuTagRepository menuTagRepo;

    @Autowired
    private OptionGroupSpecRepository optionGroupSpecRepo;

    @Autowired
    private OptionSpecRepository optionSpecRepo;

    // Entity와 Repo를 이용하여 save만 확인한다.
    // 추후 Entity Repo Test 분리, test case 추가

    // Menu Tag MenuTag 분리 / OptionGroupSpec OptionSpec 분리

    @Test
    void 메뉴카테고리_저장() {
        // Given
        UUID storeId = UUID.randomUUID();
        String name = "치킨메뉴";

        // When
        MenuCategory category = new MenuCategory(storeId, name);
        MenuCategory savedCategory = menuCategoryRepo.save(category);

        // Then
        assertNotNull(savedCategory.getId());
    }

    @Test
    void 메뉴_Tag_저장() {
        // Menu Given
        UUID storeId = UUID.randomUUID();
        UUID menuCategoryId = UUID.randomUUID();
        String name = "뿌링클";
        Integer price = 18000;
        String description = "맛있는 치킨";
        String menuPictureUrl = "http://image.com";
        boolean isHidden = false;
        String aiDescription = "AI 설명";
        String aiPrompt = "AI 프롬프트";

        // Tag Given
        String tagName = "인기";

        // MenuTag Given
        // savedMenu;
        // savedTag;


        // Menu When
        Menu menu = new Menu(
                storeId,
                menuCategoryId,
                name,
                price,
                description,
                menuPictureUrl,
                aiDescription,
                aiPrompt
        );
        Menu savedMenu = menuRepo.save(menu);

        // Tag When
        Tag tag = new Tag(tagName);
        Tag savedTag = tagRepo.save(tag);

        // MenuTag When
        MenuTagVO vo = new MenuTagVO(savedMenu.getId(), savedTag.getId());
        MenuTag menuTag = MenuTag.from(vo);
        MenuTag savedMenuTag = menuTagRepo.save(menuTag);


        // Then
        assertNotNull(savedMenu.getId());
        assertNotNull(savedTag.getId());
        assertNotNull(savedMenuTag.getId());
    }


    @Test
    void Option_저장() {
        // Before
        Menu menu = new Menu(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "뿌링클",
                18000,
                "맛있는 치킨",
                "http://image.com",
                "AI 설명",
                "AI 프롬프트"
        );
        Menu savedMenu = menuRepo.save(menu);


        // OptionGroupSpec Given & When
        String groupName = "음료추가";
        Integer minSelect = 0;
        Integer maxSelect = 1;

        OptionGroupSpec group = new OptionGroupSpec(savedMenu, groupName, minSelect, maxSelect);
        OptionGroupSpec savedGroup = optionGroupSpecRepo.save(group);


        // OptionSpec Given & When
        // OptionGroupSpec savedGroup;
        String optionName = "콜라 1L";
        Integer price = 3000;
        boolean isDefault = false;

        OptionSpec option = new OptionSpec(savedGroup, optionName, price, isDefault);
        OptionSpec savedOption = optionSpecRepo.save(option);


        // Then
        assertNotNull(savedGroup.getId());
        assertNotNull(savedOption.getId());
    }
}