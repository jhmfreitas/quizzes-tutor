package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.answer.StatementQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.question.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.question.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.LatexQuestionExportVisitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuestionsXmlImport;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.XMLQuestionExportVisitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.domain.QuestionSubmission;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.repository.QuestionSubmissionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage.*;

@Service
public class QuestionService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuestionSubmissionRepository questionSubmissionRepository;

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 2000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto findQuestionById(Integer questionId) {
        return questionRepository.findById(questionId).map(Question::getDto)
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StatementQuestionDto getStatementQuestionDto(Integer questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        return question.getStatementQuestionDto();
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto findQuestionByKey(Integer key) {
        return questionRepository.findByKey(key).map(Question::getDto)
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, key));
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findQuestions(int courseId) {
        return questionRepository.findQuestions(courseId).stream().filter(q -> !q.isInSubmission()).map(Question::getDto).collect(Collectors.toList());
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findAvailableQuestions(int courseId) {
        return questionRepository.findAvailableQuestions(courseId).stream().map(Question::getDto).collect(Collectors.toList());
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto createQuestion(int courseId, QuestionDto questionDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));
        Question question = new Question(course, questionDto);
        questionRepository.save(question);
        return question.getDto();
    }


    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto updateQuestion(Integer questionId, QuestionDto questionDto) {
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        question.update(questionDto);
        return question.getDto();
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeQuestion(Integer questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        QuestionSubmission questionSubmission = questionSubmissionRepository.findQuestionSubmissionByQuestionId(question.getId());

        if (questionSubmission != null) {
            throw new TutorException(CANNOT_DELETE_SUBMITTED_QUESTION);
        }

        question.remove();
        questionRepository.delete(question);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void questionSetStatus(Integer questionId, Question.Status status) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        question.setStatus(status);
    }


    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void uploadImage(Integer questionId, String type) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        Image image = question.getImage();
        String imageName = question.getCourse().getName().replaceAll("\\s", "") +
                question.getCourse().getType() +
                question.getKey() +
                "." + type;

        if (image == null) {
            image = new Image(imageName);

            question.setImage(image);

            imageRepository.save(image);
        } else {
            question.getImage().setUrl(imageName);
        }
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateQuestionTopics(Integer questionId, TopicDto[] topics) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        question.updateTopics(Arrays.stream(topics).map(topicDto -> topicRepository.findTopicByName(question.getCourse().getId(), topicDto.getName())).collect(Collectors.toSet()));
    }

    public String exportQuestionsToXml() {
        XMLQuestionExportVisitor xmlExporter = new XMLQuestionExportVisitor();

        return xmlExporter.export(questionRepository.findAll());
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void importQuestionsFromXml(String questionsXML) {
        QuestionsXmlImport xmlImporter = new QuestionsXmlImport();

        xmlImporter.importQuestions(questionsXML, this, courseRepository);
    }


    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String exportQuestionsToLatex() {
        LatexQuestionExportVisitor latexExporter = new LatexQuestionExportVisitor();

        return latexExporter.export(questionRepository.findAll());
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ByteArrayOutputStream exportCourseQuestions(int courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));

        String name = course.getName();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            List<Question> questions = new ArrayList<>(course.getQuestions());

            XMLQuestionExportVisitor xmlExport = new XMLQuestionExportVisitor();
            InputStream in = IOUtils.toInputStream(xmlExport.export(questions), StandardCharsets.UTF_8);
            zos.putNextEntry(new ZipEntry(name + ".xml"));
            copyToZipStream(zos, in);
            zos.closeEntry();

            LatexQuestionExportVisitor latexExport = new LatexQuestionExportVisitor();
            zos.putNextEntry(new ZipEntry(name + ".tex"));
            in = IOUtils.toInputStream(latexExport.export(questions), StandardCharsets.UTF_8);
            copyToZipStream(zos, in);
            zos.closeEntry();

            baos.flush();

            return baos;
        } catch (IOException ex) {
            throw new TutorException(ErrorMessage.CANNOT_OPEN_FILE);
        }
    }

    private void copyToZipStream(ZipOutputStream zos, InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }
        in.close();
    }


    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteQuizQuestion(QuizQuestion quizQuestion) {
        Question question = quizQuestion.getQuestion();
        quizQuestion.remove();
        quizQuestionRepository.delete(quizQuestion);

        if (question.getQuizQuestions().isEmpty() && questionSubmissionRepository.findQuestionSubmissionByQuestionId(question.getId()) == null) {
            this.removeQuestion(question.getId());
        }
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteQuestion(Question question) {
        if(question.getQuestionDetails() != null) {
            question.getQuestionDetails().delete();
        }

        if (question.getImage() != null) {
            imageRepository.delete(question.getImage());
        }

        question.getTopics().forEach(topic -> topic.getQuestions().remove(question));
        question.getTopics().clear();

        questionRepository.delete(question);
    }
}

