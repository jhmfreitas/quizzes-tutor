package pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.common.dtos.course.CourseType;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.execution.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.execution.CourseExecutionStatus;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.question.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.user.Role;
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.domain.QuestionSubmission;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage.*;

@Entity
@Table(name = "course_executions")
public class CourseExecution implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private CourseType type;

    private String acronym;
    private String academicTerm;

    @Enumerated(EnumType.STRING)
    private CourseExecutionStatus status;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToMany(mappedBy = "courseExecutions", fetch=FetchType.LAZY)
    private final Set<User> users = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch=FetchType.LAZY, orphanRemoval=true)
    private final Set<Quiz> quizzes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch=FetchType.LAZY, orphanRemoval=true)
    private final Set<Assessment> assessments = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch=FetchType.LAZY, orphanRemoval=true)
    private final Set<QuestionSubmission> questionSubmissions = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Discussion> discussions = new HashSet<>();

    public CourseExecution() {
    }

    public CourseExecution(Course course, String acronym, String academicTerm, CourseType type, LocalDateTime endDate) {
        if (course.existsCourseExecution(acronym, academicTerm, type)) {
            throw new TutorException(DUPLICATE_COURSE_EXECUTION, acronym + academicTerm);
        }

        setType(type);
        setCourse(course);
        setAcronym(acronym);
        setAcademicTerm(academicTerm);
        setStatus(CourseExecutionStatus.ACTIVE);
        setEndDate(endDate);

    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCourseExecution(this);
    }

    public Integer getId() {
        return id;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        if (type == null)
            throw new TutorException(INVALID_TYPE_FOR_COURSE_EXECUTION);
        this.type = type;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        if (acronym == null || acronym.trim().isEmpty()) {
        throw new TutorException(INVALID_ACRONYM_FOR_COURSE_EXECUTION);
    }
        this.acronym = acronym;
    }

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        if (academicTerm == null || academicTerm.isBlank())
            throw new TutorException(INVALID_ACADEMIC_TERM_FOR_COURSE_EXECUTION);

        this.academicTerm = academicTerm;
    }

    public CourseExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(CourseExecutionStatus status) {
        this.status = status;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
        course.addCourseExecution(this);
    }

    public Set<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public Set<Quiz> getQuizzes() {
        return quizzes;
    }

    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public List<Assessment> getAvailableAssessments() {
        return getAssessments().stream()
                .filter(assessment -> assessment.getStatus() == Assessment.Status.AVAILABLE)
                .collect(Collectors.toList());
    }

    public void addAssessment(Assessment assessment) {
        assessments.add(assessment);
    }

    public void addQuestionSubmission(QuestionSubmission questionSubmission) {
        questionSubmissions.add(questionSubmission);
    }

    public Set<QuestionSubmission> getQuestionSubmissions() { return questionSubmissions; }

    public Set<Discussion> getDiscussions() {
        return discussions;
    }

    public void addDiscussion(Discussion discussion) {
        discussions.add(discussion);
    }

    @Override
    public String toString() {
        return "CourseExecution{" +
                "id=" + id +
                ", type=" + type +
                ", acronym='" + acronym + '\'' +
                ", academicTerm='" + academicTerm + '\'' +
                ", status=" + status +
                '}';
    }

    public void remove() {
        if (getCourse().getCourseExecutions().size() == 1 && (!getCourse().getQuestions().isEmpty() || !getCourse().getTopics().isEmpty())) {
            throw new TutorException(CANNOT_DELETE_COURSE_EXECUTION, acronym + academicTerm);
        }

        if (!getQuizzes().isEmpty() || !getAssessments().isEmpty()) {
            throw new TutorException(CANNOT_DELETE_COURSE_EXECUTION, acronym + academicTerm);
        }

        course.getCourseExecutions().remove(this);
        users.forEach(user -> user.getCourseExecutions().remove(this));
        questionSubmissions.forEach(QuestionSubmission::remove);
    }

    public int getNumberOfActiveTeachers() {
        return (int) this.users.stream()
                .filter(user ->
                        user.getRole().equals(Role.TEACHER) && user.isActive())
                .count();
    }

    public int getNumberofInactiveTeachers() {
        return (int) this.users.stream()
                .filter(user ->
                        user.getRole().equals(Role.TEACHER) && !user.isActive())
                .count();
    }

    public int getNumberOfActiveStudents() {
        return (int) this.users.stream()
                .filter(user ->
                        user.getRole().equals(Role.STUDENT) && user.isActive())
                .count();
    }

    public int getNumberOfInactiveStudents() {
        return (int) this.users.stream()
                .filter(user ->
                        user.getRole().equals(Role.STUDENT) && !user.isActive())
                .count();
    }

    public int getNumberOfQuizzes() {
        return this.quizzes.size();
    }

    public int getNumberOfQuestions() {
        return this.course.getQuestions().size();
    }

    public Set<User> getStudents() {
        return getUsers().stream()
                .filter(user -> user.getRole().equals(Role.STUDENT))
                .collect(Collectors.toSet());
    }

    public Set<Topic> findAvailableTopics() {
        return getAvailableAssessments().stream().flatMap(assessment -> assessment.getTopics().stream()).collect(Collectors.toSet());
    }


    public List<Question> filterQuestionsByTopics(List<Question> questions, Set<TopicDto> topics) {
        Set<Integer> topicIds = topics.stream().map(TopicDto::getId).collect(Collectors.toSet());
        Set<Assessment> availableAssessments = getAvailableAssessments().stream()
                .filter(assessment -> topicIds.containsAll(assessment.getTopics().stream().map(Topic::getId).collect(Collectors.toSet())))
                .collect(Collectors.toSet());

        List<Question> result = new ArrayList<>();
        for (Question question : questions) {
            for (Assessment assessment : availableAssessments) {
                if (question.belongsToAssessment(assessment)) {
                    result.add(question);
                    break;
                }
            }
        }

        return result;
    }

    public Set<User> getTeachers() {
        return getUsers().stream()
                .filter(user -> user.getRole().equals(Role.TEACHER))
                .collect(Collectors.toSet());
    }

    public CourseExecutionDto getDto() {
        CourseExecutionDto dto = new CourseExecutionDto();
        dto.setAcademicTerm(getAcademicTerm());
        dto.setAcronym(getAcronym());
        dto.setCourseExecutionId(getId());
        dto.setCourseExecutionType(getType());
        dto.setCourseId(getCourse().getId());
        dto.setCourseType(getCourse().getType());
        dto.setName(getCourse().getName());
        dto.setStatus(getStatus());
        dto.setNumberOfActiveTeachers(getNumberOfActiveTeachers());
        dto.setNumberOfInactiveTeachers(getNumberofInactiveTeachers());
        dto.setNumberOfActiveStudents(getNumberOfActiveStudents());
        dto.setNumberOfInactiveStudents(getNumberOfInactiveStudents());
        dto.setNumberOfQuizzes(getNumberOfQuizzes());
        dto.setNumberOfQuestions(getNumberOfQuestions());
        if(getType().equals(CourseType.EXTERNAL)) {
            dto.setCourseExecutionUsers(new ArrayList<>());
            getUsers().stream().forEach(user -> {
                if (user.isStudent()) {
                    dto.getCourseExecutionUsers().add(user.getStudentDto());
                } else {
                    dto.getCourseExecutionUsers().add(user.getUserDto());
                }
            });
        }

        return dto;
    }
}