package com.sparrowx.apigateway.features.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchRequestDto {

    @NotBlank(message = "query must not be blank")
    private String query;

    private String type;

    @Min(value = 0, message = "page must be >= 0")
    private Integer page = 0;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private Integer size = 20;

    public SearchRequestDto() {
    }

    public SearchRequestDto(String query, String type, Integer page, Integer size) {
        this.query = query;
        this.type = type;
        this.page = page;
        this.size = size;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String query;
        private String type;
        private Integer page = 0;
        private Integer size = 20;

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public SearchRequestDto build() {
            return new SearchRequestDto(query, type, page, size);
        }
    }
}