package link.jinlong.learn;

import org.springframework.data.annotation.Id;

public record CashCard(@Id Long id, Double amount, String owner) {

}
