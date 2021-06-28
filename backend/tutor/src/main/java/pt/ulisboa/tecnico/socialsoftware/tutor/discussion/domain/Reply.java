package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import pt.ulisboa.tecnico.socialsoftware.common.dtos.discussion.ReplyDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.user.Role;
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.common.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage.*;

@Entity
@Table(name = "replies")
public class Reply implements DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    private Discussion discussion;

    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    private User user;

    @NotNull
    @Column(name="message", columnDefinition="text")
    private String message;

    @Column(name="date")
    private LocalDateTime date;

    @Column(columnDefinition = "boolean default false")
    private boolean isPublic;

    public Reply(){}

    public Reply(User user, ReplyDto replyDto, Discussion discussion){
        checkReplyAuthorization(user, discussion);
        setUser(user);
        setDate(DateHandler.toLocalDateTime(replyDto.getDate()));
        setMessage(replyDto.getMessage());
        setDiscussion(discussion);
        setPublic(replyDto.isPublic());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
        discussion.addReply(this);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message == null || message.trim().length() == 0) {
            throw new TutorException(REPLY_MISSING_MESSAGE);
        }

        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        user.addReply(this);
    }

    public void remove() {
        user.removeReply(this);
        user = null;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitReply(this);
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void changeAvailability() {
        this.isPublic = !this.isPublic;
    }

    private void checkReplyAuthorization(User user, Discussion discussion) {
        if (user.getRole() != Role.TEACHER && !user.getId().equals(discussion.getUser().getId())) {
            throw new TutorException(REPLY_UNAUTHORIZED_USER);
        }
    }

    @Override
    public String toString() {
        return "Reply{" +
                "id=" + id +
                ", discussion=" + discussion +
                ", user=" + user +
                ", message='" + message + '\'' +
                ", date=" + date +
                ", isPublic=" + isPublic +
                '}';
    }

    public ReplyDto getDto() {
        ReplyDto dto = new ReplyDto();
        dto.setName(getUser().getName());
        dto.setUsername(getUser().getUsername());
        dto.setId(getId());
        dto.setUserId(getUser().getId());
        checkEmptyMessage(getMessage());
        dto.setMessage(getMessage());
        dto.setDate(DateHandler.toISOString(getDate()));
        dto.setPublic(isPublic());
        return dto;
    }

    private void checkEmptyMessage(String message) {
        if (message.trim().length() == 0){
            throw new TutorException(REPLY_MISSING_DATA);
        }
    }
}
