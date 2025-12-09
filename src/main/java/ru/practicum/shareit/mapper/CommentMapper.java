package ru.practicum.shareit.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.model.Comment;

@Component
public class CommentMapper {

    public Comment toEntity(CommentDto commentDto) {
        if (commentDto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        // item, author, created устанавливаются в сервисе
        return comment;
    }

    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .created(comment.getCreated())
                .build();
    }
}
