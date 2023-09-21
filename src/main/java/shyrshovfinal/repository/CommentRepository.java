package shyrshovfinal.repository;

import org.jetbrains.annotations.NotNull;
import shyrshovfinal.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    @NotNull
    @Override
    <S extends Comment> S save(@NotNull S s);
}
