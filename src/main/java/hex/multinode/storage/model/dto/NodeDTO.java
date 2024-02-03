package hex.multinode.storage.model.dto;

import jakarta.validation.constraints.NotBlank;

public record NodeDTO(
        String id,
        @NotBlank
        String title,
        String contentText) {

    public static NodeDTO of(String title, String contentText) {
        return new NodeDTO(null, title, contentText);
    }

    public static NodeDTO of(String id, String title, String contentText) {
        return new NodeDTO(id, title, contentText);
    }
}
