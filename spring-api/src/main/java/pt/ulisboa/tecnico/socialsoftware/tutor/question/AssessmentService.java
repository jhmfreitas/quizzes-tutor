package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.AssessmentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicConjunctionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ExceptionError.ASSESSMENT_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ExceptionError.TOPIC_CONJUNCTION_NOT_FOUND;

@Service
public class AssessmentService {
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(AssessmentService.class);

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicConjunctionRepository topicConjunctionRepository;

    @PersistenceContext
    EntityManager entityManager;


    /*@Retryable(
      value = { SQLException.class },
      maxAttempts = 2,
      backoff = @Backoff(delay = 5000))*/
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<AssessmentDto> findAll() {
        return assessmentRepository.findAll().stream().map(AssessmentDto::new).collect(Collectors.toList());
    }

    /*@Retryable(
      value = { SQLException.class },
      maxAttempts = 2,
      backoff = @Backoff(delay = 5000))*/
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<AssessmentDto> findAllAvailable() {
        return assessmentRepository.findAll().stream()
                .filter(assessment -> assessment.getStatus() == Assessment.Status.AVAILABLE)
                .map(AssessmentDto::new)
                .collect(Collectors.toList());
    }


    /*@Retryable(
      value = { SQLException.class },
      maxAttempts = 2,
      backoff = @Backoff(delay = 5000))*/
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AssessmentDto createAssessment(AssessmentDto assessmentDto) {
        Assessment assessment = new Assessment();
        assessment.setTitle(assessmentDto.getTitle());
        assessment.setStatus(Assessment.Status.valueOf(assessmentDto.getStatus()));
        assessment.setTopicConjunctions(assessmentDto.getTopicConjunctions().stream()
                .map(topicConjunctionDto -> {
                    TopicConjunction topicConjunction = new TopicConjunction();
                    Set<Topic> newTopics = topicConjunctionDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow()).collect(Collectors.toSet());
                    topicConjunction.updateTopics(newTopics);
                    return topicConjunction;
                }).collect(Collectors.toList()));
        assessment.getTopicConjunctions().forEach(topicConjunction -> topicConjunction.setAssessment(assessment));

        this.entityManager.persist(assessment);
        return new AssessmentDto(assessment);
    }


    /*@Retryable(
      value = { SQLException.class },
      maxAttempts = 2,
      backoff = @Backoff(delay = 5000))*/
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AssessmentDto updateAssessment(Integer assessmentId, AssessmentDto assessmentDto) {
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow(() -> new TutorException(ASSESSMENT_NOT_FOUND, assessmentId));

        assessment.setTitle(assessmentDto.getTitle());
        assessment.setStatus(Assessment.Status.valueOf(assessmentDto.getStatus()));
        assessment.remove();
        assessment.setTopicConjunctions(assessmentDto.getTopicConjunctions().stream()
                .map(topicConjunctionDto -> {
                    TopicConjunction topicConjunction;
                    if(topicConjunctionDto.getId() != null) {
                        topicConjunction = topicConjunctionRepository.findById(topicConjunctionDto.getId()).orElseThrow(() -> new TutorException(TOPIC_CONJUNCTION_NOT_FOUND, topicConjunctionDto.getId()));
                    } else {
                        topicConjunction = new TopicConjunction();
                    }

                    Set<Topic> newTopics = topicConjunctionDto.getTopics().stream().map(topicDto -> topicRepository.findById(topicDto.getId()).orElseThrow()).collect(Collectors.toSet());
                    topicConjunction.updateTopics(newTopics);
                    return topicConjunction;
                }).collect(Collectors.toList()));
        assessment.getTopicConjunctions().forEach(topicConjunction -> topicConjunction.setAssessment(assessment));

        return new AssessmentDto(assessment);
    }


    /*@Retryable(
      value = { SQLException.class },
      maxAttempts = 2,
      backoff = @Backoff(delay = 5000))*/
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeAssessment(Integer assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow(() -> new TutorException(ASSESSMENT_NOT_FOUND, assessmentId));
        assessment.remove();
        entityManager.remove(assessment);
    }


    /*@Retryable(
      value = { SQLException.class },
      maxAttempts = 2,
      backoff = @Backoff(delay = 5000))*/
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void assessmentSetStatus(Integer assessmentId, Assessment.Status status) {
        Assessment assessment = assessmentRepository.findById(assessmentId).orElseThrow(() -> new TutorException(ASSESSMENT_NOT_FOUND, assessmentId));
        assessment.setStatus(status);
    }

/*    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void importAssessment(String assessmentXML) {
        AssessmentXmlImport xmlImporter = new AssessmentXmlImport();

        xmlImporter.importAssessment(assessmentXML, this);
    }*/
}

