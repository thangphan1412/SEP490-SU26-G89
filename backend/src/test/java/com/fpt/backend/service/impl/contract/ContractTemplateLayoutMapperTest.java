package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.exception.BadHttpException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractTemplateLayoutMapperTest {
    private final ContractTemplateLayoutMapper mapper =
            new ContractTemplateLayoutMapper(new ObjectMapper());

    @Test
    void preservesEditableBlocksWhenPositionsAreSentSeparately() {
        String layoutJson = """
                {
                  "pageCount": 1,
                  "coordinateSystem": "NORMALIZED",
                  "fields": [],
                  "blocks": [
                    {
                      "key": "custom_banner",
                      "type": "NATIONAL_HEADER",
                      "enabled": true,
                      "heading": "CUSTOM COMPANY BANNER",
                      "content": "Custom motto"
                    },
                    {
                      "key": "main_content",
                      "type": "CONTENT",
                      "enabled": true
                    }
                  ]
                }
                """;

        ContractTemplateLayout layout = mapper.normalize(
                2,
                List.of(),
                layoutJson
        );

        assertThat(layout.pageCount()).isEqualTo(2);
        assertThat(layout.blocks()).extracting("key")
                .containsExactly("custom_banner", "main_content");
        assertThat(layout.blocks().getFirst().heading())
                .isEqualTo("CUSTOM COMPANY BANNER");
    }

    @Test
    void requiresMainContentBlock() {
        String layoutJson = """
                {
                  "blocks": [
                    {
                      "key": "banner_only",
                      "type": "NATIONAL_HEADER",
                      "enabled": true
                    }
                  ]
                }
                """;

        assertThatThrownBy(() -> mapper.normalize(1, List.of(), layoutJson))
                .isInstanceOf(BadHttpException.class)
                .hasMessageContaining("CONTENT block");
    }
}
