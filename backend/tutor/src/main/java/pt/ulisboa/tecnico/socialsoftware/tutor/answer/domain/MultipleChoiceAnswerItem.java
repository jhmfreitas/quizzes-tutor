package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.common.dtos.answer.MultipleChoiceStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.answer.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.question.QuestionTypes;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;

@Entity
@DiscriminatorValue(QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceAnswerItem extends QuestionAnswerItem {

    private Integer optionId;

    public MultipleChoiceAnswerItem() {
    }

    public MultipleChoiceAnswerItem(String username, int quizId, StatementAnswerDto answer, MultipleChoiceStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.optionId = detailsDto.getOptionId();
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return this.getOptionId() != null ? questionDetails.getAnswerRepresentation(Arrays.asList(optionId)) : "-";
    }

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }
}
